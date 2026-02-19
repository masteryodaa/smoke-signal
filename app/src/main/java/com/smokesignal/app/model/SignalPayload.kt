package com.smokesignal.app.model

import org.json.JSONObject

/**
 * Simple JSON-based message protocol.
 * Uses Android's built-in org.json — zero extra dependencies.
 */
sealed class SignalPayload {
    abstract val type: String
    abstract val timestamp: Long
    abstract val senderNickname: String
    abstract val senderDeviceId: String

    data class Discovery(
        override val timestamp: Long = System.currentTimeMillis(),
        override val senderNickname: String,
        override val senderDeviceId: String
    ) : SignalPayload() {
        override val type = TYPE
        companion object { const val TYPE = "discovery" }
    }

    data class SmokeSignal(
        override val timestamp: Long = System.currentTimeMillis(),
        override val senderNickname: String,
        override val senderDeviceId: String,
        val message: String = "Smoke break! \uD83D\uDD25"
    ) : SignalPayload() {
        override val type = TYPE
        companion object { const val TYPE = "smoke_signal" }
    }

    data class Acknowledgment(
        override val timestamp: Long = System.currentTimeMillis(),
        override val senderNickname: String,
        override val senderDeviceId: String,
        val originalTimestamp: Long
    ) : SignalPayload() {
        override val type = TYPE
        companion object { const val TYPE = "ack" }
    }

    fun toJson(): String = JSONObject().apply {
        put("type", type)
        put("timestamp", timestamp)
        put("senderNickname", senderNickname)
        put("senderDeviceId", senderDeviceId)
        when (this@SignalPayload) {
            is SmokeSignal -> put("message", message)
            is Acknowledgment -> put("originalTimestamp", originalTimestamp)
            is Discovery -> { /* no extra fields */ }
        }
    }.toString()

    companion object {
        fun fromJson(raw: String): SignalPayload? = try {
            val j = JSONObject(raw)
            val ts = j.getLong("timestamp")
            val nick = j.getString("senderNickname")
            val did = j.getString("senderDeviceId")
            when (j.getString("type")) {
                Discovery.TYPE -> Discovery(ts, nick, did)
                SmokeSignal.TYPE -> SmokeSignal(ts, nick, did, j.optString("message", ""))
                Acknowledgment.TYPE -> Acknowledgment(ts, nick, did, j.getLong("originalTimestamp"))
                else -> null
            }
        } catch (_: Exception) { null }
    }
}
