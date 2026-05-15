package com.example.safedriveai.ui.edr.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safedriveai.domain.model.EdrModel
import java.util.concurrent.TimeUnit

@SuppressLint("DefaultLocale")
@Composable
fun IncidentRoomCard(
    incident: EdrModel,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onFeedback: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    // REGLA DE DATA QUALITY: 24 horas para dar feedback
    val currentTime = System.currentTimeMillis()
    val isExpired = currentTime - incident.rawTimestamp > TimeUnit.HOURS.toMillis(24)

    val statusColor = when (incident.type) {
        0 -> Color(0xFF4CAF50) // Verde: Falso Positivo
        1 -> Color(0xFFFFA000) // Naranja: Susto
        2 -> MaterialTheme.colorScheme.error // Rojo: Accidente
        else -> Color.Gray // Gris: Sin validar (Ground Truth missing)
    }

    val statusLabel = when (incident.type) {
        0 -> "Falso Positivo (Ignorado)"
        1 -> "Susto / Riesgo detectado"
        2 -> "Accidente confirmado"
        else -> "Impacto Detectado (Sin Validar)"
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Indicador de sincronización
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (incident.isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (incident.isSynced) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (incident.isSynced) "Sincronizado" else "Solo local",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isExpired) {
                        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.Edit,
                                contentDescription = "Dar Feedback",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Text(
                            text = "Feedback cerrado",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    // BOTÓN DE BORRAR INDIVIDUAL
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar incidente",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when(incident.type) {
                        0 -> Icons.Default.CheckCircle
                        else -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = "${String.format("%.1f", incident.gForce)} G registrados",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Velocidad: ${incident.speed.toInt()} km/h",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ruido: ${incident.audioAmplitude.toInt()} dB | Ubicación: ${String.format("%.4f", incident.latitude)}, ${String.format("%.4f", incident.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // SECCIÓN DESPLEGABLE DE FEEDBACK
            if (expanded && !isExpired) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF30363D))
                Spacer(Modifier.height(12.dp))
                Text(
                    "¿Qué ocurrió realmente?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onFeedback(0); expanded = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Bache/Falso", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { onFeedback(1); expanded = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Susto", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { onFeedback(2); expanded = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Accidente", fontSize = 10.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // La fecha ya viene formateada desde el Mapper
            Text(
                text = "Fecha: ${incident.time}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}