package com.example.safedriveai.domain.repository

import com.example.safedriveai.ui.preferences.ConnectionStatus

interface AiRepository {
    suspend fun testOllamaConnection(host: String): ConnectionStatus
    suspend fun testRagConnection(host: String): ConnectionStatus
    suspend fun getAiExplanation(contextSnippet: String): String
}
