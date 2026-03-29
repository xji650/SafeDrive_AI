package com.example.safedriveai.ui.edr

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.util.Date

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