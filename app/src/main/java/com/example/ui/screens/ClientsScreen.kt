package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.invoice.InvoiceStatus
import com.example.util.AngolaInvoicePrinter
import com.example.util.PdfDocumentManager
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.ui.theme.Emerald500
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.theme.FluxGoldVibrant
import com.example.ui.viewmodel.MainViewModel

fun dialPhoneNumber(context: Context, rawPhone: String) {
    val clean = rawPhone.filter { it.isDigit() || it == '+' }
    if (clean.isBlank()) {
        Toast.makeText(context, "Cliente sem número de telefone cadastrado", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Não foi possível abrir o discador telefónico", Toast.LENGTH_SHORT).show()
    }
}

fun openWhatsAppChat(context: Context, rawPhone: String, clientName: String = "", customMessage: String = "") {
    val greeting = if (clientName.isNotBlank()) "Olá $clientName," else "Olá,"
    val messageText = customMessage.ifBlank {
        "$greeting saudações através do FluxAO! Entramos em contacto a respeito das nossas soluções e faturas."
    }
    PdfDocumentManager.openWhatsAppChat(context, rawPhone, messageText)
}

@Composable
fun ClientsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onSelectClientForInvoice: ((ClientEntity) -> Unit)? = null
) {
    val clients by viewModel.allClients.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ClientsScreen(
        clients = clients,
        onSaveClient = { viewModel.saveClient(it) },
        onDeleteClient = { viewModel.deleteClient(it) },
        modifier = modifier,
        onSelectClientForInvoice = onSelectClientForInvoice,
        invoices = invoices,
        budgets = budgets,
        projects = projects,
        transactions = uiState.transactions
    )
}

@Composable
fun ClientsScreen(
    clients: List<ClientEntity>,
    onSaveClient: (ClientEntity) -> Unit,
    onDeleteClient: (ClientEntity) -> Unit,
    modifier: Modifier = Modifier,
    onSelectClientForInvoice: ((ClientEntity) -> Unit)? = null,
    invoices: List<InvoiceEntity> = emptyList(),
    budgets: List<BudgetEntity> = emptyList(),
    projects: List<ProjectEntity> = emptyList(),
    transactions: List<TransactionEntity> = emptyList()
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) } // null = Todos, "Empresa", "Particular"

    var editingClient by remember { mutableStateOf<ClientEntity?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var clientToDelete by remember { mutableStateOf<ClientEntity?>(null) }
    var selectedClientForDetails by remember { mutableStateOf<ClientEntity?>(null) }

    val filteredClients = remember(clients, searchQuery, selectedTypeFilter) {
        clients.filter { client ->
            val matchesQuery = searchQuery.isBlank() ||
                    client.name.contains(searchQuery, ignoreCase = true) ||
                    client.nif.contains(searchQuery, ignoreCase = true) ||
                    client.phone.contains(searchQuery, ignoreCase = true) ||
                    client.city.contains(searchQuery, ignoreCase = true)

            val matchesType = selectedTypeFilter == null || client.customerType.equals(selectedTypeFilter, ignoreCase = true)

            matchesQuery && matchesType
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Hero
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, FluxGoldPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Gestão de Clientes",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("👥", fontSize = 20.sp)
                                }
                                Text(
                                    text = "${clients.size} cliente(s) cadastrado(s) • Chamadas e WhatsApp direto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    editingClient = null
                                    showAddEditDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FluxGoldVibrant,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("btn_add_client_header")
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Novo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Pesquisar por nome, NIF, telefone...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar busca")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_clients")
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedTypeFilter == null,
                            onClick = { selectedTypeFilter = null },
                            label = { Text("Todos (${clients.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FluxGoldPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = FluxGoldVibrant
                            )
                        )
                        FilterChip(
                            selected = selectedTypeFilter == "Empresa",
                            onClick = { selectedTypeFilter = if (selectedTypeFilter == "Empresa") null else "Empresa" },
                            label = { Text("Empresas") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FluxGoldPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = FluxGoldVibrant
                            )
                        )
                        FilterChip(
                            selected = selectedTypeFilter == "Particular",
                            onClick = { selectedTypeFilter = if (selectedTypeFilter == "Particular") null else "Particular" },
                            label = { Text("Particulares") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FluxGoldPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = FluxGoldVibrant
                            )
                        )
                    }
                }
            }

            if (filteredClients.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isBlank()) "Nenhum cliente cadastrado ainda" else "Nenhum cliente encontrado para \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Cadastre clientes com NIF e telefone para emitir faturas e comunicar pelo WhatsApp.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    editingClient = null
                                    showAddEditDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FluxGoldVibrant)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cadastrar Cliente")
                            }
                        }
                    }
                }
            } else {
                items(filteredClients, key = { it.id }) { client ->
                    ClientCard(
                        client = client,
                        invoices = invoices.filter { it.clientName.equals(client.name, ignoreCase = true) },
                        budgets = budgets.filter { it.clientName.equals(client.name, ignoreCase = true) },
                        projects = projects.filter { it.clientId == client.id || it.clientName.equals(client.name, ignoreCase = true) },
                        transactions = transactions.filter { it.contactName.equals(client.name, ignoreCase = true) && it.type == TransactionType.INCOME },
                        onViewDetails = { selectedClientForDetails = client },
                        onCall = { dialPhoneNumber(context, client.phone) },
                        onWhatsApp = { openWhatsAppChat(context, client.whatsapp.ifBlank { client.phone }, client.name) },
                        onEdit = {
                            editingClient = client
                            showAddEditDialog = true
                        },
                        onDelete = { clientToDelete = client },
                        onSelectForInvoice = onSelectClientForInvoice
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                editingClient = null
                showAddEditDialog = true
            },
            containerColor = FluxGoldVibrant,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_client")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Cliente", modifier = Modifier.size(26.dp))
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        ClientAddEditDialog(
            initialClient = editingClient,
            onDismiss = { showAddEditDialog = false },
            onSave = { client ->
                onSaveClient(client)
                showAddEditDialog = false
            }
        )
    }

    // Client Financial Detail Dialog
    selectedClientForDetails?.let { client ->
        ClientDetailDialog(
            client = client,
            invoices = invoices.filter { it.clientName.equals(client.name, ignoreCase = true) },
            budgets = budgets.filter { it.clientName.equals(client.name, ignoreCase = true) },
            projects = projects.filter { it.clientId == client.id || it.clientName.equals(client.name, ignoreCase = true) },
            transactions = transactions.filter { it.contactName.equals(client.name, ignoreCase = true) && it.type == TransactionType.INCOME },
            onDismiss = { selectedClientForDetails = null },
            onEdit = {
                editingClient = client
                showAddEditDialog = true
                selectedClientForDetails = null
            },
            onCall = { dialPhoneNumber(context, client.phone) },
            onWhatsApp = { openWhatsAppChat(context, client.whatsapp.ifBlank { client.phone }, client.name) }
        )
    }

    // Delete Confirmation Dialog
    clientToDelete?.let { client ->
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            title = { Text("Eliminar Cliente") },
            text = {
                Text("Deseja realmente remover o cliente \"${client.name}\"? Esta ação não pode ser desfeita.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClient(client)
                        clientToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { clientToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ClientCard(
    client: ClientEntity,
    invoices: List<InvoiceEntity> = emptyList(),
    budgets: List<BudgetEntity> = emptyList(),
    projects: List<ProjectEntity> = emptyList(),
    transactions: List<TransactionEntity> = emptyList(),
    onViewDetails: () -> Unit = {},
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelectForInvoice: ((ClientEntity) -> Unit)? = null
) {
    val isCompany = client.customerType.equals("Empresa", ignoreCase = true)

    val totalPurchased = remember(invoices, projects) {
        val invTotal = invoices.sumOf { it.totalToPay }
        val projTotal = projects.sumOf { it.budgetedRevenue }
        if (invTotal > 0.0) invTotal else projTotal
    }
    val totalPaid = remember(transactions, projects) {
        val txPaid = transactions.filter { it.status == TransactionStatus.PAID }.sumOf { it.amount }
        val projPaid = projects.sumOf { it.paidAmount }
        if (txPaid > 0.0) txPaid else projPaid
    }
    val totalPending = remember(totalPurchased, totalPaid, transactions, projects) {
        if (totalPurchased > totalPaid) {
            totalPurchased - totalPaid
        } else {
            transactions.filter { it.status == TransactionStatus.PENDING }.sumOf { it.amount } +
                projects.sumOf { it.pendingAmount }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("client_card_${client.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        shape = CircleShape,
                        color = if (isCompany) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = client.initials,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isCompany) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = client.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (client.nif.isNotBlank()) {
                                Text(
                                    text = "NIF: ${client.nif}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(vertical = 1.dp)
                            ) {
                                Text(
                                    text = client.customerType,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Edit & Delete icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Client contact details
            Spacer(modifier = Modifier.height(10.dp))

            if (client.phone.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = client.phone,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (client.email.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = client.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (client.address.isNotBlank() || client.city.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    val fullLocation = listOf(client.address, client.city).filter { it.isNotBlank() }.joinToString(", ")
                    Text(text = fullLocation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (client.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Obs: ${client.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Financial Summary Chips (Total Comprado, Total Pago, Total Pendente)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text("Comprado", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = CostEngineeringCalculator.formatCurrency(totalPurchased),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald500.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text("Pago", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Emerald500)
                        Text(
                            text = CostEngineeringCalculator.formatCurrency(totalPaid),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald500,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (totalPending > 0.0) Color(0xFFEF4444).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text(
                            "Pendente",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (totalPending > 0.0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CostEngineeringCalculator.formatCurrency(totalPending),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (totalPending > 0.0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Communication Action Row: LIGAR, WHATSAPP, EXTRATO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call button
                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_call_client_${client.id}")
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ligar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // WhatsApp button
                Button(
                    onClick = onWhatsApp,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald500,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                        .testTag("btn_whatsapp_client_${client.id}")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Extrato / Detalhes button
                Button(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FluxGoldPrimary.copy(alpha = 0.15f),
                        contentColor = FluxGoldVibrant
                    ),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                        .testTag("btn_details_client_${client.id}")
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Extrato", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Optional Select for Invoice button
                if (onSelectForInvoice != null) {
                    Button(
                        onClick = { onSelectForInvoice(client) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FluxGoldVibrant,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_select_client_for_invoice")
                    ) {
                        Text("Usar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ClientAddEditDialog(
    initialClient: ClientEntity?,
    onDismiss: () -> Unit,
    onSave: (ClientEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialClient?.name ?: "") }
    var nif by remember { mutableStateOf(initialClient?.nif ?: "") }
    var phone by remember { mutableStateOf(initialClient?.phone ?: "") }
    var whatsapp by remember { mutableStateOf(initialClient?.whatsapp ?: "") }
    var email by remember { mutableStateOf(initialClient?.email ?: "") }
    var address by remember { mutableStateOf(initialClient?.address ?: "") }
    var city by remember { mutableStateOf(initialClient?.city ?: "Luanda") }
    var customerType by remember { mutableStateOf(initialClient?.customerType ?: "Empresa") }
    var notes by remember { mutableStateOf(initialClient?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (initialClient == null) "Cadastrar Cliente" else "Editar Cliente",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError && it.isNotBlank()) nameError = false
                        },
                        label = { Text("Nome do Cliente / Razão Social *") },
                        placeholder = { Text("Ex: Sociedade Comercial Lda") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                        },
                        isError = nameError,
                        supportingText = { if (nameError) Text("O nome é obrigatório", color = MaterialTheme.colorScheme.error) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_client_name")
                    )
                }

                item {
                    OutlinedTextField(
                        value = nif,
                        onValueChange = { nif = it },
                        label = { Text("NIF (Fiscal)") },
                        placeholder = { Text("5412345678") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_client_nif")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Telefone Chamadas") },
                            placeholder = { Text("+244 923 000 000") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Call, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_client_phone")
                        )

                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp Directo") },
                            placeholder = { Text("+244 923 000 000") },
                            leadingIcon = {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_client_whatsapp")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Comercial") },
                        placeholder = { Text("cliente@empresa.ao") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Endereço / Rua") },
                            placeholder = { Text("Ex: Rua Direita") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        )

                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("Cidade") },
                            placeholder = { Text("Luanda") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                }

                item {
                    Text(
                        text = "Tipo de Cliente",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        listOf("Empresa", "Particular", "Organização").forEach { type ->
                            FilterChip(
                                selected = customerType == type,
                                onClick = { customerType = type },
                                label = { Text(type) },
                                leadingIcon = {
                                    if (type == "Empresa") {
                                        Icon(imageVector = Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(14.dp))
                                    } else {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações Comerciais / Faturamento") },
                        placeholder = { Text("Ex: Faturar sempre com prazo de 15 dias.") },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    nameError = true
                                    return@Button
                                }
                                val toSave = (initialClient ?: ClientEntity(name = name.trim())).copy(
                                    name = name.trim(),
                                    nif = nif.trim(),
                                    phone = phone.trim(),
                                    whatsapp = whatsapp.trim().ifBlank { phone.trim() },
                                    email = email.trim(),
                                    address = address.trim(),
                                    city = city.trim().ifBlank { "Luanda" },
                                    customerType = customerType,
                                    notes = notes.trim()
                                )
                                onSave(toSave)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FluxGoldVibrant),
                            modifier = Modifier.testTag("btn_save_client_submit")
                        ) {
                            Text("Salvar Cliente", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientDetailDialog(
    client: ClientEntity,
    invoices: List<InvoiceEntity>,
    budgets: List<BudgetEntity>,
    projects: List<ProjectEntity>,
    transactions: List<TransactionEntity>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Histórico/Pagamentos, 1: Orçamentos, 2: Projetos, 3: Faturas
    val context = LocalContext.current

    // Calculations
    val totalPurchased = remember(invoices, projects) {
        val invTotal = invoices.sumOf { it.totalToPay }
        val projTotal = projects.sumOf { it.budgetedRevenue }
        if (invTotal > 0.0) invTotal else projTotal
    }
    val totalPaid = remember(transactions, projects) {
        val txPaid = transactions.filter { it.status == TransactionStatus.PAID }.sumOf { it.amount }
        val projPaid = projects.sumOf { it.paidAmount }
        if (txPaid > 0.0) txPaid else projPaid
    }
    val totalPending = remember(totalPurchased, totalPaid, transactions, projects) {
        if (totalPurchased > totalPaid) {
            totalPurchased - totalPaid
        } else {
            transactions.filter { it.status == TransactionStatus.PENDING }.sumOf { it.amount } +
                projects.sumOf { it.pendingAmount }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header with client initials & info
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
                            shape = CircleShape,
                            color = FluxGoldPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = client.initials,
                                    fontWeight = FontWeight.Bold,
                                    color = FluxGoldVibrant,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = client.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (client.nif.isNotBlank()) {
                                    Text(
                                        text = "NIF: ${client.nif}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = "• ${client.customerType}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Contact Actions: Chamada, WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (client.phone.isNotBlank()) {
                        OutlinedButton(
                            onClick = onCall,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ligar", fontSize = 12.sp)
                        }
                    }
                    val targetWhatsApp = client.whatsapp.ifBlank { client.phone }
                    if (targetWhatsApp.isNotBlank()) {
                        Button(
                            onClick = onWhatsApp,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(38.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Financial Cards: Total Comprado, Total Pago, Total Pendente
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Comprado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CostEngineeringCalculator.formatCurrency(totalPurchased),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Emerald500.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Pago", style = MaterialTheme.typography.labelSmall, color = Emerald500)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CostEngineeringCalculator.formatCurrency(totalPaid),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Emerald500,
                                maxLines = 1
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (totalPending > 0.0) Color(0xFFEF4444).copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "Pendente",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (totalPending > 0.0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CostEngineeringCalculator.formatCurrency(totalPending),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (totalPending > 0.0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Pagamentos, Orçamentos, Projetos, Faturas
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = FluxGoldVibrant
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Pagamentos (${transactions.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Orçamentos (${budgets.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Projetos (${projects.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Faturas (${invoices.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            if (transactions.isEmpty()) {
                                item {
                                    EmptyTabNotice(
                                        icon = Icons.Default.History,
                                        title = "Sem pagamentos registrados",
                                        subtitle = "Nenhuma movimentação de entrada vinculada a este cliente."
                                    )
                                }
                            } else {
                                items(transactions, key = { it.id }) { tx ->
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = tx.description.ifBlank { tx.category },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${tx.formattedDate} • ${tx.paymentMethod.label}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "+ ${CostEngineeringCalculator.formatCurrency(tx.amount)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Emerald500
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (tx.status == TransactionStatus.PAID) Emerald500.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = if (tx.status == TransactionStatus.PAID) "Pago" else "Pendente",
                                                        fontSize = 10.sp,
                                                        color = if (tx.status == TransactionStatus.PAID) Emerald500 else Color(0xFFD97706),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (budgets.isEmpty()) {
                                item {
                                    EmptyTabNotice(
                                        icon = Icons.Default.Assignment,
                                        title = "Sem orçamentos",
                                        subtitle = "Nenhum orçamento emitido para este cliente ainda."
                                    )
                                }
                            } else {
                                items(budgets, key = { it.id }) { b ->
                                    val calc = remember(b) { CostEngineeringCalculator.calculateForBudget(b) }
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = b.projectTitle,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Ref: ${b.budgetNumber.ifBlank { "#${b.id}" }} • ${b.status.label}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = CostEngineeringCalculator.formatCurrency(calc.finalSellingPrice),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = FluxGoldVibrant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            if (projects.isEmpty()) {
                                item {
                                    EmptyTabNotice(
                                        icon = Icons.Default.Folder,
                                        title = "Sem projetos vinculados",
                                        subtitle = "Nenhum projeto associado a este cliente."
                                    )
                                }
                            } else {
                                items(projects, key = { it.id }) { proj ->
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = proj.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Prazo: ${proj.formattedDeadline} • ${proj.status}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = CostEngineeringCalculator.formatCurrency(proj.budgetedRevenue),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Pago: ${CostEngineeringCalculator.formatCurrency(proj.paidAmount)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Emerald500
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            if (invoices.isEmpty()) {
                                item {
                                    EmptyTabNotice(
                                        icon = Icons.Default.Receipt,
                                        title = "Sem faturas emitidas",
                                        subtitle = "Nenhuma fatura comercial gerada para este cliente."
                                    )
                                }
                            } else {
                                items(invoices, key = { it.id }) { inv ->
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${inv.invoiceType.code} ${inv.invoiceNumber}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Venc: ${inv.formattedDueDate} • ${inv.status.title}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = CostEngineeringCalculator.formatCurrency(inv.totalToPay),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (inv.status == InvoiceStatus.PAID) Emerald500 else FluxGoldVibrant
                                                )
                                                IconButton(
                                                    onClick = {
                                                        val targetPhone = client.whatsapp.ifBlank { client.phone }
                                                        AngolaInvoicePrinter.sendInvoiceViaWhatsAppChat(
                                                            context = context,
                                                            invoice = inv,
                                                            phoneOverride = targetPhone
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .testTag("btn_client_invoice_whatsapp_${inv.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Chat,
                                                        contentDescription = "Enviar Fatura pelo WhatsApp",
                                                        tint = Emerald500,
                                                        modifier = Modifier.size(18.dp)
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
            }
        }
    }
}

@Composable
private fun EmptyTabNotice(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
