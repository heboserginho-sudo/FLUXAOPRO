package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.data.service.findActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.service.PointsSummary
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.I18nService
import com.example.data.i18n.LocalI18nLanguage
import com.example.data.local.AppDatabase
import com.example.data.model.AppThemeMode
import com.example.data.repository.LocalePreferenceRepository
import com.example.data.repository.AccountRepository
import com.example.data.ads.AdScreenContext
import com.example.data.ads.AdsConfigurationRepository
import com.example.data.repository.AppointmentRepository
import com.example.util.AdsManager
import com.example.data.repository.BudgetRepository
import com.example.data.repository.CashFlowRepository
import com.example.data.repository.CategoryBudgetRepository
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ClientRepository
import com.example.data.repository.CompanyRepository
import com.example.data.repository.CurrencyPreferenceRepository
import com.example.data.repository.FinancialGoalRepository
import com.example.data.repository.InvoiceCustomizationRepository
import com.example.data.repository.InvoiceRepository
import com.example.data.repository.MulticaixaExpressRepository
import com.example.data.repository.NotificationRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.ProjectRepository
import com.example.data.repository.SaleRepository
import com.example.data.repository.SubscriptionRepository
import com.example.data.repository.SupplierRepository
import com.example.data.repository.ThemePreferenceRepository
import com.example.ui.components.CurrencySelectionDialog
import com.example.ui.components.ExitAppDialog
import com.example.ui.components.MovementDialog
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.AccountTransferDialog
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.BudgetEditorScreen
import com.example.ui.screens.CashFlowScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CategoryBudgetScreen
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.CompanySetupAndPaymentScreen
import com.example.ui.screens.CustomerPortalScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinancialGoalsScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.MoreMenuScreen
import com.example.ui.screens.NotificationsCenterScreen
import com.example.ui.screens.PointsAndRewardsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SuperAdminDashboardScreen
import com.example.ui.theme.FluxPurplePrimary
import com.example.ui.theme.FluxPurpleContainer
import com.example.ui.theme.FluxTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

enum class MainTab(val translationKey: String, val fallbackLabel: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("home.tab_dashboard", "Início", Icons.Default.Dashboard, "tab_dashboard"),
    CASH_FLOW("home.tab_cash_flow", "Transações", Icons.Default.Payments, "tab_cash_flow"),
    BUDGETS("home.tab_budgets", "Orçamento", Icons.Default.Calculate, "tab_budgets"),
    REPORTS("home.tab_reports", "Relatórios", Icons.Default.Assessment, "tab_reports"),
    MORE("home.tab_more", "Mais", Icons.Default.MoreHoriz, "tab_more");

    val label: String get() = I18nService.t(translationKey)

    fun getLabel(lang: AppLanguage): String = I18nService.t(translationKey, lang = lang)
}

enum class SubScreen {
    NONE,
    PROJECTS,
    CATEGORIES,
    GOALS,
    AGENDA,
    NOTIFICATIONS,
    AI_ASSISTANT,
    PROFILE,
    SETTINGS,
    ACCOUNTS,
    CLIENTS,
    INVOICES,
    SUPER_ADMIN,
    CUSTOMER_PORTAL,
    POINTS_AND_REWARDS
}

private data class PendingLockedFeatureReq(
    val code: String,
    val title: String,
    val description: String,
    val pointsRequired: Int,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val cashFlowRepo = CashFlowRepository(database.transactionDao())
        val budgetRepo = BudgetRepository(database.budgetDao())
        val invoiceRepo = InvoiceRepository(database.invoiceDao(), database.transactionDao())
        val currencyRepo = CurrencyPreferenceRepository(applicationContext)
        val subscriptionRepo = SubscriptionRepository(applicationContext)
        val mcxRepo = MulticaixaExpressRepository(applicationContext)
        val clientRepo = ClientRepository(database.clientDao())
        val customizationRepo = InvoiceCustomizationRepository(applicationContext)
        val themeRepo = ThemePreferenceRepository(applicationContext)
        val accountRepo = AccountRepository(database.accountDao(), database.transactionDao())
        val supplierRepo = SupplierRepository(database.supplierDao())
        val productRepo = ProductRepository(database.productDao())
        val saleRepo = SaleRepository(database.saleDao(), database.transactionDao(), database.productDao(), database.accountDao())
        val projectRepo = ProjectRepository(database.projectDao(), database.transactionDao())
        val notificationRepo = NotificationRepository(database.notificationDao())
        val categoryRepo = CategoryRepository(database.categoryDao())
        val categoryBudgetRepo = CategoryBudgetRepository(database.categoryBudgetDao())
        val goalRepo = FinancialGoalRepository(database.financialGoalDao())
        val appointmentRepo = AppointmentRepository(database.appointmentDao(), database.blockedSlotDao())
        val companyRepo = CompanyRepository(
            database.companyDao(),
            database.companySettingsDao(),
            database.companyBrandingDao(),
            database.profileDao(),
            database.planDao(),
            database.subscriptionDao(),
            database.auditLogDao()
        )
        val adsConfigRepo = AdsConfigurationRepository(applicationContext)
        val localeRepo = LocalePreferenceRepository(applicationContext)
        val welcomeSplashRepo = com.example.data.repository.WelcomeSplashPreferenceRepository(applicationContext)
        MainViewModel.Factory(
            cashFlowRepo,
            budgetRepo,
            invoiceRepo,
            currencyRepo,
            subscriptionRepo,
            mcxRepo,
            clientRepo,
            customizationRepo,
            themeRepo,
            accountRepo,
            supplierRepo,
            productRepo,
            saleRepo,
            projectRepo,
            notificationRepo,
            categoryRepo,
            categoryBudgetRepo,
            goalRepo,
            appointmentRepo,
            companyRepo,
            adsConfigRepo,
            localeRepo,
            database.userPointsDao(),
            database.pointsTransactionDao(),
            database.userEntitlementDao(),
            database.userFeatureUnlockDao(),
            welcomeSplashRepo
        )
    }

    private var initialSubScreenState = mutableStateOf(SubScreen.NONE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsManager.initialize(applicationContext)
        com.example.data.service.AdMobRewardService.getInstance().initialize(applicationContext)
        com.example.data.service.AppOpenAdService.getInstance().initialize(applicationContext)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
            val themeMode by viewModel.currentThemeMode.collectAsStateWithLifecycle()
            val customization by viewModel.invoiceCustomization.collectAsStateWithLifecycle()
            val customLogoUri: android.net.Uri? = remember(customization.logoLocalPath) {
                if (customization.logoLocalPath.isNotBlank()) {
                    android.net.Uri.parse(customization.logoLocalPath)
                } else null
            }
            CompositionLocalProvider(
                LocalI18nLanguage provides currentLanguage,
                LocalLayoutDirection provides (if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr)
            ) {
                MyApplicationTheme(
                    themeMode = themeMode,
                    customLogoUri = customLogoUri
                ) {
                    MainAppContent(viewModel = viewModel, initialSubScreen = initialSubScreenState.value)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent?.action == "OPEN_APPOINTMENT" || intent?.hasExtra("extra_appointment_id") == true) {
            initialSubScreenState.value = SubScreen.AGENDA
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel, initialSubScreen: SubScreen = SubScreen.NONE) {
    var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    var currentSubScreen by remember(initialSubScreen) { mutableStateOf(initialSubScreen) }
    var isEditingBudget by remember { mutableStateOf(false) }
    var showQuickTransactionSheet by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    val subscription by viewModel.subscription.collectAsStateWithLifecycle()
    val isCompanyProfileComplete = subscription.companyName.trim().isNotBlank() &&
        subscription.nif.trim().isNotBlank() &&
        subscription.contactPhone.trim().isNotBlank()

    var showCompanyPanel by remember(isCompanyProfileComplete) {
        mutableStateOf(!isCompanyProfileComplete)
    }
    var showExitDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val budgetEditorState by viewModel.budgetEditorState.collectAsStateWithLifecycle()
    val livePricingResult by viewModel.livePricingResult.collectAsStateWithLifecycle()
    val currentCurrency by viewModel.currentCurrency.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val clients by viewModel.allClients.collectAsStateWithLifecycle()
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val themeMode by viewModel.currentThemeMode.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount = remember(notifications) { notifications.count { !it.isRead } }
    val adsConfig by viewModel.adsConfiguration.collectAsStateWithLifecycle()

    val userPoints by viewModel.userPointsFlow.collectAsStateWithLifecycle(initialValue = null)
    val pointsSummary by viewModel.pointsSummaryFlow.collectAsStateWithLifecycle(initialValue = PointsSummary())
    val pointsTransactions by viewModel.pointsTransactionsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val showInsufficientPointsDialog by viewModel.showInsufficientPointsDialog.collectAsStateWithLifecycle(initialValue = false)
    val insufficientPointsCurrentBalance by viewModel.insufficientPointsCurrentBalance.collectAsStateWithLifecycle(initialValue = 0)

    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayPointsEarned = remember(pointsTransactions) {
        pointsTransactions
            .filter { it.type == "earn" && it.createdAtEpoch >= todayStart }
            .sumOf { it.points }
    }

    val unlockedFeatures by viewModel.unlockedFeaturesState.collectAsStateWithLifecycle()
    var isWatchingAd by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showTransferDialog by remember { mutableStateOf(false) }
    var pendingLockedFeatureReq by remember { mutableStateOf<PendingLockedFeatureReq?>(null) }
    val hasSeenWelcomeSplash by viewModel.hasSeenWelcomeSplash.collectAsStateWithLifecycle()

    LaunchedEffect(currentSubScreen, isEditingBudget, isWatchingAd, showCompanyPanel, hasSeenWelcomeSplash) {
        val isCritical = !hasSeenWelcomeSplash ||
                currentSubScreen == SubScreen.INVOICES ||
                currentSubScreen == SubScreen.CLIENTS ||
                isEditingBudget ||
                isWatchingAd ||
                showCompanyPanel
        com.example.data.service.AppOpenAdService.getInstance().setCriticalFlowActive(isCritical)
    }

    fun tryOpenFeature(code: String, title: String, desc: String, cost: Int, action: () -> Unit) {
        if (com.example.data.model.FeatureKeyHelper.isUnlocked(unlockedFeatures, code) || viewModel.isFeatureUnlocked(code)) {
            action()
        } else {
            pendingLockedFeatureReq = PendingLockedFeatureReq(code, title, desc, cost, action)
        }
    }

    // Unified Back button management
    BackHandler(enabled = true) {
        when {
            !hasSeenWelcomeSplash -> {
                (context as? android.app.Activity)?.finish()
            }
            !isCompanyProfileComplete -> {
                showExitDialog = true
            }
            showCompanyPanel -> {
                showCompanyPanel = false
            }
            currentSubScreen != SubScreen.NONE -> {
                currentSubScreen = SubScreen.NONE
            }
            isEditingBudget -> {
                isEditingBudget = false
            }
            currentTab != MainTab.DASHBOARD -> {
                currentTab = MainTab.DASHBOARD
            }
            else -> {
                showExitDialog = true
            }
        }
    }

    if (!hasSeenWelcomeSplash) {
        com.example.ui.screens.InformativeSplashScreen(
            viewModel = viewModel,
            userBalance = userPoints?.balance ?: 0,
            unlockedFeatures = unlockedFeatures,
            onContinue = {
                viewModel.markWelcomeSplashSeen()
                val activity = context.findActivity()
                if (activity != null) {
                    com.example.data.service.AppOpenAdService.getInstance().showAppOpenAdIfAvailable(activity)
                }
            }
        )
        return
    }

    if (!isCompanyProfileComplete || showCompanyPanel) {
        CompanySetupAndPaymentScreen(
            subscription = subscription,
            viewModel = viewModel,
            canDismiss = isCompanyProfileComplete,
            onContinueToApp = {
                if (isCompanyProfileComplete) {
                    showCompanyPanel = false
                }
            },
            onExitApp = { showExitDialog = true }
        )
    } else if (currentSubScreen != SubScreen.NONE) {
        when (currentSubScreen) {
            SubScreen.PROJECTS -> {
                ProjectsScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = SubScreen.NONE }
                )
            }
            SubScreen.CATEGORIES -> {
                CategoriesScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = SubScreen.NONE }
                )
            }
            SubScreen.GOALS -> {
                FinancialGoalsScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = SubScreen.NONE }
                )
            }
            SubScreen.AGENDA -> {
                AgendaScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = SubScreen.NONE }
                )
            }
            SubScreen.NOTIFICATIONS -> {
                NotificationsCenterScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = SubScreen.NONE },
                    onNavigateAction = { route ->
                        currentSubScreen = when (route) {
                            "budgets" -> SubScreen.NONE.also { currentTab = MainTab.BUDGETS }
                            "goals" -> SubScreen.GOALS
                            "agenda" -> SubScreen.AGENDA
                            "transactions" -> SubScreen.NONE.also { currentTab = MainTab.CASH_FLOW }
                            "projects" -> SubScreen.PROJECTS
                            else -> SubScreen.NONE
                        }
                    }
                )
            }
            SubScreen.AI_ASSISTANT -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Assistente Inteligente (FluxAO IA)", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { currentSubScreen = SubScreen.NONE }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                    }
                ) { pad ->
                    Box(modifier = Modifier.padding(pad)) {
                        AiAssistantScreen(
                            messages = aiMessages,
                            isThinking = isAiThinking,
                            uiState = uiState,
                            currency = currentCurrency,
                            onSendMessage = { prompt -> viewModel.sendAiMessage(prompt) },
                            onClearChat = { viewModel.clearAiChat() }
                        )
                    }
                }
            }
            SubScreen.PROFILE -> {
                ProfileScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = SubScreen.NONE }
                )
            }
            SubScreen.SETTINGS -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentSubScreen = SubScreen.NONE }
                )
            }
            SubScreen.ACCOUNTS -> {
                AccountsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentSubScreen = SubScreen.NONE }
                )
            }
            SubScreen.CLIENTS -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Clientes & Entidades", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { currentSubScreen = SubScreen.NONE }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                    }
                ) { pad ->
                    Box(modifier = Modifier.padding(pad)) {
                        ClientsScreen(viewModel = viewModel)
                    }
                }
            }
            SubScreen.INVOICES -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Faturas Comerciais", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { currentSubScreen = SubScreen.NONE }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                    }
                ) { pad ->
                    Box(modifier = Modifier.padding(pad)) {
                        InvoicesScreen(
                            invoices = invoices,
                            subscription = subscription,
                            viewModel = viewModel,
                            onNavigateToClients = { currentSubScreen = SubScreen.CLIENTS }
                        )
                    }
                }
            }
            SubScreen.SUPER_ADMIN -> {
                SuperAdminDashboardScreen(
                    viewModel = viewModel,
                    onBackToApp = { currentSubScreen = SubScreen.NONE }
                )
            }
            SubScreen.CUSTOMER_PORTAL -> {
                CustomerPortalScreen(
                    viewModel = viewModel,
                    adsConfig = adsConfig,
                    onLogout = { currentSubScreen = SubScreen.NONE }
                )
            }
            SubScreen.POINTS_AND_REWARDS -> {
                PointsAndRewardsScreen(
                    viewModel = viewModel,
                    onBackClick = { currentSubScreen = SubScreen.NONE }
                )
            }
            SubScreen.NONE -> {}
        }
    } else if (isEditingBudget) {
        BudgetEditorScreen(
            state = budgetEditorState,
            pricing = livePricingResult,
            clients = clients,
            onUpdateState = { update -> viewModel.updateBudgetEditor(update) },
            onAddMaterial = { desc, qty, price -> viewModel.addMaterialItem(desc, qty, price) },
            onRemoveMaterial = { idx -> viewModel.removeMaterialItem(idx) },
            onSaveBudget = {
                viewModel.saveCurrentBudget {
                    isEditingBudget = false
                }
            },
            onBack = { isEditingBudget = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showQuickTransactionSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(12.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                    modifier = Modifier.testTag("global_quick_add_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = I18nService.t("home.new_transaction", lang = currentLanguage))
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    windowInsets = NavigationBarDefaults.windowInsets,
                    modifier = Modifier
                        .testTag("bottom_nav_bar")
                        .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant))
                ) {
                    MainTab.values().forEach { tab ->
                        val selected = currentTab == tab
                        val tabTitle = tab.getLabel(currentLanguage)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (tab == MainTab.REPORTS) {
                                    tryOpenFeature("reports", "Relatórios", "Relatórios financeiros avançados, DRE e estatísticas.", 50) {
                                        currentTab = MainTab.REPORTS
                                    }
                                } else {
                                    currentTab = tab
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tabTitle,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tabTitle,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = FluxPurplePrimary,
                                selectedTextColor = FluxPurplePrimary,
                                indicatorColor = FluxPurpleContainer,
                                unselectedIconColor = FluxTextSecondary,
                                unselectedTextColor = FluxTextSecondary
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    MainTab.DASHBOARD -> DashboardScreen(
                        uiState = uiState,
                        currentCurrency = currentCurrency,
                        subscription = subscription,
                        accounts = accounts,
                        unreadNotificationsCount = unreadNotificationsCount,
                        pointsBalance = userPoints?.balance ?: 0,
                        todayPointsEarned = todayPointsEarned,
                        rankTitle = pointsSummary.rankTitle,
                        onWatchAdClick = {
                            if (!isWatchingAd) {
                                isWatchingAd = true
                                coroutineScope.launch {
                                    viewModel.watchRewardedAd(
                                        context = context,
                                        onRewardEarned = { pts ->
                                            isWatchingAd = false
                                            Toast.makeText(context, "+$pts pontos recebidos!", Toast.LENGTH_LONG).show()
                                        },
                                        onError = { err ->
                                            isWatchingAd = false
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        },
                        onViewRewardsClick = { currentSubScreen = SubScreen.POINTS_AND_REWARDS },
                        isLoadingAd = isWatchingAd,
                        themeMode = themeMode,
                        onToggleTheme = { viewModel.toggleTheme() },
                        onNavigateToAi = {
                            tryOpenFeature("fluxoai", "FluxoAI", "Consultoria e assistente financeiro inteligente em tempo real.", 80) {
                                currentSubScreen = SubScreen.AI_ASSISTANT
                            }
                        },
                        onNavigateToNotifications = { currentSubScreen = SubScreen.NOTIFICATIONS },
                        onNavigateToMenu = { currentTab = MainTab.MORE },
                        onOpenCurrencySelector = { showCurrencyDialog = true },
                        onOpenCompanyPanel = { showCompanyPanel = true },
                        onNavigateToCashFlow = { currentTab = MainTab.CASH_FLOW },
                        onNavigateToAccounts = { currentSubScreen = SubScreen.ACCOUNTS },
                        onNavigateToBudgets = { currentTab = MainTab.BUDGETS },
                        onNavigateToInvoices = {
                            tryOpenFeature("invoices", "Faturas", "Emita e gerencie suas faturas comerciais e fiscais.", 50) {
                                currentSubScreen = SubScreen.INVOICES
                            }
                        },
                        onNavigateToClients = {
                            tryOpenFeature("customers", "Clientes", "Gerencie sua carteira de clientes, contatos e NIFs.", 30) {
                                currentSubScreen = SubScreen.CLIENTS
                            }
                        },
                        onQuickAddTransaction = { showQuickTransactionSheet = true },
                        onQuickTransfer = { showTransferDialog = true },
                        onToggleTransactionStatus = { tx -> viewModel.toggleTransactionStatus(tx) },
                        onSelectPeriodFilter = { viewModel.setPeriodFilter(it) },
                        onExitClick = { showExitDialog = true },
                        adsConfig = adsConfig
                    )

                    MainTab.CASH_FLOW -> CashFlowScreen(
                        viewModel = viewModel
                    )

                    MainTab.BUDGETS -> CategoryBudgetScreen(
                        viewModel = viewModel,
                        budgets = budgets,
                        currentCurrency = currentCurrency,
                        onOpenBudgetEditor = {
                            viewModel.resetBudgetEditor()
                            isEditingBudget = true
                        },
                        onEditExistingBudget = { budget ->
                            viewModel.loadBudgetForEditing(budget)
                            isEditingBudget = true
                        },
                        onDeleteExistingBudget = { budgetId ->
                            viewModel.deleteBudget(budgetId)
                        }
                    )

                    MainTab.REPORTS -> ReportsScreen(
                        uiState = uiState,
                        budgets = budgets,
                        currentCurrency = currentCurrency,
                        subscription = subscription,
                        onOpenCurrencySelector = { showCurrencyDialog = true },
                        onOpenCompanyPanel = { showCompanyPanel = true },
                        onExitClick = { showExitDialog = true }
                    )

                    MainTab.MORE -> MoreMenuScreen(
                        viewModel = viewModel,
                        onNavigateToProjects = { currentSubScreen = SubScreen.PROJECTS },
                        onNavigateToCategories = { currentSubScreen = SubScreen.CATEGORIES },
                        onNavigateToGoals = { currentSubScreen = SubScreen.GOALS },
                        onNavigateToAgenda = {
                            tryOpenFeature("appointments", "Agendamentos", "Gerencie seus compromissos e agenda comercial.", 30) {
                                currentSubScreen = SubScreen.AGENDA
                            }
                        },
                        onNavigateToNotifications = { currentSubScreen = SubScreen.NOTIFICATIONS },
                        onNavigateToAiAssistant = {
                            tryOpenFeature("fluxoai", "FluxoAI", "Consultoria e assistente financeiro inteligente em tempo real.", 80) {
                                currentSubScreen = SubScreen.AI_ASSISTANT
                            }
                        },
                        onNavigateToProfile = { currentSubScreen = SubScreen.PROFILE },
                        onNavigateToSettings = { currentSubScreen = SubScreen.SETTINGS },
                        onNavigateToAccounts = { currentSubScreen = SubScreen.ACCOUNTS },
                        onNavigateToClients = {
                            tryOpenFeature("customers", "Clientes", "Gerencie sua carteira de clientes, contatos e NIFs.", 30) {
                                currentSubScreen = SubScreen.CLIENTS
                            }
                        },
                        onNavigateToInvoices = {
                            tryOpenFeature("invoices", "Faturas", "Emita e gerencie suas faturas comerciais e fiscais.", 50) {
                                currentSubScreen = SubScreen.INVOICES
                            }
                        },
                        onNavigateToSuperAdmin = { currentSubScreen = SubScreen.SUPER_ADMIN },
                        onNavigateToCustomerPortal = { currentSubScreen = SubScreen.CUSTOMER_PORTAL },
                        onNavigateToPoints = { currentSubScreen = SubScreen.POINTS_AND_REWARDS },
                        onExitDashboard = { showExitDialog = true }
                    )
                }
            }
        }
    }

    if (showQuickTransactionSheet) {
        MovementDialog(
            accounts = accounts,
            projects = projects,
            onDismiss = { showQuickTransactionSheet = false },
            onSave = { type, amount, desc, category, date, method, status, notes, accId, accName, destId, destName, contact, contactType, attach, projId ->
                viewModel.addTransaction(
                    type = type,
                    amount = amount,
                    description = desc,
                    category = category,
                    dateMillis = date,
                    paymentMethod = method,
                    status = status,
                    notes = notes,
                    accountId = accId,
                    accountName = accName,
                    destinationAccountId = destId,
                    destinationAccountName = destName,
                    contactName = contact,
                    contactType = contactType,
                    attachmentUri = attach,
                    projectId = projId
                )
                showQuickTransactionSheet = false
            }
        )
    }

    if (showTransferDialog) {
        AccountTransferDialog(
            accounts = accounts.filter { it.isActive },
            onDismiss = { showTransferDialog = false },
            onTransfer = { src, dst, amount, desc, notes ->
                viewModel.transferBetweenAccounts(src, dst, amount, desc, notes)
                showTransferDialog = false
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            selectedCurrency = currentCurrency,
            onCurrencySelected = { newCurrency ->
                viewModel.setCurrency(newCurrency)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showExitDialog) {
        ExitAppDialog(
            companyName = if (isCompanyProfileComplete) subscription.companyName else "",
            onLockSession = {
                showExitDialog = false
                currentSubScreen = SubScreen.NONE
                currentTab = MainTab.DASHBOARD
                showCompanyPanel = true
            },
            onDismiss = { showExitDialog = false }
        )
    }

    if (showInsufficientPointsDialog) {
        com.example.ui.components.InsufficientPointsDialog(
            currentBalance = insufficientPointsCurrentBalance,
            requiredPoints = 2,
            isLoadingAd = isWatchingAd,
            onWatchAdClick = {
                if (!isWatchingAd) {
                    isWatchingAd = true
                    coroutineScope.launch {
                        viewModel.watchRewardedAd(
                            context = context,
                            onRewardEarned = { pts ->
                                isWatchingAd = false
                                Toast.makeText(context, "+$pts pontos recebidos! Ação retomada automaticamente.", Toast.LENGTH_LONG).show()
                            },
                            onError = { err ->
                                isWatchingAd = false
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            },
            onDismiss = { viewModel.dismissInsufficientPointsDialog() }
        )
    }

    pendingLockedFeatureReq?.let { req ->
        com.example.ui.components.FeatureLockDialog(
            featureCode = req.code,
            featureTitle = req.title,
            featureDescription = req.description,
            pointsRequired = req.pointsRequired,
            durationDays = 30,
            currentBalance = userPoints?.balance ?: 0,
            viewModel = viewModel,
            onDismiss = { pendingLockedFeatureReq = null },
            onUnlocked = {
                val action = req.action
                pendingLockedFeatureReq = null
                action()
            }
        )
    }
}
