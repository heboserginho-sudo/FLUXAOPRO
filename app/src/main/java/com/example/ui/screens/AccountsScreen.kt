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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.TransactionType
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val cashFlowState by viewModel.uiState.collectAsStateWithLifecycle()

    var showNewAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<AccountEntity?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var selectedAccountForHistory by remember { mutableStateOf<AccountEntity?>(null) }

    val totalBalance = accounts.filter { it.isActive }.sumOf { it.currentBalance }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Contas Financeiras",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showTransferDialog = true },
                        modifier = Modifier.testTag("btn_transfer_top")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Transferência")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewAccountDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_account")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Conta")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Consolidated Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "SALDO TOTAL CONSOLIDADO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = CostEngineeringCalculator.formatCurrency(totalBalance),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showTransferDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Transferir", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { showNewAccountDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Nova Conta", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Filters or Preset suggestions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suas Contas (${accounts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${accounts.count { it.isActive }} ativas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Accounts List
            items(accounts, key = { it.id }) { account ->
                AccountCard(
                    account = account,
                    onEdit = { accountToEdit = account },
                    onDelete = { viewModel.deleteAccount(account) },
                    onViewHistory = { selectedAccountForHistory = account }
                )
            }

            if (accounts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nenhuma conta cadastrada",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Toque no botão + para adicionar dinheiro, BAI, BFA, Express ou carteiras móveis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // New / Edit Account Dialog
    if (showNewAccountDialog || accountToEdit != null) {
        AccountEditDialog(
            account = accountToEdit,
            onDismiss = {
                showNewAccountDialog = false
                accountToEdit = null
            },
            onSave = { savedAccount ->
                viewModel.saveAccount(savedAccount)
                showNewAccountDialog = false
                accountToEdit = null
            }
        )
    }

    // Transfer Between Accounts Dialog
    if (showTransferDialog) {
        AccountTransferDialog(
            accounts = accounts.filter { it.isActive },
            onDismiss = { showTransferDialog = false },
            onTransfer = { src, dst, amount, desc, notes ->
                viewModel.transferBetweenAccounts(
                    sourceAccount = src,
                    destinationAccount = dst,
                    amount = amount,
                    description = desc,
                    notes = notes
                )
                showTransferDialog = false
            }
        )
    }

    // Account Movement History Dialog
    selectedAccountForHistory?.let { account ->
        AccountHistoryDialog(
            account = account,
            allTransactions = cashFlowState.transactions,
            onDismiss = { selectedAccountForHistory = null }
        )
    }
}

@Composable
fun AccountCard(
    account: AccountEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewHistory: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val icon = when (account.accountType) {
        AccountType.CASH -> Icons.Default.Payments
        AccountType.BANK -> Icons.Default.AccountBalance
        AccountType.WALLET, AccountType.MULTICAIXA_EXPRESS -> Icons.Default.PhoneAndroid
        else -> Icons.Default.AccountBalance
    }

    val cardColor = remember(account.colorHex) {
        Color(account.colorHex)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewHistory() }
            .testTag("account_card_${account.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(cardColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = cardColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = account.accountType.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opções")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ver Histórico") },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onViewHistory()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Editar Conta") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Saldo Atual",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CostEngineeringCalculator.formatCurrency(account.currentBalance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (account.currentBalance >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (account.isActive) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (account.isActive) "Ativa" else "Inativa",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (account.isActive) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (account.accountNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ref: ${account.accountNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditDialog(
    account: AccountEntity? = null,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    val isEditing = account != null
    var name by remember { mutableStateOf(account?.name ?: "") }
    var selectedType by remember { mutableStateOf(account?.accountType ?: AccountType.BANK) }
    var initialBalanceText by remember {
        mutableStateOf(if (account != null) account.initialBalance.toString() else "0.00")
    }
    var currentBalanceText by remember {
        mutableStateOf(if (account != null) account.currentBalance.toString() else "0.00")
    }
    var accountNumber by remember { mutableStateOf(account?.accountNumberOrIban ?: "") }
    var isActive by remember { mutableStateOf(account?.isActive ?: true) }
    var selectedColor by remember { mutableStateOf(account?.colorHex ?: 0xFF0055A5L) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val presetColors = listOf(
        0xFF0055A5L, // BAI
        0xFFF37021L, // BFA
        0xFF10B981L, // MCX
        0xFFDC2626L, // Unitel
        0xFFF59E0BL, // Afrimoney
        0xFF334155L, // Caixa
        0xFF8B5CF6L  // Outros
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Editar Conta" else "Nova Conta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nome
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Conta *") },
                    placeholder = { Text("Ex: BAI Directo, Caixa Geral, Multicaixa") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tipo
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Conta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        AccountType.values().forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.label) },
                                onClick = {
                                    selectedType = t
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Saldo Inicial
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = {
                        initialBalanceText = it.replace(",", ".")
                        if (!isEditing) currentBalanceText = initialBalanceText
                    },
                    label = { Text("Saldo Inicial (${CostEngineeringCalculator.currentCurrency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (isEditing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = currentBalanceText,
                        onValueChange = { currentBalanceText = it.replace(",", ".") },
                        label = { Text("Saldo Atual (${CostEngineeringCalculator.currentCurrency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // IBAN / Número
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("IBAN / Número de Conta (Opcional)") },
                    placeholder = { Text("Ex: AO06.0040.0000...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Preset Colors
                Text("Cor do Cartão", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { hexLong ->
                        val parsedColor = Color(hexLong)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .clickable { selectedColor = hexLong },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hexLong) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ativa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Conta Ativa", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val initBal = initialBalanceText.toDoubleOrNull() ?: 0.0
                            val currBal = currentBalanceText.toDoubleOrNull() ?: initBal
                            val entity = account?.copy(
                                name = name.trim(),
                                type = selectedType.name,
                                initialBalance = initBal,
                                currentBalance = currBal,
                                accountNumberOrIban = accountNumber.trim(),
                                colorHex = selectedColor,
                                status = if (isActive) "ACTIVE" else "INACTIVE"
                            ) ?: AccountEntity(
                                name = name.trim(),
                                type = selectedType.name,
                                initialBalance = initBal,
                                currentBalance = currBal,
                                accountNumberOrIban = accountNumber.trim(),
                                colorHex = selectedColor,
                                status = if (isActive) "ACTIVE" else "INACTIVE"
                            )
                            onSave(entity)
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransferDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onTransfer: (source: AccountEntity, dest: AccountEntity, amount: Double, desc: String, notes: String) -> Unit
) {
    if (accounts.size < 2) {
        Dialog(onDismissRequest = onDismiss) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Contas Insuficientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Você precisa de pelo menos duas contas ativas para realizar uma transferência interna.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss) { Text("OK") }
                }
            }
        }
        return
    }

    var sourceAccount by remember { mutableStateOf(accounts.first()) }
    var destAccount by remember { mutableStateOf(accounts.getOrElse(1) { accounts.first() }) }
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("Transferência interna") }
    var notesText by remember { mutableStateOf("") }

    var srcDropdownExpanded by remember { mutableStateOf(false) }
    var dstDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Transferência entre Contas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Não gera receita nem despesa operacional",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Conta Origem
                ExposedDropdownMenuBox(
                    expanded = srcDropdownExpanded,
                    onExpandedChange = { srcDropdownExpanded = !srcDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${sourceAccount.name} (${CostEngineeringCalculator.formatCurrency(sourceAccount.currentBalance)})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Conta de Origem (Debitar)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = srcDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = srcDropdownExpanded,
                        onDismissRequest = { srcDropdownExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} - ${CostEngineeringCalculator.formatCurrency(acc.currentBalance)}") },
                                onClick = {
                                    sourceAccount = acc
                                    srcDropdownExpanded = false
                                    if (destAccount.id == acc.id) {
                                        destAccount = accounts.first { it.id != acc.id }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Conta Destino
                ExposedDropdownMenuBox(
                    expanded = dstDropdownExpanded,
                    onExpandedChange = { dstDropdownExpanded = !dstDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${destAccount.name} (${CostEngineeringCalculator.formatCurrency(destAccount.currentBalance)})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Conta de Destino (Creditar)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dstDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = dstDropdownExpanded,
                        onDismissRequest = { dstDropdownExpanded = false }
                    ) {
                        accounts.filter { it.id != sourceAccount.id }.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} - ${CostEngineeringCalculator.formatCurrency(acc.currentBalance)}") },
                                onClick = {
                                    destAccount = acc
                                    dstDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Valor
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.replace(",", ".") },
                    label = { Text("Valor a Transferir (${CostEngineeringCalculator.currentCurrency.symbol}) *") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Descrição
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Descrição da Transferência") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Observações
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Observação (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val canTransfer = amount > 0.0 && sourceAccount.id != destAccount.id
                    Button(
                        onClick = {
                            onTransfer(sourceAccount, destAccount, amount, descriptionText.trim(), notesText.trim())
                        },
                        enabled = canTransfer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar Transferência", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AccountHistoryDialog(
    account: AccountEntity,
    allTransactions: List<TransactionEntity>,
    onDismiss: () -> Unit
) {
    val accountTransactions = remember(account.id, allTransactions) {
        allTransactions.filter {
            it.accountId == account.id || it.destinationAccountId == account.id
        }.sortedByDescending { it.dateEpochMillis }
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(600.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Extrato: ${account.name}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Saldo atual: ${CostEngineeringCalculator.formatCurrency(account.currentBalance)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (accountTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma movimentação registada nesta conta.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(accountTransactions, key = { it.id }) { tx ->
                            val isIncome = tx.type == TransactionType.INCOME || (tx.type == TransactionType.TRANSFER && tx.destinationAccountId == account.id)
                            val isTransfer = tx.type == TransactionType.TRANSFER

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isTransfer) Color(0xFFEFF6FF)
                                                    else if (isIncome) Color(0xFFD1FAE5)
                                                    else Color(0xFFFEE2E2)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isTransfer) Icons.Default.SwapHoriz
                                                else if (isIncome) Icons.AutoMirrored.Filled.TrendingUp
                                                else Icons.AutoMirrored.Filled.TrendingDown,
                                                contentDescription = null,
                                                tint = if (isTransfer) Color(0xFF1D4ED8)
                                                else if (isIncome) Color(0xFF047857)
                                                else Color(0xFFB91C1C),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = tx.description,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${tx.category} • ${dateFormatter.format(Date(tx.dateEpochMillis))}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${if (isIncome) "+" else "-"} ${CostEngineeringCalculator.formatCurrency(tx.amount)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTransfer) Color(0xFF1D4ED8)
                                        else if (isIncome) Color(0xFF047857)
                                        else Color(0xFFB91C1C)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Fechar")
                }
            }
        }
    }
}
