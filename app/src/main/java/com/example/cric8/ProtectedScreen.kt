package com.example.cric8

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavHostController

@Composable
fun ProtectedChatScreen(
    navController: NavHostController? = null,
    sendMessage: (String, String?) -> Unit,
    messages: List<ChatMessage>,
    onLogout: () -> Unit,
    editMessage: (ChatMessage, String) -> Unit,
    deleteMessage: (ChatMessage) -> Unit
) {
    var accessCodeInput by remember { mutableStateOf("") }
    var accessGranted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (!accessGranted) {
        // Access Code Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔒 Enter Family Access Code", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = accessCodeInput,
                onValueChange = { accessCodeInput = it },
                placeholder = { Text("Access Code") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (accessCodeInput == MainActivity.ACCESS_CODE) {
                        accessGranted = true
                    } else {
                        Toast.makeText(context, "❌ Wrong access code", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Continue")
            }
        }
    } else {
        // If access is granted, show actual chat
        ChatScreen(
            navController = navController,
            sendMessage = sendMessage,
            messages = messages,
            onLogout = onLogout,
            editMessage = editMessage,
            deleteMessage = deleteMessage
        )
    }
}
