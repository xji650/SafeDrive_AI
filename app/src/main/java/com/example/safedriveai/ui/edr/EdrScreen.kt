package com.example.safedriveai.ui.edr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
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
    val incidents by viewModel.incidentsHistory.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val realData by viewModel.selectedEventData.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var incidentToDelete by remember { mutableStateOf<String?>(null) }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("¿Borrar historial?") },
            text = { Text("Esta acción eliminará todos los registros y archivos de telemetría de forma permanente.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showDeleteAllDialog = false
                }) {
                    Text("BORRAR TODO", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    if (incidentToDelete != null) {
        AlertDialog(
            onDismissRequest = { incidentToDelete = null },
            title = { Text("¿Borrar registro?") },
            text = { Text("Se eliminará el registro seleccionado y su archivo asociado.") },
            confirmButton = {
                TextButton(onClick = {
                    incidentToDelete?.let { viewModel.deleteIncident(it) }
                    incidentToDelete = null
                }) {
                    Text("BORRAR", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { incidentToDelete = null }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (selectedFile == null) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Caja Negra",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row {
                        IconButton(onClick = { viewModel.syncHistoryWithCloud() }) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, "Sincronizar", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, "Borrar todo", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (incidents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay registros", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(incidents) { incident ->
                            IncidentRoomCard(
                                incident = incident,
                                onOpen = { viewModel.openDetailsFromEntity(incident) },
                                onDelete = { incidentToDelete = incident.id }
                            )
                        }
                    }
                }
            }
        } else {
            realData?.let { data ->
                EdrDetailScreen(
                    data = data,
                    file = selectedFile!!,
                    maxG = data.maxByOrNull { it.gForce }?.gForce ?: 0f,
                    onBack = { viewModel.closeDetails() }
                )
            }
        }
    }
}