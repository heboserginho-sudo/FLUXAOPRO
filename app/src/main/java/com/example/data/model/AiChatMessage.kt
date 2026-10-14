package com.example.data.model

import java.util.UUID

enum class AiSender {
    USER,
    ASSISTANT
}

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: AiSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val quickActions: List<String> = emptyList()
)
