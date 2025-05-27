package com.example.moviebooking.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.moviebooking.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    viewModel: ChatBotViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    val chatState by viewModel.chatState.collectAsState()
    val messages by viewModel.messages.collectAsState()

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Movie Assistant",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AccentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavyLight,
                    titleContentColor = Color.White,
                    navigationIconContentColor = AccentColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(message)
                }
            }

            // Input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(DarkNavyLight, RoundedCornerShape(24.dp)),
                    placeholder = { Text("Ask about movies...", color = Color.White.copy(alpha = 0.5f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkNavyLight,
                        unfocusedContainerColor = DarkNavyLight,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    enabled = chatState !is ChatState.Loading
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (message.isNotBlank()) {
                            viewModel.sendMessage(message)
                            message = ""
                        }
                    },
                    enabled = chatState !is ChatState.Loading && message.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (message.isNotBlank()) AccentColor else Color.Gray
                    )
                }
            }

            // Loading indicator
            if (chatState is ChatState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentColor
                )
            }

            // Error message
            if (chatState is ChatState.Error) {
                Text(
                    text = (chatState as ChatState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (message.isUser) AccentColor else DarkNavyLight
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = Color.White
            )
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean
) 