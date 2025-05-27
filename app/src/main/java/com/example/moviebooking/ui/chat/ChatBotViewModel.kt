package com.example.moviebooking.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.api.GeminiApi
import com.example.moviebooking.data.api.GeminiRequest
import com.example.moviebooking.data.api.Content
import com.example.moviebooking.data.api.Part
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel @Inject constructor() : ViewModel() {
    private val _chatState = MutableStateFlow<ChatState>(ChatState.Initial)
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val geminiApi = retrofit.create(GeminiApi::class.java)

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            try {
                _chatState.value = ChatState.Loading
                _messages.value = _messages.value + ChatMessage(message, true)

                val response = geminiApi.generateContent(
                    model = "gemini-1.5-flash",
                    apiKey = "API_KEY",
                    request = GeminiRequest(
                        contents = listOf(
                            Content(
                                parts = listOf(
                                    Part(text = message)
                                )
                            )
                        )
                    )
                )

                val botResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Sorry, I couldn't process your request."

                _messages.value = _messages.value + ChatMessage(botResponse, false)
                _chatState.value = ChatState.Success
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "An error occurred")
            }
        }
    }
}

sealed class ChatState {
    object Initial : ChatState()
    object Loading : ChatState()
    object Success : ChatState()
    data class Error(val message: String) : ChatState()
} 