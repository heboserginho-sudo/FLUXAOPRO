package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.FinancialCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.ui.components.MovementDialog
import com.example.ui.viewmodel.CashFlowUiState
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PeriodFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashFlowScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()

    var showMovementDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedAccountFilterId by remember { mutableStateOf<Long?>(null) }

    val transactionsToDisplay = remember(uiState.filteredTransactions, selectedAccountFilterId) {
        if (selectedAccountFilterId == null) {
            uiState.filteredTransactions
        } else {
            uiState.filteredTransactions.filter {
                it.accountId == selectedAccountFilterId || it.destinationAccountId == selectedAccountFilterId
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("cash_flow_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Movimentos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Entradas, saídas e transferências",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            transactionToEdit = null
                            showMovementDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("btn_new_movement_top")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Novo movimento", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    transactionToEdit = null
                    showMovementDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("fab_add_transaction")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "+ Novo movimento")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar & Filter Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_transaction"),
                    placeholder = { Text("Buscar por descrição, cliente, conta ou categoria...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar busca")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Filters Row (Period & Type)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type Filters
                    FilterChip(
                        selected = uiState.typeFilter == null,
                        onClick = { viewModel.setTypeFilter(null) },
                        label = { Text("Todos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    FilterChip(
                        selected = uiState.typeFilter == TransactionType.INCOME,
                        onClick = { viewModel.setTypeFilter(TransactionType.INCOME) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Entradas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD1FAE5),
                            selectedLabelColor = Color(0xFF065F46)
                        )
                    )

                    FilterChip(
                        selected = uiState.typeFilter == TransactionType.EXPENSE,
                        onClick = { viewModel.setTypeFilter(TransactionType.EXPENSE) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Saídas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEE2E2),
                            selectedLabelColor = Color(0xFF991B1B)
                        )
                    )

                    FilterChip(
                        selected = uiState.typeFilter == TransactionType.TRANSFER,
                        onClick = { viewModel.setTypeFilter(TransactionType.TRANSFER) },
                        leadingIcon = {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Transferências") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEFF6FF),
                            selectedLabelColor = Color(0xFF1D4ED8)
                        )
                    )

                    FilterChip(
                        selected = uiState.statusFilter == TransactionStatus.PENDING,
                        onClick = {
                            viewModel.setStatusFilter(
                                if (uiState.statusFilter == TransactionStatus.PENDING) null else TransactionStatus.PENDING
                            )
                        },
                        label = { Text("Pendentes") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEF3C7),
                            selectedLabelColor = Color(0xFF92400E)
                        )
                    )

                    // Period filters
                    PeriodFilter.values().forEach { filter ->
                        FilterChip(
                            selected = uiState.periodFilter == filter,
                            onClick = { viewModel.setPeriodFilter(filter) },
                            label = { Text(filter.label) }
                        )
                    }
                }

                // Account Filter Row (if accounts available)
                if (accounts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Conta:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )

                        FilterChip(
                            selected = selectedAccountFilterId == null,
                            onClick = { selectedAccountFilterId = null },
                            label = { Text("Todas", fontSize = 12.sp) }
                        )

                        accounts.forEach { acc ->
                            FilterChip(
                                selected = selectedAccountFilterId == acc.id,
                                onClick = {
                                    selectedAccountFilterId = if (selectedAccountFilterId == acc.id) null else acc.id
                                },
                                label = { Text(acc.name, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Financial Summary Bar for filtered view
            val filteredIncome = transactionsToDisplay
                .filter { it.type == TransactionType.INCOME && it.status == TransactionStatus.PAID }
                .sumOf { it.amount }
            val filteredExpense = transactionsToDisplay
                .filter { it.type == TransactionType.EXPENSE && it.status == TransactionStatus.PAID }
                .sumOf { it.amount }
            val filteredBalance = filteredIncome - filteredExpense

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Entradas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CostEngineeringCalculator.formatCurrency(filteredIncome),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }

                    Column {
                        Text(
                            text = "Saídas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CostEngineeringCalculator.formatCurrency(filteredExpense),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Saldo Operacional",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CostEngineeringCalculator.formatCurrency(filteredBalance),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (filteredBalance >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                    }
                }
            }

            // Transactions List
            if (transactionsToDisplay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhum movimento encontrado.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Clique em '+ Novo movimento' para registar entradas, saídas ou transferências.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                transactionToEdit = null
                                showMovementDialog = true
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Novo movimento")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactionsToDisplay, key = { it.id }) { tx ->
                        DetailedTransactionItem(
                            transaction = tx,
                            onToggleStatus = { viewModel.toggleTransactionStatus(tx) },
                            onEdit = {
                                transactionToEdit = tx
                                showMovementDialog = true
                            },
                            onDuplicate = { viewModel.duplicateTransaction(tx) },
                            onDelete = { viewModel.deleteTransaction(tx.id) }
                        )
                    }
                }
            }
        }
    }

    // Full Movement Dialog (Novo / Editar)
    if (showMovementDialog) {
        MovementDialog(
            initialTransaction = transactionToEdit,
            accounts = accounts,
            projects = projects,
            onDismiss = {
                showMovementDialog = false
                transactionToEdit = null
            },
            onSave = { type, amount, desc, cat, date, method, status, notes, accId, accName, dstId, dstName, contact, contactType, attach, projId ->
                if (transactionToEdit != null) {
                    val updated = transactionToEdit!!.copy(
                        type = type,
                        amount = amount,
                        description = desc,
                        category = cat,
                        dateEpochMillis = date,
                        paymentMethod = method,
                        status = status,
                        notes = notes,
                        accountId = accId,
                        accountName = accName,
                        destinationAccountId = dstId,
                        destinationAccountName = dstName,
                        contactName = contact,
                        contactType = contactType,
                        attachmentUri = attach,
                        projectId = projId
                    )
                    viewModel.updateTransaction(updated)
                } else {
                    viewModel.addTransaction(
                        type = type,
                        amount = amount,
                        description = desc,
                        category = cat,
                        dateMillis = date,
                        paymentMethod = method,
                        status = status,
                        notes = notes,
                        accountId = accId,
                        accountName = accName,
                        destinationAccountId = dstId,
                        destinationAccountName = dstName,
                        contactName = contact,
                        contactType = contactType,
                        attachmentUri = attach,
                        projectId = projId
                    )
                }
                showMovementDialog = false
                transactionToEdit = null
            }
        )
    }
}

// Backward-compatible overload for existing calls
@Composable
fun CashFlowScreen(
    uiState: CashFlowUiState,
    onPeriodFilterChanged: (PeriodFilter) -> Unit,
    onTypeFilterChanged: (TransactionType?) -> Unit,
    onStatusFilterChanged: (TransactionStatus?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleTransactionStatus: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onAddTransaction: (
        type: TransactionType,
        amount: Double,
        description: String,
        category: String,
        dateMillis: Long,
        paymentMethod: PaymentMethod,
        status: TransactionStatus
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("cash_flow_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("fab_add_transaction")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "+ Novo movimento")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_transaction"),
                    placeholder = { Text("Buscar por descrição ou categoria...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar busca")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PeriodFilter.values().forEach { filter ->
                        FilterChip(
                            selected = uiState.periodFilter == filter,
                            onClick = { onPeriodFilterChanged(filter) },
                            label = { Text(filter.label) }
                        )
                    }

                    FilterChip(
                        selected = uiState.typeFilter == null,
                        onClick = { onTypeFilterChanged(null) },
                        label = { Text("Todos") }
                    )
                    FilterChip(
                        selected = uiState.typeFilter == TransactionType.INCOME,
                        onClick = { onTypeFilterChanged(TransactionType.INCOME) },
                        label = { Text("Receitas") }
                    )
                    FilterChip(
                        selected = uiState.typeFilter == TransactionType.EXPENSE,
                        onClick = { onTypeFilterChanged(TransactionType.EXPENSE) },
                        label = { Text("Despesas") }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.filteredTransactions, key = { it.id }) { tx ->
                    DetailedTransactionItem(
                        transaction = tx,
                        onToggleStatus = { onToggleTransactionStatus(tx) },
                        onEdit = {},
                        onDelete = { onDeleteTransaction(tx.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        MovementDialog(
            onDismiss = { showAddDialog = false },
            onSave = { type, amount, desc, cat, date, method, status, _, _, _, _, _, _, _, _, _ ->
                onAddTransaction(type, amount, desc, cat, date, method, status)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DetailedTransactionItem(
    transaction: TransactionEntity,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit = {},
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val isTransfer = transaction.type == TransactionType.TRANSFER

    val amountColor = when {
        isTransfer -> Color(0xFF1D4ED8)
        isIncome -> Color(0xFF16A34A)
        else -> Color(0xFFDC2626)
    }

    val amountPrefix = when {
        isTransfer -> "⇄ "
        isIncome -> "+ "
        else -> "- "
    }

    val icon = when {
        isTransfer -> Icons.Default.SwapHoriz
        isIncome -> Icons.AutoMirrored.Filled.TrendingUp
        else -> Icons.AutoMirrored.Filled.TrendingDown
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateText = remember(transaction.dateEpochMillis) {
        dateFormatter.format(Date(transaction.dateEpochMillis))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(amountColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = amountColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${transaction.category} • $dateText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$amountPrefix${CostEngineeringCalculator.formatCurrency(transaction.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (transaction.status == TransactionStatus.PAID) {
                            Color(0xFFD1FAE5)
                        } else {
                            Color(0xFFFEF3C7)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onToggleStatus() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (transaction.status == TransactionStatus.PAID) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (transaction.status == TransactionStatus.PAID) Color(0xFF065F46) else Color(0xFF92400E),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = transaction.status.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (transaction.status == TransactionStatus.PAID) Color(0xFF065F46) else Color(0xFF92400E)
                            )
                        }
                    }
                }
            }

            // Details and Action row
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badges row
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (transaction.accountName.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = transaction.accountName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (transaction.contactName.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = transaction.contactName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (transaction.attachmentUri.isNotBlank()) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Tem anexo",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Edit, Duplicate and Delete icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicar",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Movimento",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Retained for backward compatibility
@Composable
fun AddTransactionSheetContent(
    onDismiss: () -> Unit,
    onSave: (
        type: TransactionType,
        amount: Double,
        description: String,
        category: String,
        paymentMethod: PaymentMethod,
        status: TransactionStatus
    ) -> Unit
) {
    MovementDialog(
        onDismiss = onDismiss,
        onSave = { type, amount, desc, cat, _, method, status, _, _, _, _, _, _, _, _, _ ->
            onSave(type, amount, desc, cat, method, status)
        }
    )
}
