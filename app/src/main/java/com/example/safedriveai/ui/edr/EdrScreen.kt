package com.example.safedriveai.ui.edr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.safedriveai.ui.edr.components.IncidentRoomCard
import com.example.safedriveai.ui.edr.components.TrashCard

@Composable
fun EdrScreen(viewModel: EdrViewModel, initiallyInTrashMode: Boolean = false) {
    val incidents by viewModel.incidentsHistory.collectAsState()
    val deletedIncidents by viewModel.deletedIncidents.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val realData by viewModel.selectedEventData.collectAsState()
    val selectedIncident by viewModel.selectedIncident.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showRestoreAllDialog by remember { mutableStateOf(false) }
    var incidentToDelete by remember { mutableStateOf<String?>(null) }
    var isTrashMode by remember { mutableStateOf(initiallyInTrashMode) }

    LaunchedEffect(initiallyInTrashMode) {
        isTrashMode = initiallyInTrashMode
    }

    // Diálogo para Borrar Todo (Mover a papelera)
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Borrar Historial Completo")
                }
            },
            text = {
                Column {
                    Text("Está a punto de marcar todos los registros para su eliminación.")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "IMPORTANTE: Según la normativa RGPD, los datos permanecerán en una papelera de seguridad durante 30 días antes de su eliminación definitiva física.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
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

    // Diálogo para Restaurar Todo
    if (showRestoreAllDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreAllDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RestoreFromTrash, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Restaurar Todo")
                }
            },
            text = {
                Text("¿Desea restaurar todos los incidentes de la papelera al historial principal?")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restoreAllIncidents()
                    showRestoreAllDialog = false
                }) {
                    Text("RESTAURAR TODO")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreAllDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    // Diálogo para borrar uno
    if (incidentToDelete != null) {
        AlertDialog(
            onDismissRequest = { incidentToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Aviso de Seguridad")
                }
            },
            text = {
                Column {
                    Text("Está a punto de eliminar un registro de telemetría.")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Estos datos pueden ser requeridos como prueba pericial. Se mantendrán ocultos en una papelera de seguridad durante 30 días según RGPD.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    incidentToDelete?.let { viewModel.deleteIncident(it) }
                    incidentToDelete = null
                }) {
                    Text("ENTIENDO, BORRAR", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { incidentToDelete = null }) {
                    Text("CONSERVAR")
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
                        text = if (isTrashMode) "Papelera" else "Caja Negra",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isTrashMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )

                    Row {
                        if (isTrashMode) {
                            // BOTONES MODO PAPELERA
                            IconButton(onClick = { showRestoreAllDialog = true }) {
                                Icon(
                                    Icons.Default.RestoreFromTrash,
                                    contentDescription = "Restaurar todo",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { isTrashMode = false }) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = "Volver al Historial",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            // BOTONES MODO HISTORIAL
                            IconButton(onClick = { showDeleteAllDialog = true }) {
                                Icon(
                                    Icons.Default.DeleteSweep,
                                    contentDescription = "Borrar todo",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            IconButton(onClick = { isTrashMode = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Ver Papelera",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.syncHistoryWithCloud() }) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, "Sincronizar", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val displayList = if (isTrashMode) deletedIncidents else incidents

                if (displayList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (isTrashMode) "La papelera está vacía" else "No hay registros",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayList) { incident ->
                            if (isTrashMode) {
                                TrashCard(
                                    incident = incident,
                                    onRestore = { viewModel.restoreIncident(incident.id) }
                                )
                            } else {
                                IncidentRoomCard(
                                    incident = incident,
                                    onOpen = { viewModel.openDetailsFromEntity(incident) },
                                    onDelete = { incidentToDelete = incident.id },
                                    onFeedback = { type -> viewModel.updateFeedback(incident.id, type) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            selectedIncident?.let { incident ->
                realData?.let { data ->
                    EdrDetailScreen(
                        incident = incident,
                        telemetryData = data,
                        file = selectedFile!!,
                        onBack = { viewModel.closeDetails() },
                        onFeedback = { type ->
                            viewModel.updateFeedback(incident.id, type)
                        }
                    )
                }
            }
        }
    }
}
