package com.smokesignal.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NicknameCard(nickname: String, friendCount: Int, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("You are", color = Color.White.copy(0.5f), fontSize = 12.sp)
                Text(nickname, color = Color(0xFF00D4FF), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("$friendCount friends in mesh", color = Color.White.copy(0.4f), fontSize = 12.sp)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, "Edit nickname", tint = Color(0xFF00D4FF))
            }
        }
    }
}
