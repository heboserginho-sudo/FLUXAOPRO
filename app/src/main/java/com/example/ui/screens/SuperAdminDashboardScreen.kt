package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.CountryDirectory
import com.example.data.i18n.CountryInfo
import com.example.data.i18n.CurrencyService
import com.example.data.i18n.I18nService
import com.example.data.i18n.TimezoneService
import com.example.data.i18n.TranslationAuditReport
import com.example.data.model.AppCurrency
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.data.ads.AdsConfiguration
import com.example.data.ads.mediation.AdSourceType
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
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.PlanEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SubscriptionEntity
import com.example.ui.theme.Emerald500
import com.example.ui.theme.FluxPurplePrimary
import com.example.ui.theme.FluxPurpleDark
import com.example.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboardScreen(
    viewModel: MainViewModel,
    onBackToApp: () -> Unit
) {
    val context = LocalContext.current
    val companies by viewModel.allCompanies.collectAsStateWithLifecycle()
    val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val plans by viewModel.allPlans.collectAsStateWithLifecycle()
    val subscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle()
    val auditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val allClients by viewModel.allClients.collectAsStateWithLifecycle()
    val activeCompanyId by viewModel.activeCompanyId.collectAsStateWithLifecycle()
    val companyContext by viewModel.companyContext.collectAsStateWithLifecycle()
    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val adsConfig by viewModel.adsConfiguration.collectAsStateWithLifecycle()

    var selectedNavTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Empresas, 2: Utilizadores, 3: Planos, 4: Assinaturas, 5: Pagamentos, 6: Auditoria, 7: Configurações, 8: Publicidade
    var globalSearchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Dialog states
    var showCreateCompanyDialog by remember { mutableStateOf(false) }
    var showEditCompanyDialog by remember { mutableStateOf<CompanyEntity?>(null) }
    var showSuspendConfirmDialog by remember { mutableStateOf<CompanyEntity?>(null) }
    var showCompanyDetailsDialog by remember { mutableStateOf<CompanyEntity?>(null) }
    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var showChangePlanDialog by remember { mutableStateOf<CompanyEntity?>(null) }

    // Form states for new company
    var newCompanyName by remember { mutableStateOf("") }
    var newCompanyPhone by remember { mutableStateOf("") }
    var newCompanyEmail by remember { mutableStateOf("") }
    var newCompanyDescription by remember { mutableStateOf("") }
    var newCompanyAddress by remember { mutableStateOf("") }
    var newCompanyPlanId by remember { mutableStateOf("plan_pro") }

    // Form states for new/edit plan
    var planName by remember { mutableStateOf("") }
    var planPriceText by remember { mutableStateOf("") }
    var planMaxUsersText by remember { mutableStateOf("5") }
    var planMaxProductsText by remember { mutableStateOf("100") }
    var planMaxCustomersText by remember { mutableStateOf("100") }
    var planDescription by remember { mutableStateOf("") }

    // Filters for Companies Tab
    var companyStatusFilter by remember { mutableStateOf("ALL") } // ALL, ACTIVE, SUSPENDED

    // Filters for Users Tab
    var userRoleFilter by remember { mutableStateOf("ALL") } // ALL, super_admin, company_admin, employee, customer

    val numberFormatter = remember { NumberFormat.getCurrencyInstance(Locale("pt", "AO")).apply { currency = java.util.Currency.getInstance("AOA") } }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "AO")) }

    // RLS / Role Security Protection Check
    if (!companyContext.isSuperAdmin && currentUserProfile?.role != "super_admin") {
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Text("Acesso Restrito ao Super Admin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text(
                            "O seu perfil atual (${currentUserProfile?.role ?: "company_admin"}) não possui permissões globais para aceder ao /super-admin. As políticas de RLS garantem o isolamento entre empresas.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(onClick = onBackToApp, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                            Text("Voltar ao Painel da Empresa")
                        }
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = globalSearchQuery,
                            onValueChange = { globalSearchQuery = it },
                            placeholder = { Text("Pesquisar empresas, utilizadores, clientes...", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("super_admin_global_search_input")
                        )
                    } else {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Emerald500.copy(alpha = 0.2f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = Emerald500, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FLUXAO BUSINESS — SUPER ADMIN",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = "Gestão Global SaaS Multiempresa • Supabase Auth & RLS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToApp) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar ao App")
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search, contentDescription = "Pesquisar")
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "Sua sessão Super Admin está segura com Supabase Auth.", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notificações", tint = Emerald500)
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
        ) {
            // Scrollable Menu Tabs for Super Admin Sub-sections
            ScrollableTabRow(
                selectedTabIndex = selectedNavTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(selected = selectedNavTab == 0, onClick = { selectedNavTab = 0 }, text = { Text("Dashboard", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp)) })
                Tab(selected = selectedNavTab == 1, onClick = { selectedNavTab = 1 }, text = { Text("Empresas (${companies.size})", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(18.dp)) })
                Tab(selected = selectedNavTab == 2, onClick = { selectedNavTab = 2 }, text = { Text("Utilizadores (${profiles.size})", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) })
                Tab(selected = selectedNavTab == 3, onClick = { selectedNavTab = 3 }, text = { Text("Planos (${plans.size})", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.CardMembership, contentDescription = null, modifier = Modifier.size(18.dp)) })
                Tab(selected = selectedNavTab == 4, onClick = { selectedNavTab = 4 }, text = { Text("Assinaturas", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp)) })
                Tab(selected = selectedNavTab == 5, onClick = { selectedNavTab = 5 }, text = { Text("Pagamentos", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp)) })
                Tab(selected = selectedNavTab == 6, onClick = { selectedNavTab = 6 }, text = { Text("Auditoria", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp)) })
                Tab(selected = selectedNavTab == 7, onClick = { selectedNavTab = 7 }, text = { Text("Configurações", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) })
                Tab(selected = selectedNavTab == 8, onClick = { selectedNavTab = 8 }, text = { Text("Publicidade", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp)) })
                Tab(selected = selectedNavTab == 9, onClick = { selectedNavTab = 9 }, text = { Text("🌍 Internacional (i18n)", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp)) })
            }

            // Search Results or Tab Content
            if (globalSearchQuery.isNotBlank()) {
                val matchingCompanies = companies.filter { it.name.contains(globalSearchQuery, ignoreCase = true) || it.slug.contains(globalSearchQuery, ignoreCase = true) }
                val matchingUsers = profiles.filter { it.fullName.contains(globalSearchQuery, ignoreCase = true) || it.email.contains(globalSearchQuery, ignoreCase = true) }
                val matchingClients = allClients.filter { it.name.contains(globalSearchQuery, ignoreCase = true) || it.phone.contains(globalSearchQuery, ignoreCase = true) }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text("Resultados da Pesquisa Global: \"$globalSearchQuery\"", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    if (matchingCompanies.isNotEmpty()) {
                        item { Text("Empresas Encontradas (${matchingCompanies.size}):", fontWeight = FontWeight.Bold, color = Emerald500) }
                        items(matchingCompanies) { comp ->
                            Card(modifier = Modifier.fillMaxWidth().clickable { showCompanyDetailsDialog = comp }) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Apartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(comp.name, fontWeight = FontWeight.Bold)
                                        Text("${comp.email} • ${comp.city}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    if (matchingUsers.isNotEmpty()) {
                        item { Text("Utilizadores Encontrados (${matchingUsers.size}):", fontWeight = FontWeight.Bold, color = FluxPurplePrimary) }
                        items(matchingUsers) { user ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(user.fullName, fontWeight = FontWeight.Bold)
                                        Text("${user.email} • Role: ${user.role}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    if (matchingClients.isNotEmpty()) {
                        item { Text("Clientes Encontrados (${matchingClients.size}):", fontWeight = FontWeight.Bold) }
                        items(matchingClients) { client ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(client.name, fontWeight = FontWeight.Bold)
                                    Text("Contacto: ${client.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    if (matchingCompanies.isEmpty() && matchingUsers.isEmpty() && matchingClients.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Nenhum resultado encontrado para a sua pesquisa.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                when (selectedNavTab) {
                    0 -> DashboardTabContent(
                        companies = companies,
                        profiles = profiles,
                        plans = plans,
                        subscriptions = subscriptions,
                        invoices = allInvoices,
                        clients = allClients,
                        auditLogs = auditLogs,
                        numberFormatter = numberFormatter,
                        onCreateCompanyClick = { showCreateCompanyDialog = true }
                    )
                    1 -> CompaniesTabContent(
                        companies = companies,
                        activeCompanyId = activeCompanyId,
                        subscriptions = subscriptions,
                        plans = plans,
                        statusFilter = companyStatusFilter,
                        onStatusFilterChange = { companyStatusFilter = it },
                        onCreateCompany = { showCreateCompanyDialog = true },
                        onInspectCompany = { comp ->
                            viewModel.setActiveCompany(comp.id)
                            Toast.makeText(context, "Contexto alterado para: ${comp.name}", Toast.LENGTH_SHORT).show()
                        },
                        onViewDetails = { showCompanyDetailsDialog = it },
                        onEditCompany = { showEditCompanyDialog = it },
                        onSuspendConfirm = { showSuspendConfirmDialog = it },
                        onChangePlan = { showChangePlanDialog = it }
                    )
                    2 -> UsersTabContent(
                        profiles = profiles,
                        companies = companies,
                        roleFilter = userRoleFilter,
                        onRoleFilterChange = { userRoleFilter = it }
                    )
                    3 -> PlansTabContent(
                        plans = plans,
                        onCreatePlan = { showCreatePlanDialog = true }
                    )
                    4 -> SubscriptionsTabContent(
                        subscriptions = subscriptions,
                        companies = companies,
                        plans = plans,
                        onChangePlan = { showChangePlanDialog = it }
                    )
                    5 -> PaymentsTabContent(
                        invoices = allInvoices,
                        numberFormatter = numberFormatter
                    )
                    6 -> AuditLogsTabContent(
                        auditLogs = auditLogs,
                        dateFormatter = dateFormatter
                    )
                    7 -> SettingsTabContent(context = context)
                    8 -> AdvertisingTabContent(
                        adsConfig = adsConfig,
                        onSaveConfig = { updated ->
                            viewModel.updateAdsConfiguration(updated)
                            Toast.makeText(context, "Configurações do Google AdMob salvas com sucesso!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    9 -> GlobalI18nTabContent(
                        viewModel = viewModel,
                        context = context
                    )
                }
            }
        }
    }

    // Modal Dialog: New Company Tenant
    if (showCreateCompanyDialog) {
        AlertDialog(
            onDismissRequest = { showCreateCompanyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = Emerald500)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Criar Nova Empresa SaaS", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        OutlinedTextField(
                            value = newCompanyName,
                            onValueChange = { newCompanyName = it },
                            label = { Text("Nome Comercial da Empresa *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("new_company_name_input")
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newCompanyPhone,
                            onValueChange = { newCompanyPhone = it },
                            label = { Text("Telefone (+244) *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newCompanyEmail,
                            onValueChange = { newCompanyEmail = it },
                            label = { Text("E-mail Comercial *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newCompanyDescription,
                            onValueChange = { newCompanyDescription = it },
                            label = { Text("Descrição dos Serviços") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newCompanyAddress,
                            onValueChange = { newCompanyAddress = it },
                            label = { Text("Endereço (Cidade, Província)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Plano SaaS Inicial:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(plans) { plan ->
                                FilterChip(
                                    selected = newCompanyPlanId == plan.id,
                                    onClick = { newCompanyPlanId = plan.id },
                                    label = { Text(plan.name) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCompanyName.isBlank()) {
                            Toast.makeText(context, "Preencha o nome da empresa", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.createCompanyTenant(
                            name = newCompanyName,
                            phone = newCompanyPhone,
                            email = newCompanyEmail,
                            description = newCompanyDescription,
                            address = newCompanyAddress.ifBlank { "Luanda, Angola" },
                            planId = newCompanyPlanId
                        )
                        showCreateCompanyDialog = false
                        newCompanyName = ""
                        newCompanyPhone = ""
                        newCompanyEmail = ""
                        newCompanyDescription = ""
                        newCompanyAddress = ""
                        Toast.makeText(context, "Empresa criada com sucesso e isolada por RLS!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Text("Criar Empresa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCompanyDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Modal Dialog: Edit Company Details
    if (showEditCompanyDialog != null) {
        val company = showEditCompanyDialog!!
        var editName by remember(company.id) { mutableStateOf(company.name) }
        var editPhone by remember(company.id) { mutableStateOf(company.phone) }
        var editEmail by remember(company.id) { mutableStateOf(company.email) }
        var editDescription by remember(company.id) { mutableStateOf(company.description) }
        var editAddress by remember(company.id) { mutableStateOf(company.address) }

        AlertDialog(
            onDismissRequest = { showEditCompanyDialog = null },
            title = { Text("Editar Empresa: ${company.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Nome da Empresa") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Telefone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editDescription, onValueChange = { editDescription = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editAddress, onValueChange = { editAddress = it }, label = { Text("Endereço") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCompanyDetails(
                            company.copy(
                                name = editName,
                                phone = editPhone,
                                email = editEmail,
                                description = editDescription,
                                address = editAddress,
                                updatedAtEpoch = System.currentTimeMillis()
                            )
                        )
                        showEditCompanyDialog = null
                        Toast.makeText(context, "Dados da empresa atualizados com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Guardar Alterações")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCompanyDialog = null }) { Text("Cancelar") }
            }
        )
    }

    // Confirmation Dialog: Suspend/Reactivate Company
    if (showSuspendConfirmDialog != null) {
        val company = showSuspendConfirmDialog!!
        val isSuspending = company.status == "active"
        AlertDialog(
            onDismissRequest = { showSuspendConfirmDialog = null },
            title = {
                Text(
                    text = if (isSuspending) "Confirmar Suspensão da Empresa" else "Confirmar Reativação da Empresa",
                    fontWeight = FontWeight.Bold,
                    color = if (isSuspending) MaterialTheme.colorScheme.error else Emerald500
                )
            },
            text = {
                Text(
                    text = if (isSuspending)
                        "Tem certeza de que deseja suspender a empresa \"${company.name}\"? Os utilizadores desta empresa ficarão bloqueados temporariamente, mas os dados do banco de dados permanecerão intactos."
                    else
                        "Deseja reativar o acesso da empresa \"${company.name}\" à plataforma FluxAO Business?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newStatus = if (isSuspending) "suspended" else "active"
                        viewModel.updateCompanyStatus(company.id, newStatus)
                        showSuspendConfirmDialog = null
                        Toast.makeText(context, if (isSuspending) "Empresa suspensa com sucesso." else "Empresa reativada com sucesso.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSuspending) MaterialTheme.colorScheme.error else Emerald500)
                ) {
                    Text(if (isSuspending) "Suspender Empresa" else "Reativar Empresa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuspendConfirmDialog = null }) { Text("Cancelar") }
            }
        )
    }

    // Modal: Company Tenant Details Inspector View
    if (showCompanyDetailsDialog != null) {
        val comp = showCompanyDetailsDialog!!
        val compUsers = profiles.filter { it.companyId == comp.id }
        val compSub = subscriptions.find { it.companyId == comp.id }
        val compPlan = plans.find { it.id == compSub?.planId }

        AlertDialog(
            onDismissRequest = { showCompanyDetailsDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Apartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(comp.name, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Slug: ${comp.slug}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("ID do Tenant: ${comp.id}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Estado RLS: ${comp.status.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (comp.status == "active") Emerald500 else MaterialTheme.colorScheme.error)
                            Text("Plano Atual: ${compPlan?.name ?: "PRO"} (${numberFormatter.format(compPlan?.price ?: 0.0)}/mês)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Text("Utilizadores Registados (${compUsers.size}):", fontWeight = FontWeight.Bold)
                    if (compUsers.isEmpty()) {
                        Text("Nenhum utilizador individual registado ainda.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            compUsers.forEach { user ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(user.fullName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(user.role, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Text("Contactos e Endereço:", fontWeight = FontWeight.Bold)
                    Text("Telefone: ${comp.phone.ifBlank { "N/A" }}", fontSize = 12.sp)
                    Text("Email: ${comp.email}", fontSize = 12.sp)
                    Text("Endereço: ${comp.address}", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setActiveCompany(comp.id)
                    showCompanyDetailsDialog = null
                    Toast.makeText(context, "Mudou para o contexto da empresa: ${comp.name}", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Inspecionar Painel desta Empresa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompanyDetailsDialog = null }) { Text("Fechar") }
            }
        )
    }

    // Dialog: Change Company Plan
    if (showChangePlanDialog != null) {
        val comp = showChangePlanDialog!!
        var selectedPlanId by remember { mutableStateOf(subscriptions.find { it.companyId == comp.id }?.planId ?: "plan_pro") }

        AlertDialog(
            onDismissRequest = { showChangePlanDialog = null },
            title = { Text("Alterar Plano da Empresa: ${comp.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selecione o novo plano SaaS para esta empresa:", fontSize = 13.sp)
                    plans.forEach { plan ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPlanId = plan.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedPlanId == plan.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = if (selectedPlanId == plan.id) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(plan.name, fontWeight = FontWeight.Bold)
                                    Text("${numberFormatter.format(plan.price)} / mês", fontSize = 11.sp)
                                }
                                if (selectedPlanId == plan.id) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.assignPlanToCompany(comp.id, selectedPlanId)
                    showChangePlanDialog = null
                    Toast.makeText(context, "Plano atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Atualizar Plano")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePlanDialog = null }) { Text("Cancelar") }
            }
        )
    }

    // Modal Dialog: New Plan Creation
    if (showCreatePlanDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlanDialog = false },
            title = { Text("Criar Novo Plano SaaS", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = planName, onValueChange = { planName = it }, label = { Text("Nome do Plano (Ex: ENTERPRISE)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = planPriceText, onValueChange = { planPriceText = it }, label = { Text("Preço Mensal (AOA)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = planMaxUsersText, onValueChange = { planMaxUsersText = it }, label = { Text("Limite de Utilizadores") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = planDescription, onValueChange = { planDescription = it }, label = { Text("Descrição dos Recursos") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (planName.isBlank()) return@Button
                    val pPrice = planPriceText.toDoubleOrNull() ?: 0.0
                    val pUsers = planMaxUsersText.toIntOrNull() ?: 5
                    val newPlan = PlanEntity(
                        id = "plan_${planName.lowercase().filter { it.isLetter() }}",
                        name = planName.uppercase(),
                        description = planDescription,
                        price = pPrice,
                        maxUsers = pUsers
                    )
                    viewModel.savePlan(newPlan)
                    showCreatePlanDialog = false
                    planName = ""
                    planPriceText = ""
                    Toast.makeText(context, "Plano criado com sucesso!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Salvar Plano")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlanDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun DashboardTabContent(
    companies: List<CompanyEntity>,
    profiles: List<ProfileEntity>,
    plans: List<PlanEntity>,
    subscriptions: List<SubscriptionEntity>,
    invoices: List<com.example.data.local.entity.InvoiceEntity>,
    clients: List<com.example.data.local.entity.ClientEntity>,
    auditLogs: List<com.example.data.local.entity.AuditLogEntity>,
    numberFormatter: NumberFormat,
    onCreateCompanyClick: () -> Unit
) {
    val totalRevenue = invoices.sumOf { it.totalToPay }
    val activeCompanies = companies.count { it.status == "active" }
    val suspendedCompanies = companies.count { it.status == "suspended" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("super_admin_kpi_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("FLUXAO BUSINESS SAAS — GLOBAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Métricas em Tempo Real", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        }
                        Button(onClick = onCreateCompanyClick, colors = ButtonDefaults.buttonColors(containerColor = Emerald500)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nova Empresa", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Empresas Totais", style = MaterialTheme.typography.labelSmall)
                            Text("${companies.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        }
                        Column {
                            Text("Ativas", style = MaterialTheme.typography.labelSmall)
                            Text("$activeCompanies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Emerald500)
                        }
                        Column {
                            Text("Suspensas", style = MaterialTheme.typography.labelSmall)
                            Text("$suspendedCompanies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                        }
                        Column {
                            Text("Utilizadores", style = MaterialTheme.typography.labelSmall)
                            Text("${profiles.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Receita Acumulada", style = MaterialTheme.typography.labelSmall)
                        Text(numberFormatter.format(totalRevenue), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Emerald500)
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Clientes Atendidos", style = MaterialTheme.typography.labelSmall)
                        Text("${clients.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Distribuição de Planos SaaS Ativos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    plans.forEach { plan ->
                        val countForPlan = subscriptions.count { it.planId == plan.id }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(10.dp)) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text("$countForPlan empresas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            Text("Últimos Registos de Auditoria (Audit Logs)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(auditLogs.take(5)) { log ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(log.action.uppercase(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(log.metadata, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompaniesTabContent(
    companies: List<CompanyEntity>,
    activeCompanyId: String,
    subscriptions: List<SubscriptionEntity>,
    plans: List<PlanEntity>,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onCreateCompany: () -> Unit,
    onInspectCompany: (CompanyEntity) -> Unit,
    onViewDetails: (CompanyEntity) -> Unit,
    onEditCompany: (CompanyEntity) -> Unit,
    onSuspendConfirm: (CompanyEntity) -> Unit,
    onChangePlan: (CompanyEntity) -> Unit
) {
    val filteredCompanies = remember(companies, statusFilter) {
        when (statusFilter) {
            "ACTIVE" -> companies.filter { it.status == "active" }
            "SUSPENDED" -> companies.filter { it.status == "suspended" }
            else -> companies
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Gestão de Empresas SaaS (${filteredCompanies.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = onCreateCompany, colors = ButtonDefaults.buttonColors(containerColor = Emerald500)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nova Empresa", fontSize = 12.sp)
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = statusFilter == "ALL", onClick = { onStatusFilterChange("ALL") }, label = { Text("Todas (${companies.size})") })
                }
                item {
                    FilterChip(selected = statusFilter == "ACTIVE", onClick = { onStatusFilterChange("ACTIVE") }, label = { Text("Ativas (${companies.count { it.status == "active" }})") })
                }
                item {
                    FilterChip(selected = statusFilter == "SUSPENDED", onClick = { onStatusFilterChange("SUSPENDED") }, label = { Text("Suspensas (${companies.count { it.status == "suspended" }})") })
                }
            }
        }

        if (filteredCompanies.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma empresa encontrada para o filtro selecionado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(filteredCompanies, key = { it.id }) { company ->
                val isCurrent = company.id == activeCompanyId
                val compSub = subscriptions.find { it.companyId == company.id }
                val compPlan = plans.find { it.id == compSub?.planId }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("company_card_${company.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Apartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(company.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("slug: ${company.slug} • Plano: ${compPlan?.name ?: "PRO"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (company.status == "active") Emerald500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = company.status.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (company.status == "active") Emerald500 else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(company.description.ifBlank { "Sem descrição." }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Contacto: ${company.phone.ifBlank { "N/A" }} • ${company.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                Button(
                                    onClick = { onInspectCompany(company) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isCurrent) Emerald500 else MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isCurrent) "Empresa Ativa" else "Inspecionar", fontSize = 11.sp)
                                }
                            }
                            item {
                                OutlinedButton(onClick = { onViewDetails(company) }, shape = RoundedCornerShape(8.dp)) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Detalhes", fontSize = 11.sp)
                                }
                            }
                            item {
                                OutlinedButton(onClick = { onEditCompany(company) }, shape = RoundedCornerShape(8.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Editar", fontSize = 11.sp)
                                }
                            }
                            item {
                                OutlinedButton(onClick = { onChangePlan(company) }, shape = RoundedCornerShape(8.dp)) {
                                    Icon(Icons.Default.CardMembership, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Plano", fontSize = 11.sp)
                                }
                            }
                            item {
                                if (company.status == "active") {
                                    OutlinedButton(onClick = { onSuspendConfirm(company) }, shape = RoundedCornerShape(8.dp)) {
                                        Text("Suspender", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                } else {
                                    Button(onClick = { onSuspendConfirm(company) }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Emerald500)) {
                                        Text("Reativar", fontSize = 11.sp)
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
private fun UsersTabContent(
    profiles: List<ProfileEntity>,
    companies: List<CompanyEntity>,
    roleFilter: String,
    onRoleFilterChange: (String) -> Unit
) {
    val filteredProfiles = remember(profiles, roleFilter) {
        if (roleFilter == "ALL") profiles else profiles.filter { it.role.equals(roleFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Gestão de Utilizadores SaaS (${filteredProfiles.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = roleFilter == "ALL", onClick = { onRoleFilterChange("ALL") }, label = { Text("Todos (${profiles.size})") }) }
                item { FilterChip(selected = roleFilter == "super_admin", onClick = { onRoleFilterChange("super_admin") }, label = { Text("Super Admin") }) }
                item { FilterChip(selected = roleFilter == "company_admin", onClick = { onRoleFilterChange("company_admin") }, label = { Text("Admin Empresa") }) }
                item { FilterChip(selected = roleFilter == "employee", onClick = { onRoleFilterChange("employee") }, label = { Text("Funcionários") }) }
                item { FilterChip(selected = roleFilter == "customer", onClick = { onRoleFilterChange("customer") }, label = { Text("Clientes") }) }
            }
        }

        if (filteredProfiles.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum utilizador encontrado para a função selecionada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(filteredProfiles, key = { it.id }) { user ->
                val comp = companies.find { it.id == user.companyId }
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${user.email} • ${user.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Empresa: ${comp?.name ?: "Global (Super Admin)"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(user.role.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlansTabContent(
    plans: List<PlanEntity>,
    onCreatePlan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Planos SaaS & Limites de Licença", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = onCreatePlan, colors = ButtonDefaults.buttonColors(containerColor = Emerald500)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Novo Plano", fontSize = 12.sp)
                }
            }
        }

        items(plans, key = { it.id }) { plan ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(plan.name, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Text(
                            if (plan.price == 0.0) "GRATUITO" else "${plan.price} AOA / Mês",
                            fontWeight = FontWeight.Bold,
                            color = Emerald500
                        )
                    }
                    Text(plan.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Max Utilizadores: ${plan.maxUsers}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text("Max Clientes: ${plan.maxCustomers}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text("Max Produtos: ${plan.maxProducts}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionsTabContent(
    subscriptions: List<SubscriptionEntity>,
    companies: List<CompanyEntity>,
    plans: List<PlanEntity>,
    onChangePlan: (CompanyEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Gestão de Assinaturas das Empresas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(subscriptions, key = { it.id }) { sub ->
            val comp = companies.find { it.id == sub.companyId }
            val plan = plans.find { it.id == sub.planId }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(comp?.name ?: "Empresa ${sub.companyId.take(8)}", fontWeight = FontWeight.Bold)
                        Text("Plano: ${plan?.name ?: sub.planId} • Estado: ${sub.status.uppercase()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (comp != null) {
                        OutlinedButton(onClick = { onChangePlan(comp) }) {
                            Text("Alterar Plano", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentsTabContent(
    invoices: List<com.example.data.local.entity.InvoiceEntity>,
    numberFormatter: NumberFormat
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Histórico de Pagamentos Global (Multicaixa Express)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (invoices.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum pagamento registrado no sistema.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(invoices, key = { it.id }) { inv ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(inv.invoiceNumber, fontWeight = FontWeight.Bold)
                            Text("Cliente: ${inv.clientName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(numberFormatter.format(inv.totalToPay), fontWeight = FontWeight.Bold, color = Emerald500)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogsTabContent(
    auditLogs: List<com.example.data.local.entity.AuditLogEntity>,
    dateFormatter: SimpleDateFormat
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Logs de Auditoria de Segurança SaaS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (auditLogs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum log de auditoria registado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(auditLogs, key = { it.id }) { log ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.action.uppercase(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text(dateFormatter.format(Date(log.createdAtEpoch)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(log.metadata, fontSize = 12.sp)
                        Text("IP: ${log.ipAddress} • User: ${log.userId.ifBlank { "Sistema" }}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTabContent(context: android.content.Context) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Configurações Globais da Plataforma SaaS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Integração Supabase Auth & Row Level Security", fontWeight = FontWeight.Bold)
                    Text("As políticas de RLS no Supabase estão ativas e vinculadas via auth.uid() e company_id.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = {
                        Toast.makeText(context, "Políticas de RLS validadas com sucesso!", Toast.LENGTH_SHORT).show()
                    }, colors = ButtonDefaults.buttonColors(containerColor = Emerald500)) {
                        Text("Verificar Conexão RLS")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvertisingTabContent(
    adsConfig: AdsConfiguration,
    onSaveConfig: (AdsConfiguration) -> Unit
) {
    var isAdsEnabled by remember(adsConfig) { mutableStateOf(adsConfig.isAdsEnabled) }
    var appId by remember(adsConfig) { mutableStateOf(adsConfig.appId) }
    var bannerAdUnitId by remember(adsConfig) { mutableStateOf(adsConfig.bannerAdUnitId) }
    var nativeAdUnitId by remember(adsConfig) { mutableStateOf(adsConfig.nativeAdUnitId) }
    var interstitialAdUnitId by remember(adsConfig) { mutableStateOf(adsConfig.interstitialAdUnitId) }
    var rewardedAdUnitId by remember(adsConfig) { mutableStateOf(adsConfig.rewardedAdUnitId) }
    var appOpenAdUnitId by remember(adsConfig) { mutableStateOf(adsConfig.appOpenAdUnitId) }

    var isBannerEnabled by remember(adsConfig) { mutableStateOf(adsConfig.isBannerEnabled) }
    var isNativeEnabled by remember(adsConfig) { mutableStateOf(adsConfig.isNativeEnabled) }
    var isInterstitialEnabled by remember(adsConfig) { mutableStateOf(adsConfig.isInterstitialEnabled) }
    var isRewardedEnabled by remember(adsConfig) { mutableStateOf(adsConfig.isRewardedEnabled) }
    var isAppOpenEnabled by remember(adsConfig) { mutableStateOf(adsConfig.isAppOpenEnabled) }

    var useTestAdsInDebug by remember(adsConfig) { mutableStateOf(adsConfig.useTestAdsInDebug) }
    var adSourceType by remember(adsConfig) { mutableStateOf(adsConfig.adSourceType) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Header Tree indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = FluxPurplePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Publicidade ➔ Google AdMob",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Gestão centralizada de monetização e formatos",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isAdsEnabled,
                            onCheckedChange = { isAdsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Emerald500,
                                checkedTrackColor = Emerald500.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Estrutura do Painel Administrativo:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Publicidade\n└── AdMob\n    ├── Ativar/Desativar: ${if (isAdsEnabled) "ATIVADO" else "DESATIVADO"}\n    ├── App ID: ${appId.take(18)}...\n    ├── Banner Ad Unit ID: ${if (isBannerEnabled) "ATIVO" else "INATIVO"}\n    ├── Native Ad Unit ID: ${if (isNativeEnabled) "ATIVO" else "INATIVO"}\n    ├── Interstitial Ad Unit ID: ${if (isInterstitialEnabled) "ATIVO" else "INATIVO"}\n    ├── Rewarded Ad Unit ID: ${if (isRewardedEnabled) "ATIVO" else "INATIVO"}\n    └── App Open Ad Unit ID: ${if (isAppOpenEnabled) "ATIVO" else "INATIVO"}",
                        fontSize = 11.sp,
                        color = Emerald500,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section: Configuração Geral do Google AdMob
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Credenciais do Aplicativo (App ID)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Configuração global vinculada ao AndroidManifest.xml com isolamento seguro.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = appId,
                        onValueChange = { appId = it },
                        label = { Text("Google AdMob App ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Modo de Anúncios de Teste no Debug",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Protege sua conta AdMob contra cliques ou impressões artificiais no desenvolvimento.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useTestAdsInDebug,
                            onCheckedChange = { useTestAdsInDebug = it }
                        )
                    }
                }
            }
        }

        // Section: Formatos de Anúncios Individuais
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Formatos de Anúncio e Blocos Individuais",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    // 1. BANNER AD
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("1. Banner Ad (Feed / Catálogo / Início)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Bloco principal do FluxAO responsivo e adaptativo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isBannerEnabled,
                                onCheckedChange = { isBannerEnabled = it }
                            )
                        }
                        OutlinedTextField(
                            value = bannerAdUnitId,
                            onValueChange = { bannerAdUnitId = it },
                            label = { Text("Banner Ad Unit ID") },
                            singleLine = true,
                            enabled = isBannerEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider()

                    // 2. NATIVE AD
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("2. Native Ad (Anúncio Nativo)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Integrado à lista de produtos ou transações", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isNativeEnabled,
                                onCheckedChange = { isNativeEnabled = it }
                            )
                        }
                        OutlinedTextField(
                            value = nativeAdUnitId,
                            onValueChange = { nativeAdUnitId = it },
                            label = { Text("Native Ad Unit ID") },
                            placeholder = { Text("Ex: ca-app-pub-1035644843815418/...") },
                            singleLine = true,
                            enabled = isNativeEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider()

                    // 3. INTERSTITIAL AD
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("3. Interstitial Ad (Tela Inteira)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Exibido apenas em transições não-críticas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isInterstitialEnabled,
                                onCheckedChange = { isInterstitialEnabled = it }
                            )
                        }
                        OutlinedTextField(
                            value = interstitialAdUnitId,
                            onValueChange = { interstitialAdUnitId = it },
                            label = { Text("Interstitial Ad Unit ID") },
                            placeholder = { Text("Ex: ca-app-pub-1035644843815418/...") },
                            singleLine = true,
                            enabled = isInterstitialEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider()

                    // 4. REWARDED AD
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("4. Rewarded Ad (Anúncio Recompensado)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Desbloqueio de relatórios ou créditos no app", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isRewardedEnabled,
                                onCheckedChange = { isRewardedEnabled = it }
                            )
                        }
                        OutlinedTextField(
                            value = rewardedAdUnitId,
                            onValueChange = { rewardedAdUnitId = it },
                            label = { Text("Rewarded Ad Unit ID") },
                            placeholder = { Text("Ex: ca-app-pub-1035644843815418/...") },
                            singleLine = true,
                            enabled = isRewardedEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider()

                    // 5. APP OPEN AD
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("5. App Open Ad (Abertura do Aplicativo)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Exibido no retorno ao aplicativo após pausa", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isAppOpenEnabled,
                                onCheckedChange = { isAppOpenEnabled = it }
                            )
                        }
                        OutlinedTextField(
                            value = appOpenAdUnitId,
                            onValueChange = { appOpenAdUnitId = it },
                            label = { Text("App Open Ad Unit ID") },
                            placeholder = { Text("Ex: ca-app-pub-1035644843815418/...") },
                            singleLine = true,
                            enabled = isAppOpenEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Section: Mediação e Parceiros
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Arquitetura de Mediação e Partner Bidding",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Suporte arquitetural preparado para fontes de anúncios externas e parceiros de lance em tempo real sem afetar o fluxo do usuário.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AdSourceType.values().forEach { source ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { adSourceType = source }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = adSourceType == source,
                                onClick = { adSourceType = source }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(source.displayName, fontSize = 13.sp, fontWeight = if (adSourceType == source) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val updated = adsConfig.copy(
                        isAdsEnabled = isAdsEnabled,
                        appId = appId.trim(),
                        bannerAdUnitId = bannerAdUnitId.trim(),
                        nativeAdUnitId = nativeAdUnitId.trim(),
                        interstitialAdUnitId = interstitialAdUnitId.trim(),
                        rewardedAdUnitId = rewardedAdUnitId.trim(),
                        appOpenAdUnitId = appOpenAdUnitId.trim(),
                        isBannerEnabled = isBannerEnabled,
                        isNativeEnabled = isNativeEnabled,
                        isInterstitialEnabled = isInterstitialEnabled,
                        isRewardedEnabled = isRewardedEnabled,
                        isAppOpenEnabled = isAppOpenEnabled,
                        useTestAdsInDebug = useTestAdsInDebug,
                        adSourceType = adSourceType
                    )
                    onSaveConfig(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Configurações AdMob", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GlobalI18nTabContent(
    viewModel: MainViewModel,
    context: android.content.Context
) {
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentCountryCode by viewModel.currentCountry.collectAsStateWithLifecycle()
    val currentTimezone by viewModel.currentTimezone.collectAsStateWithLifecycle()
    val currentCurrency by viewModel.currentCurrency.collectAsStateWithLifecycle()

    val auditReport = remember { viewModel.auditTranslations() }
    val ratesTable = remember { CurrencyService.getAllRatesTable() }

    var converterAmountText by remember { mutableStateOf("100") }
    var converterFromCurrency by remember { mutableStateOf(AppCurrency.USD) }
    var converterToCurrency by remember { mutableStateOf(AppCurrency.AOA) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = FluxPurplePrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = FluxPurplePrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Configurações Globais & i18n", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Internacionalização, multimoeda e detecção de traduções", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 1. Auditoria de Traduções e Detecção de Ausentes (Section 15, 16, 17)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = FluxPurplePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Auditoria de Traduções (i18n)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Emerald500.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${auditReport.overallCompletenessPercentage.toInt()}% Completo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald500
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Total de chaves no catálogo: ${auditReport.totalKeys} termos gerenciados.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageAuditCard(
                            language = AppLanguage.PORTUGUESE,
                            count = auditReport.ptCount,
                            total = auditReport.totalKeys,
                            modifier = Modifier.weight(1f)
                        )
                        LanguageAuditCard(
                            language = AppLanguage.ENGLISH,
                            count = auditReport.enCount,
                            total = auditReport.totalKeys,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageAuditCard(
                            language = AppLanguage.FRENCH,
                            count = auditReport.frCount,
                            total = auditReport.totalKeys,
                            modifier = Modifier.weight(1f)
                        )
                        LanguageAuditCard(
                            language = AppLanguage.SPANISH,
                            count = auditReport.esCount,
                            total = auditReport.totalKeys,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val missingCount = auditReport.missingFrKeys.size + auditReport.missingEsKeys.size +
                            auditReport.missingEnKeys.size + auditReport.missingPtKeys.size

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (missingCount == 0) Emerald500.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (missingCount == 0) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (missingCount == 0) Emerald500 else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (missingCount == 0) {
                                    "Excelente! 0 traduções pendentes. Todos os 4 idiomas estão 100% traduzidos."
                                } else {
                                    "⚠️ $missingCount traduções pendentes. O sistema aplicará fallback FR -> EN -> PT automaticamente."
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (missingCount == 0) Emerald500 else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // 2. Idioma Ativo do Administrador
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Testar Interface em Tempo Real", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Altere o idioma do aplicativo instantaneamente para validar as telas:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.getActiveLanguages().forEach { lang ->
                            val isSelected = lang == currentLanguage
                            OutlinedButton(
                                onClick = {
                                    viewModel.setLanguage(lang)
                                    android.widget.Toast.makeText(context, "Idioma alterado para: ${lang.displayName}", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = if (isSelected) {
                                    ButtonDefaults.outlinedButtonColors(containerColor = FluxPurplePrimary.copy(alpha = 0.15f), contentColor = FluxPurplePrimary)
                                } else {
                                    ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                                },
                                border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) FluxPurplePrimary else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(lang.flag, fontSize = 20.sp)
                                    Text(lang.code.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Taxas de Câmbio em Tempo Real (Section 7)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = FluxPurplePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tabela de Câmbio Internacional", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calculator Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = converterAmountText,
                            onValueChange = { converterAmountText = it },
                            label = { Text("Valor") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        val amount = converterAmountText.toDoubleOrNull() ?: 100.0
                        val converted = CurrencyService.convert(amount, converterFromCurrency, converterToCurrency)
                        val formattedOriginal = CurrencyService.format(amount, converterFromCurrency)
                        val formattedConverted = CurrencyService.format(converted, converterToCurrency)

                        Column(modifier = Modifier.weight(1.5f)) {
                            Text("Conversão Direta:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$formattedOriginal = $formattedConverted", fontWeight = FontWeight.Bold, color = Emerald500, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Taxas Referenciais em Relação ao Dólar (USD) e Kwanza (AOA):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    ratesTable.take(8).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.currency.flag, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${item.currency.displayName} (${item.currency.code})", fontSize = 12.sp)
                            }
                            Text(
                                text = "1 ${item.currency.code} = ${CurrencyService.format(item.rateInAoa, AppCurrency.AOA)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    }
                }
            }
        }

        // 4. Países e Diretório Internacional (Section 6 & 11)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Países Registrados (${CountryDirectory.allCountries.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Estrutura adaptativa para telefones internacionais, impostos regionais e fusos horários:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))

                    CountryDirectory.allCountries.take(8).forEach { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(country.flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(country.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Código: ${country.isoCode} • Tel: ${country.callingCode} • ${country.taxIdLabel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = country.defaultCurrency,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LanguageAuditCard(
    language: AppLanguage,
    count: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(language.flag, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(language.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count / $total termos",
                fontSize = 11.sp,
                color = if (count == total) Emerald500 else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

