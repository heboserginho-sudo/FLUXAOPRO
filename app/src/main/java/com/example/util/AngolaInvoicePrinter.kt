package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.data.model.InvoiceCustomization
import java.text.NumberFormat
import java.util.Locale

object AngolaInvoicePrinter {

    private fun formatKz(amount: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.GERMANY)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return "${nf.format(amount)} Kz"
    }

    fun generateHtmlInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        customization: InvoiceCustomization = InvoiceCustomization()
    ): String {
        val accentColor = customization.accentColorHex.ifBlank { "#D4AF37" }

        val logoHtml = if (customization.showLogoOnInvoice && customization.logoBase64.isNotBlank()) {
            """<img src="data:image/png;base64,${customization.logoBase64}" style="max-height: 65px; max-width: 220px; object-fit: contain; margin-bottom: 6px; display: block;" alt="Logótipo Empresa" />"""
        } else {
            """
            <div style="display: flex; align-items: center; margin-bottom: 6px;">
                <span style="font-size: 22px; margin-right: 6px;">🏛️</span>
                <span style="font-size: 15px; font-weight: 800; color: #0f172a;">${escapeHtml(invoice.emitterName)}</span>
            </div>
            """.trimIndent()
        }

        val watermarkHtml = if (customization.showWatermark) {
            """
            <div style="position: fixed; top: 40%; left: 15%; width: 70%; text-align: center; transform: rotate(-30deg); font-size: 80px; font-weight: 900; color: rgba(200, 200, 200, 0.14); z-index: -1; pointer-events: none; text-transform: uppercase;">
                ${if (invoice.status == com.example.data.model.invoice.InvoiceStatus.PAID) "PAGO" else "ORIGINAL"}
            </div>
            """.trimIndent()
        } else ""

        val itemsRows = StringBuilder()
        items.forEachIndexed { index, item ->
            val exemptionNote = if (item.vatRate == 0.0 && item.exemptionCode.isNotBlank()) {
                "<br><small style='color: #666;'>(${item.exemptionCode})</small>"
            } else ""

            itemsRows.append(
                """
                <tr>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd; text-align: center;">${index + 1}</td>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd;">
                        <strong>${escapeHtml(item.description)}</strong>$exemptionNote
                    </td>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd; text-align: center;">${item.quantity} ${item.unit}</td>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd; text-align: right;">${formatKz(item.unitPriceKz)}</td>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd; text-align: center;">${if (item.discountPercent > 0) "${item.discountPercent}%" else "-"}</td>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd; text-align: center;">${item.vatRate}%</td>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd; text-align: right; font-weight: bold;">${formatKz(item.finalTotal)}</td>
                </tr>
                """.trimIndent()
            )
        }

        // Group VAT summary
        val vatGroups = items.groupBy { it.vatRate }
        val vatSummaryRows = StringBuilder()
        vatGroups.forEach { (rate, groupItems) ->
            val baseIncidence = groupItems.sumOf { it.netTotal }
            val vatTotalGroup = groupItems.sumOf { it.vatAmount }
            val reason = if (rate == 0.0) groupItems.firstOrNull { it.exemptionCode.isNotBlank() }?.exemptionCode ?: "Isento Art. 12 CIVA" else "Taxa Normal / Simplificada"
            vatSummaryRows.append(
                """
                <tr>
                    <td style="padding: 6px; border: 1px solid #ccc; text-align: center;">$rate%</td>
                    <td style="padding: 6px; border: 1px solid #ccc; text-align: right;">${formatKz(baseIncidence)}</td>
                    <td style="padding: 6px; border: 1px solid #ccc; text-align: right;">${formatKz(vatTotalGroup)}</td>
                    <td style="padding: 6px; border: 1px solid #ccc;">$reason</td>
                </tr>
                """.trimIndent()
            )
        }

        val docTypeTitle = when (invoice.invoiceType.code) {
            "FT" -> "FATURA COMERCIAL"
            "FR" -> "FATURA-RECIBO"
            "FP" -> "FATURA PRÓ-FORMA"
            "NC" -> "NOTA DE CRÉDITO"
            else -> "FATURA"
        }

        val bankInfoHtml = if (customization.bankAccountInfo.isNotBlank()) {
            "<div>Dados Bancários: <strong>${escapeHtml(customization.bankAccountInfo)}</strong></div>"
        } else if (invoice.emitterIban.isNotBlank()) {
            "<div>IBAN: <strong style='font-family: monospace;'>${escapeHtml(invoice.emitterIban)}</strong></div>"
        } else ""

        val customFooter = if (customization.customFooterNotes.isNotBlank()) {
            "<div style='margin-bottom: 6px; font-weight: 500;'>${escapeHtml(customization.customFooterNotes)}</div>"
        } else ""

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8"/>
            <title>${invoice.invoiceNumber} - FluxAO</title>
            <style>
                @page {
                    size: A4;
                    margin: 12mm 14mm;
                }
                * {
                    box-sizing: border-box;
                    -webkit-print-color-adjust: exact !important;
                    print-color-adjust: exact !important;
                }
                body {
                    font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                    margin: 0;
                    padding: 10px;
                    color: #111;
                    font-size: 13px;
                    line-height: 1.4;
                    position: relative;
                }
                .header-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 20px;
                }
                .brand-title {
                    font-size: 24px;
                    font-weight: 900;
                    color: #111;
                    letter-spacing: -0.5px;
                }
                .brand-ao {
                    color: $accentColor;
                }
                .doc-box {
                    background-color: #f8fafc;
                    border: 1px solid #e2e8f0;
                    border-left: 4px solid $accentColor;
                    padding: 12px;
                    border-radius: 6px;
                }
                .info-box {
                    background-color: #fafafa;
                    border: 1px solid #e5e7eb;
                    padding: 12px;
                    border-radius: 6px;
                    margin-bottom: 16px;
                }
                table.items-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 15px;
                    margin-bottom: 20px;
                }
                table.items-table th {
                    background-color: #0f172a;
                    color: #fff;
                    padding: 8px;
                    text-align: left;
                    font-size: 12px;
                    text-transform: uppercase;
                    border-top: 2px solid $accentColor;
                }
                .totals-table {
                    width: 320px;
                    margin-left: auto;
                    border-collapse: collapse;
                    margin-bottom: 20px;
                }
                .totals-table td {
                    padding: 6px 10px;
                }
                .totals-table tr.grand-total td {
                    border-top: 2px solid $accentColor;
                    font-size: 15px;
                    font-weight: bold;
                    color: #000;
                    background-color: #fffbeb;
                }
                .footer {
                    margin-top: 30px;
                    padding-top: 12px;
                    border-top: 1px dashed #aaa;
                    font-size: 11px;
                    color: #555;
                    text-align: center;
                }
            </style>
        </head>
        <body>
            $watermarkHtml

            <table class="header-table">
                <tr>
                    <td style="vertical-align: top; width: 55%;">
                        $logoHtml
                        <div class="brand-title">Flux<span class="brand-ao">AO</span></div>
                        <div style="font-size: 14px; font-weight: bold; margin-top: 4px;">${escapeHtml(invoice.emitterName)}</div>
                        <div>NIF: <strong>${escapeHtml(invoice.emitterNif)}</strong></div>
                        <div>Endereço: ${escapeHtml(invoice.emitterAddress)}</div>
                        <div>${escapeHtml(invoice.emitterCity)} - República de Angola</div>
                        <div>Regime: <strong>${escapeHtml(invoice.vatRegime)}</strong></div>
                    </td>
                    <td style="vertical-align: top; width: 45%;">
                        <div class="doc-box">
                            <div style="font-size: 16px; font-weight: bold; color: #0f172a;">$docTypeTitle</div>
                            <div style="font-size: 15px; font-weight: bold; color: $accentColor; margin: 4px 0;">${invoice.invoiceNumber}</div>
                            <div>Data de Emissão: <strong>${invoice.formattedDate}</strong></div>
                            <div>Data de Vencimento: <strong>${invoice.formattedDueDate}</strong></div>
                            <div>Via: <strong>Original</strong></div>
                        </div>
                    </td>
                </tr>
            </table>

            <div class="info-box">
                <table style="width: 100%;">
                    <tr>
                        <td style="vertical-align: top;">
                            <strong style="color: #666; font-size: 11px; text-transform: uppercase;">Exmo.(s) Sr.(s) Adquirente / Cliente:</strong>
                            <div style="font-size: 14px; font-weight: bold; margin-top: 2px;">${escapeHtml(invoice.clientName)}</div>
                            <div>NIF: <strong>${escapeHtml(invoice.clientNif)}</strong></div>
                            <div>Endereço: ${escapeHtml(invoice.clientAddress)}, ${escapeHtml(invoice.clientCity)}</div>
                            ${if (invoice.clientPhone.isNotBlank()) "<div>Contacto: ${escapeHtml(invoice.clientPhone)}</div>" else ""}
                        </td>
                        <td style="vertical-align: top; width: 40%;">
                            <strong style="color: #666; font-size: 11px; text-transform: uppercase;">Condições Comerciais:</strong>
                            <div>Forma de Pagamento: <strong>${escapeHtml(invoice.paymentMethod)}</strong></div>
                            <div>Estado: <strong>${invoice.status.title}</strong></div>
                            $bankInfoHtml
                            ${if (invoice.emitterBank.isNotBlank() && customization.bankAccountInfo.isBlank()) "<div>Banco: <strong>${escapeHtml(invoice.emitterBank)}</strong></div>" else ""}
                        </td>
                    </tr>
                </table>
            </div>

            <table class="items-table">
                <thead>
                    <tr>
                        <th style="width: 5%; text-align: center;">#</th>
                        <th style="width: 45%;">Descrição</th>
                        <th style="width: 12%; text-align: center;">Qtd</th>
                        <th style="width: 15%; text-align: right;">P. Unit.</th>
                        <th style="width: 8%; text-align: center;">Desc.</th>
                        <th style="width: 7%; text-align: center;">IVA</th>
                        <th style="width: 18%; text-align: right;">Total Líquido</th>
                    </tr>
                </thead>
                <tbody>
                    $itemsRows
                </tbody>
            </table>

            <table style="width: 100%;">
                <tr>
                    <td style="vertical-align: top; width: 55%;">
                        <strong style="font-size: 11px; text-transform: uppercase; color: #444;">Quadro Resumo de Imposto (IVA - AGT)</strong>
                        <table style="width: 100%; border-collapse: collapse; margin-top: 6px; font-size: 11px;">
                            <thead>
                                <tr style="background: #eef2f6;">
                                    <th style="border: 1px solid #ccc; padding: 4px;">Taxa</th>
                                    <th style="border: 1px solid #ccc; padding: 4px;">Incidência</th>
                                    <th style="border: 1px solid #ccc; padding: 4px;">Montante</th>
                                    <th style="border: 1px solid #ccc; padding: 4px;">Enquadramento Legal</th>
                                </tr>
                            </thead>
                            <tbody>
                                $vatSummaryRows
                            </tbody>
                        </table>
                        <div style="margin-top: 10px; font-size: 11px; color: #555;">
                            <em>${escapeHtml(invoice.notes)}</em>
                        </div>
                    </td>
                    <td style="vertical-align: top; width: 45%;">
                        <table class="totals-table">
                            <tr>
                                <td>Total Ilíquido:</td>
                                <td style="text-align: right; font-weight: bold;">${formatKz(invoice.totalGross)}</td>
                            </tr>
                            ${if (invoice.totalDiscount > 0) """
                            <tr>
                                <td>Desconto Comercial:</td>
                                <td style="text-align: right; color: #c00;">-${formatKz(invoice.totalDiscount)}</td>
                            </tr>
                            """ else ""}
                            <tr>
                                <td>Base de Incidência:</td>
                                <td style="text-align: right;">${formatKz(invoice.totalNet)}</td>
                            </tr>
                            <tr>
                                <td>Total de IVA:</td>
                                <td style="text-align: right; font-weight: bold;">${formatKz(invoice.totalVat)}</td>
                            </tr>
                            <tr class="grand-total">
                                <td>TOTAL A PAGAR:</td>
                                <td style="text-align: right;">${formatKz(invoice.totalToPay)}</td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>

            <div class="footer">
                $customFooter
                <div>${escapeHtml(invoice.fiscalValidationHash)}</div>
                <div style="margin-top: 4px;">Processado por computador • FluxAO Gestão Financeira Empresarial • República de Angola</div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun printInvoice(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        customization: InvoiceCustomization = InvoiceCustomization(),
        onFinished: () -> Unit = {}
    ) {
        val webView = WebView(context)
        val htmlContent = generateHtmlInvoice(invoice, items, customization)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager == null) {
                    onFinished()
                    return
                }
                val printAdapter = webView.createPrintDocumentAdapter("${invoice.invoiceNumber}_FluxAO")
                val jobName = "${invoice.invoiceNumber} FluxAO Angola"
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

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    /**
     * Direct alias for exporting invoice to PDF using the native Android print spooler (Save as PDF).
     */
    fun exportInvoicePdf(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        customization: InvoiceCustomization = InvoiceCustomization(),
        onFinished: () -> Unit = {}
    ) {
        printInvoice(context, invoice, items, customization, onFinished)
    }

    /**
     * Generates a printable consolidated HTML document listing all invoices issued in the period.
     */
    fun generateHtmlInvoicesSummary(
        invoices: List<InvoiceEntity>,
        customization: InvoiceCustomization = InvoiceCustomization()
    ): String {
        val accentColor = customization.accentColorHex.ifBlank { "#D4AF37" }
        val logoHtml = if (customization.showLogoOnInvoice && customization.logoBase64.isNotBlank()) {
            """<img src="data:image/png;base64,${customization.logoBase64}" style="max-height: 55px; max-width: 200px; object-fit: contain; margin-bottom: 6px; display: block;" alt="Logótipo" />"""
        } else {
            """<div style="font-size: 18px; font-weight: 800; color: #0f172a;">Flux<span style="color: $accentColor;">AO</span></div>"""
        }

        val totalIssued = invoices.sumOf { it.totalToPay }
        val totalPaid = invoices.filter { it.status == com.example.data.model.invoice.InvoiceStatus.PAID }.sumOf { it.totalToPay }
        val totalPending = invoices.filter { it.status == com.example.data.model.invoice.InvoiceStatus.PENDING }.sumOf { it.totalToPay }

        val rows = StringBuilder()
        invoices.forEachIndexed { idx, inv ->
            val statusColor = if (inv.status == com.example.data.model.invoice.InvoiceStatus.PAID) "#047857" else "#b45309"
            rows.append(
                """
                <tr>
                    <td style="padding: 7px; border-bottom: 1px solid #e2e8f0; text-align: center;">${idx + 1}</td>
                    <td style="padding: 7px; border-bottom: 1px solid #e2e8f0; font-weight: bold;">${inv.invoiceNumber}</td>
                    <td style="padding: 7px; border-bottom: 1px solid #e2e8f0;">${escapeHtml(inv.clientName)}<br><small style="color: #64748b;">NIF: ${escapeHtml(inv.clientNif)}</small></td>
                    <td style="padding: 7px; border-bottom: 1px solid #e2e8f0; text-align: center;">${inv.formattedDate}</td>
                    <td style="padding: 7px; border-bottom: 1px solid #e2e8f0; text-align: center; color: $statusColor; font-weight: bold;">${inv.status.title}</td>
                    <td style="padding: 7px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: bold;">${formatKz(inv.totalToPay)}</td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8"/>
            <title>Relatório de Faturas Emitidas - FluxAO</title>
            <style>
                @page { size: A4; margin: 12mm 14mm; }
                * { box-sizing: border-box; -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; }
                body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; font-size: 12px; margin: 0; padding: 0; color: #0f172a; line-height: 1.4; }
                table { width: 100%; border-collapse: collapse; margin-top: 14px; font-size: 11px; }
                th { background: #0f172a; color: #fff; padding: 8px; text-align: left; }
                .kpi-row { display: flex; gap: 10px; margin: 14px 0; }
                .kpi-card { flex: 1; background: #f8fafc; border: 1px solid #e2e8f0; padding: 10px; border-radius: 6px; text-align: center; }
            </style>
        </head>
        <body>
            <div style="display: flex; justify-content: space-between; border-bottom: 2px solid $accentColor; padding-bottom: 10px;">
                <div>
                    $logoHtml
                    <div style="font-size: 15px; font-weight: bold;">Relatório de Faturação &amp; Documentos Fiscais AGT</div>
                </div>
                <div style="text-align: right; font-size: 11px; color: #64748b;">
                    <div>Total de Documentos: <strong>${invoices.size}</strong></div>
                    <div>Processado por FluxAO</div>
                </div>
            </div>

            <div class="kpi-row">
                <div class="kpi-card">
                    <div style="font-size: 10px; color: #64748b; text-transform: uppercase;">Total Faturado</div>
                    <div style="font-size: 15px; font-weight: 800; color: #0f172a; margin-top: 2px;">${formatKz(totalIssued)}</div>
                </div>
                <div class="kpi-card">
                    <div style="font-size: 10px; color: #64748b; text-transform: uppercase;">Liquidado / Pago</div>
                    <div style="font-size: 15px; font-weight: 800; color: #047857; margin-top: 2px;">${formatKz(totalPaid)}</div>
                </div>
                <div class="kpi-card">
                    <div style="font-size: 10px; color: #64748b; text-transform: uppercase;">Pendente de Cobrança</div>
                    <div style="font-size: 15px; font-weight: 800; color: #b45309; margin-top: 2px;">${formatKz(totalPending)}</div>
                </div>
            </div>

            <table>
                <thead>
                    <tr>
                        <th style="width: 5%; text-align: center;">#</th>
                        <th style="width: 22%;">Nº Fatura</th>
                        <th style="width: 35%;">Cliente / NIF</th>
                        <th style="width: 14%; text-align: center;">Emissão</th>
                        <th style="width: 12%; text-align: center;">Estado</th>
                        <th style="width: 12%; text-align: right;">Total</th>
                    </tr>
                </thead>
                <tbody>
                    $rows
                </tbody>
            </table>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Exports the summary list of invoices to PDF.
     */
    fun exportInvoicesSummaryPdf(
        context: Context,
        invoices: List<InvoiceEntity>,
        customization: InvoiceCustomization = InvoiceCustomization(),
        onFinished: () -> Unit = {}
    ) {
        val webView = WebView(context)
        val htmlContent = generateHtmlInvoicesSummary(invoices, customization)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager == null) {
                    onFinished()
                    return
                }
                val printAdapter = webView.createPrintDocumentAdapter("Relatorio_Faturas_FluxAO")
                printManager.print(
                    "Relatorio_Faturas_FluxAO",
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

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    fun downloadInvoicePdf(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        customization: InvoiceCustomization = InvoiceCustomization(),
        onCompleted: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val htmlContent = generateHtmlInvoice(invoice, items, customization)
        val cleanNumber = invoice.invoiceNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "${cleanNumber}_FluxAO.pdf"
        PdfDocumentManager.downloadPdfToDevice(context, htmlContent, fileName, "Fatura ${invoice.invoiceNumber}", onCompleted)
    }

    /**
     * Sends the complete invoice message directly to the client's WhatsApp chat.
     */
    fun sendInvoiceViaWhatsAppChat(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity> = emptyList(),
        phoneOverride: String = ""
    ) {
        val targetPhone = phoneOverride.ifBlank { invoice.clientPhone }
        val caption = generateShareText(invoice, items)
        PdfDocumentManager.openWhatsAppChat(context, targetPhone, caption)
    }

    /**
     * Generates and shares the official A4 invoice PDF via WhatsApp or standard share sheet.
     */
    fun shareInvoicePdfViaWhatsApp(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        customization: InvoiceCustomization = InvoiceCustomization(),
        phoneOverride: String = "",
        onCompleted: (Boolean) -> Unit = {}
    ) {
        val targetPhone = phoneOverride.ifBlank { invoice.clientPhone }
        val htmlContent = generateHtmlInvoice(invoice, items, customization)
        val cleanNumber = invoice.invoiceNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "${cleanNumber}_FluxAO.pdf"
        val caption = generateShareText(invoice, items)
        PdfDocumentManager.sharePdfViaWhatsApp(
            context = context,
            html = htmlContent,
            fileName = fileName,
            phoneNumber = targetPhone,
            message = caption,
            onCompleted = onCompleted
        )
    }

    fun generateShareText(invoice: InvoiceEntity, items: List<InvoiceItemEntity> = emptyList()): String {
        val itemsSummary = if (items.isNotEmpty()) {
            val list = items.take(6).joinToString("\n") { "  • ${it.quantity}x ${it.description} - ${formatKz(it.finalTotal)}" }
            val more = if (items.size > 6) "\n  • ... e mais ${items.size - 6} item(ns)" else ""
            "\n📦 *Itens do Documento:*\n$list$more\n"
        } else ""

        val bankDetails = if (invoice.emitterIban.isNotBlank()) {
            "🏦 *IBAN para Pagamento:* ${invoice.emitterIban}\n${if (invoice.emitterBank.isNotBlank()) "🏦 *Banco:* ${invoice.emitterBank}\n" else ""}"
        } else "🏦 *IBAN:* Solicitar à empresa\n"

        return """
        *FLUXAO - GESTÃO & FATURAÇÃO EMPRESARIAL*
        📄 *${invoice.invoiceType.title.uppercase()} Nº ${invoice.invoiceNumber}*

        Olá *${invoice.clientName}*,
        Segue os dados da sua fatura emitida pelo sistema de faturação FluxAO:

        🏢 *Emissor:* ${invoice.emitterName} (NIF: ${invoice.emitterNif})
        👤 *Adquirente:* ${invoice.clientName} (NIF: ${invoice.clientNif})
        📅 *Emissão:* ${invoice.formattedDate} | *Vencimento:* ${invoice.formattedDueDate}
        $itemsSummary
        💰 *TOTAL A PAGAR: ${formatKz(invoice.totalToPay)}*
        $bankDetails
        📱 *Multicaixa Express:* Disponível
        ⚖️ *Certificação AGT:* ${invoice.fiscalValidationHash.ifBlank { "FT-AGT-ORIGINAL" }}

        _Por favor, envie o comprovativo após a liquidação do valor._
        _Documento processado por programa certificado • FluxAO Angola._
        """.trimIndent()
    }
}
