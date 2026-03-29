package com.example.safedriveai.ui.edr

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.safedriveai.ui.theme.*
@Composable
fun EDRDetailView(data: List<EDRSnapshot>, file: File, maxG: Float, onBack: () -> Unit) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // CABECERA
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onBackground)
            }
            Column {
                Text(
                    text = "Detalle del Incidente",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                // MOSTRAMOS EL PICO DE IMPACTO REAL
                Text(
                    text = "Impacto Máximo: ${String.format("%.1f", maxG)} G",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // GRÁFICA REAL
        if (data.isNotEmpty()) {
            TelemetryGraph(data)
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("Error leyendo datos del archivo", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(Modifier.height(24.dp))

        // FEEDBACK
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

        // EXPORTAR
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { /* Exportar JSON */ },
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Exportar JSON")
            }
            Button(
                onClick = { generateAndSharePDF(context, file) }, // Ojo a la mayúscula/minúscula de tu función original
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, null)
                Spacer(Modifier.width(8.dp))
                Text("Informe PDF")
            }
        }
    }
}
