package com.example.safedriveai.ui.edr

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Path
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import java.io.File
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.safedriveai.domain.model.EdrModel
import com.example.safedriveai.ui.edr.components.FeedbackButton
import com.example.safedriveai.ui.edr.components.TelemetryGraph
import java.util.Date

@Composable
fun EdrDetailScreen(data: List<EdrModel>, file: File, maxG: Float, onBack: () -> Unit) {
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
                onClick = { generateAndOpenPDF(context, file, data, maxG) },
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

// Función para compartir el JSON crudo
fun shareJsonFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Exportar Datos JSON Crudos"))
}

@SuppressLint("DefaultLocale")
fun generateAndOpenPDF(context: Context, sourceFile: File, data: List<EdrModel>, maxG: Float) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    
    // Configuración de pinceles
    val titlePaint = Paint().apply { color = android.graphics.Color.BLACK; textSize = 22f; isFakeBoldText = true }
    val headerPaint = Paint().apply { color = android.graphics.Color.BLACK; textSize = 14f; isFakeBoldText = true }
    val bodyPaint = Paint().apply { color = android.graphics.Color.BLACK; textSize = 11f }
    val grayPaint = Paint().apply { color = android.graphics.Color.GRAY; textSize = 10f }
    val redPaint = Paint().apply { color = android.graphics.Color.parseColor("#FFEBEE") } // Rojo muy claro
    val graphLinePaint = Paint().apply { color = android.graphics.Color.RED; strokeWidth = 2.5f; style = Paint.Style.STROKE }

    // 1. LOGO Y CABECERA
    // Dibujamos un círculo decorativo como logo
    val logoPaint = Paint().apply { color = android.graphics.Color.RED; style = Paint.Style.FILL }
    canvas.drawCircle(70f, 60f, 15f, logoPaint)
    canvas.drawText("SD", 60f, 66f, Paint().apply { color = android.graphics.Color.WHITE; textSize = 14f; isFakeBoldText = true })
    
    canvas.drawText("INFORME PERICIAL - SAFEDRIVE AI", 100f, 65f, titlePaint)
    canvas.drawLine(50f, 95f, 545f, 95f, Paint().apply { color = android.graphics.Color.BLACK; strokeWidth = 1f })

    // 2. RESUMEN EJECUTIVO
    val peak = data.maxByOrNull { it.gForce }
    canvas.drawText("DATOS DEL INCIDENTE", 50f, 130f, headerPaint)
    canvas.drawText("ID Registro: ${sourceFile.name}", 50f, 150f, bodyPaint)
    canvas.drawText("Fecha y Hora: ${peak?.time ?: Date()}", 50f, 165f, bodyPaint)
    canvas.drawText("Ubicación: Lat ${peak?.latitude ?: 0.0}, Lon ${peak?.longitude ?: 0.0}", 50f, 180f, bodyPaint)
    canvas.drawText("Velocidad de Impacto: ${peak?.speed?.toInt() ?: 0} km/h", 50f, 195f, bodyPaint)

    // 3. GRÁFICA DE TELEMETRÍA CON ZONA DE IMPACTO
    canvas.drawText("ANÁLISIS DE ACELERACIÓN G", 50f, 240f, headerPaint)
    val gLeft = 70f
    val gTop = 260f
    val gRight = 525f
    val gBottom = 400f
    
    // Cuadrícula y etiquetas
    val gridPaint = Paint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 0.5f }
    val axisLabelPaint = Paint().apply { color = android.graphics.Color.DKGRAY; textSize = 9f }
    
    // Dibujar zona de peligro (>4G) sombreada
    val maxGraphG = (data.maxOfOrNull { it.gForce } ?: 1f).coerceAtLeast(6f)
    val scaleY = (gBottom - gTop) / maxGraphG
    val scaleX = (gRight - gLeft) / (data.size - 1).coerceAtLeast(1)
    
    val dangerY = gBottom - (4.0f * scaleY)
    if (dangerY > gTop) {
        canvas.drawRect(gLeft, gTop, gRight, dangerY, redPaint) // Zona roja
        canvas.drawText("ZONA DE IMPACTO (>4G)", gRight - 110f, gTop + 15f, axisLabelPaint.apply { color = android.graphics.Color.RED; isFakeBoldText = true })
    }

    // Dibujar líneas de referencia G
    for (i in 0..maxGraphG.toInt()) {
        val y = gBottom - (i * scaleY)
        canvas.drawLine(gLeft, y, gRight, y, gridPaint)
        canvas.drawText("${i}G", gLeft - 25f, y + 4f, axisLabelPaint)
    }

    // Ejes
    canvas.drawLine(gLeft, gTop, gLeft, gBottom, Paint().apply { color = android.graphics.Color.BLACK })
    canvas.drawLine(gLeft, gBottom, gRight, gBottom, Paint())

    // Línea de telemetría (Sombra bajo la curva)
    if (data.isNotEmpty()) {
        val fillPath = Path()
        fillPath.moveTo(gLeft, gBottom)
        data.forEachIndexed { i, m ->
            val x = gLeft + (i * scaleX)
            val y = gBottom - (m.gForce * scaleY)
            fillPath.lineTo(x, y)
        }
        fillPath.lineTo(gLeft + (data.size - 1) * scaleX, gBottom)
        fillPath.close()
        val fillPaint = Paint().apply { color = android.graphics.Color.argb(40, 255, 0, 0); style = Paint.Style.FILL }
        canvas.drawPath(fillPath, fillPaint)

        val path = Path()
        data.forEachIndexed { i, m ->
            val x = gLeft + (i * scaleX)
            val y = gBottom - (m.gForce * scaleY)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, graphLinePaint)
    }

    // 4. TABLA DE PUNTOS CRÍTICOS (Top 5 momentos)
    canvas.drawText("TOP 5 PUNTOS DE MAYOR TENSIÓN", 50f, 440f, headerPaint)
    val topPoints = data.sortedByDescending { it.gForce }.take(5)
    var tableY = 470f
    
    // Encabezado tabla
    canvas.drawText("Tiempo", 70f, tableY, headerPaint.apply { textSize = 10f })
    canvas.drawText("Fuerza G", 200f, tableY, headerPaint)
    canvas.drawText("Velocidad", 330f, tableY, headerPaint)
    canvas.drawLine(50f, tableY + 5f, 545f, tableY + 5f, Paint().apply { color = android.graphics.Color.LTGRAY })
    
    topPoints.forEach { p ->
        tableY += 20f
        canvas.drawText(p.time.takeLast(8), 70f, tableY, bodyPaint)
        canvas.drawText("${String.format("%.2f", p.gForce)} G", 200f, tableY, bodyPaint.apply { isFakeBoldText = p.gForce > 4 })
        canvas.drawText("${p.speed.toInt()} km/h", 330f, tableY, bodyPaint)
    }

    // 5. DIAGNÓSTICO Y FIRMA
    canvas.drawText("DIAGNÓSTICO TÉCNICO", 50f, 620f, headerPaint)
    val diagnostic = when {
        maxG > 4.5 -> "CRÍTICO: Colisión severa detectada. Riesgo de daño estructural y lesiones."
        maxG > 2.5 -> "ADVERTENCIA: Maniobra brusca o impacto leve registrado."
        else -> "NORMAL: No se detectan anomalías significativas en el periodo registrado."
    }
    canvas.drawText(diagnostic, 50f, 645f, bodyPaint.apply { isFakeBoldText = false })

    canvas.drawText("Generado por SafeDrive AI EDR Core v2.0", 50f, 800f, grayPaint)
    canvas.drawText("Página 1 de 1", 500f, 800f, grayPaint)

    pdfDocument.finishPage(page)

    val pdfFile = File(context.cacheDir, "Informe_SafeDrive_Premium.pdf")
    pdfDocument.writeTo(pdfFile.outputStream())
    pdfDocument.close()

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Abrir Informe Premium"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error abriendo el informe", android.widget.Toast.LENGTH_SHORT).show()
    }
}