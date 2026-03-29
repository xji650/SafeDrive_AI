package com.example.safedriveai.ui.edr

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

// Función para leer el archivo y convertirlo de nuevo en una lista de datos
fun loadEDRData(file: File): List<EDRSnapshot> {
    return try {
        val jsonString = file.readText()
        // Le decimos a Gson qué tipo de lista estamos esperando
        val type = object : TypeToken<List<EDRSnapshot>>() {}.type
        Gson().fromJson(jsonString, type)
    } catch (e: Exception) {
        emptyList()
    }
}