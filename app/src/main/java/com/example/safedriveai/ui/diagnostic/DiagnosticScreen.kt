package com.example.safedriveai.ui.diagnostic

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.LocationManager
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.remote.creation.second
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.safedriveai.sensors.DiagnosticEngine
import com.example.safedriveai.sensors.SensorChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class DiagnosticStatus {
    PENDING, TESTING, OK, WARNING, ERROR
}

data class DiagnosticItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String,
    var status: DiagnosticStatus = DiagnosticStatus.PENDING,
    var message: String = "Esperando..."
)

@Composable
fun DiagnosticApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var diagnosticItems by remember {
        mutableStateOf(
            listOf(
                DiagnosticItem("ACCEL", "Acelerómetro", Icons.Default.Speed, "Mide la aceleración y frenado."),
                DiagnosticItem("GYRO", "Giroscopio", Icons.Default.ScreenRotation, "Detecta las curvas y giros del coche."),
                DiagnosticItem("GPS", "Antena GPS", Icons.Default.LocationOn, "Precisión de la ubicación en tiempo real."),
                DiagnosticItem("MIC", "Micrófono", Icons.Default.Mic, "Reconocimiento de comandos de voz.")
            )
        )
    }

    var isRunningTests by remember { mutableStateOf(false) }

    fun runDiagnostics() {
        if (isRunningTests) return
        isRunningTests = true

        coroutineScope.launch {
            diagnosticItems = diagnosticItems.map { it.copy(status = DiagnosticStatus.PENDING, message = "Esperando...") }

            val newItems = diagnosticItems.toMutableList()

            for (i in newItems.indices) {
                newItems[i] = newItems[i].copy(status = DiagnosticStatus.TESTING, message = "Analizando hardware...")
                diagnosticItems = newItems.toList()

                delay(800)

                val result = DiagnosticEngine.runDiagnosticOn(context, newItems[i].id)
                newItems[i] = newItems[i].copy(status = result.first, message = result.second)
                diagnosticItems = newItems.toList()
            }
            isRunningTests = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Diagnóstico del Sistema",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Verifica el estado de los sensores críticos para la conducción segura.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(diagnosticItems) { item ->
                DiagnosticRowUi(item)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { runDiagnostics() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isRunningTests
        ) {
            if (isRunningTests) {
                Text("Ejecutando pruebas...")
            } else {
                Text("Iniciar Diagnóstico", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DiagnosticRowUi(item: DiagnosticItem) {

    val (statusColor, statusIcon) = when (item.status) {
        DiagnosticStatus.PENDING -> Pair(Color.Gray, Icons.Default.HourglassEmpty)
        DiagnosticStatus.TESTING -> Pair(MaterialTheme.colorScheme.primary, null)
        DiagnosticStatus.OK -> Pair(Color(0xFF4CAF50), Icons.Default.CheckCircle)
        DiagnosticStatus.WARNING -> Pair(Color(0xFFFF9800), Icons.Default.Warning)
        DiagnosticStatus.ERROR -> Pair(Color.Red, Icons.Default.Error)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.message,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor // El texto toma el color del estado
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (item.status == DiagnosticStatus.TESTING) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = statusColor
            )
        } else if (statusIcon != null) {
            Icon(
                imageVector = statusIcon,
                contentDescription = item.status.name,
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}