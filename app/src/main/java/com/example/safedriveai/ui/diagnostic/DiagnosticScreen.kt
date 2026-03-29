package com.example.safedriveai.ui.diagnostic

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.safedriveai.sensors.DiagnosticEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 1. DEFINICIÓN DE ESTADOS Y MODELOS
enum class DiagnosticStatus {
    PENDING, TESTING, OK, WARNING, ERROR
}

data class DiagnosticItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String,
    val status: DiagnosticStatus = DiagnosticStatus.PENDING,
    val message: String = "Esperando..."
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiagnosticApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Lista de chequeo inicial
    var diagnosticItems by remember {
        mutableStateOf(
            listOf(
                DiagnosticItem("ACCEL", "Acelerómetro", Icons.Default.Speed, "Mide la aceleración y frenado."),
                DiagnosticItem("GYRO", "Giroscopio", Icons.Default.ScreenRotation, "Detecta las curvas y giros del coche."),
                DiagnosticItem("GPS", "Antena GPS", Icons.Default.LocationOn, "Precisión de la ubicación en tiempo real."),
                DiagnosticItem("MIC", "Micrófono", Icons.Default.Mic, "Firma acústica de colisión."),
                DiagnosticItem("SOS", "Protocolo SOS", Icons.Default.Phone, "Permisos para llamadas y SMS."),
                DiagnosticItem("NET", "Conectividad", Icons.Default.Wifi, "Conexión para alertas DGT 3.0.")
            )
        )
    }

    var isRunningTests by remember { mutableStateOf(false) }

    // 2. LÓGICA DE EJECUCIÓN REAL
    fun runDiagnostics() {
        if (isRunningTests) return
        isRunningTests = true

        coroutineScope.launch {
            // Reiniciamos todos a "Pending"
            diagnosticItems = diagnosticItems.map { it.copy(status = DiagnosticStatus.PENDING, message = "Esperando...") }

            val updatedList = diagnosticItems.toMutableList()

            for (i in updatedList.indices) {
                // Marcamos el actual como "Testing"
                updatedList[i] = updatedList[i].copy(status = DiagnosticStatus.TESTING, message = "Analizando...")
                diagnosticItems = updatedList.toList()

                // Pequeño delay para que el usuario vea el proceso (UX)
                delay(600)

                // LLAMADA REAL AL MOTOR DE DIAGNÓSTICO
                val (resultStatus, resultMessage) = DiagnosticEngine.runDiagnosticOn(context, updatedList[i].id)

                // Actualizamos con el resultado real del sensor/permiso
                updatedList[i] = updatedList[i].copy(status = resultStatus, message = resultMessage)
                diagnosticItems = updatedList.toList()
            }
            isRunningTests = false
        }
    }

    // 3. DISEÑO DE LA PANTALLA
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Diagnóstico del Sistema",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Verifica los sensores y permisos críticos antes de iniciar el viaje.",
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
            enabled = !isRunningTests,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isRunningTests) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text("Analizando hardware...")
            } else {
                Text("Iniciar Diagnóstico Completo", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DiagnosticRowUi(item: DiagnosticItem) {
    val (statusColor, statusIcon) = when (item.status) {
        DiagnosticStatus.PENDING -> Pair(Color.Gray, Icons.Default.HourglassEmpty)
        DiagnosticStatus.TESTING -> Pair(MaterialTheme.colorScheme.primary, null)
        DiagnosticStatus.OK -> Pair(Color(0xFF10B981), Icons.Default.CheckCircle)
        DiagnosticStatus.WARNING -> Pair(Color(0xFFF59E0B), Icons.Default.Warning)
        DiagnosticStatus.ERROR -> Pair(Color(0xFFEF4444), Icons.Default.Error)
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
            tint = if(item.status == DiagnosticStatus.OK) statusColor else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = item.message, style = MaterialTheme.typography.bodySmall, color = statusColor)
        }

        if (item.status == DiagnosticStatus.TESTING) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else if (statusIcon != null) {
            Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
        }
    }
}