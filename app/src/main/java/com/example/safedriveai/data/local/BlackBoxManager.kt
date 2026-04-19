package com.example.safedriveai.data.local

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.safedriveai.domain.model.EdrModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import java.io.File
import java.time.LocalDateTime

class BlackBoxManager @Inject constructor (
    @ApplicationContext private val context: Context
) {
    // 30 segundos de datos a 10 registros por segundo = 300 puntos
    private val buffer = mutableListOf<EdrModel>()
    private val MAX_SAMPLES = 300
    @RequiresApi(Build.VERSION_CODES.O)
    fun addPoint(g: Float, s: Float, a: Float, lat: Double, lon: Double) { // <-- 1. PIDE LAS COORDENADAS AQUÍ
        if (buffer.size >= MAX_SAMPLES) buffer.removeAt(0)

        val currentMillis = System.currentTimeMillis()

        buffer.add(
            EdrModel(
                time = LocalDateTime.now().toString(),
                rawTimestamp = currentMillis, // <-- El número largo para buscar archivos
                gForce = g,
                speed = s,
                audioAmplitude = a,
                latitude = lat,       // <-- La que recibimos arriba
                longitude = lon,      // <-- La que recibimos arriba
                isSynced = false      // <-- Por defecto false porque aún no ha subido a Firebase
            )
        )
    }

    fun saveEventToDisk(timestamp: Long) {
        val fileName = "EDR_EVENT_${timestamp}.json" // <-- USA EL PARÁMETRO
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