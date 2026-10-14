package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ads.AdScreenContext
import com.example.data.ads.AdsConfiguration
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.AdMobBannerView
import com.example.ui.theme.Emerald500
import com.example.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPortalScreen(
    viewModel: MainViewModel,
    adsConfig: AdsConfiguration = AdsConfiguration(),
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Catálogo, 1: Meus Orçamentos, 2: Agendamentos, 3: Pagamentos

    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val companyContext by viewModel.companyContext.collectAsStateWithLifecycle()
    val appointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()

    var showRequestQuoteDialog by remember { mutableStateOf(false) }
    var quoteTitle by remember { mutableStateOf("") }
    var quoteDescription by remember { mutableStateOf("") }
    var catalogSearchQuery by remember { mutableStateOf("") }
    val numberFormatter = remember { NumberFormat.getCurrencyInstance(Locale("pt", "AO")).apply { currency = java.util.Currency.getInstance("AOA") } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = companyContext.branding?.appName ?: "Área do Cliente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Olá, ${currentUserProfile?.fullName ?: "Cliente"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onLogout) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Sair")
                    }
                },
                actions = {
                    IconButton(onClick = { showRequestQuoteDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Pedir Orçamento", tint = Emerald500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Catálogo", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Orçamentos", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Agendamentos", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Pagamentos", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: CATÁLOGO & FEED DE PRODUTOS E SERVIÇOS (Posicionamento do anúncio AdMob)
                    val filteredProducts = if (catalogSearchQuery.isBlank()) {
                        products
                    } else {
                        products.filter {
                            it.name.contains(catalogSearchQuery, ignoreCase = true) ||
                            it.category.contains(catalogSearchQuery, ignoreCase = true)
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Banner Responsivo do Google AdMob no Topo do Catálogo
                        item {
                            AdMobBannerView(
                                screenContext = AdScreenContext.FEED_CATALOG,
                                config = adsConfig
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = catalogSearchQuery,
                                onValueChange = { catalogSearchQuery = it },
                                placeholder = { Text("Pesquisar catálogo de produtos e serviços...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("catalog_search_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        if (filteredProducts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Nenhum item disponível no catálogo no momento.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            items(filteredProducts, key = { it.id }) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("product_item_${item.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text(
                                                    text = "${item.category} • ${if (item.type == "SERVICE") "Serviço" else "Produto"}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = numberFormatter.format(item.sellingPrice),
                                                fontWeight = FontWeight.Black,
                                                color = Emerald500,
                                                fontSize = 15.sp
                                            )
                                        }

                                        if (item.notes.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = item.notes,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                quoteTitle = "Orçamento: ${item.name}"
                                                quoteDescription = "Interesse no item ${item.name} (${item.category})"
                                                showRequestQuoteDialog = true
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500.copy(alpha = 0.9f))
                                        ) {
                                            Text("Solicitar Orçamento Deste Item", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB ORÇAMENTOS DO CLIENTE
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Button(
                                onClick = { showRequestQuoteDialog = true },
                                modifier = Modifier.fillMaxWidth().testTag("btn_customer_request_quote"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Solicitar Novo Orçamento", fontWeight = FontWeight.Bold)
                            }
                        }

                        items(budgets, key = { it.id }) { budget ->
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("customer_budget_${budget.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = budget.projectTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Emerald500.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = budget.status.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald500,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Escopo: ${budget.projectScope.ifBlank { "Orçamento técnico solicitado" }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB AGENDAMENTOS
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(appointments, key = { it.id }) { appt ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = "Serviço: ${appt.serviceType}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = "Data: ${appt.formattedDate} às ${appt.time}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "Status: ${appt.displayStatusLabel}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald500)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB PAGAMENTOS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Opções de Pagamento em Angola", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Pague com segurança via Multicaixa Express, Referência Bancária BAI/BFA ou IBAN direto.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showRequestQuoteDialog) {
        AlertDialog(
            onDismissRequest = { showRequestQuoteDialog = false },
            title = { Text("Solicitar Orçamento Comercial", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quoteTitle,
                        onValueChange = { quoteTitle = it },
                        label = { Text("Título do Projeto / Serviço") },
                        placeholder = { Text("Ex: Móvel de Sala, Pintura, Reparo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quoteDescription,
                        onValueChange = { quoteDescription = it },
                        label = { Text("Descrição e Medidas") },
                        placeholder = { Text("Descreva o que precisa com detalhes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quoteTitle.isBlank()) return@Button
                        Toast.makeText(context, "Pedido de orçamento enviado com sucesso!", Toast.LENGTH_SHORT).show()
                        showRequestQuoteDialog = false
                        quoteTitle = ""
                        quoteDescription = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Text("Enviar Pedido")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestQuoteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
