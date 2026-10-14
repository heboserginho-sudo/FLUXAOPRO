package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.domain.calculator.CostEngineeringCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementDialog(
    initialTransaction: TransactionEntity? = null,
    accounts: List<AccountEntity> = emptyList(),
    projects: List<ProjectEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        type: TransactionType,
        amount: Double,
        description: String,
        category: String,
        dateMillis: Long,
        paymentMethod: PaymentMethod,
        status: TransactionStatus,
        notes: String,
        accountId: Long?,
        accountName: String,
        destinationAccountId: Long?,
        destinationAccountName: String,
        contactName: String,
        contactType: String,
        attachmentUri: String,
        projectId: Long?
    ) -> Unit
) {
    val isEditing = initialTransaction != null
    var selectedType by remember {
        mutableStateOf(initialTransaction?.type ?: TransactionType.INCOME)
    }

    val incomeCategories = listOf(
        "Venda", "Pagamento de cliente", "Sinal", "Serviço",
        "Empréstimo", "Investimento", "Outros"
    )

    val expenseCategories = listOf(
        "Material", "Fornecedor", "Transporte", "Salário",
        "Alimentação", "Renda", "Energia", "Água",
        "Internet", "Marketing", "Equipamento", "Manutenção",
        "Impostos", "Outros"
    )

    var amountText by remember {
        mutableStateOf(if (initialTransaction != null) initialTransaction.amount.toString() else "")
    }
    var descriptionText by remember {
        mutableStateOf(initialTransaction?.description ?: "")
    }
    var selectedCategory by remember(selectedType) {
        val defaultCat = when (selectedType) {
            TransactionType.INCOME -> incomeCategories.first()
            TransactionType.EXPENSE -> expenseCategories.first()
            TransactionType.TRANSFER -> "Transferência Interna"
        }
        mutableStateOf(initialTransaction?.category ?: defaultCat)
    }

    var dateMillis by remember {
        mutableLongStateOf(initialTransaction?.dateEpochMillis ?: System.currentTimeMillis())
    }
    var showDatePicker by remember { mutableStateOf(false) }

    // Accounts
    var selectedAccountId by remember {
        val defaultId = initialTransaction?.accountId ?: accounts.firstOrNull()?.id
        mutableStateOf(defaultId)
    }
    var selectedDestinationAccountId by remember {
        val destId = initialTransaction?.destinationAccountId ?: accounts.getOrNull(1)?.id
        mutableStateOf(destId)
    }

    var contactName by remember {
        mutableStateOf(initialTransaction?.contactName ?: "")
    }
    var selectedMethod by remember {
        mutableStateOf(initialTransaction?.paymentMethod ?: PaymentMethod.MULTICAIXA_EXPRESS)
    }
    var selectedStatus by remember {
        mutableStateOf(initialTransaction?.status ?: TransactionStatus.PAID)
    }
    var notesText by remember {
        mutableStateOf(initialTransaction?.notes ?: "")
    }
    var attachmentName by remember {
        mutableStateOf(initialTransaction?.attachmentUri ?: "")
    }
    var selectedProjectId by remember {
        mutableStateOf(initialTransaction?.projectId)
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var destAccountDropdownExpanded by remember { mutableStateOf(false) }
    var paymentDropdownExpanded by remember { mutableStateOf(false) }
    var projectDropdownExpanded by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .testTag("dialog_movement"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "Editar Movimento" else "+ Novo movimento",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Controle financeiro e fluxo de caixa",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Type selector
                    Text(
                        text = "Tipo de Movimento *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Entrada
                        Button(
                            onClick = {
                                selectedType = TransactionType.INCOME
                                selectedCategory = incomeCategories.first()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == TransactionType.INCOME)
                                    Color(0xFF16A34A) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selectedType == TransactionType.INCOME)
                                    Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Entrada", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }

                        // Saída
                        Button(
                            onClick = {
                                selectedType = TransactionType.EXPENSE
                                selectedCategory = expenseCategories.first()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == TransactionType.EXPENSE)
                                    Color(0xFFDC2626) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selectedType == TransactionType.EXPENSE)
                                    Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Saída", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }

                        // Transferência
                        Button(
                            onClick = {
                                selectedType = TransactionType.TRANSFER
                                selectedCategory = "Transferência Interna"
                            },
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == TransactionType.TRANSFER)
                                    Color(0xFF2563EB) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selectedType == TransactionType.TRANSFER)
                                    Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Transferência", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (selectedType == TransactionType.TRANSFER) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = "Transferências internas movem fundos entre contas e não contam como receita ou despesa.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1D4ED8),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Valor
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.replace(",", ".") },
                        label = { Text("Valor (${CostEngineeringCalculator.currentCurrency.symbol}) *") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_movement_amount"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Descrição
                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        label = { Text("Descrição *") },
                        placeholder = { Text(if (selectedType == TransactionType.TRANSFER) "Ex: Transferência de reforço de caixa" else "Ex: Pagamento de cliente Silva") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_movement_desc"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Categoria (se não for transferência)
                    if (selectedType != TransactionType.TRANSFER) {
                        val currentCategories = if (selectedType == TransactionType.INCOME) incomeCategories else expenseCategories

                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoria *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                currentCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Data
                    OutlinedTextField(
                        value = dateFormatter.format(Date(dateMillis)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Data do Movimento") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar Data")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Conta (Origem / Principal)
                    val selectedAccount = accounts.find { it.id == selectedAccountId }
                    ExposedDropdownMenuBox(
                        expanded = accountDropdownExpanded,
                        onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedAccount?.name ?: "Selecionar conta",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (selectedType == TransactionType.TRANSFER) "Conta de Origem *" else "Conta Bancária / Caixa *") },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = accountDropdownExpanded,
                            onDismissRequest = { accountDropdownExpanded = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(acc.name, fontWeight = FontWeight.Medium)
                                            Text(
                                                CostEngineeringCalculator.formatCurrency(acc.currentBalance),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedAccountId = acc.id
                                        accountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Conta Destino (Apenas Transferência)
                    if (selectedType == TransactionType.TRANSFER) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val destAccount = accounts.find { it.id == selectedDestinationAccountId }
                        ExposedDropdownMenuBox(
                            expanded = destAccountDropdownExpanded,
                            onExpandedChange = { destAccountDropdownExpanded = !destAccountDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = destAccount?.name ?: "Selecionar conta destino",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Conta de Destino *") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destAccountDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = destAccountDropdownExpanded,
                                onDismissRequest = { destAccountDropdownExpanded = false }
                            ) {
                                accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(acc.name, fontWeight = FontWeight.Medium)
                                                Text(
                                                    CostEngineeringCalculator.formatCurrency(acc.currentBalance),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedDestinationAccountId = acc.id
                                            destAccountDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Cliente / Fornecedor (se não for transferência)
                    if (selectedType != TransactionType.TRANSFER) {
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text(if (selectedType == TransactionType.INCOME) "Cliente (Opcional)" else "Fornecedor / Beneficiário (Opcional)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            placeholder = { Text("Nome do contacto") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Método de Pagamento
                    ExposedDropdownMenuBox(
                        expanded = paymentDropdownExpanded,
                        onExpandedChange = { paymentDropdownExpanded = !paymentDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMethod.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Método de Pagamento") },
                            leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = paymentDropdownExpanded,
                            onDismissRequest = { paymentDropdownExpanded = false }
                        ) {
                            PaymentMethod.values().forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method.label) },
                                    onClick = {
                                        selectedMethod = method
                                        paymentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status (Pago / Pendente)
                    Text(
                        text = "Status da Movimentação",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = selectedStatus == TransactionStatus.PAID,
                            onClick = { selectedStatus = TransactionStatus.PAID },
                            label = { Text("Pago / Efetuado") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD1FAE5),
                                selectedLabelColor = Color(0xFF065F46)
                            )
                        )
                        FilterChip(
                            selected = selectedStatus == TransactionStatus.PENDING,
                            onClick = { selectedStatus = TransactionStatus.PENDING },
                            label = { Text("Pendente / A Liquidar") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEF3C7),
                                selectedLabelColor = Color(0xFF92400E)
                            )
                        )
                    }

                    // Projeto (Opcional)
                    if (projects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val selectedProject = projects.find { it.id == selectedProjectId }
                        ExposedDropdownMenuBox(
                            expanded = projectDropdownExpanded,
                            onExpandedChange = { projectDropdownExpanded = !projectDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedProject?.name ?: "Nenhum projeto vinculado",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Vincular a Projeto (Opcional)") },
                                leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = projectDropdownExpanded,
                                onDismissRequest = { projectDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Nenhum projeto") },
                                    onClick = {
                                        selectedProjectId = null
                                        projectDropdownExpanded = false
                                    }
                                )
                                projects.forEach { proj ->
                                    DropdownMenuItem(
                                        text = { Text(proj.name) },
                                        onClick = {
                                            selectedProjectId = proj.id
                                            projectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Observação
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Observação / Detalhes") },
                        placeholder = { Text("Notas internas ou referência do comprovativo") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Anexo
                    OutlinedTextField(
                        value = attachmentName,
                        onValueChange = { attachmentName = it },
                        label = { Text("Anexo / Nome do Comprovativo") },
                        placeholder = { Text("Ex: comprovativo_mcx_102.pdf") },
                        leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    val canSave = (amountText.toDoubleOrNull() ?: 0.0) > 0.0 && descriptionText.isNotBlank()
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            val accName = accounts.find { it.id == selectedAccountId }?.name ?: "Dinheiro / Caixa"
                            val destAccName = if (selectedType == TransactionType.TRANSFER) {
                                accounts.find { it.id == selectedDestinationAccountId }?.name ?: ""
                            } else ""

                            val contactType = if (selectedType == TransactionType.INCOME) "CLIENT" else "SUPPLIER"

                            onSave(
                                selectedType,
                                amount,
                                descriptionText.trim(),
                                selectedCategory,
                                dateMillis,
                                selectedMethod,
                                selectedStatus,
                                notesText.trim(),
                                selectedAccountId,
                                accName,
                                selectedDestinationAccountId,
                                destAccName,
                                contactName.trim(),
                                contactType,
                                attachmentName.trim(),
                                selectedProjectId
                            )
                        },
                        enabled = canSave,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_save_movement")
                    ) {
                        Text(if (isEditing) "Salvar Alterações" else "Registar Movimento", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
