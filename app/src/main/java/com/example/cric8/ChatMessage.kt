package com.example.cric8


import com.google.firebase.Timestamp


data class ChatMessage(
    val messageId: String = "", // new field
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: Timestamp? = null,
    val imageUrl: String? = null
)