package com.nonsense.chat.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nonsense.chat.ui.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

/** App-level preferences: chosen theme and notification toggle. */
@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val THEME = stringPreferencesKey("theme")
    private val NOTIFS = booleanPreferencesKey("notifications_enabled")

    val theme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        runCatching { AppTheme.valueOf(prefs[THEME] ?: AppTheme.DARK.name) }
            .getOrDefault(AppTheme.DARK)
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFS] ?: true }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[THEME] = theme.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFS] = enabled }
    }
}
