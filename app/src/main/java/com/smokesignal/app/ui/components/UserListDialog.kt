package com.smokesignal.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smokesignal.app.model.NearbyUser

@Composable
fun UserListDialog(users: List<NearbyUser>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier.fillMaxWidth().heightIn(max = 420.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Nearby Friends", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${users.size} in Bluetooth range", color = Color.White.copy(0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))

                if (users.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No friends nearby yet\u2026", color = Color.White.copy(0.4f), fontSize = 14.sp)
                    }
                } else {
                    LazyColumn {
                        items(users) { user ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(40.dp).background(Color(0xFF00D4FF).copy(0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        user.nickname.firstOrNull()?.uppercase() ?: "?",
                                        color = Color(0xFF00D4FF), fontSize = 18.sp, fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(user.nickname, color = Color.White, fontSize = 16.sp)
                                    Text("id: ${user.deviceId.take(8)}\u2026", color = Color.White.copy(0.4f), fontSize = 10.sp)
                                }
                                Box(Modifier.size(8.dp).background(Color(0xFF00FF88), CircleShape))
                            }
                        }
                    }
                }
            }
        }
    }
}
