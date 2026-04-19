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
import com.example.safedriveai.ui.edr.components.IncidentRoomCard
import kotlin.collections.maxByOrNull

@Composable
fun EdrScreen(viewModel: EdrViewModel) {
    // 1. La Vista "escucha" los estados del ViewModel
    val incidents by viewModel.incidentsHistory.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val realData by viewModel.selectedEventData.collectAsState()

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

                if (incidents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay registros disponibles.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(incidents) { incident ->
                            IncidentRoomCard(
                                incident = incident,
                                onOpen = { viewModel.openDetailsFromEntity(incident) }
                            )
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