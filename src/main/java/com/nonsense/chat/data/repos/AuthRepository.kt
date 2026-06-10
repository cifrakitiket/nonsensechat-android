package com.nonsense.chat.data.repos

import com.nonsense.chat.data.DocOps
import com.nonsense.chat.data.DocRepository
import com.nonsense.chat.data.Tables
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val client: SupabaseClient,
    private val docs: DocRepository,
) {
    val sessionStatus: StateFlow<SessionStatus> get() = client.auth.sessionStatus

    fun currentUid(): String? = client.auth.currentUserOrNull()?.id

    suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    /** Create the auth user, then create their profile document (mirrors web sign-up). */
    suspend fun signUp(email: String, password: String, nick: String) {
        client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        val uid = currentUid() ?: error("Sign-up succeeded but no session (is email confirmation on?)")
        val profile = buildJsonObject {
            put("uid", uid)
            put("nick", nick.trim())
            put("nickLower", nick.trim().lowercase())
            put("avatar", "")
            put("bio", "")
            put("fname", "")
            put("lname", "")
            put("bday", "")
            put("phone", "")
            put("username", "")
            put("online", true)
        }
        docs.apply(
            Tables.USERS, uid,
            DocOps().setWhole(profile).serverNow("lastSeen").build(),
        )
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
}
