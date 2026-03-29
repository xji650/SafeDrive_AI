package com.example.safedriveai.ui.edr


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import java.io.File
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.safedriveai.ui.theme.*
import kotlin.emptyArray

@Composable
fun EDRApp() {
    val context = LocalContext.current
    var selectedFile by remember { mutableStateOf<File?>(null) }

    val incidentFiles = remember {
        context.filesDir.listFiles { file ->
            file.name.startsWith("EDR_EVENT_") && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    // Fondo nativo del sistema (se adapta a modo claro/oscuro automáticamente)
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (selectedFile == null) {
            // --- VISTA A: LISTA (Estilo Nativo) ---
            Column(modifier = Modifier.padding(16.dp)) {

                // Tipografía estándar para Título
                Text(
                    text = "Registros Caja Negra",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Tipografía estándar para Subtítulo
                Text(
                    text = "Historial de telemetría e incidentes detectados.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (incidentFiles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay registros disponibles.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(incidentFiles) { file ->
                            IncidentCard(file, onOpen = { selectedFile = file })
                        }
                    }
                }
            }
        } else {
            // --- VISTA B: DETALLE FORENSE REAL ---
            // 1. Leemos el archivo real
            val realData = remember(selectedFile) { loadEDRData(selectedFile!!) }

            // 2. Buscamos cuál fue el golpe más fuerte para mostrarlo
            val maxG = realData.maxByOrNull { it.gForce }?.gForce ?: 0f

            EDRDetailView(
                data = realData, // Pasamos la lista completa
                file = selectedFile!!,
                maxG = maxG,
                onBack = { selectedFile = null }
            )
        }
    }
}






