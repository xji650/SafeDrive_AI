package com.example.safedriveai.ui.edr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.safedriveai.ui.edr.components.IncidentCard
import kotlin.collections.maxByOrNull

@Composable
fun EdrScreen(viewModel: EdrViewModel) {
    // 1. La Vista "escucha" los estados del ViewModel
    val incidentFiles by viewModel.incidentFiles.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val realData by viewModel.selectedEventData.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFile()
    }


    // Fondo nativo del sistema
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        if (selectedFile == null) {
            // --- VISTA A: LISTA (Estilo Nativo) ---
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Registros Caja Negra",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

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
                            // En lugar de guardar el estado aquí, se lo decimos al ViewModel
                            IncidentCard(file, onOpen = { viewModel.openDetails(file) })
                        }
                    }
                }
            }
        } else {
            // --- VISTA B: DETALLE FORENSE REAL ---
            // Solo dibujamos la vista si los datos ya han sido cargados por el ViewModel
            realData?.let { data ->
                val maxG = data.maxByOrNull { it.gForce }?.gForce ?: 0f

                EdrDetailScreen(
                    data = data,
                    file = selectedFile!!,
                    maxG = maxG,
                    onBack = { viewModel.closeDetails() }
                )
            } ?: run {
                // Muestra un estado de carga mientras el ViewModel lee el JSON
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}