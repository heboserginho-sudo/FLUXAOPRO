package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.TransactionType
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.ui.theme.Emerald500
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val clients by viewModel.allClients.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var statusFilter by remember { mutableStateOf<String?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var projectToEdit by remember { mutableStateOf<ProjectEntity?>(null) }
    var selectedProjectForDetails by remember { mutableStateOf<ProjectEntity?>(null) }

    val projectStatuses = listOf(
        "Orçamento" to Color(0xFF94A3B8),
        "Aprovado" to Color(0xFF3B82F6),
        "Em produção" to FluxGoldPrimary,
        "Em montagem" to Color(0xFFF97316),
        "Concluído" to Emerald500,
        "Cancelado" to MaterialTheme.colorScheme.error
    )

    val filteredProjects = if (statusFilter == null) projects else projects.filter { it.status == statusFilter }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projetos & Obras", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("projects_back_btn")) {
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
                    projectToEdit = null
                    showAddEditDialog = true
                },
                containerColor = FluxGoldPrimary,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_project_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Projeto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("Todos (${projects.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FluxGoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
                projectStatuses.take(3).forEach { (statusName, _) ->
                    FilterChip(
                        selected = statusFilter == statusName,
                        onClick = { statusFilter = if (statusFilter == statusName) null else statusName },
                        label = { Text(statusName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FluxGoldPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            if (filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = FluxGoldPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Nenhum projeto cadastrado", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Cadastre projetos para calcular custos, receitas, lucros e margens automaticamente com base no fluxo de caixa.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddEditDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FluxGoldPrimary, contentColor = Color.Black)
                        ) {
                            Text("Criar Primeiro Projeto")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredProjects, key = { it.id }) { proj ->
                        // Calculate live financial figures for this project
                        val projectTxs = uiState.transactions.filter { it.projectId == proj.id }
                        val costs = projectTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                        val actualRevenue = projectTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                        val revenue = if (actualRevenue > 0) actualRevenue else proj.budgetedRevenue
                        val profit = revenue - costs
                        val margin = if (revenue > 0) (profit / revenue) * 100 else 0.0

                        ProjectCard(
                            project = proj,
                            costs = costs,
                            revenue = revenue,
                            profit = profit,
                            margin = margin,
                            onClick = { selectedProjectForDetails = proj },
                            onEdit = {
                                projectToEdit = proj
                                showAddEditDialog = true
                            },
                            onDelete = { viewModel.deleteProject(proj) }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditProjectDialog(
            initialProject = projectToEdit,
            clients = clients,
            onDismiss = { showAddEditDialog = false },
            onSave = { saved ->
                viewModel.saveProject(saved)
                showAddEditDialog = false
            }
        )
    }

    if (selectedProjectForDetails != null) {
        val proj = selectedProjectForDetails!!
        val projectTxs = uiState.transactions.filter { it.projectId == proj.id }
        ProjectDetailsDialog(
            project = proj,
            transactions = projectTxs,
            onDismiss = { selectedProjectForDetails = null }
        )
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    costs: Double,
    revenue: Double,
    profit: Double,
    margin: Double,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (project.status) {
        "Aprovado" -> Color(0xFF3B82F6)
        "Em produção" -> FluxGoldPrimary
        "Em montagem" -> Color(0xFFF97316)
        "Concluído" -> Emerald500
        "Cancelado" -> MaterialTheme.colorScheme.error
        else -> Color(0xFF94A3B8)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = project.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (project.clientName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = project.clientName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = project.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // Financial KPIs Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Receita", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CostEngineeringCalculator.formatCurrency(revenue), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Emerald500)
                }
                Column {
                    Text("Custos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CostEngineeringCalculator.formatCurrency(costs), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                }
                Column {
                    Text("Lucro", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CostEngineeringCalculator.formatCurrency(profit), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (profit >= 0) FluxGoldPrimary else MaterialTheme.colorScheme.error)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Margem", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.1f".format(margin)}%", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = if (margin >= 0) Emerald500 else MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prazo: ${project.formattedDeadline}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun AddEditProjectDialog(
    initialProject: ProjectEntity?,
    clients: List<ClientEntity>,
    onDismiss: () -> Unit,
    onSave: (ProjectEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialProject?.name ?: "") }
    var selectedClientName by remember { mutableStateOf(initialProject?.clientName ?: "") }
    var budgetedRevenueInput by remember { mutableStateOf(initialProject?.let { "%.0f".format(it.budgetedRevenue) } ?: "0") }
    var selectedStatus by remember { mutableStateOf(initialProject?.status ?: "Orçamento") }
    var description by remember { mutableStateOf(initialProject?.description ?: "") }

    val statusList = listOf("Orçamento", "Aprovado", "Em produção", "Em montagem", "Concluído", "Cancelado")
    var statusExpanded by remember { mutableStateOf(false) }
    var clientExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialProject == null) "Novo Projeto" else "Editar Projeto") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Projeto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(expanded = clientExpanded, onExpandedChange = { clientExpanded = it }) {
                    OutlinedTextField(
                        value = selectedClientName.ifBlank { "Nenhum cliente associado" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cliente") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = clientExpanded, onDismissRequest = { clientExpanded = false }) {
                        clients.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedClientName = c.name
                                    clientExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                    OutlinedTextField(
                        value = selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado do Projeto") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        statusList.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    selectedStatus = s
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = budgetedRevenueInput,
                    onValueChange = { budgetedRevenueInput = it },
                    label = { Text("Receita / Valor Orçado (Kz)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Escopo") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rev = budgetedRevenueInput.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        val proj = (initialProject ?: ProjectEntity(
                            name = name.trim(),
                            clientName = selectedClientName.trim(),
                            budgetedRevenue = rev,
                            status = selectedStatus,
                            description = description.trim()
                        )).copy(
                            name = name.trim(),
                            clientName = selectedClientName.trim(),
                            budgetedRevenue = rev,
                            status = selectedStatus,
                            description = description.trim()
                        )
                        onSave(proj)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FluxGoldPrimary, contentColor = Color.Black)
            ) {
                Text("Salvar Projeto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ProjectDetailsDialog(
    project: ProjectEntity,
    transactions: List<TransactionEntity>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(project.name, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (project.clientName.isNotBlank()) {
                    Text("Cliente: ${project.clientName}", fontSize = 13.sp)
                }
                Text("Status: ${project.status}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                if (project.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(project.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Lançamentos Financeiros Vinculados (${transactions.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                if (transactions.isEmpty()) {
                    Text("Nenhuma transação vinculada ainda a este projeto.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(transactions) { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.description, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("${tx.category} • ${tx.formattedDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                val isIncome = tx.type == TransactionType.INCOME
                                Text(
                                    text = "${if (isIncome) "+" else "-"}${CostEngineeringCalculator.formatCurrency(tx.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isIncome) Emerald500 else MaterialTheme.colorScheme.error
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = FluxGoldPrimary, contentColor = Color.Black)) {
                Text("Fechar")
            }
        }
    )
}
