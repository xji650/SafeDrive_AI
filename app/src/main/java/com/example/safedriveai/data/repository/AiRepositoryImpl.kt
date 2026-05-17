package com.example.safedriveai.data.repository

import com.example.safedriveai.domain.repository.AiRepository
import com.example.safedriveai.ui.preferences.ConnectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor() : AiRepository {

    override suspend fun testOllamaConnection(host: String): ConnectionStatus = withContext(Dispatchers.IO) {
        try {
            val urlString = if (host.startsWith("http")) host else "http://$host:11434"
            val url = URL("$urlString/api/tags")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) ConnectionStatus.ONLINE else ConnectionStatus.OFFLINE
        } catch (e: Exception) {
            ConnectionStatus.OFFLINE
        }
    }

    override suspend fun testRagConnection(host: String): ConnectionStatus = withContext(Dispatchers.IO) {
        try {
            // En lugar de llamar a retrieve (que ejecuta lógica pesada), 
            // solo probamos si el servidor responde en la raíz o en docs
            val urlString = if (host.startsWith("http")) host else "http://$host:8000"
            val url = URL(urlString) 
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2000
                readTimeout = 2000
            }
            
            val code = connection.responseCode
            // Cualquier código (200, 404, 405) significa que el servidor está VIVO.
            // Solo marcamos OFFLINE si hay una excepción de red.
            if (code != -1) ConnectionStatus.ONLINE else ConnectionStatus.OFFLINE
        } catch (e: Exception) {
            ConnectionStatus.OFFLINE
        }
    }

    override suspend fun getAiExplanation(contextSnippet: String): String = withContext(Dispatchers.IO) {
        // Implementación básica por ahora, simula una llamada a la IA
        "Según el análisis de los sensores ($contextSnippet), el sistema detecta un funcionamiento óptimo de los protocolos de seguridad."
    }
}
