package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BudgetEntity
import com.example.data.model.AppCurrency
import com.example.data.model.BudgetStatus
import com.example.data.model.CompanySubscription
import com.example.data.model.InvoiceCustomization
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.ui.components.CashFlowEvolutionChart
import com.example.ui.viewmodel.CashFlowUiState
import com.example.util.FinancialReportPdfExporter
import com.example.util.HourlyRateSimulation

@Composable
fun ReportsScreen(
    uiState: CashFlowUiState,
    budgets: List<BudgetEntity>,
    currentCurrency: AppCurrency,
    subscription: CompanySubscription,
    customization: InvoiceCustomization = InvoiceCustomization(),
    onOpenCurrencySelector: () -> Unit,
    onOpenCompanyPanel: () -> Unit,
    onExitClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // PDF Export modal state
    var showExportDialog by remember { mutableStateOf(false) }
    var isExportingPdf by remember { mutableStateOf(false) }

    // Interactive Labor Hourly Rate Simulator state
    var desiredSalaryText by remember { mutableStateOf("6000.00") }
    var fixedPersonalCostText by remember { mutableStateOf("1500.00") }
    var workingDaysText by remember { mutableStateOf("21") }
    var billableHoursPerDayText by remember { mutableStateOf("6.0") }

    val desiredSalary = desiredSalaryText.toDoubleOrNull() ?: 0.0
    val fixedCosts = fixedPersonalCostText.toDoubleOrNull() ?: 0.0
    val workingDays = workingDaysText.toIntOrNull() ?: 21
    val billableHoursPerDay = billableHoursPerDayText.toDoubleOrNull() ?: 6.0
    val totalBillableHoursMonth = (workingDays * billableHoursPerDay).coerceAtLeast(1.0)
    val totalMonthlyNeed = desiredSalary + fixedCosts
    val calculatedMinimumHourlyRate = totalMonthlyNeed / totalBillableHoursMonth

    // DRE calculations for current month
    val monthGrossRevenue = uiState.monthIncome
    val directMaterialAndLaborExpenses = uiState.transactions
        .filter {
            (it.category.contains("Matéria-prima", ignoreCase = true) || it.category.contains("Mão de Obra", ignoreCase = true))
        }
        .sumOf { it.amount }
    val contributionMarginBruta = monthGrossRevenue - directMaterialAndLaborExpenses
    val overheadFixedExpenses = uiState.transactions
        .filter {
            (it.category.contains("Aluguel", ignoreCase = true) || it.category.contains("Ferramentas", ignoreCase = true) || it.category.contains("Marketing", ignoreCase = true))
        }
        .sumOf { it.amount }
    val taxesExpense = uiState.transactions
        .filter { it.category.contains("Impostos", ignoreCase = true) }
        .sumOf { it.amount }
    val netResultReal = uiState.monthNetProfit

    // Budget conversion metrics
    val totalBudgets = budgets.size
    val approvedBudgets = budgets.count { it.status == BudgetStatus.APPROVED }
    val conversionRate = if (totalBudgets > 0) (approvedBudgets.toDouble() / totalBudgets) * 100.0 else 0.0

    fun triggerPdfExport() {
        isExportingPdf = true
        FinancialReportPdfExporter.exportFinancialReportPdf(
            context = context,
            uiState = uiState,
            budgets = budgets,
            subscription = subscription,
            customization = customization,
            currency = currentCurrency,
            simulation = HourlyRateSimulation(
                desiredSalary = desiredSalary,
                fixedCosts = fixedCosts,
                workingDays = workingDays,
                billableHoursPerDay = billableHoursPerDay,
                calculatedMinimumHourlyRate = calculatedMinimumHourlyRate
            ),
            onFinished = {
                isExportingPdf = false
                showExportDialog = false
            }
        )
    }

    fun downloadReportPdf() {
        isExportingPdf = true
        FinancialReportPdfExporter.downloadFinancialReportPdf(
            context = context,
            uiState = uiState,
            budgets = budgets,
            subscription = subscription,
            customization = customization,
            currency = currentCurrency,
            simulation = HourlyRateSimulation(
                desiredSalary = desiredSalary,
                fixedCosts = fixedCosts,
                workingDays = workingDays,
                billableHoursPerDay = billableHoursPerDay,
                calculatedMinimumHourlyRate = calculatedMinimumHourlyRate
            ),
            onCompleted = { _, _ ->
                isExportingPdf = false
                showExportDialog = false
            }
        )
    }

    fun shareReportPdfWhatsApp() {
        isExportingPdf = true
        FinancialReportPdfExporter.shareFinancialReportPdfViaWhatsApp(
            context = context,
            uiState = uiState,
            budgets = budgets,
            subscription = subscription,
            customization = customization,
            currency = currentCurrency,
            simulation = HourlyRateSimulation(
                desiredSalary = desiredSalary,
                fixedCosts = fixedCosts,
                workingDays = workingDays,
                billableHoursPerDay = billableHoursPerDay,
                calculatedMinimumHourlyRate = calculatedMinimumHourlyRate
            ),
            onCompleted = {
                isExportingPdf = false
                showExportDialog = false
            }
        )
    }

    fun shareSummaryText() {
        val summaryText = FinancialReportPdfExporter.generateReportShareSummary(
            uiState = uiState,
            budgets = budgets,
            subscription = subscription,
            currency = currentCurrency
        )
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, summaryText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Resumo Financeiro"))
        showExportDialog = false
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // PDF Export Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_export_report_pdf"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Exportar Relatório PDF",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "DRE, Custos, Fluxo de Caixa e Logo Oficial",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Button(
                            onClick = { showExportDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_export_reports_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar PDF", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (customization.logoBase64.isNotBlank()) Color(0xFF047857) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (customization.logoBase64.isNotBlank()) "Logótipo da Empresa ativo" else "Logótipo: Emblema Institucional Padrão",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        TextButton(
                            onClick = onOpenCompanyPanel,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Ajustar Empresa", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        // Company & Subscription Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_company_subscription"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text("🏢", fontSize = 20.sp, modifier = Modifier.padding(6.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = subscription.companyName.ifBlank { "Minha Empresa" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (subscription.nif.isNotBlank()) {
                                    Text(
                                        text = "NIF: ${subscription.nif}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (subscription.isValid) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = if (subscription.isValid) "Ativo (${subscription.daysRemaining}d)" else "Expirado",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (subscription.isValid) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Plano Mensal (5.000 Kz / Mês)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Validade: ${subscription.formattedExpirationDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenCompanyPanel,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_manage_company_subscription")
                        ) {
                            Text(if (subscription.isValid) "Gerenciar / Renovar" else "Ativar com MCX")
                        }

                        OutlinedButton(
                            onClick = onExitClick,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_exit_reports")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sair")
                        }
                    }
                    }
                }
            }
        }

        // Currency Selector Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCurrencySelector() }
                    .testTag("card_currency_selector"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentCurrency.flag,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Moeda Padrão: ${currentCurrency.displayName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Símbolo: ${currentCurrency.symbol} • Código: ${currentCurrency.code}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Button(
                        onClick = onOpenCurrencySelector,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.CurrencyExchange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Alterar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Evolução Mensal do Fluxo de Caixa (Recharts Style)
        item {
            CashFlowEvolutionChart(
                data = uiState.monthlyBars,
                currency = currentCurrency,
                title = "Evolução do Fluxo de Caixa Mensal",
                subtitle = "Histórico dinâmico com análise de superávit e margem",
                modifier = Modifier.testTag("reports_cash_flow_evolution_chart")
            )
        }

        // DRE Gerencial Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "DRE Gerencial do Exercício (Mês)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Demonstrativo contábil simplificado para apuração do lucro real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    DreRow(
                        label = "(+) Faturamento Bruto (Receitas)",
                        value = CostEngineeringCalculator.formatCurrency(monthGrossRevenue),
                        color = MaterialTheme.colorScheme.secondary,
                        isBold = true
                    )
                    DreRow(
                        label = "(-) Custos Diretos (Materiais & Serviços)",
                        value = "- ${CostEngineeringCalculator.formatCurrency(directMaterialAndLaborExpenses)}",
                        color = MaterialTheme.colorScheme.error
                    )
                    DreRow(
                        label = "(=) Margem de Contribuição Bruta",
                        value = CostEngineeringCalculator.formatCurrency(contributionMarginBruta),
                        color = MaterialTheme.colorScheme.primary,
                        isBold = true
                    )
                    DreRow(
                        label = "(-) Custos Fixos & Overhead (Estrutura)",
                        value = "- ${CostEngineeringCalculator.formatCurrency(overheadFixedExpenses)}",
                        color = MaterialTheme.colorScheme.error
                    )
                    DreRow(
                        label = "(-) Tributos e Impostos Pagos",
                        value = "- ${CostEngineeringCalculator.formatCurrency(taxesExpense)}",
                        color = MaterialTheme.colorScheme.error
                    )

                    HorizontalDivider()

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (netResultReal >= 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "(=) Lucro Líquido Real:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (netResultReal >= 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = CostEngineeringCalculator.formatCurrency(netResultReal),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netResultReal >= 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { triggerPdfExport() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_dre_pdf"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar DRE e Relatório em PDF", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Labor Hourly Rate Calculator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Calculadora da Taxa/Hora do Profissional",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Descubra qual deve ser o valor mínimo da sua hora técnica para sustentar sua meta financeira mensal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = desiredSalaryText,
                            onValueChange = { desiredSalaryText = it.replace(",", ".") },
                            label = { Text("Pró-labore (${CostEngineeringCalculator.currentCurrency.symbol})") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = fixedPersonalCostText,
                            onValueChange = { fixedPersonalCostText = it.replace(",", ".") },
                            label = { Text("Custos Fixos (${CostEngineeringCalculator.currentCurrency.symbol})") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = workingDaysText,
                            onValueChange = { workingDaysText = it },
                            label = { Text("Dias Úteis / Mês") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = billableHoursPerDayText,
                            onValueChange = { billableHoursPerDayText = it.replace(",", ".") },
                            label = { Text("Horas Produtivas / Dia") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TAXA HORÁRIA MÍNIMA SUGERIDA",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${CostEngineeringCalculator.formatCurrency(calculatedMinimumHourlyRate)} / hora",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Baseado em $totalBillableHoursMonth horas produtivas no mês.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Budget Conversion & Performance Metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Desempenho Comercial de Orçamentos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total de Propostas Criadas:", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalBudgets propostas", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Propostas Aprovadas:", style = MaterialTheme.typography.bodyMedium)
                        Text("$approvedBudgets propostas", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Taxa de Conversão:", style = MaterialTheme.typography.bodyMedium)
                        Text(CostEngineeringCalculator.formatPercent(conversionRate), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isExportingPdf) showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar Relatório PDF (A4)")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isExportingPdf) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("A preparar documento para impressão/PDF...")
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = subscription.companyName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "NIF: ${subscription.nif.ifBlank { "Consumidor Final" }} • ${subscription.contactPhone.ifBlank { "Luanda, Angola" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (customization.logoBase64.isNotBlank()) Color(0xFF047857) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (customization.logoBase64.isNotBlank()) "Logótipo oficial configurado" else "Emblema oficial FluxAO",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Escolha como deseja exportar o documento A4:",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Action 1: Enviar pelo WhatsApp PDF
                        Button(
                            onClick = { shareReportPdfWhatsApp() },
                            enabled = !isExportingPdf,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_export_pdf_whatsapp")
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar PDF pelo WhatsApp", fontWeight = FontWeight.Bold)
                        }

                        // Action 2: Baixar PDF para o Dispositivo
                        Button(
                            onClick = { downloadReportPdf() },
                            enabled = !isExportingPdf,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_download_pdf_device")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Baixar PDF (Salvar no Celular)", fontWeight = FontWeight.Bold)
                        }

                        // Action 3: Imprimir / Visualizar
                        OutlinedButton(
                            onClick = { triggerPdfExport() },
                            enabled = !isExportingPdf,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_confirm_export_pdf")
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Imprimir / Salvar como PDF (A4)")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showExportDialog = false },
                    enabled = !isExportingPdf
                ) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
fun DreRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}
