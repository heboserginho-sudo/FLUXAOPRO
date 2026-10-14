package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ClientEntity
import com.example.ui.components.InvoiceCustomizationDialog
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.theme.FluxGoldVibrant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.data.model.CompanySubscription
import com.example.data.model.invoice.AngolaPaymentMethod
import com.example.data.model.invoice.AngolaVatRegime
import com.example.data.model.invoice.InvoiceItemData
import com.example.data.model.invoice.InvoiceStatus
import com.example.data.model.invoice.InvoiceType
import com.example.data.repository.SubscriptionRepository
import com.example.ui.viewmodel.MainViewModel
import com.example.util.AngolaInvoicePrinter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private fun formatKz(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("pt", "AO"))
    nf.minimumFractionDigits = 2
    nf.maximumFractionDigits = 2
    return "${nf.format(amount)} Kz"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    invoices: List<InvoiceEntity>,
    subscription: CompanySubscription,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onNavigateToClients: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTypeFilter by remember { mutableStateOf<InvoiceType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showCustomizationDialog by remember { mutableStateOf(false) }
    var invoiceToDelete by remember { mutableStateOf<InvoiceEntity?>(null) }
    var invoiceForExport by remember { mutableStateOf<InvoiceEntity?>(null) }
    var exportInvoiceItems by remember { mutableStateOf<List<InvoiceItemEntity>>(emptyList()) }
    var isPrintingId by remember { mutableStateOf<Long?>(null) }

    val customization by viewModel.invoiceCustomization.collectAsStateWithLifecycle()
    val registeredClients by viewModel.allClients.collectAsStateWithLifecycle()

    val filteredInvoices = remember(invoices, selectedTypeFilter, searchQuery) {
        invoices.filter { inv ->
            val matchesType = selectedTypeFilter == null || inv.invoiceType == selectedTypeFilter
            val matchesQuery = searchQuery.isBlank() ||
                    inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                    inv.clientName.contains(searchQuery, ignoreCase = true) ||
                    inv.clientNif.contains(searchQuery, ignoreCase = true)
            matchesType && matchesQuery
        }
    }

    val totalInvoiced = remember(invoices) { invoices.sumOf { it.totalToPay } }
    val totalPaid = remember(invoices) {
        invoices.filter { it.status == InvoiceStatus.PAID }.sumOf { it.totalToPay }
    }
    val totalPending = remember(invoices) {
        invoices.filter { it.status == InvoiceStatus.PENDING }.sumOf { it.totalToPay }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Screen Header & AGT Compliance Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Faturas AGT",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFD4AF37).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37))
                            ) {
                                Text(
                                    text = "Angola 🇦🇴",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Emissão Fiscal de Faturas e Personalização com Logo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showCustomizationDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FluxGoldPrimary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_open_invoice_customization")
                        ) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = FluxGoldVibrant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Personalizar", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FluxGoldVibrant)
                        }

                        if (onNavigateToClients != null) {
                            Button(
                                onClick = onNavigateToClients,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_invoices_goto_clients")
                            ) {
                                Icon(imageVector = Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clientes", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // AGT Compliance Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "AGT Legal",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Em conformidade com o Decreto Presidencial n.º 292/18",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Hash de 4 caracteres, IVA discriminado, NIF do adquirente e menção AGT.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Company Logo & Customization Banner
            item {
                val logoBmp = remember(customization.logoBase64) {
                    if (customization.logoBase64.isNotBlank()) {
                        try {
                            val bytes = Base64.decode(customization.logoBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCustomizationDialog = true }
                        .testTag("card_invoice_customization_shortcut"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FluxGoldPrimary.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoBmp != null) {
                                Image(
                                    bitmap = logoBmp.asImageBitmap(),
                                    contentDescription = "Logotipo da Empresa",
                                    modifier = Modifier
                                        .size(46.dp)
                                        .padding(2.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.flux_ao_icon),
                                    contentDescription = "Logo Padrão",
                                    modifier = Modifier.size(38.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (logoBmp != null) "Logótipo Configurado" else "Adicionar Logótipo da Empresa",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(customization.composeAccentColor)
                                )
                            }
                            Text(
                                text = "Estilo: ${customization.styleDisplayName} • Toque para alterar logotipo, cores e rodapé",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Personalizar",
                            tint = FluxGoldVibrant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Summary Financial Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryStatCard(
                        title = "Total Faturado",
                        value = formatKz(totalInvoiced),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "Liquidado",
                        value = formatKz(totalPaid),
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "Pendente",
                        value = formatKz(totalPending),
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search and Filters
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar nº fatura, cliente ou NIF...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Pesquisar")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Limpar")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_invoices_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )
            }

            // Document Type Filter Tabs
            item {
                val types = listOf<InvoiceType?>(null) + InvoiceType.values().toList()
                ScrollableTabRow(
                    selectedTabIndex = types.indexOf(selectedTypeFilter).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    types.forEach { type ->
                        val selected = selectedTypeFilter == type
                        Tab(
                            selected = selected,
                            onClick = { selectedTypeFilter = type },
                            text = {
                                Text(
                                    text = type?.shortName ?: "Todas (${invoices.size})",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }

            // Invoices List
            if (filteredInvoices.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (invoices.isEmpty()) "Nenhuma fatura emitida ainda" else "Nenhum documento encontrado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Emita faturas comerciais (FT) ou faturas-recibo (FR) no padrão fiscal de Angola com cálculo de IVA e hash AGT.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreateSheet = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("empty_emit_invoice_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Emitir Documento AGT")
                            }
                        }
                    }
                }
            } else {
                items(filteredInvoices, key = { it.id }) { invoice ->
                    InvoiceItemCard(
                        invoice = invoice,
                        onPrint = {
                            scope.launch {
                                isPrintingId = invoice.id
                                val items = viewModel.getInvoiceItemsSync(invoice.id)
                                exportInvoiceItems = items
                                invoiceForExport = invoice
                                isPrintingId = null
                            }
                        },
                        onShareWhatsApp = {
                            scope.launch {
                                isPrintingId = invoice.id
                                val items = viewModel.getInvoiceItemsSync(invoice.id)
                                exportInvoiceItems = items
                                invoiceForExport = invoice
                                isPrintingId = null
                            }
                        },
                        onTogglePaid = {
                            val newStatus = if (invoice.status == InvoiceStatus.PAID) {
                                InvoiceStatus.PENDING
                            } else {
                                InvoiceStatus.PAID
                            }
                            viewModel.updateInvoiceStatus(invoice.id, newStatus, syncCashFlow = true)
                        },
                        onDelete = {
                            invoiceToDelete = invoice
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB to create invoice
        FloatingActionButton(
            onClick = { showCreateSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_create_invoice"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Emitir Fatura")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emitir Fatura", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal to create invoice
    if (showCreateSheet) {
        InvoiceEditorSheet(
            subscription = subscription,
            viewModel = viewModel,
            onDismiss = { showCreateSheet = false },
            onInvoiceSaved = {
                showCreateSheet = false
            }
        )
    }

    // Modal to customize invoice (Logo, Colors, Watermark, Bank Details)
    if (showCustomizationDialog) {
        InvoiceCustomizationDialog(
            customization = customization,
            onDismiss = { showCustomizationDialog = false },
            onPickLogo = { uri ->
                viewModel.saveInvoiceLogo(uri) { /* handled */ }
            },
            onRemoveLogo = {
                viewModel.removeInvoiceLogo()
            },
            onSaveCustomization = { accentColor, style, footer, showLogo, showWatermark, showQr, bankInfo ->
                viewModel.updateInvoiceCustomization(
                    accentColorHex = accentColor,
                    templateStyle = style,
                    customFooterNotes = footer,
                    showLogoOnInvoice = showLogo,
                    showWatermark = showWatermark,
                    showQrCode = showQr,
                    bankAccountInfo = bankInfo
                )
            }
        )
    }

    // Delete Confirmation Dialog
    if (invoiceToDelete != null) {
        val invoice = invoiceToDelete!!
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            title = { Text("Eliminar ${invoice.invoiceNumber}?") },
            text = {
                Text("Deseja realmente apagar esta fatura? Em ambientes fiscais normais, faturas erradas devem ser anuladas via Nota de Crédito (NC).")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInvoice(invoice.id)
                        invoiceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (invoiceForExport != null) {
        val invoice = invoiceForExport!!
        var targetWhatsAppPhone by remember(invoice.id) { mutableStateOf(invoice.clientPhone) }

        AlertDialog(
            onDismissRequest = { invoiceForExport = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exportar & Partilhar ${invoice.invoiceNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${invoice.invoiceType.code} ${invoice.invoiceNumber} • ${invoice.clientName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Total a Pagar: ${formatKz(invoice.totalToPay)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Vencimento: ${invoice.formattedDueDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Campo editável do WhatsApp para confirmação do número
                    OutlinedTextField(
                        value = targetWhatsAppPhone,
                        onValueChange = { targetWhatsAppPhone = it },
                        label = { Text("Número WhatsApp do Cliente", fontSize = 12.sp) },
                        placeholder = { Text("Ex: 923 456 789 ou +244 923 456 789", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "WhatsApp",
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (targetWhatsAppPhone.isNotBlank()) {
                                IconButton(onClick = { targetWhatsAppPhone = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpar", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_invoice_whatsapp_phone")
                    )

                    // Ação 1: Enviar Mensagem Direta pelo WhatsApp com dados da fatura e IBAN
                    Button(
                        onClick = {
                            AngolaInvoicePrinter.sendInvoiceViaWhatsAppChat(
                                context = context,
                                invoice = invoice,
                                items = exportInvoiceItems,
                                phoneOverride = targetWhatsAppPhone
                            )
                            scope.launch {
                                viewModel.awardPointsForAction("invoice_whatsapp", referenceId = invoice.id.toString(), description = "Fatura enviada via WhatsApp")
                            }
                            invoiceForExport = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_invoice_share_whatsapp_direct")
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar Mensagem no WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    // Ação 2: Enviar Arquivo PDF pelo WhatsApp
                    Button(
                        onClick = {
                            AngolaInvoicePrinter.shareInvoicePdfViaWhatsApp(
                                context = context,
                                invoice = invoice,
                                items = exportInvoiceItems,
                                customization = customization,
                                phoneOverride = targetWhatsAppPhone
                            )
                            invoiceForExport = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_invoice_share_whatsapp")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar Arquivo PDF pelo WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    // Ação 3: Baixar PDF para o Aparelho
                    Button(
                        onClick = {
                            AngolaInvoicePrinter.downloadInvoicePdf(
                                context = context,
                                invoice = invoice,
                                items = exportInvoiceItems,
                                customization = customization
                            )
                            invoiceForExport = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_invoice_download_pdf")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Baixar PDF (Salvar no Celular)", fontWeight = FontWeight.Bold)
                    }

                    // Ação 4: Imprimir / Visualizar A4
                    OutlinedButton(
                        onClick = {
                            AngolaInvoicePrinter.printInvoice(
                                context = context,
                                invoice = invoice,
                                items = exportInvoiceItems,
                                customization = customization
                            )
                            invoiceForExport = null
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_invoice_print_pdf")
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Imprimir / Salvar como PDF (A4)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { invoiceForExport = null }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
private fun SummaryStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InvoiceItemCard(
    invoice: InvoiceEntity,
    onPrint: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    val (typeColor, typeBg) = when (invoice.invoiceType) {
        InvoiceType.FT -> Pair(Color(0xFF2563EB), Color(0xFFDBEAFE))
        InvoiceType.FR -> Pair(Color(0xFF059669), Color(0xFFD1FAE5))
        InvoiceType.FP -> Pair(Color(0xFF7C3AED), Color(0xFFEDE9FE))
        InvoiceType.NC -> Pair(Color(0xFFDC2626), Color(0xFFFEE2E2))
    }

    val (statusColor, statusBg) = when (invoice.status) {
        InvoiceStatus.PAID -> Pair(Color(0xFF059669), Color(0xFFD1FAE5))
        InvoiceStatus.PENDING -> Pair(Color(0xFFD97706), Color(0xFFFEF3C7))
        InvoiceStatus.CANCELLED -> Pair(Color(0xFFDC2626), Color(0xFFFEE2E2))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("invoice_card_${invoice.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top row: Document Type badge, Invoice Number, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeBg
                    ) {
                        Text(
                            text = invoice.invoiceType.code,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = invoice.invoiceNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg,
                    modifier = Modifier.clickable { onTogglePaid() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = invoice.status.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Client Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invoice.clientName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "NIF: ${invoice.clientNif} • ${invoice.formattedDate}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatKz(invoice.totalToPay),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "IVA: ${formatKz(invoice.totalVat)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Fiscal Hash preview
            if (invoice.fiscalValidationHash.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "AGT Hash: ${invoice.fiscalValidationHash}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Venc: ${invoice.formattedDueDate}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // WhatsApp Share Button
                    Button(
                        onClick = onShareWhatsApp,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        modifier = Modifier.testTag("btn_invoice_card_whatsapp_${invoice.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Print / PDF Button
                    OutlinedButton(
                        onClick = onPrint,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("print_invoice_${invoice.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Imprimir",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Imprimir / PDF", fontSize = 12.sp)
                    }

                    // Mark as Paid / Pending toggle
                    if (invoice.status == InvoiceStatus.PENDING) {
                        Button(
                            onClick = onTogglePaid,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier.testTag("liquidate_invoice_${invoice.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Liquidar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Liquidar", fontSize = 12.sp)
                        }
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("delete_invoice_${invoice.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceEditorSheet(
    subscription: CompanySubscription,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onInvoiceSaved: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val registeredClients by viewModel.allClients.collectAsStateWithLifecycle()
    var showClientPicker by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf(InvoiceType.FT) }
    var invoiceNumber by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var clientNif by remember { mutableStateOf("999999999") } // Default consumidor final NIF
    var clientAddress by remember { mutableStateOf("Luanda, Angola") }
    var clientPhone by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Transferência Bancária / IBAN") }
    var vatRegime by remember { mutableStateOf("Regime Geral (IVA 14%)") }
    var selectedStatus by remember { mutableStateOf(InvoiceStatus.PENDING) }
    var notes by remember { mutableStateOf("Os bens ou serviços foram colocados à disposição do adquirente na data do documento.") }

    val items = remember {
        mutableStateListOf(
            InvoiceItemData(
                description = "Serviços de Consultoria e Engenharia",
                unit = "Un",
                quantity = 1.0,
                unitPriceKz = 150000.0,
                discountPercent = 0.0,
                vatRate = 14.0
            )
        )
    }

    // Auto-generate invoice number when type changes
    LaunchedEffect(selectedType) {
        invoiceNumber = viewModel.generateNextInvoiceNumber(selectedType)
        if (selectedType == InvoiceType.FR) {
            selectedStatus = InvoiceStatus.PAID
        }
    }

    val totalGross = items.sumOf { it.grossTotal }
    val totalDiscount = items.sumOf { it.discountAmount }
    val totalNet = items.sumOf { it.netTotal }
    val totalVat = items.sumOf { it.vatAmount }
    val totalToPay = items.sumOf { it.finalTotal }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nova Fatura Angola",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                }
            }
            Divider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Document Type Selection
                Text(
                    text = "Tipo de Documento Fiscal (AGT)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InvoiceType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type }
                                .testTag("type_${type.code}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = type.code,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = type.shortName,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Document Number and Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        label = { Text("Número do Documento") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = if (selectedStatus == InvoiceStatus.PAID) "Liquidada" else "Pendente",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = {
                            IconButton(onClick = {
                                selectedStatus = if (selectedStatus == InvoiceStatus.PAID) InvoiceStatus.PENDING else InvoiceStatus.PAID
                            }) {
                                Icon(
                                    imageVector = if (selectedStatus == InvoiceStatus.PAID) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = "Alternar",
                                    tint = if (selectedStatus == InvoiceStatus.PAID) Color(0xFF059669) else Color(0xFFD97706)
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Emitter Card (Company)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "EMISSOR (Sua Empresa)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${subscription.companyName.ifBlank { "Minha Empresa Lda" }} • NIF: ${subscription.nif.ifBlank { "5001234567" }}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "IBAN: ${subscription.destinationIban.ifBlank { "AO06 0040 0000 1234 5678 9012 3" }} • Tel: ${subscription.contactPhone.ifBlank { "+244 923 000 000" }}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Client Details Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dados do Adquirente / Cliente",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (registeredClients.isNotEmpty()) {
                        TextButton(
                            onClick = { showClientPicker = true },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("btn_select_client_in_invoice")
                        ) {
                            Icon(imageVector = Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp), tint = FluxGoldVibrant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Selecionar Cliente (${registeredClients.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FluxGoldVibrant)
                        }
                    }
                }

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nome do Cliente / Empresa *") },
                    placeholder = { Text("Ex: Sonangol EP, Banco BAI, ou João Silva") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invoice_client_name"),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = clientNif,
                        onValueChange = { clientNif = it },
                        label = { Text("NIF Angolano") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_client_nif"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedButton(
                        onClick = { clientNif = "Consumidor Final" },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("Consumidor Final", fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = clientAddress,
                        onValueChange = { clientAddress = it },
                        label = { Text("Endereço / Cidade") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Telefone") },
                        placeholder = { Text("+244 923...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Payment Method & Regime
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        label = { Text("Forma de Pagamento") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = vatRegime,
                        onValueChange = { vatRegime = it },
                        label = { Text("Regime de IVA") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Items Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Linhas de Artigos e Serviços (${items.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TextButton(
                        onClick = {
                            items.add(
                                InvoiceItemData(
                                    description = "Novo item faturável",
                                    unit = "Un",
                                    quantity = 1.0,
                                    unitPriceKz = 25000.0,
                                    discountPercent = 0.0,
                                    vatRate = 14.0
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar Item")
                    }
                }

                items.forEachIndexed { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Item #${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (items.size > 1) {
                                    IconButton(
                                        onClick = { items.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remover",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = item.description,
                                onValueChange = { desc -> items[index] = item.copy(description = desc) },
                                label = { Text("Descrição do Produto ou Serviço") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = if (item.quantity == 0.0) "" else item.quantity.toString(),
                                    onValueChange = { q ->
                                        val qty = q.toDoubleOrNull() ?: 0.0
                                        items[index] = item.copy(quantity = qty)
                                    },
                                    label = { Text("Qtd") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = item.unit,
                                    onValueChange = { u -> items[index] = item.copy(unit = u) },
                                    label = { Text("Unidade") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = if (item.unitPriceKz == 0.0) "" else item.unitPriceKz.toString(),
                                    onValueChange = { p ->
                                        val price = p.toDoubleOrNull() ?: 0.0
                                        items[index] = item.copy(unitPriceKz = price)
                                    },
                                    label = { Text("Preço Unit. (Kz)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1.6f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = if (item.discountPercent == 0.0) "" else item.discountPercent.toString(),
                                    onValueChange = { d ->
                                        val disc = d.toDoubleOrNull() ?: 0.0
                                        items[index] = item.copy(discountPercent = disc)
                                    },
                                    label = { Text("Desc. %") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = if (item.vatRate == 0.0) "0" else item.vatRate.toString(),
                                    onValueChange = { v ->
                                        val rate = v.toDoubleOrNull() ?: 0.0
                                        items[index] = item.copy(vatRate = rate)
                                    },
                                    label = { Text("Taxa IVA %") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1.4f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Total Linha",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatKz(item.finalTotal),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Financial Summary Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Totais do Documento (AOA / Kz)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider()
                        SummaryRow("Total Ilíquido (Bruto):", formatKz(totalGross))
                        if (totalDiscount > 0) {
                            SummaryRow("Desconto Comercial:", "- ${formatKz(totalDiscount)}")
                        }
                        SummaryRow("Total Incidência Líquida:", formatKz(totalNet))
                        SummaryRow("Total IVA Liquidado:", formatKz(totalVat), isHighlighted = true)
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL A PAGAR:",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatKz(totalToPay),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações Legais / Menções Obrigatórias") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )
            }

            // Save & Emit Button
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val emitter = subscription.companyName.ifBlank { "FluxAO Empresa Demo" }
                            val nif = subscription.nif.ifBlank { "5001234567" }
                            val invoiceEntity = InvoiceEntity(
                                invoiceNumber = invoiceNumber.ifBlank { "FT 2026/001" },
                                invoiceType = selectedType,
                                status = selectedStatus,
                                clientName = clientName.ifBlank { "Consumidor Final" },
                                clientNif = clientNif.ifBlank { "999999999" },
                                clientAddress = clientAddress,
                                clientPhone = clientPhone,
                                emitterName = emitter,
                                emitterNif = nif,
                                emitterAddress = "Luanda, Angola",
                                emitterCity = "Luanda",
                                emitterIban = subscription.destinationIban.ifBlank { "AO06 0040 0000 1234 5678 9012 3" },
                                vatRegime = vatRegime,
                                paymentMethod = paymentMethod,
                                notes = notes
                            )

                            viewModel.saveInvoice(invoiceEntity, items) {
                                onInvoiceSaved()
                            }
                        },
                        modifier = Modifier
                            .weight(2f)
                            .testTag("submit_save_invoice"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Emitir Documento AGT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showClientPicker) {
        AlertDialog(
            onDismissRequest = { showClientPicker = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.People, contentDescription = null, tint = FluxGoldVibrant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Selecionar Cliente", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(registeredClients, key = { it.id }) { c ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clientName = c.name
                                    if (c.nif.isNotBlank()) clientNif = c.nif
                                    if (c.address.isNotBlank() || c.city.isNotBlank()) {
                                        clientAddress = listOf(c.address, c.city).filter { it.isNotBlank() }.joinToString(", ")
                                    }
                                    if (c.phone.isNotBlank()) clientPhone = c.phone
                                    showClientPicker = false
                                }
                                .testTag("picker_client_${c.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(c.initials, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(c.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    val sub = listOf(
                                        if (c.nif.isNotBlank()) "NIF: ${c.nif}" else null,
                                        if (c.phone.isNotBlank()) c.phone else null
                                    ).filterNotNull().joinToString(" • ")
                                    if (sub.isNotBlank()) {
                                        Text(sub, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showClientPicker = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}
