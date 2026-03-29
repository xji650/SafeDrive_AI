package com.example.safedriveai.ui.edr

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import java.io.File
import java.time.LocalDateTime

data class EDRSnapshot(
    val time: String,
    val gForce: Float,
    val speed: Float,
    val audioAmplitude: Float
)

class BlackBoxManager(private val context: Context) {
    // 30 segundos de datos a 10 registros por segundo = 300 puntos
    private val buffer = mutableListOf<EDRSnapshot>()
    private val MAX_SAMPLES = 300

    @RequiresApi(Build.VERSION_CODES.O)
    fun addPoint(g: Float, s: Float, a: Float) {
        if (buffer.size >= MAX_SAMPLES) buffer.removeAt(0)
        buffer.add(EDRSnapshot(LocalDateTime.now().toString(), g, s, a))
    }

    fun saveEventToDisk() {
        val fileName = "EDR_EVENT_${System.currentTimeMillis()}.json"
        val file = File(context.filesDir, fileName)

        // Usamos Gson para convertir la lista en un JSON estructurado
        val jsonString = Gson().toJson(buffer)
        file.writeText(jsonString)

        Log.d("EDR", "Caja Negra guardada: ${file.absolutePath}")
    }
}