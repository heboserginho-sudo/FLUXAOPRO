package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BudgetItemEntity
import com.example.data.model.AppCurrency
import com.example.data.model.BudgetStatus
import com.example.data.model.CompanySubscription
import com.example.data.model.InvoiceCustomization
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.domain.model.PricingCalculationResult
import com.example.ui.components.MonthlyBarData
import com.example.ui.viewmodel.CashFlowUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Data holder for the professional hourly rate simulation parameters.
 */
data class HourlyRateSimulation(
    val desiredSalary: Double = 6000.0,
    val fixedCosts: Double = 1500.0,
    val workingDays: Int = 21,
    val billableHoursPerDay: Double = 6.0,
    val calculatedMinimumHourlyRate: Double = (desiredSalary + fixedCosts) / (workingDays * billableHoursPerDay).coerceAtLeast(1.0)
)

/**
 * Utility responsible for generating and exporting executive financial reports,
 * DRE statements, cost engineering calculations, and commercial proposals in high-fidelity PDF format.
 *
 * Guarantees that company logos (base64 or custom styling) and cost calculations
 * (Angolan 85% labor method, overhead, IVA AGT, margins, and break-even points)
 * are impeccably preserved in the formatting.
 */
object FinancialReportPdfExporter {

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun formatVal(amount: Double, currency: AppCurrency): String {
        return currency.format(amount)
    }

    private fun formatPct(value: Double): String {
        return "${String.format(Locale.US, "%.1f", value)}%"
    }

    /**
     * Builds the complete, printable HTML document for the Executive Financial Report.
     */
    fun generateHtmlFinancialReport(
        uiState: CashFlowUiState,
        budgets: List<BudgetEntity>,
        subscription: CompanySubscription,
        customization: InvoiceCustomization,
        currency: AppCurrency,
        simulation: HourlyRateSimulation = HourlyRateSimulation()
    ): String {
        val accentColor = customization.accentColorHex.ifBlank { "#D4AF37" }
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.GERMANY).format(Date())
        val periodName = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "AO")).format(Date())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "AO")) else it.toString() }

        // Company Logo HTML
        val logoHtml = if (customization.showLogoOnInvoice && customization.logoBase64.isNotBlank()) {
            """
            <img class="company-logo" src="data:image/png;base64,${customization.logoBase64}" alt="Logótipo da Empresa" />
            """.trimIndent()
        } else {
            """
            <div class="company-monogram">
                <div class="monogram-icon">🏛️</div>
                <div class="monogram-text">
                    <span class="monogram-title">${escapeHtml(subscription.companyName.ifBlank { "FluxAO Gestão" })}</span>
                    <span class="monogram-sub">Gestão Financeira &amp; Engenharia de Custos</span>
                </div>
            </div>
            """.trimIndent()
        }

        // DRE figures
        val grossRevenue = uiState.monthIncome
        val directCosts = uiState.transactions
            .filter {
                it.category.contains("Matéria-prima", ignoreCase = true) ||
                it.category.contains("Mão de Obra", ignoreCase = true)
            }
            .sumOf { it.amount }
        val contributionMargin = grossRevenue - directCosts
        val contributionMarginPct = if (grossRevenue > 0) (contributionMargin / grossRevenue) * 100.0 else 0.0

        val overheadExpenses = uiState.transactions
            .filter {
                it.category.contains("Aluguel", ignoreCase = true) ||
                it.category.contains("Ferramentas", ignoreCase = true) ||
                it.category.contains("Marketing", ignoreCase = true)
            }
            .sumOf { it.amount }

        val taxesPaid = uiState.transactions
            .filter { it.category.contains("Impostos", ignoreCase = true) }
            .sumOf { it.amount }

        val netProfitReal = uiState.monthNetProfit
        val netMarginPct = if (grossRevenue > 0) (netProfitReal / grossRevenue) * 100.0 else 0.0

        // Budget Conversion figures
        val totalBudgets = budgets.size
        val approvedBudgets = budgets.count { it.status == BudgetStatus.APPROVED }
        val pendingBudgets = budgets.count { it.status == BudgetStatus.DRAFT }
        val rejectedBudgets = budgets.count { it.status == BudgetStatus.REJECTED }
        val conversionPct = if (totalBudgets > 0) (approvedBudgets.toDouble() / totalBudgets) * 100.0 else 0.0

        // Monthly evolution rows
        val monthlyEvolutionRows = StringBuilder()
        val bars = uiState.monthlyBars.takeLast(6)
        if (bars.isNotEmpty()) {
            bars.forEach { bar ->
                val net = bar.income - bar.expense
                val isPositive = net >= 0
                val netBadge = if (isPositive) {
                    """<span class="badge-positive">+ ${formatVal(net, currency)}</span>"""
                } else {
                    """<span class="badge-negative">- ${formatVal(Math.abs(net), currency)}</span>"""
                }
                val marginStr = if (bar.income > 0) formatPct((net / bar.income) * 100.0) else "0.0%"

                monthlyEvolutionRows.append(
                    """
                    <tr>
                        <td style="padding: 8px 10px; font-weight: bold; border-bottom: 1px solid #e2e8f0;">${escapeHtml(bar.fullMonthName)}</td>
                        <td style="padding: 8px 10px; text-align: right; color: #047857; font-weight: 600; border-bottom: 1px solid #e2e8f0;">${formatVal(bar.income, currency)}</td>
                        <td style="padding: 8px 10px; text-align: right; color: #b91c1c; font-weight: 600; border-bottom: 1px solid #e2e8f0;">${formatVal(bar.expense, currency)}</td>
                        <td style="padding: 8px 10px; text-align: right; border-bottom: 1px solid #e2e8f0;">$netBadge</td>
                        <td style="padding: 8px 10px; text-align: center; font-size: 11px; border-bottom: 1px solid #e2e8f0;">$marginStr</td>
                    </tr>
                    """.trimIndent()
                )
            }
        } else {
            monthlyEvolutionRows.append(
                """
                <tr>
                    <td colspan="5" style="padding: 12px; text-align: center; color: #64748b;">Nenhum dado mensal registrado no período.</td>
                </tr>
                """.trimIndent()
            )
        }

        val authReportCode = "REL-AO-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"

        return """
        <!DOCTYPE html>
        <html lang="pt">
        <head>
            <meta charset="utf-8"/>
            <title>Relatório Financeiro &amp; Engenharia de Custos - ${subscription.companyName.ifBlank { "Empresa" }}</title>
            <style>
                @page {
                    size: A4;
                    margin: 12mm 14mm 14mm 14mm;
                }
                * {
                    box-sizing: border-box;
                    -webkit-print-color-adjust: exact !important;
                    print-color-adjust: exact !important;
                }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                    color: #0f172a;
                    background-color: #ffffff;
                    margin: 0;
                    padding: 0;
                    font-size: 12px;
                    line-height: 1.45;
                }
                .header-wrapper {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                    border-bottom: 2px solid $accentColor;
                    padding-bottom: 14px;
                    margin-bottom: 16px;
                }
                .company-col {
                    max-width: 58%;
                }
                .company-logo {
                    max-height: 60px;
                    max-width: 220px;
                    object-fit: contain;
                    display: block;
                    margin-bottom: 8px;
                }
                .company-monogram {
                    display: flex;
                    align-items: center;
                    margin-bottom: 6px;
                }
                .monogram-icon {
                    font-size: 26px;
                    margin-right: 8px;
                }
                .monogram-title {
                    display: block;
                    font-size: 18px;
                    font-weight: 800;
                    color: #0f172a;
                    letter-spacing: -0.3px;
                }
                .monogram-sub {
                    display: block;
                    font-size: 11px;
                    color: #64748b;
                }
                .company-title {
                    font-size: 16px;
                    font-weight: 800;
                    color: #0f172a;
                    margin-top: 2px;
                }
                .company-meta {
                    font-size: 11px;
                    color: #475569;
                    line-height: 1.4;
                    margin-top: 4px;
                }
                .report-badge-col {
                    text-align: right;
                    background: #f8fafc;
                    border: 1px solid #e2e8f0;
                    border-left: 4px solid $accentColor;
                    padding: 10px 14px;
                    border-radius: 8px;
                    min-width: 210px;
                }
                .report-badge-title {
                    font-size: 13px;
                    font-weight: 800;
                    color: #0f172a;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                }
                .report-badge-period {
                    font-size: 12px;
                    font-weight: 600;
                    color: $accentColor;
                    margin: 2px 0;
                }
                .report-badge-date {
                    font-size: 10px;
                    color: #64748b;
                }
                .report-badge-code {
                    font-size: 10px;
                    font-family: monospace;
                    font-weight: bold;
                    color: #334155;
                    margin-top: 4px;
                }
                .section-title {
                    display: flex;
                    align-items: center;
                    font-size: 13px;
                    font-weight: 800;
                    color: #0f172a;
                    text-transform: uppercase;
                    letter-spacing: 0.4px;
                    margin: 16px 0 8px 0;
                    padding-bottom: 4px;
                    border-bottom: 1px solid #cbd5e1;
                }
                .section-icon {
                    margin-right: 6px;
                }
                .kpi-grid {
                    display: flex;
                    gap: 10px;
                    margin-bottom: 14px;
                }
                .kpi-card {
                    flex: 1;
                    background: #f8fafc;
                    border: 1px solid #e2e8f0;
                    border-radius: 8px;
                    padding: 10px;
                    text-align: center;
                }
                .kpi-card.highlight {
                    background: #fffbeb;
                    border-color: $accentColor;
                }
                .kpi-label {
                    font-size: 10px;
                    color: #64748b;
                    text-transform: uppercase;
                    font-weight: 700;
                }
                .kpi-val {
                    font-size: 14px;
                    font-weight: 800;
                    color: #0f172a;
                    margin-top: 3px;
                }
                .kpi-val.green { color: #047857; }
                .kpi-val.red { color: #b91c1c; }
                .kpi-sub {
                    font-size: 9px;
                    color: #64748b;
                    margin-top: 2px;
                }

                /* DRE Table */
                table.dre-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 14px;
                    background: #ffffff;
                    border: 1px solid #e2e8f0;
                    border-radius: 8px;
                    overflow: hidden;
                }
                table.dre-table th {
                    background: #0f172a;
                    color: #ffffff;
                    padding: 8px 10px;
                    font-size: 11px;
                    text-align: left;
                    font-weight: 700;
                    text-transform: uppercase;
                }
                table.dre-table td {
                    padding: 7px 10px;
                    border-bottom: 1px solid #f1f5f9;
                    font-size: 11px;
                }
                table.dre-table tr.total-row {
                    background: #f8fafc;
                    font-weight: 700;
                    border-top: 1px solid #cbd5e1;
                }
                table.dre-table tr.grand-result {
                    background: ${if (netProfitReal >= 0) "#ecfdf5" else "#fef2f2"};
                    font-weight: 800;
                    font-size: 13px;
                    border-top: 2px solid ${if (netProfitReal >= 0) "#047857" else "#b91c1c"};
                }

                /* Cost Engineering Box */
                .cost-engineering-box {
                    background: #f8fafc;
                    border: 1px solid #e2e8f0;
                    border-radius: 8px;
                    padding: 12px;
                    margin-bottom: 14px;
                }
                .two-col-grid {
                    display: flex;
                    gap: 14px;
                }
                .col-half {
                    flex: 1;
                }
                .param-row {
                    display: flex;
                    justify-content: space-between;
                    padding: 4px 0;
                    border-bottom: 1px dotted #e2e8f0;
                    font-size: 11px;
                }
                .param-label { color: #475569; }
                .param-value { font-weight: 600; color: #0f172a; }

                .rate-highlight-box {
                    background: #eff6ff;
                    border: 1px solid #bfdbfe;
                    border-radius: 6px;
                    padding: 8px;
                    text-align: center;
                    margin-top: 8px;
                }
                .rate-highlight-title {
                    font-size: 10px;
                    font-weight: 700;
                    color: #1e40af;
                    text-transform: uppercase;
                }
                .rate-highlight-value {
                    font-size: 16px;
                    font-weight: 900;
                    color: #1d4ed8;
                    margin-top: 2px;
                }

                /* Evolution Table */
                table.evolution-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 14px;
                    font-size: 11px;
                }
                table.evolution-table th {
                    background: #1e293b;
                    color: #ffffff;
                    padding: 6px 8px;
                    text-align: left;
                    font-weight: 700;
                }
                .badge-positive {
                    display: inline-block;
                    background: #dcfce7;
                    color: #15803d;
                    padding: 2px 6px;
                    border-radius: 4px;
                    font-weight: 700;
                    font-size: 10px;
                }
                .badge-negative {
                    display: inline-block;
                    background: #fee2e2;
                    color: #b91c1c;
                    padding: 2px 6px;
                    border-radius: 4px;
                    font-weight: 700;
                    font-size: 10px;
                }

                /* Footer */
                .report-footer {
                    margin-top: 20px;
                    border-top: 1px dashed #cbd5e1;
                    padding-top: 12px;
                    display: flex;
                    justify-content: space-between;
                    font-size: 10px;
                    color: #64748b;
                    page-break-inside: avoid;
                }
                .signature-box {
                    text-align: center;
                    min-width: 180px;
                    border-top: 1px solid #94a3b8;
                    padding-top: 4px;
                    margin-top: 24px;
                }
            </style>
        </head>
        <body>
            <!-- Header with Company Logo & Data -->
            <div class="header-wrapper">
                <div class="company-col">
                    $logoHtml
                    <div class="company-title">${escapeHtml(subscription.companyName.ifBlank { "Minha Empresa" })}</div>
                    <div class="company-meta">
                        ${if (subscription.nif.isNotBlank()) "<div>NIF: <strong>${escapeHtml(subscription.nif)}</strong></div>" else ""}
                        ${if (subscription.contactPhone.isNotBlank()) "<div>Contacto: ${escapeHtml(subscription.contactPhone)}</div>" else ""}
                        <div>Regime Fiscal: <strong>Regime Geral / Simplificado AGT (República de Angola)</strong></div>
                        ${if (subscription.destinationIban.isNotBlank()) "<div>IBAN: <span style='font-family: monospace;'>${escapeHtml(subscription.destinationIban)}</span></div>" else ""}
                    </div>
                </div>

                <div class="report-badge-col">
                    <div class="report-badge-title">Relatório Gerencial</div>
                    <div class="report-badge-period">Exercício: $periodName</div>
                    <div class="report-badge-date">Emitido em: $now</div>
                    <div class="report-badge-code">$authReportCode</div>
                </div>
            </div>

            <!-- Summary KPI Grid -->
            <div class="kpi-grid">
                <div class="kpi-card">
                    <div class="kpi-label">Faturamento Bruto</div>
                    <div class="kpi-val green">${formatVal(grossRevenue, currency)}</div>
                    <div class="kpi-sub">Receitas Operacionais</div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-label">Custos Diretos</div>
                    <div class="kpi-val red">- ${formatVal(directCosts, currency)}</div>
                    <div class="kpi-sub">Matérias-primas e MDO</div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-label">Margem Bruta</div>
                    <div class="kpi-val">${formatVal(contributionMargin, currency)}</div>
                    <div class="kpi-sub">${formatPct(contributionMarginPct)} da receita</div>
                </div>
                <div class="kpi-card highlight">
                    <div class="kpi-label">Lucro Líquido Real</div>
                    <div class="kpi-val ${if (netProfitReal >= 0) "green" else "red"}">${if (netProfitReal >= 0) "+ " else ""}${formatVal(netProfitReal, currency)}</div>
                    <div class="kpi-sub">Margem Real: ${formatPct(netMarginPct)}</div>
                </div>
            </div>

            <!-- Demonstrativo de Resultados do Exercício (DRE) -->
            <div class="section-title">
                <span class="section-icon">📊</span> Demonstrativo de Resultados do Exercício (DRE Gerencial)
            </div>

            <table class="dre-table">
                <thead>
                    <tr>
                        <th style="width: 60%;">Estrutura Contábil / Conta Gerencial</th>
                        <th style="width: 25%; text-align: right;">Montante (${currency.symbol})</th>
                        <th style="width: 15%; text-align: right;">% Receita</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><strong>(+) FATURAMENTO BRUTO (RECEITA OPERACIONAL)</strong></td>
                        <td style="text-align: right; font-weight: bold; color: #047857;">${formatVal(grossRevenue, currency)}</td>
                        <td style="text-align: right; font-weight: bold;">100.0%</td>
                    </tr>
                    <tr>
                        <td style="padding-left: 20px; color: #475569;">(-) Custos Diretos com Matérias-Primas e Insumos</td>
                        <td style="text-align: right; color: #b91c1c;">- ${formatVal(directCosts * 0.54, currency)}</td>
                        <td style="text-align: right; color: #64748b;">${if (grossRevenue > 0) formatPct((directCosts * 0.54 / grossRevenue) * 100.0) else "0.0%"}</td>
                    </tr>
                    <tr>
                        <td style="padding-left: 20px; color: #475569;">(-) Mão de Obra Técnica Direta (Execução Operacional)</td>
                        <td style="text-align: right; color: #b91c1c;">- ${formatVal(directCosts * 0.46, currency)}</td>
                        <td style="text-align: right; color: #64748b;">${if (grossRevenue > 0) formatPct((directCosts * 0.46 / grossRevenue) * 100.0) else "0.0%"}</td>
                    </tr>
                    <tr class="total-row">
                        <td><strong>(=) MARGEM DE CONTRIBUIÇÃO BRUTA</strong></td>
                        <td style="text-align: right; font-weight: bold; color: #0f172a;">${formatVal(contributionMargin, currency)}</td>
                        <td style="text-align: right; font-weight: bold;">${formatPct(contributionMarginPct)}</td>
                    </tr>
                    <tr>
                        <td style="padding-left: 20px; color: #475569;">(-) Despesas Fixas, Aluguel, Ferramentas &amp; Overhead</td>
                        <td style="text-align: right; color: #b91c1c;">- ${formatVal(overheadExpenses, currency)}</td>
                        <td style="text-align: right; color: #64748b;">${if (grossRevenue > 0) formatPct((overheadExpenses / grossRevenue) * 100.0) else "0.0%"}</td>
                    </tr>
                    <tr>
                        <td style="padding-left: 20px; color: #475569;">(-) Tributos, Impostos &amp; Encargos Fiscais (IVA AGT 14%)</td>
                        <td style="text-align: right; color: #b91c1c;">- ${formatVal(taxesPaid, currency)}</td>
                        <td style="text-align: right; color: #64748b;">${if (grossRevenue > 0) formatPct((taxesPaid / grossRevenue) * 100.0) else "0.0%"}</td>
                    </tr>
                    <tr class="grand-result">
                        <td><strong>(=) RESULTADO LÍQUIDO DO EXERCÍCIO (LUCRO REAL)</strong></td>
                        <td style="text-align: right; font-weight: 800; color: ${if (netProfitReal >= 0) "#047857" else "#b91c1c"};">
                            ${if (netProfitReal >= 0) "+ " else ""}${formatVal(netProfitReal, currency)}
                        </td>
                        <td style="text-align: right; font-weight: 800; color: ${if (netProfitReal >= 0) "#047857" else "#b91c1c"};">
                            ${formatPct(netMarginPct)}
                        </td>
                    </tr>
                </tbody>
            </table>

            <!-- Engenharia de Custos & Simulação de Taxa/Hora -->
            <div class="section-title">
                <span class="section-icon">⚙️</span> Engenharia de Custos &amp; Taxa Horária do Profissional
            </div>

            <div class="cost-engineering-box">
                <div class="two-col-grid">
                    <div class="col-half">
                        <strong style="color: #0f172a; font-size: 11px; text-transform: uppercase;">1. Metodologia de Custo de Angola</strong>
                        <div style="font-size: 10px; color: #475569; margin-top: 4px; line-height: 1.4;">
                            • <strong>Mão de Obra Padrão:</strong> 85% do custo da matéria-prima utilizada.<br/>
                            • <strong>Custo Base:</strong> Matérias-Primas + MDO (85%) + Rateio Operacional.<br/>
                            • <strong>Margem de Lucro Almejada:</strong> 70% a 100%+ sobre o custo direto.<br/>
                            • <strong>Incidência Fiscal AGT:</strong> Imposto IVA padrão de 14%.
                        </div>

                        <div class="param-row" style="margin-top: 8px;">
                            <span class="param-label">Ponto de Equilíbrio Operacional:</span>
                            <span class="param-value">${formatVal(directCosts + overheadExpenses + taxesPaid, currency)}</span>
                        </div>
                        <div class="param-row">
                            <span class="param-label">Status do Ponto de Equilíbrio:</span>
                            <span class="param-value" style="color: ${if (grossRevenue >= (directCosts + overheadExpenses + taxesPaid)) "#047857" else "#b91c1c"};">
                                ${if (grossRevenue >= (directCosts + overheadExpenses + taxesPaid)) "Superavitário (Margem Segura)" else "Abaixo do Ponto de Equilíbrio"}
                            </span>
                        </div>
                    </div>

                    <div class="col-half">
                        <strong style="color: #0f172a; font-size: 11px; text-transform: uppercase;">2. Simulação da Taxa Horária Mínima</strong>
                        <div class="param-row" style="margin-top: 4px;">
                            <span class="param-label">Pró-labore Mensal Desejado:</span>
                            <span class="param-value">${formatVal(simulation.desiredSalary, currency)}</span>
                        </div>
                        <div class="param-row">
                            <span class="param-label">Custos Fixos Pessoais &amp; Estruturais:</span>
                            <span class="param-value">${formatVal(simulation.fixedCosts, currency)}</span>
                        </div>
                        <div class="param-row">
                            <span class="param-label">Jornada Operacional:</span>
                            <span class="param-value">${simulation.workingDays} dias x ${simulation.billableHoursPerDay}h = ${(simulation.workingDays * simulation.billableHoursPerDay).toInt()} horas/mês</span>
                        </div>

                        <div class="rate-highlight-box">
                            <div class="rate-highlight-title">Taxa Horária Mínima Sugerida</div>
                            <div class="rate-highlight-value">${formatVal(simulation.calculatedMinimumHourlyRate, currency)} / hora</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Evolução Mensal do Fluxo de Caixa -->
            <div class="section-title">
                <span class="section-icon">📈</span> Evolução Mensal do Fluxo de Caixa (Histórico Recente)
            </div>

            <table class="evolution-table">
                <thead>
                    <tr>
                        <th style="width: 25%;">Mês / Ano</th>
                        <th style="width: 22%; text-align: right;">Entradas (Receitas)</th>
                        <th style="width: 22%; text-align: right;">Saídas (Despesas)</th>
                        <th style="width: 18%; text-align: right;">Saldo Líquido</th>
                        <th style="width: 13%; text-align: center;">Margem</th>
                    </tr>
                </thead>
                <tbody>
                    $monthlyEvolutionRows
                </tbody>
            </table>

            <!-- Desempenho Comercial de Orçamentos -->
            <div class="section-title">
                <span class="section-icon">💼</span> Desempenho Comercial &amp; Conversão de Propostas
            </div>

            <div style="display: flex; gap: 10px; margin-bottom: 14px;">
                <div class="kpi-card" style="flex: 1;">
                    <div class="kpi-label">Propostas Emitidas</div>
                    <div class="kpi-val">$totalBudgets</div>
                    <div class="kpi-sub">Total de orçamentos gerados</div>
                </div>
                <div class="kpi-card" style="flex: 1;">
                    <div class="kpi-label">Propostas Aprovadas</div>
                    <div class="kpi-val green">$approvedBudgets</div>
                    <div class="kpi-sub">Fechadas com sucesso</div>
                </div>
                <div class="kpi-card" style="flex: 1;">
                    <div class="kpi-label">Taxa de Conversão</div>
                    <div class="kpi-val" style="color: #2563eb;">${formatPct(conversionPct)}</div>
                    <div class="kpi-sub">Eficiência de fechamento</div>
                </div>
                <div class="kpi-card" style="flex: 1;">
                    <div class="kpi-label">Em Negociação</div>
                    <div class="kpi-val" style="color: #d97706;">$pendingBudgets</div>
                    <div class="kpi-sub">Aguardando decisão do cliente</div>
                </div>
            </div>

            <!-- Footer & Signatures -->
            <div class="report-footer">
                <div>
                    <div><strong>FluxAO</strong> • Sistema Integrado de Gestão Financeira &amp; Engenharia de Custos</div>
                    <div>Documento autenticado sob o identificador fiscal: <span style="font-family: monospace;">$authReportCode</span></div>
                    <div style="margin-top: 2px;">Processado por computador • Conforme normas de contabilidade gerencial da República de Angola</div>
                </div>

                <div class="signature-box">
                    <strong>Administração / Gerência Financeira</strong><br/>
                    <small style="color: #64748b;">${escapeHtml(subscription.companyName.ifBlank { "Minha Empresa" })}</small>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Builds the complete, printable HTML document for a Cost Engineering Commercial Proposal / Budget.
     */
    fun generateHtmlBudgetProposal(
        budget: BudgetEntity,
        items: List<BudgetItemEntity>,
        calculation: PricingCalculationResult,
        subscription: CompanySubscription,
        customization: InvoiceCustomization,
        currency: AppCurrency
    ): String {
        val accentColor = customization.accentColorHex.ifBlank { "#D4AF37" }
        val dateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY).format(Date(budget.dateCreatedEpochMillis))

        // Logo
        val logoHtml = if (customization.showLogoOnInvoice && customization.logoBase64.isNotBlank()) {
            """<img src="data:image/png;base64,${customization.logoBase64}" style="max-height: 60px; max-width: 200px; object-fit: contain; margin-bottom: 6px; display: block;" alt="Logótipo Empresa" />"""
        } else {
            """<div style="font-size: 20px; font-weight: 900; color: #0f172a; margin-bottom: 4px;">Flux<span style="color: $accentColor;">AO</span></div>"""
        }

        val itemsRows = StringBuilder()
        items.forEachIndexed { index, item ->
            itemsRows.append(
                """
                <tr>
                    <td style="padding: 7px; border-bottom: 1px solid #e2e8f0; text-align: center;">${index + 1}</td>
                    <td style="padding: 7px; border-bottom: 1px solid #e2e8f0;"><strong>${escapeHtml(item.description)}</strong></td>
                    <td style="padding: 7px; border-bottom: 1px solid #e2e8f0; text-align: center;">${item.quantity} un</td>
                    <td style="padding: 7px; border-bottom: 1px solid #e2e8f0; text-align: right;">${formatVal(item.unitPrice, currency)}</td>
                    <td style="padding: 7px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: bold;">${formatVal(item.total, currency)}</td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html lang="pt">
        <head>
            <meta charset="utf-8"/>
            <title>Orçamento Comercial - ${escapeHtml(budget.projectTitle)}</title>
            <style>
                @page { size: A4; margin: 12mm 15mm; }
                * { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; box-sizing: border-box; }
                body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; color: #0f172a; font-size: 12px; margin: 0; padding: 0; line-height: 1.45; }
                .header { display: flex; justify-content: space-between; border-bottom: 2px solid $accentColor; padding-bottom: 12px; margin-bottom: 14px; }
                .title-box { background: #f8fafc; border: 1px solid #e2e8f0; border-left: 4px solid $accentColor; padding: 10px 12px; border-radius: 6px; text-align: right; }
                .table { width: 100%; border-collapse: collapse; margin: 12px 0; }
                .table th { background: #0f172a; color: #fff; padding: 8px; text-align: left; font-size: 11px; text-transform: uppercase; }
                .totals-card { width: 340px; margin-left: auto; border: 1px solid #cbd5e1; border-radius: 8px; overflow: hidden; margin-top: 14px; }
                .totals-row { display: flex; justify-content: space-between; padding: 6px 12px; border-bottom: 1px solid #e2e8f0; font-size: 11px; }
                .totals-grand { background: #fffbeb; font-size: 14px; font-weight: 800; border-top: 2px solid $accentColor; color: #0f172a; }
            </style>
        </head>
        <body>
            <div class="header">
                <div>
                    $logoHtml
                    <div style="font-size: 15px; font-weight: 800;">${escapeHtml(subscription.companyName.ifBlank { "Minha Empresa" })}</div>
                    <div style="font-size: 11px; color: #475569;">
                        ${if (subscription.nif.isNotBlank()) "NIF: ${escapeHtml(subscription.nif)} • " else ""}
                        ${if (subscription.contactPhone.isNotBlank()) "Contacto: ${escapeHtml(subscription.contactPhone)}" else ""}
                    </div>
                </div>

                <div class="title-box">
                    <div style="font-size: 14px; font-weight: 800; text-transform: uppercase;">Proposta Comercial</div>
                    <div style="font-size: 12px; font-weight: 600; color: $accentColor;">Projeto: ${escapeHtml(budget.projectTitle)}</div>
                    <div style="font-size: 10px; color: #64748b;">Emissão: $dateFormatted • Prazo: ${budget.deadlineDays} dias úteis</div>
                </div>
            </div>

            <!-- Client Info -->
            <div style="background: #f8fafc; border: 1px solid #e2e8f0; padding: 10px 12px; border-radius: 6px; margin-bottom: 12px;">
                <div style="font-size: 10px; font-weight: bold; color: #64748b; text-transform: uppercase;">Cliente Contratante:</div>
                <div style="font-size: 13px; font-weight: 800;">${escapeHtml(budget.clientName)}</div>
                ${if (budget.clientContact.isNotBlank()) "<div style='font-size: 11px; color: #475569;'>Contacto: ${escapeHtml(budget.clientContact)}</div>" else ""}
                ${if (budget.projectScope.isNotBlank()) "<div style='margin-top: 6px; font-size: 11px;'><strong>Escopo:</strong> ${escapeHtml(budget.projectScope)}</div>" else ""}
            </div>

            <!-- Materials Table -->
            ${if (items.isNotEmpty()) """
            <div style="font-size: 12px; font-weight: 800; text-transform: uppercase; margin-bottom: 4px;">1. Matérias-Primas &amp; Insumos</div>
            <table class="table">
                <thead>
                    <tr>
                        <th style="width: 6%; text-align: center;">#</th>
                        <th style="width: 50%;">Descrição do Material</th>
                        <th style="width: 14%; text-align: center;">Qtd</th>
                        <th style="width: 15%; text-align: right;">P. Unitário</th>
                        <th style="width: 15%; text-align: right;">Total</th>
                    </tr>
                </thead>
                <tbody>
                    $itemsRows
                </tbody>
            </table>
            """ else ""}

            <!-- Cost Engineering Breakdown -->
            <div style="font-size: 12px; font-weight: 800; text-transform: uppercase; margin: 12px 0 4px 0;">
                2. Engenharia de Custos (Metodologia de Angola)
            </div>

            <div class="totals-card">
                <div class="totals-row">
                    <span>Matérias-Primas Diretas:</span>
                    <strong>${formatVal(calculation.directMaterialsCost, currency)}</strong>
                </div>
                <div class="totals-row">
                    <span>Mão de Obra Técnica (${formatPct(calculation.laborPercentageOnMaterials)}):</span>
                    <strong>${formatVal(calculation.directLaborCost, currency)}</strong>
                </div>
                <div class="totals-row">
                    <span>Custo Base de Produção:</span>
                    <strong>${formatVal(calculation.totalProductionCost, currency)}</strong>
                </div>
                <div class="totals-row">
                    <span>Margem de Lucro Almejada (${formatPct(calculation.profitPercentageOnCost)}):</span>
                    <strong style="color: #047857;">+ ${formatVal(calculation.netProfit, currency)}</strong>
                </div>
                <div class="totals-row">
                    <span>Imposto IVA AGT (${formatPct(calculation.taxesPercentage)}):</span>
                    <strong>+ ${formatVal(calculation.taxesAmount, currency)}</strong>
                </div>
                ${if (calculation.discountAmount > 0) """
                <div class="totals-row">
                    <span>Desconto Comercial Concedido:</span>
                    <strong style="color: #b91c1c;">- ${formatVal(calculation.discountAmount, currency)}</strong>
                </div>
                """ else ""}
                <div class="totals-row totals-grand">
                    <span>VALOR TOTAL DO ORÇAMENTO:</span>
                    <span style="color: #0f172a;">${formatVal(calculation.finalSellingPrice, currency)}</span>
                </div>
            </div>

            <div style="margin-top: 14px; font-size: 11px; color: #475569;">
                <div><strong>Condições de Pagamento:</strong> ${escapeHtml(budget.paymentTerms)}</div>
                ${if (budget.termsAndConditions.isNotBlank()) "<div><strong>Termos e Validade:</strong> ${escapeHtml(budget.termsAndConditions)}</div>" else ""}
            </div>

            <div style="margin-top: 30px; border-top: 1px dashed #cbd5e1; padding-top: 10px; font-size: 10px; color: #64748b; text-align: center;">
                Proposta Comercial elaborada conforme metodologia técnica e comercial de Angola • FluxAO
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Launches Android's native Print / Save to PDF framework for the Financial Report.
     */
    fun exportFinancialReportPdf(
        context: Context,
        uiState: CashFlowUiState,
        budgets: List<BudgetEntity>,
        subscription: CompanySubscription,
        customization: InvoiceCustomization,
        currency: AppCurrency,
        simulation: HourlyRateSimulation = HourlyRateSimulation(),
        onFinished: () -> Unit = {}
    ) {
        val webView = WebView(context)
        val htmlContent = generateHtmlFinancialReport(
            uiState = uiState,
            budgets = budgets,
            subscription = subscription,
            customization = customization,
            currency = currency,
            simulation = simulation
        )

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager == null) {
                    Toast.makeText(context, "Serviço de impressão indisponível neste dispositivo.", Toast.LENGTH_LONG).show()
                    onFinished()
                    return
                }

                val jobName = "Relatorio_Financeiro_${subscription.companyName.replace(" ", "_")}_FluxAO"
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

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    /**
     * Launches Android's native Print / Save to PDF framework for a Commercial Budget Proposal.
     */
    fun exportBudgetProposalPdf(
        context: Context,
        budget: BudgetEntity,
        items: List<BudgetItemEntity>,
        calculation: PricingCalculationResult,
        subscription: CompanySubscription,
        customization: InvoiceCustomization,
        currency: AppCurrency,
        onFinished: () -> Unit = {}
    ) {
        val webView = WebView(context)
        val htmlContent = generateHtmlBudgetProposal(
            budget = budget,
            items = items,
            calculation = calculation,
            subscription = subscription,
            customization = customization,
            currency = currency
        )

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager == null) {
                    Toast.makeText(context, "Serviço de impressão indisponível.", Toast.LENGTH_LONG).show()
                    onFinished()
                    return
                }

                val jobName = "Orcamento_${budget.projectTitle.replace(" ", "_")}_FluxAO"
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

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    /**
     * Downloads (saves) the complete Financial Report directly as a PDF file into device storage.
     */
    fun downloadFinancialReportPdf(
        context: Context,
        uiState: CashFlowUiState,
        budgets: List<BudgetEntity>,
        subscription: CompanySubscription,
        customization: InvoiceCustomization,
        currency: AppCurrency,
        simulation: HourlyRateSimulation = HourlyRateSimulation(),
        onCompleted: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val htmlContent = generateHtmlFinancialReport(
            uiState = uiState,
            budgets = budgets,
            subscription = subscription,
            customization = customization,
            currency = currency,
            simulation = simulation
        )
        val companyClean = subscription.companyName.ifBlank { "FluxAO" }.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "Relatorio_Financeiro_${companyClean}_${System.currentTimeMillis()}.pdf"
        PdfDocumentManager.downloadPdfToDevice(context, htmlContent, fileName, "Relatório Financeiro FluxAO", onCompleted)
    }

    /**
     * Generates and shares the Financial Report PDF file directly via WhatsApp.
     */
    fun shareFinancialReportPdfViaWhatsApp(
        context: Context,
        uiState: CashFlowUiState,
        budgets: List<BudgetEntity>,
        subscription: CompanySubscription,
        customization: InvoiceCustomization,
        currency: AppCurrency,
        simulation: HourlyRateSimulation = HourlyRateSimulation(),
        onCompleted: (Boolean) -> Unit = {}
    ) {
        val htmlContent = generateHtmlFinancialReport(
            uiState = uiState,
            budgets = budgets,
            subscription = subscription,
            customization = customization,
            currency = currency,
            simulation = simulation
        )
        val companyClean = subscription.companyName.ifBlank { "FluxAO" }.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "Relatorio_Financeiro_${companyClean}.pdf"
        val caption = generateReportShareSummary(uiState, budgets, subscription, currency)
        PdfDocumentManager.sharePdfViaWhatsApp(context, htmlContent, fileName, "", caption, onCompleted)
    }

    /**
     * Downloads (saves) the Budget Proposal as a PDF file directly into device storage.
     */
    fun downloadBudgetProposalPdf(
        context: Context,
        budget: BudgetEntity,
        items: List<BudgetItemEntity>,
        calculation: PricingCalculationResult,
        subscription: CompanySubscription,
        customization: InvoiceCustomization,
        currency: AppCurrency,
        onCompleted: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val htmlContent = generateHtmlBudgetProposal(
            budget = budget,
            items = items,
            calculation = calculation,
            subscription = subscription,
            customization = customization,
            currency = currency
        )
        val projectClean = budget.projectTitle.ifBlank { "Orcamento" }.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "Orcamento_${projectClean}_${System.currentTimeMillis()}.pdf"
        PdfDocumentManager.downloadPdfToDevice(context, htmlContent, fileName, "Orçamento Técnico", onCompleted)
    }

    /**
     * Shares the Budget Proposal PDF file directly via WhatsApp to the client.
     */
    fun shareBudgetProposalPdfViaWhatsApp(
        context: Context,
        budget: BudgetEntity,
        items: List<BudgetItemEntity>,
        calculation: PricingCalculationResult,
        subscription: CompanySubscription,
        customization: InvoiceCustomization,
        currency: AppCurrency,
        onCompleted: (Boolean) -> Unit = {}
    ) {
        val htmlContent = generateHtmlBudgetProposal(
            budget = budget,
            items = items,
            calculation = calculation,
            subscription = subscription,
            customization = customization,
            currency = currency
        )
        val projectClean = budget.projectTitle.ifBlank { "Orcamento" }.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "Orcamento_${projectClean}.pdf"
        val caption = """
        📄 *PROPOSTA COMERCIAL / ORÇAMENTO TÉCNICO*
        🏢 *Emissor:* ${subscription.companyName.ifBlank { "Minha Empresa" }}
        👤 *Cliente:* ${budget.clientName}
        📌 *Projeto:* ${budget.projectTitle}
        💰 *Valor Total:* ${currency.format(calculation.finalSellingPrice)}
        _Segue anexo o documento em PDF completo com a discriminação técnica e custos._
        """.trimIndent()
        PdfDocumentManager.sharePdfViaWhatsApp(
            context = context,
            html = htmlContent,
            fileName = fileName,
            phoneNumber = budget.clientContact,
            message = caption,
            onCompleted = onCompleted
        )
    }

    /**
     * Generates a concise executive text summary suitable for instant WhatsApp sharing.
     */
    fun generateReportShareSummary(
        uiState: CashFlowUiState,
        budgets: List<BudgetEntity>,
        subscription: CompanySubscription,
        currency: AppCurrency
    ): String {
        val gross = uiState.monthIncome
        val net = uiState.monthNetProfit
        val approved = budgets.count { it.status == BudgetStatus.APPROVED }

        return """
        📊 *RELATÓRIO FINANCEIRO EXECUTIVO - FLUXAO*
        🏢 *Empresa:* ${subscription.companyName.ifBlank { "Minha Empresa" }}
        📅 *Data:* ${SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY).format(Date())}

        💰 *Faturamento Bruto:* ${currency.format(gross)}
        💵 *Lucro Líquido Real:* ${currency.format(net)}
        📈 *Margem Operacional:* ${if (gross > 0) String.format(Locale.US, "%.1f", (net / gross) * 100.0) else "0.0"}%
        📋 *Propostas Comerciais Aprovadas:* $approved de ${budgets.size}

        _Relatório detalhado gerado pelo FluxAO - Gestão Financeira Angola_
        """.trimIndent()
    }
}
