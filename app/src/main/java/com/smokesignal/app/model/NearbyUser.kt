package com.smokesignal.app.model

data class NearbyUser(
    val endpointId: String,
    val nickname: String,
    val deviceId: String = "",
    val lastSeen: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true
)
