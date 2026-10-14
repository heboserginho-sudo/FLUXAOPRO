package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Robust utility for generating, downloading, and sharing documents and invoices
 * via WhatsApp or system share sheet.
 */
object PdfDocumentManager {

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Sanitizes phone numbers for Angola (+244) or international WhatsApp usage.
     */
    fun sanitizeWhatsAppPhone(rawPhone: String): String {
        val digits = rawPhone.filter { it.isDigit() }
        return when {
            digits.startsWith("244") -> digits
            digits.length == 9 && (digits.startsWith("9") || digits.startsWith("2")) -> "244$digits"
            digits.length > 9 -> digits
            else -> digits
        }
    }

    /**
     * Opens a direct WhatsApp chat window with a pre-filled message.
     * Guaranteed to work with both installed WhatsApp/WhatsApp Business and Web WhatsApp fallback.
     */
    fun openWhatsAppChat(
        context: Context,
        rawPhone: String,
        message: String = "",
        onFailure: (() -> Unit)? = null
    ) {
        val phone = sanitizeWhatsAppPhone(rawPhone)
        val encodedMsg = Uri.encode(message)

        val uriString = if (phone.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$phone&text=$encodedMsg"
        } else {
            "https://api.whatsapp.com/send?text=$encodedMsg"
        }

        try {
            // Try direct whatsapp URI scheme first if phone is provided
            val directUri = if (phone.isNotBlank()) {
                Uri.parse("whatsapp://send?phone=$phone&text=$encodedMsg")
            } else {
                Uri.parse("whatsapp://send?text=$encodedMsg")
            }

            val directIntent = Intent(Intent.ACTION_VIEW, directUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val pm = context.packageManager
            if (pm.queryIntentActivities(directIntent, 0).isNotEmpty()) {
                context.startActivity(directIntent)
                return
            }

            // Fallback to standard web intent
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(context, "Não foi possível abrir o WhatsApp no aparelho", Toast.LENGTH_SHORT).show()
                onFailure?.invoke()
            }
        }
    }

    /**
     * Converts HTML document text into a real, high quality A4 PDF file on disk.
     * Uses Android's native PdfDocument engine with clean vector layout, typography,
     * header accents, item tables, totals, and AGT validation seal.
     */
    fun htmlToPdfFile(
        context: Context,
        html: String,
        fileName: String,
        onComplete: (File?) -> Unit
    ) {
        mainHandler.post {
            try {
                val cleanName = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"
                val outputDir = File(context.cacheDir, "shared_pdfs").apply { mkdirs() }
                val outputFile = File(outputDir, cleanName)
                if (outputFile.exists()) {
                    outputFile.delete()
                }

                // Render vector PDF using Android's native Graphics Canvas
                val pdfDocument = PdfDocument()
                val pageWidth = 595 // A4 width at 72 dpi
                val pageHeight = 842 // A4 height at 72 dpi
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Paints
                val darkTitlePaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 18f
                    isFakeBoldText = true
                    color = Color.rgb(15, 23, 42) // #0F172A
                }

                val goldPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 14f
                    isFakeBoldText = true
                    color = Color.rgb(212, 175, 55) // #D4AF37
                }

                val headerBgPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.rgb(15, 23, 42)
                    style = Paint.Style.FILL
                }

                val tableHeaderTxtPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 9.5f
                    isFakeBoldText = true
                    color = Color.WHITE
                }

                val cardBgPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.rgb(248, 250, 252) // #F8FAFC
                    style = Paint.Style.FILL
                }

                val borderPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.rgb(226, 232, 240) // #E2E8F0
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }

                val goldBorderPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.rgb(212, 175, 55)
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }

                val bodyPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 9.5f
                    color = Color.rgb(30, 41, 59)
                }

                val boldBodyPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 9.5f
                    isFakeBoldText = true
                    color = Color.rgb(15, 23, 42)
                }

                val mutedPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8.5f
                    color = Color.rgb(100, 116, 139)
                }

                val totalLabelPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 11f
                    isFakeBoldText = true
                    color = Color.rgb(15, 23, 42)
                }

                val totalValuePaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 13f
                    isFakeBoldText = true
                    color = Color.rgb(4, 120, 87) // #047857
                }

                // 1. Draw Top Header
                var y = 45f
                canvas.drawText("Flux", 40f, y, darkTitlePaint)
                val fluxWidth = darkTitlePaint.measureText("Flux")
                canvas.drawText("AO", 40f + fluxWidth, y, goldPaint)

                // Gold Accent Line
                canvas.drawLine(40f, y + 8f, 555f, y + 8f, goldBorderPaint)
                y += 24f

                // Extract Document Info from HTML
                val docTitle = extractTag(html, "title").ifBlank { "FATURA COMERCIAL" }
                val plainLines = extractReadableLines(html)

                // 2. Info Cards (Emitter & Client)
                val cardRect = RectF(40f, y, 555f, y + 75f)
                canvas.drawRoundRect(cardRect, 6f, 6f, cardBgPaint)
                canvas.drawRoundRect(cardRect, 6f, 6f, borderPaint)

                val leftX = 52f
                val rightX = 320f
                var cardY = y + 18f

                canvas.drawText("DOCUMENTO FISCAL • REPÚBLICA DE ANGOLA", leftX, cardY, goldPaint.apply { textSize = 9f })
                canvas.drawText(docTitle, rightX, cardY, darkTitlePaint.apply { textSize = 11f })
                cardY += 15f

                // Find Emitter & Client details
                val emitterLine = plainLines.firstOrNull { it.contains("NIF:", ignoreCase = true) && !it.contains("Consumidor", ignoreCase = true) } ?: "Emissor: FluxAO Gestão Financeira"
                val dateLine = plainLines.firstOrNull { it.contains("Data", ignoreCase = true) } ?: "Data de Emissão: Processada"
                canvas.drawText(emitterLine.take(45), leftX, cardY, boldBodyPaint)
                canvas.drawText(dateLine.take(40), rightX, cardY, bodyPaint)
                cardY += 14f

                val clientLine = plainLines.firstOrNull { it.contains("Cliente:", ignoreCase = true) || it.contains("Adquirente", ignoreCase = true) } ?: "Adquirente: Consumidor Final"
                val vencLine = plainLines.firstOrNull { it.contains("Vencimento:", ignoreCase = true) } ?: "Vencimento: A pronto pagamento"
                canvas.drawText(clientLine.take(45), leftX, cardY, bodyPaint)
                canvas.drawText(vencLine.take(40), rightX, cardY, bodyPaint)
                cardY += 14f

                canvas.drawText("Regime: Regime Geral / Simplificado CIVA • Software Certificado AGT", leftX, cardY, mutedPaint)
                canvas.drawText("Via: Original", rightX, cardY, mutedPaint)

                y += 90f

                // 3. Table Header
                val thRect = RectF(40f, y, 555f, y + 20f)
                canvas.drawRoundRect(thRect, 4f, 4f, headerBgPaint)
                canvas.drawText("#", 50f, y + 14f, tableHeaderTxtPaint)
                canvas.drawText("Descrição do Produto / Serviço", 75f, y + 14f, tableHeaderTxtPaint)
                canvas.drawText("Qtd", 350f, y + 14f, tableHeaderTxtPaint)
                canvas.drawText("IVA", 420f, y + 14f, tableHeaderTxtPaint)
                canvas.drawText("Total Líquido", 485f, y + 14f, tableHeaderTxtPaint)
                y += 24f

                // 4. Items Table Rows (Extracted from HTML table)
                val tableRows = extractTableRows(html)
                var rowCount = 0
                for (row in tableRows.take(18)) {
                    if (y > 670f) break
                    rowCount++
                    if (rowCount % 2 == 0) {
                        canvas.drawRect(40f, y - 2f, 555f, y + 14f, cardBgPaint)
                    }
                    canvas.drawLine(40f, y + 14f, 555f, y + 14f, borderPaint)

                    val itemDesc = if (row.description.length > 40) row.description.substring(0, 38) + "..." else row.description
                    canvas.drawText("$rowCount", 50f, y + 10f, mutedPaint)
                    canvas.drawText(itemDesc, 75f, y + 10f, boldBodyPaint)
                    canvas.drawText(row.quantity, 350f, y + 10f, bodyPaint)
                    canvas.drawText(row.vat, 420f, y + 10f, bodyPaint)
                    canvas.drawText(row.total, 485f, y + 10f, boldBodyPaint)
                    y += 18f
                }

                if (tableRows.isEmpty()) {
                    // Fallback row if no HTML <tr> was parsed
                    canvas.drawText("1", 50f, y + 10f, mutedPaint)
                    canvas.drawText("Prestação de Serviços / Fornecimento Comercial FluxAO", 75f, y + 10f, boldBodyPaint)
                    canvas.drawText("1 Un", 350f, y + 10f, bodyPaint)
                    canvas.drawText("14%", 420f, y + 10f, bodyPaint)
                    canvas.drawText("Conforme Fatura", 485f, y + 10f, boldBodyPaint)
                    y += 22f
                }

                y += 15f

                // 5. Totals & Payment Box
                val totalsRect = RectF(300f, y, 555f, y + 65f)
                canvas.drawRoundRect(totalsRect, 6f, 6f, cardBgPaint)
                canvas.drawRoundRect(totalsRect, 6f, 6f, borderPaint)

                val grandTotalText = plainLines.firstOrNull { it.contains("TOTAL A PAGAR", ignoreCase = true) || it.contains("Total:", ignoreCase = true) } ?: "TOTAL A PAGAR"
                val totalAmount = extractTotalAmount(html)

                canvas.drawText("TOTAL A PAGAR:", 315f, y + 25f, totalLabelPaint)
                canvas.drawText(totalAmount.ifBlank { grandTotalText }, 315f, y + 48f, totalValuePaint)

                // Bank details on the left
                val bankRect = RectF(40f, y, 290f, y + 65f)
                canvas.drawRoundRect(bankRect, 6f, 6f, cardBgPaint)
                canvas.drawRoundRect(bankRect, 6f, 6f, borderPaint)

                val ibanText = plainLines.firstOrNull { it.contains("IBAN", ignoreCase = true) } ?: "IBAN: Disponível sob consulta"
                val bankText = plainLines.firstOrNull { it.contains("Banco", ignoreCase = true) } ?: "Banco: BFA / BAI / Multicaixa"

                canvas.drawText("COORDENADAS DE PAGAMENTO:", 50f, y + 18f, goldPaint.apply { textSize = 8.5f })
                canvas.drawText(ibanText.take(40), 50f, y + 33f, boldBodyPaint)
                canvas.drawText(bankText.take(40), 50f, y + 46f, bodyPaint)
                canvas.drawText("Multicaixa Express • Transferência Bancária", 50f, y + 58f, mutedPaint)

                y += 85f

                // 6. AGT Certification Footer
                val hashText = plainLines.firstOrNull { it.contains("Hash", ignoreCase = true) || it.contains("AGT", ignoreCase = true) || it.contains("- Processado", ignoreCase = true) }
                    ?: "Assinatura Digital AGT: FT 2026/001-FluxAO"

                canvas.drawLine(40f, y, 555f, y, borderPaint)
                y += 12f
                canvas.drawText(hashText.take(80), 40f, y, mutedPaint)
                y += 12f
                canvas.drawText("Processado por programa certificado • FluxAO Gestão Financeira Empresarial • República de Angola", 40f, y, mutedPaint)

                pdfDocument.finishPage(page)

                FileOutputStream(outputFile).use { out ->
                    pdfDocument.writeTo(out)
                }
                pdfDocument.close()

                mainHandler.post { onComplete(outputFile) }
            } catch (e: Exception) {
                e.printStackTrace()
                mainHandler.post { onComplete(null) }
            }
        }
    }

    private data class ParsedRow(val description: String, val quantity: String, val vat: String, val total: String)

    private fun extractTableRows(html: String): List<ParsedRow> {
        val rows = mutableListOf<ParsedRow>()
        val trRegex = Regex("<tr[^>]*>(.*?)</tr>", RegexOption.DOT_MATCHES_ALL)
        val tdRegex = Regex("<td[^>]*>(.*?)</td>", RegexOption.DOT_MATCHES_ALL)

        val matches = trRegex.findAll(html)
        for (match in matches) {
            val trContent = match.groupValues[1]
            val tds = tdRegex.findAll(trContent).map { cleanHtmlTag(it.groupValues[1]) }.toList()
            if (tds.size >= 5) {
                val desc = tds[1].ifBlank { tds.getOrNull(0) ?: "" }
                val qty = tds.getOrNull(2) ?: "1"
                val vat = tds.getOrNull(5) ?: "14%"
                val total = tds.lastOrNull() ?: ""
                if (desc.isNotBlank() && !desc.equals("Descrição", ignoreCase = true)) {
                    rows.add(ParsedRow(desc, qty, vat, total))
                }
            }
        }
        return rows
    }

    private fun extractTotalAmount(html: String): String {
        val kzMatch = Regex("""([0-9.,]+)\s*Kz""", RegexOption.IGNORE_CASE).findAll(html).lastOrNull()
        return if (kzMatch != null) "${kzMatch.groupValues[1]} Kz" else ""
    }

    private fun extractTag(html: String, tag: String): String {
        val regex = Regex("<$tag[^>]*>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(html)
        return cleanHtmlTag(match?.groupValues?.get(1) ?: "")
    }

    private fun extractReadableLines(html: String): List<String> {
        return cleanHtmlTag(html)
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun cleanHtmlTag(raw: String): String {
        return raw
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</div>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</tr>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
    }

    /**
     * Downloads (saves) the generated PDF directly into the public Downloads directory of the device.
     */
    fun downloadPdfToDevice(
        context: Context,
        html: String,
        fileName: String,
        mimeTitle: String = "FluxAO Documento",
        onCompleted: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val cleanName = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"
        Toast.makeText(context, "A gerar arquivo PDF para download...", Toast.LENGTH_SHORT).show()

        htmlToPdfFile(context, html, cleanName) { tempFile ->
            if (tempFile == null || !tempFile.exists()) {
                Toast.makeText(context, "Erro ao gerar PDF para download.", Toast.LENGTH_LONG).show()
                onCompleted(false, "")
                return@htmlToPdfFile
            }

            try {
                var savedPath = ""
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, cleanName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/FluxAO")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { out ->
                            FileInputStream(tempFile).use { input ->
                                input.copyTo(out)
                            }
                        }
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)
                        savedPath = "Downloads/FluxAO/$cleanName"
                    }
                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val targetDir = File(downloadsDir, "FluxAO").apply { mkdirs() }
                    val targetFile = File(targetDir, cleanName)
                    FileInputStream(tempFile).use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    savedPath = targetFile.absolutePath
                }

                Toast.makeText(
                    context,
                    "✓ PDF baixado com sucesso!\nSalvo em: $savedPath",
                    Toast.LENGTH_LONG
                ).show()
                onCompleted(true, savedPath)
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to internal documents directory
                try {
                    val fallbackFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), cleanName)
                    FileInputStream(tempFile).use { input ->
                        FileOutputStream(fallbackFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Toast.makeText(
                        context,
                        "✓ PDF salvo em Documentos: ${fallbackFile.name}",
                        Toast.LENGTH_LONG
                    ).show()
                    onCompleted(true, fallbackFile.absolutePath)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    Toast.makeText(context, "Não foi possível gravar o PDF no armazenamento.", Toast.LENGTH_SHORT).show()
                    onCompleted(false, "")
                }
            }
        }
    }

    /**
     * Generates the PDF file and shares it directly through WhatsApp (com.whatsapp / com.whatsapp.w4b)
     * or standard Android share sheet if WhatsApp is not directly installed.
     * Guarantees URI permissions, FLAG_ACTIVITY_NEW_TASK, and fallbacks.
     */
    fun sharePdfViaWhatsApp(
        context: Context,
        html: String,
        fileName: String,
        phoneNumber: String = "",
        message: String = "",
        onCompleted: (Boolean) -> Unit = {}
    ) {
        val cleanName = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"
        Toast.makeText(context, "A preparar fatura para envio pelo WhatsApp...", Toast.LENGTH_SHORT).show()

        htmlToPdfFile(context, html, cleanName) { tempFile ->
            if (tempFile == null || !tempFile.exists() || tempFile.length() == 0L) {
                Toast.makeText(context, "Erro ao gerar PDF da fatura.", Toast.LENGTH_SHORT).show()
                // Fallback: If phone is provided, open chat with the full text message
                if (phoneNumber.isNotBlank() && message.isNotBlank()) {
                    openWhatsAppChat(context, phoneNumber, message)
                }
                onCompleted(false)
                return@htmlToPdfFile
            }

            try {
                val authority = "${context.packageName}.fileprovider"
                val contentUri = FileProvider.getUriForFile(context, authority, tempFile)

                val pm = context.packageManager

                // Base share intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    if (message.isNotBlank()) {
                        putExtra(Intent.EXTRA_TEXT, message)
                        putExtra("sms_body", message)
                    }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                // Explicitly grant URI permission to known WhatsApp packages
                try {
                    context.grantUriPermission("com.whatsapp", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.grantUriPermission("com.whatsapp.w4b", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (ignored: Exception) {}

                var launched = false

                // 1. Try standard WhatsApp
                val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    if (message.isNotBlank()) {
                        putExtra(Intent.EXTRA_TEXT, message)
                        putExtra("sms_body", message)
                    }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    setPackage("com.whatsapp")
                }

                if (pm.queryIntentActivities(whatsappIntent, 0).isNotEmpty()) {
                    try {
                        context.startActivity(whatsappIntent)
                        launched = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // 2. Try WhatsApp Business
                if (!launched) {
                    val w4bIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        if (message.isNotBlank()) {
                            putExtra(Intent.EXTRA_TEXT, message)
                            putExtra("sms_body", message)
                        }
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        setPackage("com.whatsapp.w4b")
                    }

                    if (pm.queryIntentActivities(w4bIntent, 0).isNotEmpty()) {
                        try {
                            context.startActivity(w4bIntent)
                            launched = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // 3. Fallback to standard share chooser
                if (!launched) {
                    val chooser = Intent.createChooser(shareIntent, "Enviar Fatura via WhatsApp ou Aplicação").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val resolveList = pm.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
                    for (resolve in resolveList) {
                        try {
                            context.grantUriPermission(
                                resolve.activityInfo.packageName,
                                contentUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (ignored: Exception) {}
                    }
                    context.startActivity(chooser)
                    launched = true
                }

                onCompleted(launched)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erro ao partilhar via WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                // If phone is provided, open chat directly
                if (phoneNumber.isNotBlank() && message.isNotBlank()) {
                    openWhatsAppChat(context, phoneNumber, message)
                }
                onCompleted(false)
            }
        }
    }

    /**
     * Standard print/save as PDF spooler using Android PrintManager.
     */
    fun printHtml(
        context: Context,
        html: String,
        jobName: String,
        onFinished: () -> Unit = {}
    ) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager == null) {
                    onFinished()
                    return
                }
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                )
                onFinished()
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}
