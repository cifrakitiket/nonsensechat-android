package com.nonsense.chat.data

/**
 * Backend constants — identical to the web client so this app is a true peer of the website,
 * reading/writing the same Supabase project.  (index.html:1807 / :3880)
 */
object SupabaseConfig {
    const val URL = "https://xpkiirwnpxyfwbrktmqm.supabase.co"
    const val ANON_KEY = "sb_publishable_RiO2j0EsDPsJ0mO7v-7ebw_g1rI1jqc"
    const val BUCKET = "uploads"

    // Firebase project that owns FCM (push). Used by the server Edge Function; kept here for reference.
    const val FIREBASE_PROJECT = "nonsensechattm-e5d18"
}

/** Logical table names (Firestore "collections" → Supabase tables). See supabase_schema.sql. */
object Tables {
    const val USERS = "users"
    const val CHATS = "chats"
    const val MESSAGES = "messages"
    const val FOLDERS = "folders"
    const val FRIEND_REQUESTS = "friend_requests"
    const val STICKER_PACKS = "sticker_packs"
    const val CALL_SESSIONS = "call_sessions"
    const val CALL_HISTORY = "call_history"
}
