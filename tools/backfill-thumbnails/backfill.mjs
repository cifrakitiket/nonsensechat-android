#!/usr/bin/env node
// One-time backfill: generate small (~320px) preview thumbnails for OLD photo messages that
// predate the photoThumbUrl feature, so chat bubbles can load a ~30KB preview instead of a
// 1–2MB original on a throttled/blocked network.
//
// WHY A SERVER-SIDE SCRIPT (not in-app): the Android app only ships the anon key, and Supabase
// RLS lets a user patch only their OWN messages. An in-app migration would therefore skip every
// photo sent by anyone else (group chats, DMs from the other side). This script uses the
// service-role key — which bypasses RLS — so it can backfill ALL old photos in one pass.
//
// It mirrors the app exactly:
//   • thumbnail = long-edge 320px JPEG, quality stepped down until <= ~50KB  (Storage.kt)
//   • field is set via the same idempotent doc_apply RPC the app uses:
//       _ops: [{ op: "set", path: ["photoThumbUrl"], value: <publicUrl> }]   (DocOps.kt)
//
// SAFE TO RE-RUN: thumb objects use a deterministic path (overwritten on re-run), and doc_apply
// "set" is idempotent. Messages that already have photoThumbUrl are skipped.
//
// Usage:  see README.md.  Quick start:
//   SUPABASE_SERVICE_ROLE_KEY=... node backfill.mjs --dry-run
//   SUPABASE_SERVICE_ROLE_KEY=... node backfill.mjs

import { createClient } from '@supabase/supabase-js'
import sharp from 'sharp'

// ── Config (env-driven; only the service-role key is required) ───────────────────────────────
const SUPABASE_URL = process.env.SUPABASE_URL || 'https://xpkiirwnpxyfwbrktmqm.supabase.co'
const SERVICE_ROLE = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_SERVICE_KEY
const BUCKET = process.env.BUCKET || 'uploads'
const TABLE = process.env.TABLE || 'messages'

const THUMB_DIM = Number(process.env.THUMB_DIM || 320) // long-edge px, matches Storage.THUMB_DIM
const THUMB_MAX_BYTES = Number(process.env.THUMB_MAX_BYTES || 50_000)
const PAGE_SIZE = Number(process.env.PAGE_SIZE || 500) // rows scanned per DB page
const CONCURRENCY = Number(process.env.CONCURRENCY || 4) // images processed in parallel
const LIMIT = process.env.LIMIT ? Number(process.env.LIMIT) : Infinity // cap candidates (for testing)

const DRY_RUN = ['1', 'true', 'yes'].includes(String(process.env.DRY_RUN || '').toLowerCase())
  || process.argv.includes('--dry-run')

if (!SERVICE_ROLE) {
  console.error('ERROR: set SUPABASE_SERVICE_ROLE_KEY (service-role key, NOT the anon key).')
  console.error('  PowerShell:  $env:SUPABASE_SERVICE_ROLE_KEY="..."; node backfill.mjs')
  console.error('  bash:        SUPABASE_SERVICE_ROLE_KEY=... node backfill.mjs')
  process.exit(1)
}

const sb = createClient(SUPABASE_URL, SERVICE_ROLE, { auth: { persistSession: false } })

// ── Step 1: collect candidates (old photo messages with no thumbnail yet) ────────────────────
// Select only the jsonb fields we need (not the whole doc) to keep the scan light. Paginate by a
// stable order so we see every row exactly once. We DON'T server-filter on photoThumbUrl IS NULL
// (jsonb null filtering is fiddly) — we filter in JS, which is robust.
async function collectCandidates() {
  const candidates = []
  let from = 0
  for (;;) {
    const { data, error } = await sb
      .from(TABLE)
      .select(
        'id, type:doc->>type, photoUrl:doc->>photoUrl, photoThumbUrl:doc->>photoThumbUrl, deleted:doc->>_deleted',
      )
      .eq('doc->>type', 'photo')
      .order('id', { ascending: true })
      .range(from, from + PAGE_SIZE - 1)

    if (error) throw new Error(`scan failed at offset ${from}: ${error.message}`)
    if (!data || data.length === 0) break

    for (const r of data) {
      if (r.deleted === 'true') continue
      if (!r.photoUrl) continue
      if (r.photoThumbUrl) continue // already has a thumb
      candidates.push({ id: r.id, photoUrl: r.photoUrl })
      if (candidates.length >= LIMIT) return candidates
    }

    process.stdout.write(`\rScanning… ${from + data.length} rows, ${candidates.length} candidates`)
    if (data.length < PAGE_SIZE) break
    from += PAGE_SIZE
  }
  process.stdout.write('\n')
  return candidates
}

// ── Step 2: per-message work — download → thumbnail → upload → patch field ────────────────────
async function makeThumb(srcBytes) {
  // Long-edge THUMB_DIM, never upscale; step quality down until under the byte cap (matches app).
  let quality = 80
  for (;;) {
    const out = await sharp(srcBytes)
      .rotate() // honour EXIF orientation
      .resize(THUMB_DIM, THUMB_DIM, { fit: 'inside', withoutEnlargement: true })
      .jpeg({ quality })
      .toBuffer()
    if (out.length <= THUMB_MAX_BYTES || quality <= 40) return out
    quality -= 10
  }
}

async function processOne(c) {
  // 1. download original
  const res = await fetch(c.photoUrl)
  if (!res.ok) throw new Error(`download ${res.status}`)
  const srcBytes = Buffer.from(await res.arrayBuffer())

  // 2. thumbnail
  const thumb = await makeThumb(srcBytes)

  // 3. upload to a deterministic path (re-runs overwrite, so it's idempotent)
  const path = `backfill-thumbs/${c.id}.jpg`
  if (DRY_RUN) return { path, size: thumb.length, dryRun: true }

  const up = await sb.storage
    .from(BUCKET)
    .upload(path, thumb, { contentType: 'image/jpeg', upsert: true })
  if (up.error) throw new Error(`upload: ${up.error.message}`)

  const publicUrl = sb.storage.from(BUCKET).getPublicUrl(path).data.publicUrl

  // 4. set photoThumbUrl via the same idempotent RPC the app uses
  const { error } = await sb.rpc('doc_apply', {
    _table: TABLE,
    _id: c.id,
    _ops: [{ op: 'set', path: ['photoThumbUrl'], value: publicUrl }],
  })
  if (error) throw new Error(`doc_apply: ${error.message}`)

  return { path, size: thumb.length, url: publicUrl }
}

// Minimal concurrency pool (no extra deps).
async function runPool(items, worker, concurrency) {
  let i = 0
  const stats = { ok: 0, failed: 0 }
  async function next() {
    while (i < items.length) {
      const idx = i++
      const c = items[idx]
      try {
        const r = await worker(c)
        stats.ok++
        const kb = (r.size / 1024).toFixed(0)
        process.stdout.write(
          `\r[${stats.ok + stats.failed}/${items.length}] ok=${stats.ok} fail=${stats.failed}  last ${kb}KB ${DRY_RUN ? '(dry-run)' : ''}   `,
        )
      } catch (e) {
        stats.failed++
        console.error(`\n  ! ${c.id}: ${e.message}`)
      }
    }
  }
  await Promise.all(Array.from({ length: concurrency }, next))
  process.stdout.write('\n')
  return stats
}

async function main() {
  console.log(`Backfill thumbnails  url=${SUPABASE_URL}  bucket=${BUCKET}  table=${TABLE}`)
  console.log(`thumb=${THUMB_DIM}px/<=${(THUMB_MAX_BYTES / 1024).toFixed(0)}KB  concurrency=${CONCURRENCY}  ${DRY_RUN ? 'DRY-RUN (no writes)' : 'LIVE'}`)

  const candidates = await collectCandidates()
  console.log(`Found ${candidates.length} photo message(s) without a thumbnail.`)
  if (candidates.length === 0) return

  const stats = await runPool(candidates, processOne, CONCURRENCY)
  console.log(`Done. updated=${stats.ok} failed=${stats.failed}${DRY_RUN ? ' (dry-run — nothing written)' : ''}`)
  if (stats.failed > 0) {
    console.log('Re-run the script to retry failures (it skips messages that already have a thumb).')
  }
}

main().catch((e) => {
  console.error('\nFATAL:', e.message)
  process.exit(1)
})
