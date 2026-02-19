package com.smokesignal.app.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.smokesignal.app.identity.UserIdentityManager
import com.smokesignal.app.model.NearbyUser
import com.smokesignal.app.model.SignalPayload
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class SmokeSignalManager(private val context: Context) {

    companion object {
        private const val TAG = "SmokeSignal"
        private const val SERVICE_ID = "com.smokesignal.app.mesh"
        private val STRATEGY = Strategy.P2P_CLUSTER
    }

    private val client = Nearby.getConnectionsClient(context)
    private val identity = UserIdentityManager(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Suppress("DEPRECATION")
    private val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // ---- public state ----
    private val _nearbyUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val nearbyUsers: StateFlow<List<NearbyUser>> = _nearbyUsers.asStateFlow()

    private val _nickname = MutableStateFlow("")
    val currentNickname: StateFlow<String> = _nickname.asStateFlow()

    private val _incomingSignal = MutableSharedFlow<SignalPayload.SmokeSignal>()
    val incomingSignal: SharedFlow<SignalPayload.SmokeSignal> = _incomingSignal.asSharedFlow()

    // ---- internal ----
    private val peers = mutableMapOf<String, NearbyUser>()
    private var myNick = ""
    private var myDeviceId = ""

    // ---- lifecycle ----
    fun start() {
        scope.launch {
            myNick = identity.getOrCreateNickname()
            myDeviceId = identity.getOrCreateDeviceId()
            _nickname.value = myNick
            startAdvertising()
            startDiscovery()
        }
    }

    fun stop() {
        runCatching {
            client.stopAdvertising()
            client.stopDiscovery()
            client.stopAllEndpoints()
        }
        scope.cancel()
    }

    suspend fun updateNickname(name: String) {
        identity.setNickname(name)
        myNick = name
        _nickname.value = name
        runCatching { client.stopAdvertising() }
        startAdvertising()
    }

    // ---- networking ----
    private suspend fun startAdvertising() {
        val opts = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        runCatching {
            client.startAdvertising(myNick, SERVICE_ID, connCallback, opts).await()
            Log.d(TAG, "Advertising as $myNick")
        }.onFailure { Log.e(TAG, "Advertise fail: ${it.message}") }
    }

    private fun startDiscovery() {
        val opts = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        client.startDiscovery(SERVICE_ID, discoveryCallback, opts)
            .addOnSuccessListener { Log.d(TAG, "Discovery started") }
            .addOnFailureListener { Log.e(TAG, "Discovery fail: ${it.message}") }
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(id: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "Found ${info.endpointName} ($id)")
            client.requestConnection(myNick, id, connCallback)
        }
        override fun onEndpointLost(id: String) {
            peers.remove(id); refresh()
        }
    }

    private val connCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(id: String, info: ConnectionInfo) {
            client.acceptConnection(id, payloadCb)
        }
        override fun onConnectionResult(id: String, res: ConnectionResolution) {
            if (res.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                Log.d(TAG, "Connected $id")
                sendTo(id, SignalPayload.Discovery(senderNickname = myNick, senderDeviceId = myDeviceId))
            }
        }
        override fun onDisconnected(id: String) {
            peers.remove(id); refresh()
        }
    }

    private val payloadCb = object : PayloadCallback() {
        override fun onPayloadReceived(id: String, payload: Payload) {
            val json = payload.asBytes()?.let { String(it) } ?: return
            handle(id, json)
        }
        override fun onPayloadTransferUpdate(id: String, update: PayloadTransferUpdate) {}
    }

    private fun handle(endpointId: String, json: String) {
        when (val p = SignalPayload.fromJson(json)) {
            is SignalPayload.Discovery -> {
                peers[endpointId] = NearbyUser(endpointId, p.senderNickname, p.senderDeviceId)
                refresh()
                sendTo(endpointId, SignalPayload.Acknowledgment(
                    senderNickname = myNick, senderDeviceId = myDeviceId,
                    originalTimestamp = p.timestamp
                ))
            }
            is SignalPayload.SmokeSignal -> {
                vibrateHard()
                scope.launch { _incomingSignal.emit(p) }
            }
            is SignalPayload.Acknowledgment -> Log.d(TAG, "Ack from ${p.senderNickname}")
            null -> Log.w(TAG, "Unknown payload")
        }
    }

    // ---- public actions ----
    fun sendSmokeSignal(message: String = "Smoke break! \uD83D\uDD25") {
        vibrateTap()
        val signal = SignalPayload.SmokeSignal(
            senderNickname = myNick, senderDeviceId = myDeviceId, message = message
        )
        peers.keys.forEach { sendTo(it, signal) }
    }

    private fun sendTo(id: String, payload: SignalPayload) {
        client.sendPayload(id, Payload.fromBytes(payload.toJson().toByteArray()))
    }

    private fun refresh() { _nearbyUsers.value = peers.values.toList() }

    // ---- haptics ----
    private fun vibrateTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        else @Suppress("DEPRECATION") vibrator.vibrate(50)
    }

    private fun vibrateHard() {
        val pattern = longArrayOf(0, 150, 80, 150, 80, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        else @Suppress("DEPRECATION") vibrator.vibrate(pattern, -1)
    }
}
