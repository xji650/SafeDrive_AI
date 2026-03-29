package com.example.safedriveai.ui.edr

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.safedriveai.ui.theme.SuccessGreen
import java.io.File
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.safedriveai.ui.theme.*
import java.util.Date

@Composable
fun EDRDetailView(data: List<EDRSnapshot>, file: File, maxG: Float, onBack: () -> Unit) {
    val context = LocalContext.current

    // Buscamos el "frame" exacto donde ocurrió el golpe más fuerte para extraer sus datos
    val peakSnapshot = data.maxByOrNull { it.gForce }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- CABECERA ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Detalle del Incidente",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- PANEL DE PREVISUALIZACIÓN DE DATOS ---
        if (peakSnapshot != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Dato 1: Fuerza G Máxima
                    DataColumn(icon = Icons.Default.Warning, label = "Impacto", value = "${String.format("%.1f", maxG)} G", color = MaterialTheme.colorScheme.error)
                    // Dato 2: Velocidad en el impacto
                    DataColumn(icon = Icons.Default.Speed, label = "Velocidad", value = "${peakSnapshot.speed.toInt()} km/h", color = MaterialTheme.colorScheme.primary)
                    // Dato 3: Hora del impacto
                    // Cortamos el string de la fecha para que se vea más limpio (Ej: saca solo la hora)
                    val shortTime = peakSnapshot.time.takeLast(12).take(8)
                    DataColumn(icon = Icons.Default.AccessTime, label = "Hora", value = shortTime, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // --- GRÁFICA REAL ---
        if (data.isNotEmpty()) {
            TelemetryGraph(data)
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("Error leyendo datos del archivo", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- FEEDBACK IA ---
        Text(
            text = "¿Qué ha pasado realmente?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Etiqueta este evento para entrenar la IA.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FeedbackButton(MaterialTheme.colorScheme.error, "Accidente") { /* CRASH */ }
            FeedbackButton(Color(0xFFF57C00), "Susto") { /* BUMP */ }
            FeedbackButton(Color(0xFF388E3C), "Falso") { /* FALSE */ }
        }

        Spacer(Modifier.weight(1f))

        // --- EXPORTAR ---
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // AHORA EL BOTÓN JSON SÍ FUNCIONA
            OutlinedButton(
                onClick = { shareJsonFile(context, file) },
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Exportar JSON")
            }
            Button(
                onClick = { generateAndSharePDF(context, file) },
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, null)
                Spacer(Modifier.width(8.dp))
                Text("Informe PDF")
            }
        }
    }
}

// Mini-componente para que los datos queden ordenados en columnas
@Composable
fun DataColumn(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Función para compartir el JSON crudo (Similar a la del PDF)
fun shareJsonFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Exportar Datos JSON Crudos"))
}

fun generateAndSharePDF(context: Context, sourceFile: File) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint()

    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("INFORME PERICIAL - SAFEDRIVE AI", 50f, 50f, paint)

    paint.textSize = 14f
    paint.isFakeBoldText = false
    canvas.drawText("ID Evento: ${sourceFile.name}", 50f, 100f, paint)
    canvas.drawText("Fecha: ${Date()}", 50f, 120f, paint)
    canvas.drawText("Resultado: Posible impacto detectado (>4G)", 50f, 140f, paint)

    pdfDocument.finishPage(page)

    val pdfFile = File(context.cacheDir, "Informe_SafeDrive.pdf")
    pdfDocument.writeTo(pdfFile.outputStream())
    pdfDocument.close()

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Enviar Informe EDR"))
}