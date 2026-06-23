# Nonsense Chat — native Android app (Kotlin / Jetpack Compose)

A native Android port of the web messenger. It talks to the **same Supabase backend** as the
website (`xpkiirwnpxyfwbrktmqm.supabase.co`), so accounts, chats and messages are shared and sync
in realtime between web and Android. UI is **Material 3 / Jetpack Compose** (Material You dynamic
color).

## Features
- Email/password auth (shared with web), session persistence
- Chat list with **folders as tabs**, pinned chats, unread badges, search
- DMs, groups (create / info / leave / admin remove member), channels (read)
- Messages: text, **photos** (caption + spoiler), **files**, **stickers**, **polls/quizzes**,
  **replies**, **reactions**, edit, soft-delete, **read receipts (✓/✓✓)**
- **Typing indicators**, **online / last-seen presence** (45 s heartbeat, respects “hide last seen”)
- Friend requests (search by nickname, accept → opens DM)
- Profile view/edit, settings (theme, notifications, privacy, sign out)
- Image viewer (pinch-zoom)
- **FCM push notifications** that work when the app is closed (see `../supabase/README_PUSH.md`)

> **Deferred to a later phase:** live WebRTC voice/video calls. Call/system messages still render
> in the thread so history looks correct.

## Architecture
- **UI:** Compose + Material 3, Navigation-Compose, one ViewModel per screen (Hilt).
- **Backend:** `supabase-kt` (Auth + Postgrest + Realtime + Storage).
  - All mutations go through the `doc_apply` / `doc_apply_batch` / `doc_delete` RPCs
    (`data/DocOps.kt`, `data/DocRepository.kt`) — the exact mirror of the web shim.
  - Realtime `postgres_changes` per table → `Flow<RowChange>` (`data/Realtime.kt`).
  - Server timestamps use the `{"__ts__": iso}` sentinel (`data/Timestamps.kt`).
- **Session hub:** `data/AccountManager.kt` (current user, presence heartbeat, push token).

## Build
Requirements: **Android Studio** (latest) with a bundled **JDK 17–23** (the IDE's JBR is fine —
Gradle does **not** support JDK 24+), and **Android SDK platform 35** installed (Android Studio
offers to download it automatically on first sync; on the CLI run
`sdkmanager "platforms;android-35"`).

1. **`app/google-services.json`** is already in place (Firebase project `nonsensechattm-e5d18`).
   The build fails without it because the `google-services` plugin is applied.
2. The Gradle wrapper is included (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`).
   Open the `android/` folder in Android Studio and press Run, **or** build from a terminal:
   ```bash
   cd android
   ./gradlew assembleDebug
   ```
   APK: `app/build/outputs/apk/debug/app-debug.apk`.

   **If the system default JDK is 24+** (this machine has JDK 26, which Gradle/Kotlin reject with a
   cryptic `IllegalArgumentException: 26.0.1`), point Gradle at Android Studio's bundled JBR 21:
   ```bash
   ./gradlew assembleDebug -Dorg.gradle.java.home="F:/AndroidStudio/jbr"
   ```
   The current debug build (verified to compile and package) produces a ~22 MB `app-debug.apk`.

### Package naming
- **applicationId** `com.nonsensechat.app` — the runtime package; must match the Firebase
  registration in `google-services.json` (changing it breaks FCM token delivery).
- **namespace / Kotlin source package** `com.nonsense.chat` — where `R`/`BuildConfig` and all
  classes live. The manifest's relative names (`.MainActivity`, `.NonsenseApp`, `.push.FcmService`)
  resolve against this.

### Cyrillic path note
The repo lives under `F:\Загрузки\…`. `android.overridePathCheck=true` is set in
`gradle.properties`; if aapt2 still trips on the non-ASCII path, copy the project to a Latin path
(e.g. `C:\nonsense`) and build there.

## Push
Client side is built in (`push/`). For delivery when the app is closed you must deploy the server
Edge Function — see **`../supabase/README_PUSH.md`**.
