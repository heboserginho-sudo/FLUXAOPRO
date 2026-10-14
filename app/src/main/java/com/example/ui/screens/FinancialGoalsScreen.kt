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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.FinancialGoalEntity
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.ui.theme.Emerald500
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.viewmodel.MainViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialGoalsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<FinancialGoalEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metas Financeiras", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("goals_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    goalToEdit = null
                    showAddEditDialog = true
                },
                containerColor = FluxGoldPrimary,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_goal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Meta")
            }
        }
    ) { padding ->
        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = FluxGoldPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Nenhuma meta cadastrada", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Defina metas financeiras de lucro, vendas ou reserva de emergência para acompanhar o crescimento do seu negócio.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showAddEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FluxGoldPrimary, contentColor = Color.Black)
                    ) {
                        Text("Criar Primeira Meta")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onEdit = {
                            goalToEdit = goal
                            showAddEditDialog = true
                        },
                        onDelete = {
                            viewModel.deleteGoal(goal)
                        }
                    )
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditGoalDialog(
            initialGoal = goalToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { saved ->
                viewModel.saveGoal(saved)
                showAddEditDialog = false
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: FinancialGoalEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = goal.progressPercent
    val now = System.currentTimeMillis()
    val diffMillis = goal.deadlineEpochMillis - now
    val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis).coerceAtLeast(0)

    val (statusLabel, statusColor) = when (goal.status) {
        "REACHED" -> "Atingida" to Emerald500
        "AT_RISK" -> "Em Risco" to MaterialTheme.colorScheme.error
        "CANCELLED" -> "Cancelada" to MaterialTheme.colorScheme.onSurfaceVariant
        else -> "Em Andamento" to FluxGoldPrimary
    }

    val typeIcon = when (goal.type) {
        "PROFIT" -> Icons.Default.TrendingUp
        "SALES" -> Icons.Default.Flag
        "EXPENSE_REDUCTION" -> Icons.Default.TrendingDown
        "EMERGENCY_RESERVE" -> Icons.Default.Savings
        else -> Icons.Default.Flag
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FluxGoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = typeIcon, contentDescription = null, tint = FluxGoldPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = goal.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = if (daysRemaining > 0) "$daysRemaining dias restantes" else "Prazo encerrado",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = statusLabel, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = goal.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Atual: ${CostEngineeringCalculator.formatCurrency(goal.currentAmount)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Alvo: ${CostEngineeringCalculator.formatCurrency(goal.targetAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (progress / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progress >= 100) Emerald500 else FluxGoldPrimary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${"%.1f".format(progress)}% alcançado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (progress >= 100) Emerald500 else FluxGoldPrimary
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalDialog(
    initialGoal: FinancialGoalEntity?,
    onDismiss: () -> Unit,
    onSave: (FinancialGoalEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialGoal?.name ?: "") }
    var description by remember { mutableStateOf(initialGoal?.description ?: "") }
    var targetInput by remember { mutableStateOf(initialGoal?.let { "%.0f".format(it.targetAmount) } ?: "500000") }
    var currentInput by remember { mutableStateOf(initialGoal?.let { "%.0f".format(it.currentAmount) } ?: "0") }
    var selectedType by remember { mutableStateOf(initialGoal?.type ?: "PROFIT") }
    var selectedStatus by remember { mutableStateOf(initialGoal?.status ?: "ACTIVE") }

    val goalTypes = listOf(
        "PROFIT" to "Meta de Lucro",
        "SALES" to "Meta de Vendas",
        "EXPENSE_REDUCTION" to "Redução de Despesas",
        "EMERGENCY_RESERVE" to "Reserva de Emergência",
        "CUSTOM" to "Personalizada"
    )

    val statuses = listOf(
        "ACTIVE" to "Em Andamento",
        "REACHED" to "Atingida",
        "AT_RISK" to "Em Risco",
        "CANCELLED" to "Cancelada"
    )

    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGoal == null) "Nova Meta Financeira" else "Editar Meta") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Meta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = goalTypes.find { it.first == selectedType }?.second ?: selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Meta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        goalTypes.forEach { (typeKey, typeLabel) ->
                            DropdownMenuItem(
                                text = { Text(typeLabel) },
                                onClick = {
                                    selectedType = typeKey
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { targetInput = it },
                        label = { Text("Valor Alvo (Kz)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { currentInput = it },
                        label = { Text("Valor Atual (Kz)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = statuses.find { it.first == selectedStatus }?.second ?: selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado da Meta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        statuses.forEach { (statKey, statLabel) ->
                            DropdownMenuItem(
                                text = { Text(statLabel) },
                                onClick = {
                                    selectedStatus = statKey
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Observação / Descrição") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetVal = targetInput.toDoubleOrNull() ?: 0.0
                    val currentVal = currentInput.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && targetVal > 0) {
                        val goal = (initialGoal ?: FinancialGoalEntity(
                            name = name.trim(),
                            targetAmount = targetVal,
                            currentAmount = currentVal,
                            type = selectedType,
                            status = selectedStatus,
                            description = description.trim()
                        )).copy(
                            name = name.trim(),
                            targetAmount = targetVal,
                            currentAmount = currentVal,
                            type = selectedType,
                            status = selectedStatus,
                            description = description.trim()
                        )
                        onSave(goal)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FluxGoldPrimary, contentColor = Color.Black)
            ) {
                Text("Salvar Meta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
