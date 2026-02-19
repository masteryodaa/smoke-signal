package com.smokesignal.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smokesignal.app.service.NotificationHelper
import com.smokesignal.app.service.SmokeSignalManager
import com.smokesignal.app.ui.components.AuraVisualizer
import com.smokesignal.app.ui.components.NicknameCard
import com.smokesignal.app.ui.components.NicknameEditDialog
import com.smokesignal.app.ui.components.UserListDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(smokeSignalManager: SmokeSignalManager) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val users by smokeSignalManager.nearbyUsers.collectAsState()
    val nick by smokeSignalManager.currentNickname.collectAsState()

    var transmitting by remember { mutableStateOf(false) }
    var showNickDlg by remember { mutableStateOf(false) }
    var showUsers by remember { mutableStateOf(false) }

    // Show notification on incoming signal
    LaunchedEffect(Unit) {
        smokeSignalManager.incomingSignal.collect { sig ->
            NotificationHelper.showSmokeSignal(context, sig.senderNickname, sig.message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SmokeSignal", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("offline \u2022 p2p \u2022 mesh", color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp, letterSpacing = 2.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    BadgedBox(badge = {
                        if (users.isNotEmpty()) Badge(containerColor = Color(0xFF00E676)) { Text("${users.size}") }
                    }) {
                        IconButton(onClick = { showUsers = true }) {
                            Icon(Icons.Default.Person, "Friends", tint = Color.White.copy(0.8f))
                        }
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        Box(
            Modifier.fillMaxSize().padding(pad).background(
                Brush.verticalGradient(listOf(Color(0xFF0D0D1A), Color(0xFF0A0A12), Color(0xFF050508)))
            ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(8.dp))
                NicknameCard(nick, users.size) { showNickDlg = true }
                Spacer(Modifier.weight(1f))

                // ---- AURA BUTTON ----
                val src = remember { MutableInteractionSource() }
                val pressed by src.collectIsPressedAsState()

                LaunchedEffect(pressed) {
                    if (pressed && !transmitting) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        transmitting = true
                        smokeSignalManager.sendSmokeSignal()
                        delay(2500)
                        transmitting = false
                    }
                }

                Box(Modifier.size(300.dp).clickable(src, null) {}, contentAlignment = Alignment.Center) {
                    AuraVisualizer(isActive = pressed || transmitting, modifier = Modifier.fillMaxSize())

                    Box(
                        Modifier.size(130.dp).background(
                            brush = if (transmitting) Brush.radialGradient(listOf(Color(0xFFFF6B35), Color(0xFFE55100)))
                            else Brush.radialGradient(listOf(Color(0xFF1C1C30), Color(0xFF12121F))),
                            shape = CircleShape
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment, "Send",
                                tint = if (transmitting) Color.White else Color(0xFF00D4FF),
                                modifier = Modifier.size(52.dp)
                            )
                            if (transmitting) {
                                Spacer(Modifier.height(4.dp))
                                Text("SENT", color = Color.White, fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(48.dp))
                Text(
                    when {
                        transmitting -> "Signalling ${users.size} friends\u2026"
                        users.isEmpty() -> "Searching for friends nearby\u2026"
                        else -> "Hold to send smoke signal"
                    },
                    color = Color.White.copy(0.6f), fontSize = 16.sp, fontWeight = FontWeight.Light
                )
                Spacer(Modifier.weight(1f))
                Text("SmokeSignal \u2022 No internet required",
                    color = Color.White.copy(0.2f), fontSize = 11.sp, modifier = Modifier.padding(bottom = 24.dp))
            }
        }
    }

    if (showNickDlg) NicknameEditDialog(nick, { showNickDlg = false }) { n ->
        scope.launch { smokeSignalManager.updateNickname(n) }; showNickDlg = false
    }
    if (showUsers) UserListDialog(users) { showUsers = false }
}
