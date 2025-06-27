package com.example.cric8

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ChatViewModel : ViewModel() {

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // OkHttpClient with increased timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Retrofit setup
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GeminiApi::class.java)

    // Actual API Key (NO "Bearer" prefix)
    private val apiKey = "AIzaSyBgojIrxIPdc-LS2UKymWw8RR_5x_9Fg6Q"

    var messages = mutableStateListOf<Pair<String, Boolean>>() // Pair<text, isUser>
        private set
    fun addMessage(text: String, isUser: Boolean) {
        messages.add(Pair(text, isUser))
    }

    fun sendMessage(message: String) {
        messages.add(Pair(message, true)) // Add user message
        _isLoading.value = true // Show loading

        viewModelScope.launch {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(text = message))
                        )
                    )
                )
                val response = api.getBotReply(apiKey, request)
                val reply = response.candidates
                    .firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No reply"
                messages.add(Pair(reply, false)) // Add bot reply
            } catch (e: Exception) {
                messages.add(Pair("Error: ${e.localizedMessage}", false))
            } finally {
                _isLoading.value = false // Hide loading
            }
        }
    }
}
