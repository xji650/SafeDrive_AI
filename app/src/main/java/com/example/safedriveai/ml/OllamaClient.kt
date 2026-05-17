package com.example.safedriveai.ml

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object OllamaClient {

    private var _baseUrl = "http://192.168.1.130:11434"
    private var _chromaUrl = "http://192.168.1.130:8000"

    fun updateUrls(ollamaUrl: String, ragUrl: String) {
        _baseUrl = ollamaUrl
        _chromaUrl = ragUrl
    }

    private val BASE_URL get() = _baseUrl
    private val CHROMA_URL get() = _chromaUrl
    private const val USE_MOCK = false

    suspend fun getModels(): List<String> = withContext(Dispatchers.IO) {
        if (USE_MOCK) {
            return@withContext listOf("llama3.2", "mistral", "gemma:2b", "phi3")
        }

        try {
            val url = URL("$BASE_URL/api/tags")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext listOf("Error loading models")
            }

            val response = connection.inputStream
                .bufferedReader()
                .use(BufferedReader::readText)

            val json = JSONObject(response)
            val modelsArray = json.optJSONArray("models") ?: JSONArray()

            val models = mutableListOf<String>()
            for (i in 0 until modelsArray.length()) {
                val obj = modelsArray.getJSONObject(i)
                models.add(obj.optString("name", "unknown"))
            }

            if (models.isEmpty()) listOf("No models found") else models
        } catch (e: Exception) {
            listOf("Connection error")
        }
    }

    // 🔥 MODIFICACIÓN: Ahora la función acepta el parámetro 'history' (por defecto vacío)
    suspend fun askModel(model: String, prompt: String, history: String = ""): String = withContext(Dispatchers.IO) {
        if (USE_MOCK) {
            return@withContext "[MOCK][$model] Respuesta simulada para: \"$prompt\""
        }

        try {
            // 1. Buscamos el contexto en la BBDD usando SOLO la pregunta actual
            val contextoRecuperado = fetchContextFromDb(prompt)

            // 2. PROMPT REFINADO: Menos énfasis en el pasado, más en la respuesta directa
            val ragPrompt = """
            Responde de forma concisa y directa a la pregunta del usuario. Respuesta siempre en español.
            Usa el contexto técnico si es relevante, pero no lo menciones si no es necesario.
            
            [Contexto Técnico]:
            $contextoRecuperado
            
            [Hilo de conversación]:
            $history
            
            Pregunta: $prompt
            
            Respuesta (en español):
        """.trimIndent()

            val url = URL("$BASE_URL/api/generate")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 120000 // Asegúrate de tener el timeout alto (2 min)
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            val body = JSONObject().apply {
                put("model", model)
                put("prompt", ragPrompt)
                put("stream", false)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                json.optString("response", "Error: Clave 'response' no encontrada.")
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                "Ollama Error ($responseCode)"
            }
        } catch (e: Exception) {
            "Error connecting to Ollama: ${e.message}"
        }
    }

    /**
     * FUNCIÓN AUXILIAR RAG: Se conecta a tu base de datos local (Chroma)
     * para extraer los fragmentos de texto relacionados con la pregunta.
     */
    private suspend fun fetchContextFromDb(query: String): String = withContext(Dispatchers.IO) {
        try {
            // Apuntamos a la API que consulta tu archivo chroma.sqlite3
            val url = URL("$CHROMA_URL/api/v1/retrieve")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 5000
                readTimeout = 5000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            val body = JSONObject().apply {
                put("query", query)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                json.optString("context", "No se encontró contexto relevante en la base de datos.")
            } else {
                "Aviso: No se pudo recuperar contexto de los manuales locales."
            }
        } catch (e: Exception) {
            // Si la base de datos está apagada durante tus pruebas, el chat sigue funcionando sin romper la app
            "Contexto no disponible por error de conexión con la base de datos."
        }
    }
}