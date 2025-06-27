package com.example.cric8

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun Chat(
    navController: NavHostController = rememberNavController(),
    viewModel: ChatViewModel = viewModel()
) {
    val messages = viewModel.messages
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    val isLoading by viewModel.isLoading.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show welcome message only once
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            viewModel.addMessage("👋 Welcome to CRIC8 – your cricket buddy! Ask me anything about cricket 🏏", isUser = false)
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute,
                onNavigate = { destination ->
                    if (destination != currentRoute) {
                        navController.navigate(destination) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6366F1), // Purple 500
                            Color(0xFF8B5CF6)  // Purple 400
                        )
                    )
                )
        ) {

            // 🔷 Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF4C1D95)) // Deep purple
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "CRIC8 Box",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Go to Home",
                        tint = Color.White
                    )
                }
            }

            // 🔷 Chat Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { (text, isUser) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(
                                    color = if (isUser) Color(0xFFD0F0FD) else Color(0xFFE5FFE1),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(12.dp),
                            color = Color.Black
                        )
                    }
                }
            }

            // 🔷 Loading Indicator
            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thinking...", color = Color.White)
                }
            }

            // 🔷 Input Box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your message") }
                )
                IconButton(onClick = {
                    val msg = userInput.text.trim()
                    if (msg.isNotEmpty()) {
                        viewModel.sendMessage(msg)
                        userInput = TextFieldValue("")
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}
