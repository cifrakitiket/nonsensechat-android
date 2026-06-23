# Backfill photo thumbnails (one-time)

Generates a small **~320px / ≤50KB** preview (`photoThumbUrl`) for **old** photo messages that
were sent before the thumbnail feature existed. After this runs, chat bubbles load a ~30KB preview
instead of the 1–2MB original — the difference between "loads on a throttled/blocked network" and
"times out".

New photos already get a thumbnail at send time; this only fixes the **backlog**.

## Why this runs server-side (and not in the app)

The Android app ships only the **anon key**, and Supabase **RLS** lets a user patch only their
**own** messages. An in-app migration would therefore skip every photo sent by anyone else. This
script uses the **service-role key** (bypasses RLS) so it backfills **all** old photos in one pass.

> ⚠️ The service-role key is a full-access secret. Never commit it, never put it in the app. Pass
> it through an environment variable, run the script from a trusted machine, and that's it.

## Requirements

- Node.js 18+ (uses the built-in `fetch`).
- Your Supabase **service-role** key (Dashboard → Project Settings → API → `service_role`).

## Install

```bash
cd tools/backfill-thumbnails
npm install
```

(`sharp` downloads a small native binary — first install needs internet.)

## Run

Always dry-run first — it scans and reports counts/sizes **without writing anything**:

```bash
# bash / git-bash
SUPABASE_SERVICE_ROLE_KEY="paste-service-role-key" node backfill.mjs --dry-run
```

```powershell
# PowerShell
$env:SUPABASE_SERVICE_ROLE_KEY = "paste-service-role-key"
node backfill.mjs --dry-run
```

If the candidate count looks right, run for real (drop `--dry-run`):

```bash
SUPABASE_SERVICE_ROLE_KEY="paste-service-role-key" node backfill.mjs
```

### Try a few first

Cap the number processed to sanity-check end-to-end before the full run:

```bash
SUPABASE_SERVICE_ROLE_KEY="..." LIMIT=20 node backfill.mjs
```

Then open those chats in the app — the photos should now load a small preview, full image on tap.

## What it does, per message

1. Finds photo messages where `doc.type == "photo"`, not deleted, has `photoUrl`, and **no**
   `photoThumbUrl` yet.
2. Downloads the original from `photoUrl`.
3. Makes a long-edge-320px JPEG, quality stepped down until ≤50KB (mirrors the app's `Storage.kt`).
4. Uploads it to the `uploads` bucket at `backfill-thumbs/<messageId>.jpg`.
5. Sets `photoThumbUrl` to the public URL via the **same idempotent `doc_apply` RPC** the app uses
   (`{op:"set", path:["photoThumbUrl"], value:<url>}`).

## Safe to re-run

- Thumb objects use a deterministic path (overwritten on re-run, `upsert: true`).
- `doc_apply` `set` is idempotent.
- Messages that already have `photoThumbUrl` are skipped.

So if some downloads fail (e.g. a deleted source file 404s), just run it again — only the
remaining ones are processed.

## Environment variables

| Var | Default | Meaning |
|-----|---------|---------|
| `SUPABASE_SERVICE_ROLE_KEY` | — (**required**) | service-role key |
| `SUPABASE_URL` | project URL (baked in) | override if needed |
| `BUCKET` | `uploads` | storage bucket |
| `TABLE` | `messages` | messages table |
| `THUMB_DIM` | `320` | long-edge px |
| `THUMB_MAX_BYTES` | `50000` | thumbnail size cap |
| `CONCURRENCY` | `4` | images processed in parallel |
| `PAGE_SIZE` | `500` | rows scanned per DB page |
| `LIMIT` | ∞ | cap candidates processed (testing) |
| `DRY_RUN` / `--dry-run` | off | scan + report, write nothing |
