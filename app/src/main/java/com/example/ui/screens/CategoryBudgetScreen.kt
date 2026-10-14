package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryBudgetEntity
import com.example.data.model.AppCurrency
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.ui.theme.Emerald500
import com.example.ui.theme.FluxGoldRewards
import com.example.ui.theme.FluxGreenSuccess
import com.example.ui.theme.FluxPurplePrimary
import com.example.ui.theme.FluxRedExpense
import com.example.ui.theme.FluxWhite
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBudgetScreen(
    viewModel: MainViewModel,
    budgets: List<BudgetEntity>,
    currentCurrency: AppCurrency,
    onOpenBudgetEditor: () -> Unit = {},
    onEditExistingBudget: (BudgetEntity) -> Unit = {},
    onDeleteExistingBudget: (Long) -> Unit = {}
) {
    val categoryBudgets by viewModel.allCategoryBudgets.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Orçamento Mensal (Gastos), 1 = Orçamentos de Clientes (Propostas)
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<CategoryBudgetEntity?>(null) }

    // Calculate actual monthly expenses per category
    val currentMonthExpensesByCategory = remember(uiState.transactions) {
        val map = mutableMapOf<String, Double>()
        uiState.transactions
            .filter { it.type == com.example.data.model.TransactionType.EXPENSE }
            .forEach { tx ->
                map[tx.category] = (map[tx.category] ?: 0.0) + tx.amount
            }
        map
    }

    val totalBudgetLimit = categoryBudgets.sumOf { it.monthlyLimit }
    val totalActualSpent = categoryBudgets.sumOf { currentMonthExpensesByCategory[it.categoryName] ?: 0.0 }
    val totalProgress = if (totalBudgetLimit > 0.0) (totalActualSpent / totalBudgetLimit).toFloat().coerceIn(0f, 1.5f) else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Gestão de Orçamentos", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        budgetToEdit = null
                        showSetBudgetDialog = true
                    } else {
                        onOpenBudgetEditor()
                    }
                },
                containerColor = FluxPurplePrimary,
                contentColor = FluxWhite,
                modifier = Modifier.testTag("budget_screen_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = FluxPurplePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Orçamento Mensal", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Propostas de Clientes (${budgets.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (selectedTab == 0) {
                // Monthly Category Budgets Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Global Summary Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Orçamento Total do Mês",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    val percent = if (totalBudgetLimit > 0) ((totalActualSpent / totalBudgetLimit) * 100).toInt() else 0
                                    Text(
                                        text = "$percent%",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (percent > 100) FluxRedExpense else FluxPurplePrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Gasto: ${CostEngineeringCalculator.formatCurrency(totalActualSpent)}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Limite: ${CostEngineeringCalculator.formatCurrency(totalBudgetLimit)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = { totalProgress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp)),
                                    color = if (totalProgress > 1f) FluxRedExpense else FluxPurplePrimary,
                                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )

                                if (totalActualSpent > totalBudgetLimit && totalBudgetLimit > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "⚠️ Orçamento global do mês ultrapassado!",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section Title
                    item {
                        Text(
                            text = "Orçamento por Categoria",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Items for each category budget
                    if (categoryBudgets.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Nenhum limite de categoria configurado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showSetBudgetDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = FluxPurplePrimary, contentColor = FluxWhite)
                                    ) {
                                        Text("Definir Limite de Categoria")
                                    }
                                }
                            }
                        }
                    } else {
                        items(categoryBudgets, key = { it.id }) { catBudget ->
                            val spent = currentMonthExpensesByCategory[catBudget.categoryName] ?: 0.0
                            val limit = catBudget.monthlyLimit
                            val ratio = if (limit > 0) spent / limit else 0.0
                            val percent = (ratio * 100).toInt()

                            val statusColor = when {
                                percent >= 100 -> FluxRedExpense
                                percent >= 90 -> Color(0xFFF97316) // Orange
                                percent >= 75 -> FluxGoldRewards // Gold
                                percent >= 50 -> FluxPurplePrimary
                                else -> FluxGreenSuccess
                            }

                            val alertMessage = when {
                                percent > 100 -> "⚠️ O orçamento desta categoria foi ultrapassado."
                                percent == 100 -> "⚠️ 100% do orçamento utilizado! Limite atingido."
                                percent >= 90 -> "⚠️ 90% utilizado! Atenção máxima aos gastos."
                                percent >= 75 -> "ℹ️ 75% utilizado. Restam ${CostEngineeringCalculator.formatCurrency(limit - spent)}."
                                percent >= 50 -> "ℹ️ 50% utilizado da meta mensal."
                                else -> null
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = catBudget.categoryName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "$percent%",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = statusColor
                                            )
                                            IconButton(
                                                onClick = {
                                                    budgetToEdit = catBudget
                                                    showSetBudgetDialog = true
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteCategoryBudget(catBudget.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${CostEngineeringCalculator.formatCurrency(spent)} / ${CostEngineeringCalculator.formatCurrency(limit)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val remaining = limit - spent
                                        Text(
                                            text = if (remaining >= 0) "Resta: ${CostEngineeringCalculator.formatCurrency(remaining)}" else "Excesso: ${CostEngineeringCalculator.formatCurrency(-remaining)}",
                                            fontSize = 12.sp,
                                            color = if (remaining >= 0) Emerald500 else MaterialTheme.colorScheme.error
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LinearProgressIndicator(
                                        progress = { ratio.toFloat().coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = statusColor,
                                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )

                                    if (alertMessage != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = alertMessage,
                                            color = statusColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Client Technical Proposals Tab
                BudgetsListScreen(
                    budgets = budgets,
                    onCreateBudget = onOpenBudgetEditor,
                    onSelectBudget = onEditExistingBudget,
                    onDeleteBudget = onDeleteExistingBudget
                )
            }
        }
    }

    if (showSetBudgetDialog) {
        val expenseCategories = categories.filter { it.type == "EXPENSE" }
        var selectedCat by remember { mutableStateOf(budgetToEdit?.categoryName ?: expenseCategories.firstOrNull()?.name ?: "Material") }
        var limitInput by remember { mutableStateOf(budgetToEdit?.let { "%.0f".format(it.monthlyLimit) } ?: "100000") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSetBudgetDialog = false },
            title = { Text(if (budgetToEdit == null) "Definir Limite Mensal" else "Editar Limite") },
            text = {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCat,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria de Despesa") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            expenseCategories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedCat = c.name
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it },
                        label = { Text("Limite Mensal (${currentCurrency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limitVal = limitInput.toDoubleOrNull() ?: 0.0
                        if (selectedCat.isNotBlank() && limitVal > 0) {
                            viewModel.saveCategoryBudget(selectedCat, limitVal)
                            showSetBudgetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FluxPurplePrimary, contentColor = FluxWhite)
                ) {
                    Text("Salvar Limite")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetBudgetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
