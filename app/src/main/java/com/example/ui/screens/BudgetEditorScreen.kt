package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.model.AppCurrency
import com.example.data.model.BudgetStatus
import com.example.data.model.CompanySubscription
import com.example.data.model.InvoiceCustomization
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.domain.model.PricingCalculationResult
import com.example.ui.components.ViabilityBadge
import com.example.ui.theme.Emerald500
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.theme.FluxGoldVibrant
import com.example.ui.viewmodel.BudgetEditorState
import com.example.util.FinancialReportPdfExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditorScreen(
    state: BudgetEditorState,
    pricing: PricingCalculationResult,
    clients: List<ClientEntity> = emptyList(),
    subscription: CompanySubscription = CompanySubscription(),
    customization: InvoiceCustomization = InvoiceCustomization(),
    currency: AppCurrency = AppCurrency.AOA,
    onUpdateState: (BudgetEditorState.() -> BudgetEditorState) -> Unit,
    onAddMaterial: (String, Double, Double) -> Unit,
    onRemoveMaterial: (Int) -> Unit,
    onSaveBudget: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("1. Dados & Escopo", "2. Matéria-Prima & MDO", "3. Lucro & Imposto (IVA)", "4. Proposta Angolana")

    var showClientPicker by remember { mutableStateOf(false) }

    fun createBudgetEntity(): BudgetEntity {
        return BudgetEntity(
            id = state.budgetId,
            clientName = state.clientName.ifBlank { "Cliente Estimado" },
            clientContact = state.clientContact,
            projectTitle = state.projectTitle.ifBlank { "Proposta de Serviço / Obra" },
            projectScope = state.projectScope,
            dateCreatedEpochMillis = System.currentTimeMillis(),
            deadlineDays = state.deadlineDays,
            status = state.status,
            directMaterialsTotal = state.materialsList.sumOf { it.total },
            laborPercentageOnMaterials = state.laborPercentageOnMaterials,
            laborHours = state.laborHours,
            laborHourlyRate = state.laborHourlyRate,
            overheadPercentage = state.overheadPercentage,
            taxesAndSalesFeePercent = state.taxesAndSalesFeePercent,
            desiredProfitMarginPercent = state.desiredProfitMarginPercent,
            discountAmount = state.discountAmount,
            paymentTerms = state.paymentTerms,
            termsAndConditions = state.termsAndConditions
        )
    }

    fun triggerBudgetPdfExport() {
        val budgetEntity = createBudgetEntity()
        FinancialReportPdfExporter.exportBudgetProposalPdf(
            context = context,
            budget = budgetEntity,
            items = state.materialsList,
            calculation = pricing,
            subscription = subscription,
            customization = customization,
            currency = currency
        )
    }

    fun downloadBudgetPdf() {
        val budgetEntity = createBudgetEntity()
        FinancialReportPdfExporter.downloadBudgetProposalPdf(
            context = context,
            budget = budgetEntity,
            items = state.materialsList,
            calculation = pricing,
            subscription = subscription,
            customization = customization,
            currency = currency
        )
    }

    fun shareBudgetPdfWhatsApp() {
        val budgetEntity = createBudgetEntity()
        FinancialReportPdfExporter.shareBudgetProposalPdfViaWhatsApp(
            context = context,
            budget = budgetEntity,
            items = state.materialsList,
            calculation = pricing,
            subscription = subscription,
            customization = customization,
            currency = currency
        )
    }

    // Material item temporary state
    var newMaterialName by remember { mutableStateOf("") }
    var newMaterialQty by remember { mutableStateOf("1") }
    var newMaterialUnitPrice by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.testTag("budget_editor_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.budgetId == 0L) "Novo Orçamento Técnico" else "Editar Orçamento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { triggerBudgetPdfExport() },
                        modifier = Modifier.testTag("btn_export_budget_top_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Exportar Orçamento em PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onSaveBudget,
                        modifier = Modifier.testTag("btn_save_budget_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Salvar Orçamento",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Price Sticky Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Preço Final Orçado (Método AO)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CostEngineeringCalculator.formatCurrency(pricing.finalSellingPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        ViabilityBadge(status = pricing.breakEvenStatus)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Lucro Líquido: ${CostEngineeringCalculator.formatPercent(pricing.netProfitMarginPercent)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (pricing.netProfit >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Tabs Header
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 1: DADOS & ESCOPO
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Identificação do Projeto & Cliente",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        if (clients.isNotEmpty()) {
                                            TextButton(
                                                onClick = { showClientPicker = true },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.testTag("btn_select_client_in_budget")
                                            ) {
                                                Icon(imageVector = Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp), tint = FluxGoldVibrant)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Clientes (${clients.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FluxGoldVibrant)
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = state.projectTitle,
                                        onValueChange = { title -> onUpdateState { copy(projectTitle = title) } },
                                        label = { Text("Título do Projeto / Obra / Serviço *") },
                                        placeholder = { Text("Ex: Fabricação de Gradeamento e Portão") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_project_title"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = state.clientName,
                                        onValueChange = { name -> onUpdateState { copy(clientName = name) } },
                                        label = { Text("Nome do Cliente / Empresa *") },
                                        placeholder = { Text("Ex: Luanda Logística Lda") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_client_name"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = state.clientContact,
                                        onValueChange = { contact -> onUpdateState { copy(clientContact = contact) } },
                                        label = { Text("Contacto / Telefone / WhatsApp") },
                                        placeholder = { Text("+244 923 000 000") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_client_contact"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    // Direct Call & WhatsApp Action Row when phone is filled
                                    if (state.clientContact.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { dialPhoneNumber(context, state.clientContact) },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .testTag("btn_budget_call_client")
                                            ) {
                                                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Ligar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { openWhatsAppChat(context, state.clientContact, state.clientName) },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                                modifier = Modifier
                                                    .weight(1.2f)
                                                    .height(40.dp)
                                                    .testTag("btn_budget_whatsapp_client")
                                            ) {
                                                Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = state.projectScope,
                                        onValueChange = { scope -> onUpdateState { copy(projectScope = scope) } },
                                        label = { Text("Descrição do Escopo e Entregáveis") },
                                        placeholder = { Text("Descreva detalhadamente o que está incluso no orçamento...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = state.deadlineDays.toString(),
                                            onValueChange = { days ->
                                                days.toIntOrNull()?.let { d -> onUpdateState { copy(deadlineDays = d) } }
                                            },
                                            label = { Text("Prazo (dias úteis)") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Status",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                BudgetStatus.values().forEach { st ->
                                                    FilterChip(
                                                        selected = state.status == st,
                                                        onClick = { onUpdateState { copy(status = st) } },
                                                        label = { Text(st.label, fontSize = 10.sp) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 2: MATÉRIA-PRIMA & MÃO DE OBRA (MÉTODO ANGOLANO)
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "1. Matérias-Primas & Insumos",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Base do Cálculo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FluxGoldVibrant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "Cadastre todos os materiais necessários. A mão de obra e os demais custos serão baseados no valor total destes materiais.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Add Item Fields
                                    OutlinedTextField(
                                        value = newMaterialName,
                                        onValueChange = { newMaterialName = it },
                                        label = { Text("Descrição da Matéria-Prima / Insumo") },
                                        placeholder = { Text("Ex: Tubos de Alumínio 50x50, Placas de Pladur") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newMaterialQty,
                                            onValueChange = { newMaterialQty = it },
                                            label = { Text("Qtd") },
                                            modifier = Modifier.weight(0.8f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = newMaterialUnitPrice,
                                            onValueChange = { newMaterialUnitPrice = it.replace(",", ".") },
                                            label = { Text("Preço Unit. (${CostEngineeringCalculator.currentCurrency.symbol})") },
                                            modifier = Modifier.weight(1.2f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Button(
                                            onClick = {
                                                val qty = newMaterialQty.toDoubleOrNull() ?: 1.0
                                                val unitPrice = newMaterialUnitPrice.toDoubleOrNull() ?: 0.0
                                                if (newMaterialName.isNotBlank() && unitPrice > 0) {
                                                    onAddMaterial(newMaterialName, qty, unitPrice)
                                                    newMaterialName = ""
                                                    newMaterialQty = "1"
                                                    newMaterialUnitPrice = ""
                                                }
                                            },
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar")
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    // Materials Items List
                                    if (state.materialsList.isEmpty()) {
                                        Text(
                                            text = "Nenhuma matéria-prima cadastrada ainda. Adicione os itens acima.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        state.materialsList.forEachIndexed { index, item ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = item.description,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "${item.quantity} un x ${CostEngineeringCalculator.formatCurrency(item.unitPrice)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Text(
                                                    text = CostEngineeringCalculator.formatCurrency(item.total),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                IconButton(
                                                    onClick = { onRemoveMaterial(index) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Remover",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Total Matéria-Prima:",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = CostEngineeringCalculator.formatCurrency(pricing.directMaterialsCost),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // MÃO DE OBRA A 85% (REGRA ANGOLANA)
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "2. Mão de Obra Técnica (MDO)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Padrão AO: 85%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Emerald500,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "Em Angola, a prática padrão e recomendada de precificação estabelece a mão de obra em 85% do valor total da matéria-prima utilizada.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Fast buttons for % of labor
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(75.0, 85.0, 100.0).forEach { pct ->
                                            val isSelected = (state.laborPercentageOnMaterials == pct)
                                            OutlinedButton(
                                                onClick = { onUpdateState { copy(laborPercentageOnMaterials = pct) } },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                                            ) {
                                                Text(
                                                    text = "${pct.toInt()}%",
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }

                                    // Slider for fine tuning labor %
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Taxa de Mão de Obra sobre Matéria-Prima:",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${state.laborPercentageOnMaterials.toInt()}%",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Slider(
                                            value = state.laborPercentageOnMaterials.toFloat(),
                                            onValueChange = { v -> onUpdateState { copy(laborPercentageOnMaterials = v.toDouble()) } },
                                            valueRange = 0f..150f,
                                            steps = 30
                                        )
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Emerald500.copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Mão de Obra (${state.laborPercentageOnMaterials.toInt()}%):",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = CostEngineeringCalculator.formatCurrency(pricing.directLaborCost),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald500
                                            )
                                        }
                                    }

                                    // Total Base Cost (Materials + Labor)
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Custo Base de Produção (Matéria + MDO):",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = CostEngineeringCalculator.formatCurrency(pricing.totalDirectCost),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 3: LUCRO & IMPOSTO (IVA) - METODOLOGIA ANGOLANA
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
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "1. Margem de Lucro Almejada",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Padrão: 70% a 100%+",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FluxGoldVibrant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "No mercado de prestação de serviços e manufatura em Angola, a margem de lucro líquido pretendida varia tipicamente entre 70% e 100% (ou mais) sobre o custo base de produção.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Fast buttons for % of Profit
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(70.0, 80.0, 85.0, 100.0, 120.0).forEach { pct ->
                                            val isSelected = (state.desiredProfitMarginPercent == pct)
                                            OutlinedButton(
                                                onClick = { onUpdateState { copy(desiredProfitMarginPercent = pct) } },
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else ButtonDefaults.outlinedButtonColors()
                                            ) {
                                                Text(
                                                    text = "${pct.toInt()}%",
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    // Slider for fine tuning profit %
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Lucro Líquido Pretendido:",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${state.desiredProfitMarginPercent.toInt()}% (${CostEngineeringCalculator.formatCurrency(pricing.netProfit)})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        Slider(
                                            value = state.desiredProfitMarginPercent.toFloat(),
                                            onValueChange = { v -> onUpdateState { copy(desiredProfitMarginPercent = v.toDouble()) } },
                                            valueRange = 10f..200f,
                                            steps = 38
                                        )
                                    }

                                    HorizontalDivider()

                                    // Imposto IVA AGT (14%)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "2. Imposto IVA AGT de Angola",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Padrão AGT: 14%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "Alíquota do Imposto sobre o Valor Acrescentado (IVA) conforme a Administração Geral Tributária (AGT). Selecione a alíquota aplicável:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Quick IVA chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(0.0 to "0% Isento", 5.0 to "5% Cativação", 7.0 to "7% Simplificado", 14.0 to "14% Geral AGT").forEach { (tax, label) ->
                                            val isSelected = (state.taxesAndSalesFeePercent == tax)
                                            OutlinedButton(
                                                onClick = { onUpdateState { copy(taxesAndSalesFeePercent = tax) } },
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer) else ButtonDefaults.outlinedButtonColors()
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 10.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Imposto IVA AGT (${state.taxesAndSalesFeePercent.toInt()}%):",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = CostEngineeringCalculator.formatCurrency(pricing.taxesAmount),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }

                                    HorizontalDivider()

                                    // Desconto Comercial
                                    OutlinedTextField(
                                        value = state.discountAmount.toString(),
                                        onValueChange = { d ->
                                            d.toDoubleOrNull()?.let { v -> onUpdateState { copy(discountAmount = v) } }
                                        },
                                        label = { Text("Desconto Comercial Aplicado (${CostEngineeringCalculator.currentCurrency.symbol})") },
                                        placeholder = { Text("0.00") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAB 4: PROPOSTA ANGOLANA & RESULTADOS
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "VALOR TOTAL DO ORÇAMENTO (MÉTODO ANGOLANO)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = CostEngineeringCalculator.formatCurrency(pricing.finalSellingPrice),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Matéria-Prima + MDO (85%) + Lucro (${pricing.profitPercentageOnCost.toInt()}%) + IVA (${pricing.taxesPercentage.toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        // Detailed Breakdown Card
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Demonstrativo Financeiro do Projeto",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        ViabilityBadge(status = pricing.breakEvenStatus)
                                    }

                                    Text(
                                        text = pricing.breakEvenAnalysisText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    HorizontalDivider()

                                    MetricRow(
                                        label = "1. Matérias-Primas & Insumos:",
                                        value = CostEngineeringCalculator.formatCurrency(pricing.directMaterialsCost)
                                    )
                                    MetricRow(
                                        label = "2. Mão de Obra (${pricing.laborPercentageOnMaterials.toInt()}% sobre materiais):",
                                        value = CostEngineeringCalculator.formatCurrency(pricing.directLaborCost),
                                        valueColor = Emerald500
                                    )
                                    MetricRow(
                                        label = "3. Custo Base de Produção:",
                                        value = CostEngineeringCalculator.formatCurrency(pricing.totalProductionCost),
                                        isBold = true
                                    )
                                    MetricRow(
                                        label = "4. Lucro Líquido Almejado (${pricing.profitPercentageOnCost.toInt()}%):",
                                        value = "+${CostEngineeringCalculator.formatCurrency(pricing.netProfit)}",
                                        valueColor = MaterialTheme.colorScheme.secondary,
                                        isBold = true
                                    )
                                    MetricRow(
                                        label = "5. Imposto IVA AGT (${pricing.taxesPercentage.toInt()}%):",
                                        value = "+${CostEngineeringCalculator.formatCurrency(pricing.taxesAmount)}",
                                        valueColor = MaterialTheme.colorScheme.error
                                    )
                                    if (pricing.discountAmount > 0) {
                                        MetricRow(
                                            label = "6. Desconto Comercial:",
                                            value = "-${CostEngineeringCalculator.formatCurrency(pricing.discountAmount)}",
                                            valueColor = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    MetricRow(
                                        label = "★ Preço Final Cobrado ao Cliente:",
                                        value = CostEngineeringCalculator.formatCurrency(pricing.finalSellingPrice),
                                        valueColor = MaterialTheme.colorScheme.primary,
                                        isBold = true
                                    )
                                }
                            }
                        }

                        // Commercial Terms
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
                                    Text(
                                        text = "Condições Comerciais & Pagamento",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    OutlinedTextField(
                                        value = state.paymentTerms,
                                        onValueChange = { t -> onUpdateState { copy(paymentTerms = t) } },
                                        label = { Text("Condições de Pagamento") },
                                        placeholder = { Text("Ex: 50% de sinal e 50% na conclusão") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = state.termsAndConditions,
                                        onValueChange = { t -> onUpdateState { copy(termsAndConditions = t) } },
                                        label = { Text("Termos e Validade da Proposta") },
                                        placeholder = { Text("Ex: Proposta válida por 15 dias.") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }

                        // Actions: WhatsApp direct, generic share, copy
                        item {
                            val proposalText = CostEngineeringCalculator.generateExecutiveProposalText(
                                budget = BudgetEntity(
                                    id = state.budgetId,
                                    clientName = state.clientName.ifBlank { "Cliente" },
                                    clientContact = state.clientContact,
                                    projectTitle = state.projectTitle.ifBlank { "Proposta de Serviço" },
                                    projectScope = state.projectScope,
                                    dateCreatedEpochMillis = System.currentTimeMillis(),
                                    deadlineDays = state.deadlineDays,
                                    status = state.status,
                                    directMaterialsTotal = state.materialsList.sumOf { it.total },
                                    laborPercentageOnMaterials = state.laborPercentageOnMaterials,
                                    laborHours = state.laborHours,
                                    laborHourlyRate = state.laborHourlyRate,
                                    overheadPercentage = state.overheadPercentage,
                                    taxesAndSalesFeePercent = state.taxesAndSalesFeePercent,
                                    desiredProfitMarginPercent = state.desiredProfitMarginPercent,
                                    discountAmount = state.discountAmount,
                                    paymentTerms = state.paymentTerms,
                                    termsAndConditions = state.termsAndConditions
                                ),
                                items = state.materialsList,
                                calculation = pricing
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { shareBudgetPdfWhatsApp() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_share_budget_whatsapp_pdf"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                                ) {
                                    Icon(imageVector = Icons.Default.Chat, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Enviar Orçamento em PDF pelo WhatsApp", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { downloadBudgetPdf() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_download_budget_pdf"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Baixar Orçamento em PDF", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { triggerBudgetPdfExport() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_export_budget_pdf"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Print, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Imprimir / Salvar como PDF (A4)")
                                }

                                if (state.clientContact.isNotBlank()) {
                                    Button(
                                        onClick = {
                                            openWhatsAppChat(context, state.clientContact, state.clientName, proposalText)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_direct_whatsapp_proposal"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                                    ) {
                                        Icon(imageVector = Icons.Default.Chat, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Enviar Direto ao WhatsApp (${state.clientContact})", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, proposalText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Proposta Comercial")
                                        context.startActivity(shareIntent)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_share_proposal"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Compartilhar Proposta", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Proposta Comercial", proposalText)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Proposta copiada para a área de transferência!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Copiar Texto Completo da Proposta")
                                }
                            }
                        }

                        // Save Budget Button
                        item {
                            Button(
                                onClick = onSaveBudget,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_save_budget_bottom"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Salvar Orçamento", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Client Selector Dialog
    if (showClientPicker) {
        Dialog(onDismissRequest = { showClientPicker = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selecionar Cliente Cadastrado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showClientPicker = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(clients) { client ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onUpdateState {
                                            copy(
                                                clientName = client.name,
                                                clientContact = client.phone
                                            )
                                        }
                                        showClientPicker = false
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = client.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (client.phone.isNotBlank()) {
                                            Text(
                                                text = "Tel/WhatsApp: ${client.phone}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (client.nif.isNotBlank()) {
                                            Text(
                                                text = "NIF: ${client.nif}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selecionar",
                                        tint = FluxGoldVibrant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}
