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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BlackBoxManager @Inject constructor (
    @ApplicationContext private val context: Context
) {
    private val buffer = mutableListOf<EdrModel>()
    private val MAX_SAMPLES = 300
    // Reutilizamos el formateador para ahorrar recursos
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    @RequiresApi(Build.VERSION_CODES.O)
    fun addPoint(g: Float, speed: Float, amplitude: Float, j: Float, a: Float, lat: Double, lon: Double) {
        if (buffer.size >= MAX_SAMPLES) buffer.removeAt(0)

        val currentMillis = System.currentTimeMillis()

        // Añadimos ID y Fecha de forma eficiente
        buffer.add(
            EdrModel(
                id = UUID.randomUUID().toString(),
                time = dateFormat.format(Date(currentMillis)),
                rawTimestamp = currentMillis,
                gForce = g,
                speed = speed,
                audioAmplitude = amplitude,
                jerk = j,
                angle = a,
                latitude = lat,
                longitude = lon,
                isSynced = false
            )
        )
    }

    /**
     * Elimina el archivo físico de telemetría del disco.
     */
    fun deleteEventFile(timestamp: Long) {
        val fileName = "EDR_EVENT_${timestamp}.json"
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
            Log.d("EDR", "Archivo físico eliminado: ${file.absolutePath}")
        }
    }

    fun saveEventToDisk(timestamp: Long) {
        val fileName = "EDR_EVENT_${timestamp}.json"
        val file = File(context.filesDir, fileName)
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
            val type = object : TypeToken<List<EdrModel>>() {}.type
            Gson().fromJson(jsonString, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}