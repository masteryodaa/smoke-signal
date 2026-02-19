package com.smokesignal.app.identity

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smoke_signal_identity")

/**
 * Bitchat-style identity: auto-generated nickname, persistent device ID,
 * user can change nickname anytime — no verification, no servers.
 */
class UserIdentityManager(private val context: Context) {

    companion object {
        private val NICKNAME_KEY = stringPreferencesKey("user_nickname")
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    }

    suspend fun getOrCreateNickname(): String {
        val existing = context.dataStore.data.map { it[NICKNAME_KEY] }.first()
        return existing ?: run {
            val nick = "smoker${(1000..9999).random()}"
            setNickname(nick)
            nick
        }
    }

    suspend fun setNickname(nickname: String) {
        context.dataStore.edit { it[NICKNAME_KEY] = nickname.take(20) }
    }

    fun getNicknameFlow(): Flow<String> =
        context.dataStore.data.map { it[NICKNAME_KEY] ?: "anonymous" }

    suspend fun getOrCreateDeviceId(): String {
        val existing = context.dataStore.data.map { it[DEVICE_ID_KEY] }.first()
        return existing ?: run {
            val id = UUID.randomUUID().toString().replace("-", "").take(16)
            context.dataStore.edit { it[DEVICE_ID_KEY] = id }
            id
        }
    }
}
