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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.CategoryEntity
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Receitas, 1 = Despesas
    val currentType = if (selectedTabIndex == 0) "INCOME" else "EXPENSE"
    val filteredCategories = categories.filter { it.type == currentType }

    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }

    // State for intelligent deletion
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var transactionCountForDelete by remember { mutableIntStateOf(0) }
    var showMigrationDeleteDialog by remember { mutableStateOf(false) }
    var showSimpleDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Gerenciar Categorias", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("categories_back_button")) {
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
                    categoryToEdit = null
                    showAddEditDialog = true
                },
                containerColor = FluxGoldPrimary,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_category_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Categoria")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = FluxGoldPrimary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Receitas (${categories.count { it.type == "INCOME" }})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Despesas (${categories.count { it.type == "EXPENSE" }})", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (filteredCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma categoria de ${if (selectedTabIndex == 0) "receita" else "despesa"} encontrada.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCategories, key = { it.id }) { cat ->
                        CategoryListItem(
                            category = cat,
                            onEdit = {
                                categoryToEdit = cat
                                showAddEditDialog = true
                            },
                            onDelete = {
                                scope.launch {
                                    val count = viewModel.countTransactionsWithCategory(cat.name)
                                    categoryToDelete = cat
                                    transactionCountForDelete = count
                                    if (count > 0) {
                                        showMigrationDeleteDialog = true
                                    } else {
                                        showSimpleDeleteDialog = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditCategoryDialog(
            initialCategory = categoryToEdit,
            defaultType = currentType,
            onDismiss = { showAddEditDialog = false },
            onSave = { updated ->
                viewModel.saveCategory(updated)
                showAddEditDialog = false
            }
        )
    }

    // Simple Delete Confirmation
    if (showSimpleDeleteDialog && categoryToDelete != null) {
        val cat = categoryToDelete!!
        AlertDialog(
            onDismissRequest = { showSimpleDeleteDialog = false },
            title = { Text("Eliminar Categoria") },
            text = { Text("Deseja realmente eliminar a categoria '${cat.name}'? Não há transações associadas.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategoryDirectly(cat)
                        showSimpleDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimpleDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Migration Delete Dialog (when transactions exist!)
    if (showMigrationDeleteDialog && categoryToDelete != null) {
        val cat = categoryToDelete!!
        val replacementOptions = categories.filter { it.type == cat.type && it.id != cat.id }
        var selectedReplacement by remember { mutableStateOf(replacementOptions.firstOrNull()?.name ?: "") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showMigrationDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Substituição Obrigatória") },
            text = {
                Column {
                    Text(
                        "A categoria '${cat.name}' possui $transactionCountForDelete transação(ões) vinculada(s).\n\n" +
                                "Para manter o histórico e a consistência do fluxo de caixa, escolha uma categoria de destino para transferir esses lançamentos antes de excluir:"
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (replacementOptions.isEmpty()) {
                        Text("Crie outra categoria primeiro antes de poder eliminar esta.", color = MaterialTheme.colorScheme.error)
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedReplacement,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoria de Substituição") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                replacementOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt.name) },
                                        onClick = {
                                            selectedReplacement = opt.name
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedReplacement.isNotBlank()) {
                            viewModel.deleteCategoryWithMigration(cat, selectedReplacement)
                            showMigrationDeleteDialog = false
                        }
                    },
                    enabled = selectedReplacement.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reatribuir e Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMigrationDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CategoryListItem(
    category: CategoryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(category.colorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryVectorIcon(category.iconName),
                        contentDescription = category.name,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = category.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (category.type == "INCOME") "Receita" else "Despesa",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = FluxGoldPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryDialog(
    initialCategory: CategoryEntity?,
    defaultType: String,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var selectedColor by remember { mutableStateOf(initialCategory?.colorHex ?: 0xFFF59E0B) }
    var selectedIcon by remember { mutableStateOf(initialCategory?.iconName ?: "Category") }

    val colorsList = listOf(
        0xFF10B981, 0xFF059669, 0xFF047857, 0xFF3B82F6, 0xFF2563EB,
        0xFF6366F1, 0xFF8B5CF6, 0xFFEC4899, 0xFFEF4444, 0xFFF97316,
        0xFFF59E0B, 0xFFEAB308, 0xFF14B8A6, 0xFF64748B, 0xFF475569
    )

    val iconOptions = listOf(
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "Handyman" to Icons.Default.Handyman,
        "Assignment" to Icons.Default.Assignment,
        "Payments" to Icons.Default.Payments,
        "Inventory2" to Icons.Default.Inventory2,
        "LocalShipping" to Icons.Default.LocalShipping,
        "Build" to Icons.Default.Build,
        "Restaurant" to Icons.Default.Restaurant,
        "Badge" to Icons.Default.Badge,
        "HomeWork" to Icons.Default.HomeWork,
        "Campaign" to Icons.Default.Campaign,
        "Bolt" to Icons.Default.Bolt,
        "Wifi" to Icons.Default.Wifi,
        "Category" to Icons.Default.Category
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialCategory == null) "Nova Categoria" else "Editar Categoria")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Categoria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Cor:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorsList.take(6).forEach { colorVal ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .clickable { selectedColor = colorVal },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == colorVal) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Ícone:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.take(6).forEach { (iconName, vector) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedIcon == iconName) FluxGoldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = iconName,
                                tint = if (selectedIcon == iconName) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val category = (initialCategory ?: CategoryEntity(
                            name = name.trim(),
                            type = defaultType,
                            colorHex = selectedColor,
                            iconName = selectedIcon
                        )).copy(
                            name = name.trim(),
                            colorHex = selectedColor,
                            iconName = selectedIcon
                        )
                        onSave(category)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = FluxGoldPrimary, contentColor = Color.Black)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun getCategoryVectorIcon(name: String): ImageVector {
    return when (name) {
        "ShoppingBag" -> Icons.Default.ShoppingBag
        "Handyman" -> Icons.Default.Handyman
        "Assignment" -> Icons.Default.Assignment
        "Payments" -> Icons.Default.Payments
        "Inventory2" -> Icons.Default.Inventory2
        "LocalShipping" -> Icons.Default.LocalShipping
        "Build" -> Icons.Default.Build
        "Restaurant" -> Icons.Default.Restaurant
        "Badge" -> Icons.Default.Badge
        "HomeWork" -> Icons.Default.HomeWork
        "Campaign" -> Icons.Default.Campaign
        "Bolt" -> Icons.Default.Bolt
        "Wifi" -> Icons.Default.Wifi
        else -> Icons.Default.Category
    }
}
