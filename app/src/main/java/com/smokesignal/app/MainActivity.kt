package com.smokesignal.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.smokesignal.app.service.SmokeSignalManager
import com.smokesignal.app.service.SmokeSignalService
import com.smokesignal.app.ui.screens.MainScreen
import com.smokesignal.app.ui.theme.SmokeSignalTheme

class MainActivity : ComponentActivity() {

    private lateinit var smokeSignalManager: SmokeSignalManager

    private val requiredPermissions: Array<String>
        get() = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }.toTypedArray()

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) initializeApp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smokeSignalManager = SmokeSignalManager(this)

        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) initializeApp() else permissionsLauncher.launch(missing.toTypedArray())
    }

    private fun initializeApp() {
        smokeSignalManager.start()

        // Start foreground service to keep mesh alive
        val intent = Intent(this, SmokeSignalService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)

        setContent {
            SmokeSignalTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    MainScreen(smokeSignalManager = smokeSignalManager)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::smokeSignalManager.isInitialized) smokeSignalManager.stop()
    }
}
