package com.example.safedriveai.data.local

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.safedriveai.domain.model.EdrModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDateTime

class BlackBoxManager(private val context: Context) {
    // 30 segundos de datos a 10 registros por segundo = 300 puntos
    private val buffer = mutableListOf<EdrModel>()
    private val MAX_SAMPLES = 300

    @RequiresApi(Build.VERSION_CODES.O)
    fun addPoint(g: Float, s: Float, a: Float) {
        if (buffer.size >= MAX_SAMPLES) buffer.removeAt(0)
        buffer.add(EdrModel(LocalDateTime.now().toString(), g, s, a))
    }

    fun saveEventToDisk() {
        val fileName = "EDR_EVENT_${System.currentTimeMillis()}.json"
        val file = File(context.filesDir, fileName)

        // Usamos Gson para convertir la lista en un JSON estructurado
        val jsonString = Gson().toJson(buffer)
        file.writeText(jsonString)

        Log.d("EDR", "Caja Negra guardada: ${file.absolutePath}")
    }

    fun getSavedEvents(): List<File> {
        return context.filesDir.listFiles { file ->
            file.name.startsWith("EDR_EVENT_") && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun loadEventFromDisk(file: File): List<EdrModel> {
        return try {
            val jsonString = file.readText()
            // Le decimos a Gson qué tipo de lista estamos esperando
            val type = object : TypeToken<List<EdrModel>>() {}.type
            Gson().fromJson(jsonString, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}