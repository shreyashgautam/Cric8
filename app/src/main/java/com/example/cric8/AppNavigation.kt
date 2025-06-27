package com.example.cric8

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cricketapp.ui.screens.HomeScreen
import com.example.cric.ProfileScreen

@Composable
fun AppNavigation(
    onLogout: () -> Unit,
    sendMessage: (String, String?) -> Unit,
    messages: List<ChatMessage>,
    editMessage: (ChatMessage, String) -> Unit,
    deleteMessage: (ChatMessage) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "HomeScreen"
    ) {
        composable("HomeScreen") {
            HomeScreen(navController = navController)
        }
        composable("fixtures") {
            Fixtures(navController = navController)
        }
        composable("profile") {
            ProfileScreen(navController = navController, onLogout = onLogout)
        }
        composable("Weather") {
            Weather()
        }


        composable("Chat") {
            ProtectedChatScreen(
                navController = navController,
                sendMessage = sendMessage,
                messages = messages,
                onLogout = onLogout,
                editMessage = editMessage,
                deleteMessage = deleteMessage
            )
        }
    }
}
