package com.example.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AppointmentEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BudgetItemEntity
import com.example.data.local.entity.CategoryBudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.FinancialGoalEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AiChatMessage
import com.example.data.model.AiSender
import com.example.data.model.AppCurrency
import com.example.data.model.AppThemeMode
import com.example.data.model.BudgetItemType
import com.example.data.model.BudgetStatus
import com.example.data.ads.AdsConfiguration
import com.example.data.ads.AdsConfigurationRepository
import com.example.data.ads.AdScreenContext
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.PlanEntity
import com.example.data.local.entity.SubscriptionEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.model.CompanyContext
import com.example.data.model.CompanySubscription
import com.example.data.model.FinancialCategory
import com.example.data.model.InvoiceCustomization
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.invoice.InvoiceItemData
import com.example.data.model.invoice.InvoiceStatus
import com.example.data.model.invoice.InvoiceType
import com.example.data.repository.AccountRepository
import com.example.data.repository.AppointmentRepository
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
import com.example.data.repository.McxPaymentInitResult
import com.example.data.repository.MulticaixaExpressRepository
import com.example.data.repository.NotificationRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.ProjectRepository
import com.example.data.repository.SaleRepository
import com.example.data.repository.SubscriptionRepository
import com.example.data.repository.SupplierRepository
import com.example.data.repository.ThemePreferenceRepository
import com.example.data.repository.LocalePreferenceRepository
import com.example.data.repository.WelcomeSplashPreferenceRepository
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.CountryDirectory
import com.example.data.i18n.CurrencyService
import com.example.data.i18n.I18nService
import com.example.data.i18n.TranslationAuditReport
import com.example.data.local.dao.UserPointsDao
import com.example.data.local.dao.PointsTransactionDao
import com.example.data.local.dao.UserFeatureUnlockDao
import com.example.data.model.FeatureKeyHelper
import com.example.data.service.AntiFraudService
import com.example.data.service.AdsRewardService
import com.example.data.service.AwardPointsResult
import com.example.data.service.ConsumePointsResult
import com.example.data.service.EntitlementService
import com.example.data.service.FeatureUnlockService
import com.example.data.service.PointsService
import com.example.data.service.RewardService
import com.example.data.service.RewardItem
import com.example.data.service.PointsTransactionService
import com.example.data.service.GeminiAiService
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.domain.model.PricingCalculationResult
import com.example.util.AppointmentScheduler
import com.example.util.NotificationScheduler
import android.content.Context
import android.net.Uri
import com.example.ui.components.CategoryShare
import com.example.ui.components.MonthlyBarData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

enum class PeriodFilter(val label: String) {
    THIS_MONTH("Este Mês"),
    LAST_30_DAYS("Últimos 30 Dias"),
    ALL("Todos")
}

data class FilterState(
    val period: PeriodFilter = PeriodFilter.THIS_MONTH,
    val type: TransactionType? = null,
    val status: TransactionStatus? = null,
    val category: String? = null,
    val query: String = ""
)

data class CashFlowUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val currentBalance: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val monthNetProfit: Double = 0.0,
    val monthNetMarginPercent: Double = 0.0,
    val pendingIncome: Double = 0.0,
    val pendingExpense: Double = 0.0,
    val monthlyBars: List<MonthlyBarData> = emptyList(),
    val categoryShares: List<CategoryShare> = emptyList(),
    val periodFilter: PeriodFilter = PeriodFilter.THIS_MONTH,
    val typeFilter: TransactionType? = null,
    val statusFilter: TransactionStatus? = null,
    val categoryFilter: String? = null,
    val searchQuery: String = ""
)

data class BudgetEditorState(
    val budgetId: Long = 0,
    val clientName: String = "",
    val clientContact: String = "",
    val projectTitle: String = "",
    val projectScope: String = "",
    val deadlineDays: Int = 15,
    val status: BudgetStatus = BudgetStatus.DRAFT,
    val materialsList: List<BudgetItemEntity> = emptyList(),
    val laborPercentageOnMaterials: Double = 85.0, // Método Angolano (85% da matéria-prima)
    val laborHours: Double = 0.0,
    val laborHourlyRate: Double = 0.0,
    val overheadPercentage: Double = 0.0,
    val taxesAndSalesFeePercent: Double = 14.0, // IVA AGT padrão de Angola (14%)
    val desiredProfitMarginPercent: Double = 80.0, // Margem de Lucro Líquido no Método Angolano (70% a 100%+)
    val discountAmount: Double = 0.0,
    val paymentTerms: String = "50% de sinal e 50% na conclusão",
    val termsAndConditions: String = "Proposta válida por 15 dias. Conforme regulamentação comercial e fiscal em Angola.",
    val notes: String = ""
)

class MainViewModel(
    private val cashFlowRepository: CashFlowRepository,
    private val budgetRepository: BudgetRepository,
    private val invoiceRepository: InvoiceRepository,
    private val currencyRepository: CurrencyPreferenceRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val mcxRepository: MulticaixaExpressRepository,
    private val clientRepository: ClientRepository,
    private val customizationRepository: InvoiceCustomizationRepository,
    private val themePreferenceRepository: ThemePreferenceRepository,
    private val accountRepository: AccountRepository,
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val projectRepository: ProjectRepository,
    private val notificationRepository: NotificationRepository,
    private val categoryRepository: CategoryRepository,
    private val categoryBudgetRepository: CategoryBudgetRepository,
    private val financialGoalRepository: FinancialGoalRepository,
    private val appointmentRepository: AppointmentRepository,
    private val companyRepository: CompanyRepository,
    private val adsConfigurationRepository: AdsConfigurationRepository,
    private val localePreferenceRepository: LocalePreferenceRepository,
    private val userPointsDao: UserPointsDao,
    private val pointsTransactionDao: PointsTransactionDao,
    private val userEntitlementDao: com.example.data.local.dao.UserEntitlementDao? = null,
    private val userFeatureUnlockDao: UserFeatureUnlockDao? = null,
    private val welcomeSplashPreferenceRepository: WelcomeSplashPreferenceRepository? = null
) : ViewModel() {

    val currentCurrency: StateFlow<AppCurrency> = currencyRepository.currentCurrency
    val subscription: StateFlow<CompanySubscription> = subscriptionRepository.subscription
    val invoiceCustomization: StateFlow<InvoiceCustomization> = customizationRepository.customization
    val currentThemeMode: StateFlow<AppThemeMode> = themePreferenceRepository.currentThemeMode
    val adsConfiguration: StateFlow<AdsConfiguration> = adsConfigurationRepository.config

    val hasSeenWelcomeSplash: StateFlow<Boolean> =
        welcomeSplashPreferenceRepository?.hasSeenWelcomeSplash ?: MutableStateFlow(true)

    fun markWelcomeSplashSeen() {
        welcomeSplashPreferenceRepository?.markWelcomeSplashCompleted()
    }

    fun resetWelcomeSplash() {
        welcomeSplashPreferenceRepository?.resetWelcomeSplash()
    }

    // Points & Rewards Services
    val antiFraudService = AntiFraudService(pointsTransactionDao)
    val adsRewardService = AdsRewardService(antiFraudService)
    val pointsService = PointsService(userPointsDao, pointsTransactionDao, antiFraudService)
    val entitlementService = userEntitlementDao?.let { EntitlementService(it) }
    val rewardService = RewardService(pointsService, entitlementService)
    val pointsTransactionService = PointsTransactionService(pointsService)

    // Feature Unlock Service (Persistent Room + Supabase Sync + Fast Cache)
    val featureUnlockService = userFeatureUnlockDao?.let {
        FeatureUnlockService(it, userEntitlementDao, pointsService, viewModelScope)
    }

    val unlockedFeaturesState: StateFlow<Set<String>> =
        featureUnlockService?.unlockedFeaturesState ?: MutableStateFlow(emptySet())

    val userPointsFlow = pointsService.getUserPointsFlow("default_user")
    val pointsSummaryFlow = pointsTransactionService.getSummaryFlow("default_user")
    val pointsTransactionsFlow = pointsTransactionService.getTransactionsFlow("default_user")
    val activeEntitlementsFlow = entitlementService?.getActiveEntitlementsFlow("default_user")

    val activeEntitlementsList: StateFlow<List<com.example.data.local.entity.UserEntitlementEntity>> =
        activeEntitlementsFlow?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        ) ?: MutableStateFlow(emptyList())

    /**
     * Primary feature unlock check for navigation and UI elements.
     * Guaranteed instant (0ms latency) and persistent across app sessions.
     */
    fun isFeatureUnlocked(featureCode: String): Boolean {
        return featureUnlockService?.isFeatureUnlocked("default_user", featureCode)
            ?: (FeatureKeyHelper.normalize(featureCode) == FeatureKeyHelper.CASHFLOW)
    }

    suspend fun checkFeatureUnlock(userId: String = "default_user", featureKey: String): Boolean {
        return featureUnlockService?.checkFeatureUnlock(userId, featureKey)
            ?: (FeatureKeyHelper.normalize(featureKey) == FeatureKeyHelper.CASHFLOW)
    }

    suspend fun redeemFeatureReward(reward: RewardItem): AwardPointsResult {
        if (featureUnlockService != null) {
            return featureUnlockService.unlockFeature(
                userId = "default_user",
                featureKey = reward.code,
                pointsCost = reward.pointsRequired,
                featureTitle = reward.title
            )
        }
        return rewardService.redeemReward("default_user", reward)
    }

    private val _showInsufficientPointsDialog = MutableStateFlow(false)
    val showInsufficientPointsDialog: StateFlow<Boolean> = _showInsufficientPointsDialog.asStateFlow()

    private val _insufficientPointsCurrentBalance = MutableStateFlow(0)
    val insufficientPointsCurrentBalance: StateFlow<Int> = _insufficientPointsCurrentBalance.asStateFlow()

    private var pendingBlockedAction: (() -> Unit)? = null

    fun dismissInsufficientPointsDialog() {
        _showInsufficientPointsDialog.value = false
        pendingBlockedAction = null
    }

    fun checkPointsAndExecute(
        source: String,
        referenceId: String = "",
        description: String? = null,
        onPointsConsumedFeedback: (String) -> Unit = {},
        actionToExecute: () -> Unit
    ) {
        viewModelScope.launch {
            val balance = pointsService.getBalance("default_user")
            val cost = 2
            if (balance >= cost) {
                val consumeRes = pointsService.consumePoints(
                    userId = "default_user",
                    source = source,
                    referenceId = referenceId,
                    customDescription = description,
                    requiredPoints = cost
                )
                if (consumeRes is ConsumePointsResult.Success) {
                    onPointsConsumedFeedback("-$cost pts (Saldo: ${consumeRes.newBalance})")
                    actionToExecute()
                } else {
                    _insufficientPointsCurrentBalance.value = balance
                    pendingBlockedAction = actionToExecute
                    _showInsufficientPointsDialog.value = true
                }
            } else {
                _insufficientPointsCurrentBalance.value = balance
                pendingBlockedAction = actionToExecute
                _showInsufficientPointsDialog.value = true
            }
        }
    }

    suspend fun watchRewardedAd(
        context: Context,
        onRewardEarned: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        adsRewardService.showRewardedAd(
            context = context,
            userId = "default_user",
            onRewardEarned = { points, rewardType, ssvToken ->
                viewModelScope.launch {
                    val result = pointsService.awardPointsForAd(
                        userId = "default_user",
                        ssvToken = ssvToken
                    )
                    when (result) {
                        is AwardPointsResult.Success -> {
                            onRewardEarned(result.pointsAwarded)
                            // Resume blocked action automatically if any was pending!
                            _showInsufficientPointsDialog.value = false
                            val pending = pendingBlockedAction
                            pendingBlockedAction = null
                            pending?.invoke()
                        }
                        is AwardPointsResult.AlreadyAwarded -> onError(result.message)
                        is AwardPointsResult.Failed -> onError(result.reason)
                    }
                }
            },
            onAdFailedOrClosedEarly = { reason -> onError(reason) }
        )
    }

    suspend fun awardPointsForAction(
        source: String,
        referenceId: String = "",
        description: String? = null
    ) {
        if (source == "rewarded_ad") {
            pointsService.awardPointsForAd(userId = "default_user", ssvToken = referenceId)
        } else {
            pointsService.consumePoints(
                userId = "default_user",
                source = source,
                referenceId = referenceId,
                customDescription = description
            )
        }
    }

    suspend fun redeemReward(
        reward: RewardItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val res = rewardService.redeemReward("default_user", reward)
        when (res) {
            is AwardPointsResult.Success -> onSuccess()
            is AwardPointsResult.AlreadyAwarded -> onError(res.message)
            is AwardPointsResult.Failed -> onError(res.reason)
        }
    }

    // Internationalization (i18n & l10n)
    val currentLanguage: StateFlow<AppLanguage> = localePreferenceRepository.currentLanguage
    val currentCountry: StateFlow<String> = localePreferenceRepository.currentCountry
    val currentTimezone: StateFlow<String> = localePreferenceRepository.currentTimezone
    val secondaryCurrency: StateFlow<AppCurrency> = localePreferenceRepository.secondaryCurrency

    fun setLanguage(language: AppLanguage) {
        localePreferenceRepository.setLanguage(language)
    }

    fun setCountry(countryIsoCode: String) {
        localePreferenceRepository.setCountry(countryIsoCode)
        val defaultCurr = CurrencyService.getCurrencyForCountry(countryIsoCode)
        setCurrency(defaultCurr)
    }

    fun setTimezone(timezoneId: String) {
        localePreferenceRepository.setTimezone(timezoneId)
    }

    fun setSecondaryCurrency(currency: AppCurrency) {
        localePreferenceRepository.setSecondaryCurrency(currency)
    }

    fun auditTranslations(): TranslationAuditReport {
        return I18nService.auditMissingTranslations()
    }

    // Multi-tenant SaaS Company Management
    val allCompanies: StateFlow<List<CompanyEntity>> = companyRepository.allCompanies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProfiles: StateFlow<List<ProfileEntity>> = companyRepository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlans: StateFlow<List<PlanEntity>> = companyRepository.allPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubscriptions: StateFlow<List<SubscriptionEntity>> = companyRepository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = companyRepository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeCompanyId = MutableStateFlow("company_sh_moveis")
    val activeCompanyId: StateFlow<String> = _activeCompanyId.asStateFlow()

    val companyContext: StateFlow<CompanyContext> = combine(
        companyRepository.allCompanies,
        _activeCompanyId
    ) { companies, activeId ->
        val current = companies.find { it.id == activeId } ?: companies.firstOrNull()
        val companyId = current?.id ?: activeId
        val branding = companyRepository.getBrandingByCompanyId(companyId)
        val settings = companyRepository.getSettingsByCompanyId(companyId)
        val profile = companyRepository.getProfileById("profile_admin_$companyId") ?: companyRepository.getProfileById("profile_super_admin")
        CompanyContext(
            currentCompany = current,
            companyId = companyId,
            branding = branding,
            settings = settings,
            userProfile = profile,
            userRole = profile?.role ?: "company_admin"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompanyContext())

    val currentUserProfile: StateFlow<ProfileEntity?> = companyContext
        .combine(MutableStateFlow(Unit)) { ctx, _ -> ctx.userProfile }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            companyRepository.seedDefaultMultiCompanyData()
        }
    }

    fun setActiveCompany(companyId: String) {
        _activeCompanyId.value = companyId
    }

    fun updateCompanyStatus(companyId: String, status: String) {
        viewModelScope.launch {
            companyRepository.updateCompanyStatus(companyId, status)
        }
    }

    fun updateCompanyDetails(company: CompanyEntity) {
        viewModelScope.launch {
            companyRepository.updateCompany(company)
        }
    }

    fun createCompanyTenant(name: String, phone: String, email: String, description: String = "", address: String = "Luanda, Angola", planId: String = "plan_pro") {
        viewModelScope.launch {
            val slug = name.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
            val newCompany = CompanyEntity(
                name = name,
                slug = slug,
                phone = phone,
                email = email,
                address = address,
                description = description.ifBlank { "Empresa SaaS $name na plataforma FluxAO Business" }
            )
            companyRepository.saveCompanyWithBranding(
                company = newCompany,
                planId = planId
            )
        }
    }

    fun savePlan(plan: PlanEntity) {
        viewModelScope.launch {
            companyRepository.savePlan(plan)
        }
    }

    fun assignPlanToCompany(companyId: String, planId: String) {
        viewModelScope.launch {
            companyRepository.assignPlanToCompany(companyId, planId)
        }
    }

    fun updateAdsConfiguration(config: AdsConfiguration) {
        adsConfigurationRepository.saveConfig(config)
    }

    fun toggleAdsEnabled() {
        val current = adsConfiguration.value
        adsConfigurationRepository.saveConfig(current.copy(isAdsEnabled = !current.isAdsEnabled))
    }

    // Categorias
    val allCategories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Orçamentos por Categoria
    val allCategoryBudgets: StateFlow<List<CategoryBudgetEntity>> = categoryBudgetRepository.allCategoryBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metas Financeiras
    val allGoals: StateFlow<List<FinancialGoalEntity>> = financialGoalRepository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Agenda / Compromissos
    val allAppointments: StateFlow<List<AppointmentEntity>> = appointmentRepository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setThemeMode(mode: AppThemeMode) {
        themePreferenceRepository.setThemeMode(mode)
    }

    fun toggleTheme() {
        themePreferenceRepository.toggleTheme()
    }

    val allAccounts: StateFlow<List<AccountEntity>> = accountRepository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSuppliers: StateFlow<List<SupplierEntity>> = supplierRepository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = productRepository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = productRepository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<SaleEntity>> = saleRepository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects: StateFlow<List<ProjectEntity>> = projectRepository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = notificationRepository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = notificationRepository.unreadCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unreadNotificationCount: StateFlow<Int> = unreadNotificationsCount

    fun updateCompanySubscription(
        companyName: String,
        nif: String = "",
        contactPhone: String = "",
        destinationIban: String = "0006 0000 43818047301 44"
    ) {
        subscriptionRepository.updateCompanyInfo(companyName, nif, contactPhone, destinationIban)
    }

    // Contas
    fun saveAccount(account: AccountEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (account.id == 0L) {
                accountRepository.insertAccount(account)
            } else {
                accountRepository.updateAccount(account)
            }
            onComplete()
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            accountRepository.deleteAccount(account)
        }
    }

    fun transferBetweenAccounts(
        sourceAccount: AccountEntity,
        destinationAccount: AccountEntity,
        amount: Double,
        description: String = "",
        notes: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            accountRepository.transferBetweenAccounts(
                sourceAccount = sourceAccount,
                destinationAccount = destinationAccount,
                amount = amount,
                description = description,
                notes = notes
            )
            onComplete()
        }
    }

    // Fornecedores
    fun saveSupplier(supplier: SupplierEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (supplier.id == 0L) {
                supplierRepository.insertSupplier(supplier)
            } else {
                supplierRepository.updateSupplier(supplier)
            }
            onComplete()
        }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            supplierRepository.deleteSupplier(supplier)
        }
    }

    // Produtos e Serviços
    fun saveProduct(product: ProductEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (product.id == 0L) {
                productRepository.insertProduct(product)
            } else {
                productRepository.updateProduct(product)
            }
            onComplete()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    // Vendas
    fun saveSale(sale: SaleEntity, items: List<SaleItemEntity>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            saleRepository.registerSale(sale, items)
            onComplete()
        }
    }

    fun deleteSale(sale: SaleEntity) {
        viewModelScope.launch {
            saleRepository.deleteSale(sale)
        }
    }

    // Projetos
    fun saveProject(project: ProjectEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (project.id == 0L) {
                projectRepository.insertProject(project)
            } else {
                projectRepository.updateProject(project)
            }
            onComplete()
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectRepository.deleteProject(project)
        }
    }

    // Notificações
    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            notificationRepository.clearAll()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            notificationRepository.deleteById(id)
        }
    }

    // Categorias
    fun saveCategory(category: CategoryEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (category.id == 0L) {
                categoryRepository.insertCategory(category)
            } else {
                categoryRepository.updateCategory(category)
            }
            onComplete()
        }
    }

    suspend fun countTransactionsWithCategory(categoryName: String): Int {
        return categoryRepository.countTransactionsWithCategory(categoryName)
    }

    fun deleteCategoryWithMigration(category: CategoryEntity, replacementCategoryName: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            categoryRepository.migrateCategoryAndRemove(category, replacementCategoryName)
            onComplete()
        }
    }

    fun deleteCategoryDirectly(category: CategoryEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
            onComplete()
        }
    }

    // Orçamento por Categoria
    fun saveCategoryBudget(categoryName: String, monthlyLimit: Double, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            categoryBudgetRepository.setOrUpdateBudget(categoryName, monthlyLimit)
            onComplete()
        }
    }

    fun deleteCategoryBudget(id: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            categoryBudgetRepository.deleteBudget(id)
            onComplete()
        }
    }

    // Metas Financeiras
    fun saveGoal(goal: FinancialGoalEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (goal.id == 0L) {
                financialGoalRepository.saveGoal(goal)
            } else {
                financialGoalRepository.updateGoal(goal)
            }
            onComplete()
        }
    }

    fun deleteGoal(goal: FinancialGoalEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            financialGoalRepository.deleteGoal(goal)
            onComplete()
        }
    }

    // Agenda / Compromissos
    fun saveAppointment(appointment: AppointmentEntity, context: Context? = null, onComplete: () -> Unit = {}) {
        val isNew = appointment.id == 0L
        if (isNew) {
            checkPointsAndExecute(
                source = "appointment_created",
                referenceId = appointment.title + "_" + System.currentTimeMillis(),
                description = "Novo agendamento realizado"
            ) {
                viewModelScope.launch {
                    val id = appointmentRepository.saveAppointment(appointment)
                    context?.let { ctx ->
                        val finalAppt = appointment.copy(id = id)
                        val scheduler = NotificationScheduler.getInstance(ctx)
                        scheduler.scheduleAppointmentReminders(finalAppt)
                    }
                    onComplete()
                }
            }
        } else {
            viewModelScope.launch {
                appointmentRepository.updateAppointment(appointment)
                context?.let { ctx ->
                    val scheduler = NotificationScheduler.getInstance(ctx)
                    if (appointment.status == "CANCELADO" || appointment.status == "CONCLUIDO") {
                        scheduler.cancelAppointmentReminders(appointment.id)
                    } else {
                        scheduler.scheduleAppointmentReminders(appointment)
                    }
                }
                onComplete()
            }
        }
    }

    fun toggleAppointmentStatus(appointment: AppointmentEntity, context: Context? = null) {
        viewModelScope.launch {
            val nextStatus = if (appointment.status == "CONCLUIDO") "SOLICITADO" else "CONCLUIDO"
            val updated = appointment.copy(status = nextStatus)
            appointmentRepository.updateAppointment(updated)
            context?.let { ctx ->
                val scheduler = NotificationScheduler.getInstance(ctx)
                if (nextStatus == "CONCLUIDO") {
                    scheduler.cancelAppointmentReminders(appointment.id)
                } else {
                    scheduler.scheduleAppointmentReminders(updated)
                }
            }
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity, context: Context? = null, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            appointmentRepository.deleteAppointment(appointment)
            context?.let { ctx ->
                NotificationScheduler.getInstance(ctx).cancelAppointmentReminders(appointment.id)
            }
            onComplete()
        }
    }

    fun duplicateTransaction(transaction: TransactionEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val duplicated = transaction.copy(
                id = 0,
                dateEpochMillis = System.currentTimeMillis(),
                description = "${transaction.description} (Cópia)"
            )
            cashFlowRepository.insert(duplicated)
            onComplete()
        }
    }

    val allInvoices: StateFlow<List<InvoiceEntity>> = invoiceRepository.getAllInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClients: StateFlow<List<ClientEntity>> = clientRepository.getAllClients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveClient(client: ClientEntity, onComplete: () -> Unit = {}) {
        if (client.id == 0L) {
            checkPointsAndExecute(
                source = "customer_created",
                referenceId = client.phone.ifBlank { client.name },
                description = "Novo cliente cadastrado"
            ) {
                viewModelScope.launch {
                    clientRepository.insertClient(client)
                    onComplete()
                }
            }
        } else {
            viewModelScope.launch {
                clientRepository.updateClient(client)
                onComplete()
            }
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            clientRepository.deleteClient(client)
        }
    }

    fun deleteClientById(id: Long) {
        viewModelScope.launch {
            clientRepository.deleteClientById(id)
        }
    }

    fun saveInvoiceLogo(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = customizationRepository.saveLogoFromUri(uri)
            onResult(ok)
        }
    }

    fun removeInvoiceLogo() {
        customizationRepository.removeLogo()
    }

    fun updateInvoiceCustomization(
        accentColorHex: String = invoiceCustomization.value.accentColorHex,
        templateStyle: String = invoiceCustomization.value.templateStyle,
        customFooterNotes: String = invoiceCustomization.value.customFooterNotes,
        showLogoOnInvoice: Boolean = invoiceCustomization.value.showLogoOnInvoice,
        showWatermark: Boolean = invoiceCustomization.value.showWatermark,
        showQrCode: Boolean = invoiceCustomization.value.showQrCode,
        bankAccountInfo: String = invoiceCustomization.value.bankAccountInfo
    ) {
        customizationRepository.updateCustomization(
            accentColorHex = accentColorHex,
            templateStyle = templateStyle,
            customFooterNotes = customFooterNotes,
            showLogoOnInvoice = showLogoOnInvoice,
            showWatermark = showWatermark,
            showQrCode = showQrCode,
            bankAccountInfo = bankAccountInfo
        )
    }

    suspend fun getInvoiceItemsSync(invoiceId: Long): List<InvoiceItemEntity> {
        return invoiceRepository.getItemsForInvoiceSync(invoiceId)
    }

    suspend fun generateNextInvoiceNumber(type: InvoiceType): String {
        return invoiceRepository.generateNextInvoiceNumber(type)
    }

    fun saveInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemData>,
        onComplete: (Long) -> Unit
    ) {
        checkPointsAndExecute(
            source = "invoice_created",
            referenceId = invoice.invoiceNumber,
            description = "Emissão de fatura"
        ) {
            viewModelScope.launch {
                val id = invoiceRepository.saveInvoice(invoice, items)
                onComplete(id)
            }
        }
    }

    fun sendInvoiceViaWhatsapp(invoiceId: Long, onAction: () -> Unit) {
        checkPointsAndExecute(
            source = "invoice_whatsapp",
            referenceId = invoiceId.toString(),
            description = "Envio de fatura via WhatsApp"
        ) {
            onAction()
        }
    }

    fun downloadInvoicePdf(invoiceId: Long, onAction: () -> Unit) {
        checkPointsAndExecute(
            source = "invoice_downloaded",
            referenceId = invoiceId.toString(),
            description = "Download de PDF de fatura"
        ) {
            onAction()
        }
    }

    fun updateInvoiceStatus(id: Long, newStatus: InvoiceStatus, syncCashFlow: Boolean = true) {
        viewModelScope.launch {
            invoiceRepository.updateInvoiceStatus(id, newStatus, syncCashFlow)
        }
    }

    fun deleteInvoice(id: Long) {
        viewModelScope.launch {
            invoiceRepository.deleteInvoice(id)
        }
    }

    init {
        CostEngineeringCalculator.setCurrency(currencyRepository.currentCurrency.value)
    }

    // FluxAO AI Assistant State & Service
    private val geminiAiService = GeminiAiService()

    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                sender = AiSender.ASSISTANT,
                text = """
                    Olá! Sou o **FluxAO AI**, seu assistente financeiro inteligente integrado ao FluxAO.
                    
                    Estou conectado aos seus dados em tempo real para analisar o fluxo de caixa, calcular lucros, despesas, faturas pendentes e previsões.
                    
                    💡 **Toque em uma sugestão abaixo ou faça uma pergunta:**
                """.trimIndent(),
                quickActions = listOf(
                    "Quanto tenho em caixa?",
                    "Quanto gastei este mês?",
                    "Quem ainda me deve?",
                    "Qual foi minha maior despesa?",
                    "Faça um relatório mensal",
                    "Alertas e insights financeiros"
                )
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    fun sendAiMessage(promptText: String) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isBlank()) return

        checkPointsAndExecute(
            source = "fluxai_consultation",
            referenceId = "ai_" + System.currentTimeMillis(),
            description = "Consulta concluída no FluxoAI"
        ) {
            val userMsg = AiChatMessage(sender = AiSender.USER, text = cleanPrompt)
            _aiMessages.update { it + userMsg }

            viewModelScope.launch {
                _isAiThinking.value = true
                try {
                    val reply = geminiAiService.queryAssistant(
                        userMessage = cleanPrompt,
                        chatHistory = _aiMessages.value,
                        uiState = uiState.value,
                        invoices = allInvoices.value,
                        clients = allClients.value,
                        budgets = allBudgets.value,
                        subscription = subscription.value,
                        currency = currentCurrency.value
                    )
                    val assistantMsg = AiChatMessage(
                        sender = AiSender.ASSISTANT,
                        text = reply,
                        quickActions = listOf(
                            "Como está meu fluxo de caixa?",
                            "Mostre minhas contas a pagar",
                            "Previsão para o próximo mês"
                        )
                    )
                    _aiMessages.update { it + assistantMsg }
                } catch (e: Exception) {
                    val fallbackMsg = AiChatMessage(
                        sender = AiSender.ASSISTANT,
                        text = "Ocorreu um erro temporário ao processar sua análise. Por favor, tente novamente."
                    )
                    _aiMessages.update { it + fallbackMsg }
                } finally {
                    _isAiThinking.value = false
                }
            }
        }
    }

    fun clearAiChat() {
        _aiMessages.value = listOf(
            AiChatMessage(
                sender = AiSender.ASSISTANT,
                text = """
                    Conversa reiniciada. Sou o **FluxAO AI**. Como posso ajudar na gestão financeira da sua empresa hoje?
                """.trimIndent(),
                quickActions = listOf(
                    "Quanto tenho em caixa?",
                    "Quanto gastei este mês?",
                    "Quem ainda me deve?",
                    "Qual foi minha maior despesa?",
                    "Faça um relatório mensal",
                    "Alertas e insights financeiros"
                )
            )
        )
    }

    val allBudgets: StateFlow<List<BudgetEntity>> = budgetRepository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cash flow filter state
    private val _filterState = MutableStateFlow(FilterState())

    val uiState: StateFlow<CashFlowUiState> = combine(
        cashFlowRepository.allTransactions,
        _filterState,
        currencyRepository.currentCurrency
    ) { txList, filter, currency ->
        CostEngineeringCalculator.setCurrency(currency)
        calculateUiState(txList, filter)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CashFlowUiState()
    )

    // Budget Editor State
    private val _budgetEditorState = MutableStateFlow(BudgetEditorState())
    val budgetEditorState: StateFlow<BudgetEditorState> = _budgetEditorState.asStateFlow()

    // Dynamic Live Calculation based on Editor State and Selected Currency (Método Angolano)
    val livePricingResult: StateFlow<PricingCalculationResult> = combine(
        _budgetEditorState,
        currencyRepository.currentCurrency
    ) { editorState, currency ->
        CostEngineeringCalculator.setCurrency(currency)
        val materialsCost = editorState.materialsList.sumOf { it.total }
        CostEngineeringCalculator.calculate(
            materialsCost = materialsCost,
            laborHours = editorState.laborHours,
            laborHourlyRate = editorState.laborHourlyRate,
            overheadPercentage = editorState.overheadPercentage,
            taxesPercent = editorState.taxesAndSalesFeePercent,
            desiredProfitMarginPercent = editorState.desiredProfitMarginPercent,
            discountAmount = editorState.discountAmount,
            currency = currency,
            laborPercentageOnMaterials = editorState.laborPercentageOnMaterials,
            useAngolanMethod = true
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CostEngineeringCalculator.calculate(0.0, 0.0, 0.0, 0.0, 14.0, 80.0, 0.0, currencyRepository.currentCurrency.value, 85.0, true)
    )

    // Company & Subscription actions
    fun updateCompanyInfo(companyName: String, nif: String = "", contactPhone: String = "") {
        subscriptionRepository.updateCompanyInfo(companyName, nif, contactPhone)
    }

    fun initiateMcxPayment(
        phoneNumber: String,
        amountKz: Double = 5000.0,
        onSuccess: (McxPaymentInitResult) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val companyName = subscription.value.companyName.ifBlank { "Minha Empresa" }
            val result = mcxRepository.initiatePayment(phoneNumber, amountKz, companyName)
            result.onSuccess {
                onSuccess(it)
            }.onFailure {
                onError(it.localizedMessage ?: "Erro ao iniciar pagamento Multicaixa Express.")
            }
        }
    }

    suspend fun checkMcxPaymentStatus(reference: String, delayCount: Int): Boolean {
        val result = mcxRepository.verifyPaymentStatus(reference, delayCount)
        return result.getOrNull()?.isPaid ?: false
    }

    fun activateSubscription(paymentPhone: String, paymentRef: String, transactionId: String, destinationIban: String = mcxRepository.destinationIban) {
        subscriptionRepository.activateMonthlyPlan(paymentPhone, paymentRef, transactionId, destinationIban)
        // Also register subscription payment in cash flow as an operating license expense
        viewModelScope.launch {
            addTransaction(
                type = TransactionType.EXPENSE,
                amount = 5000.0,
                description = "Assinatura Mensal App Fluxo & Custos (Multicaixa Express)",
                category = "Ferramentas & Softwares",
                dateMillis = System.currentTimeMillis(),
                paymentMethod = PaymentMethod.MULTICAIXA_EXPRESS,
                status = TransactionStatus.PAID,
                notes = "Ref MCX: $paymentRef • Transação: $transactionId • Destino: IBAN $destinationIban (BFA 0006)"
            )
        }
    }

    fun resetSubscriptionForTesting() {
        subscriptionRepository.resetSubscription()
    }

    fun getMcxGatewayUrl(): String = mcxRepository.gatewayUrl
    fun getMcxApiKey(): String = mcxRepository.apiKey
    fun getMcxMerchantId(): String = mcxRepository.merchantId
    fun isMcxSandbox(): Boolean = mcxRepository.isSandboxMode
    fun getMcxDestinationIban(): String = mcxRepository.destinationIban
    fun getFormattedDestinationIban(): String = mcxRepository.formattedDestinationIban
    fun getDestinationBankName(): String = mcxRepository.destinationBankName

    fun updateMcxSettings(
        gatewayUrl: String,
        apiKey: String,
        merchantId: String,
        isSandbox: Boolean,
        destinationIban: String = mcxRepository.destinationIban
    ) {
        mcxRepository.gatewayUrl = gatewayUrl
        mcxRepository.apiKey = apiKey
        mcxRepository.merchantId = merchantId
        mcxRepository.isSandboxMode = isSandbox
        mcxRepository.destinationIban = destinationIban
    }

    fun setCurrency(currency: AppCurrency) {
        currencyRepository.setCurrency(currency)
        CostEngineeringCalculator.setCurrency(currency)
    }

    fun setPeriodFilter(filter: PeriodFilter) {
        _filterState.value = _filterState.value.copy(period = filter)
    }

    fun setTypeFilter(filter: TransactionType?) {
        _filterState.value = _filterState.value.copy(type = filter)
    }

    fun setStatusFilter(filter: TransactionStatus?) {
        _filterState.value = _filterState.value.copy(status = filter)
    }

    fun setCategoryFilter(category: String?) {
        _filterState.value = _filterState.value.copy(category = category)
    }

    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun addTransaction(
        type: TransactionType,
        amount: Double,
        description: String,
        category: String,
        dateMillis: Long,
        paymentMethod: PaymentMethod,
        status: TransactionStatus,
        notes: String = "",
        accountId: Long? = null,
        accountName: String = "Dinheiro / Caixa",
        destinationAccountId: Long? = null,
        destinationAccountName: String = "",
        contactName: String = "",
        contactType: String = "",
        attachmentUri: String = "",
        projectId: Long? = null
    ) {
        checkPointsAndExecute(
            source = "movement_created",
            referenceId = description + "_" + dateMillis,
            description = "Lançamento de movimento no caixa"
        ) {
            viewModelScope.launch {
                if (type == TransactionType.TRANSFER) {
                    if (accountId != null && destinationAccountId != null) {
                        val src = accountRepository.getAccountById(accountId)
                        val dst = accountRepository.getAccountById(destinationAccountId)
                        if (src != null && dst != null) {
                            accountRepository.transferBetweenAccounts(
                                sourceAccount = src,
                                destinationAccount = dst,
                                amount = amount,
                                description = description,
                                notes = notes
                            )
                            return@launch
                        }
                    }
                }

                val entity = TransactionEntity(
                    type = type,
                    amount = amount,
                    description = description,
                    category = category,
                    dateEpochMillis = dateMillis,
                    paymentMethod = paymentMethod,
                    status = status,
                    notes = notes,
                    accountId = accountId,
                    accountName = accountName,
                    destinationAccountId = destinationAccountId,
                    destinationAccountName = destinationAccountName,
                    contactName = contactName,
                    contactType = contactType,
                    attachmentUri = attachmentUri,
                    projectId = projectId
                )
                cashFlowRepository.insert(entity)

                if (status == TransactionStatus.PAID && accountId != null) {
                    val delta = if (type == TransactionType.INCOME) amount else -amount
                    accountRepository.getAccountById(accountId)?.let { acc ->
                        accountRepository.updateAccount(acc.copy(currentBalance = acc.currentBalance + delta))
                    }
                }
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            cashFlowRepository.update(transaction)
        }
    }

    fun toggleTransactionStatus(transaction: TransactionEntity) {
        viewModelScope.launch {
            cashFlowRepository.toggleStatus(transaction.id, transaction.status)
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            cashFlowRepository.deleteById(transactionId)
        }
    }

    // Budget Editor Functions
    fun updateBudgetEditor(update: BudgetEditorState.() -> BudgetEditorState) {
        _budgetEditorState.value = _budgetEditorState.value.update()
    }

    fun addMaterialItem(description: String, quantity: Double, unitPrice: Double) {
        val current = _budgetEditorState.value.materialsList
        val newItem = BudgetItemEntity(
            budgetId = _budgetEditorState.value.budgetId,
            type = BudgetItemType.MATERIAL,
            description = description,
            quantity = quantity,
            unitPrice = unitPrice
        )
        _budgetEditorState.value = _budgetEditorState.value.copy(
            materialsList = current + newItem
        )
    }

    fun removeMaterialItem(index: Int) {
        val current = _budgetEditorState.value.materialsList.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _budgetEditorState.value = _budgetEditorState.value.copy(materialsList = current)
        }
    }

    fun resetBudgetEditor() {
        _budgetEditorState.value = BudgetEditorState()
    }

    fun loadBudgetForEditing(budget: BudgetEntity) {
        viewModelScope.launch {
            val items = budgetRepository.getItemsForBudgetSync(budget.id)
            _budgetEditorState.value = BudgetEditorState(
                budgetId = budget.id,
                clientName = budget.clientName,
                clientContact = budget.clientContact,
                projectTitle = budget.projectTitle,
                projectScope = budget.projectScope,
                deadlineDays = budget.deadlineDays,
                status = budget.status,
                materialsList = items,
                laborPercentageOnMaterials = budget.laborPercentageOnMaterials,
                laborHours = budget.laborHours,
                laborHourlyRate = budget.laborHourlyRate,
                overheadPercentage = budget.overheadPercentage,
                taxesAndSalesFeePercent = budget.taxesAndSalesFeePercent,
                desiredProfitMarginPercent = budget.desiredProfitMarginPercent,
                discountAmount = budget.discountAmount,
                paymentTerms = budget.paymentTerms,
                termsAndConditions = budget.termsAndConditions,
                notes = budget.notes
            )
        }
    }

    fun saveCurrentBudget(onSuccess: (Long) -> Unit = {}) {
        val state = _budgetEditorState.value
        checkPointsAndExecute(
            source = "quote_created",
            referenceId = state.projectTitle + "_" + System.currentTimeMillis(),
            description = "Criação de orçamento"
        ) {
            viewModelScope.launch {
                val currentState = _budgetEditorState.value
                val materialsTotal = currentState.materialsList.sumOf { it.total }

                val entity = BudgetEntity(
                    id = currentState.budgetId,
                    clientName = currentState.clientName.ifBlank { "Cliente Sem Nome" },
                    clientContact = currentState.clientContact,
                    projectTitle = currentState.projectTitle.ifBlank { "Novo Projeto Orçado" },
                    projectScope = currentState.projectScope,
                    dateCreatedEpochMillis = System.currentTimeMillis(),
                    deadlineDays = currentState.deadlineDays,
                    status = currentState.status,
                    directMaterialsTotal = materialsTotal,
                    laborPercentageOnMaterials = currentState.laborPercentageOnMaterials,
                    laborHours = currentState.laborHours,
                    laborHourlyRate = currentState.laborHourlyRate,
                    overheadPercentage = currentState.overheadPercentage,
                    taxesAndSalesFeePercent = currentState.taxesAndSalesFeePercent,
                    desiredProfitMarginPercent = currentState.desiredProfitMarginPercent,
                    discountAmount = currentState.discountAmount,
                    paymentTerms = currentState.paymentTerms,
                    termsAndConditions = currentState.termsAndConditions,
                    notes = currentState.notes
                )

                val id = budgetRepository.saveBudget(entity, currentState.materialsList)
                onSuccess(id)
            }
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(id)
        }
    }

    fun updateBudgetStatus(id: Long, status: BudgetStatus) {
        viewModelScope.launch {
            budgetRepository.updateStatus(id, status)
        }
    }

    private fun calculateUiState(
        txList: List<TransactionEntity>,
        filter: FilterState
    ): CashFlowUiState {
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        var totalPaidIncome = 0.0
        var totalPaidExpense = 0.0
        var monthIncome = 0.0
        var monthExpense = 0.0
        var pendingIncome = 0.0
        var pendingExpense = 0.0

        val categoryExpenseMap = mutableMapOf<String, Double>()

        txList.forEach { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateEpochMillis }
            val isCurrentMonth = txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear

            if (tx.status == TransactionStatus.PAID) {
                when (tx.type) {
                    TransactionType.INCOME -> {
                        totalPaidIncome += tx.amount
                        if (isCurrentMonth) monthIncome += tx.amount
                    }
                    TransactionType.EXPENSE -> {
                        totalPaidExpense += tx.amount
                        if (isCurrentMonth) {
                            monthExpense += tx.amount
                            categoryExpenseMap[tx.category] = (categoryExpenseMap[tx.category] ?: 0.0) + tx.amount
                        }
                    }
                    TransactionType.TRANSFER -> {
                        // Transferência interna: NÃO é contabilizada como receita nem como despesa
                    }
                }
            } else {
                when (tx.type) {
                    TransactionType.INCOME -> pendingIncome += tx.amount
                    TransactionType.EXPENSE -> pendingExpense += tx.amount
                    TransactionType.TRANSFER -> {}
                }
            }
        }

        val currentBalance = totalPaidIncome - totalPaidExpense
        val monthNetProfit = monthIncome - monthExpense
        val monthNetMargin = if (monthIncome > 0) (monthNetProfit / monthIncome) * 100.0 else 0.0

        // Build 12 monthly data points for flexible 3M / 6M / 12M Recharts-style visualization
        val monthBars = mutableListOf<MonthlyBarData>()
        val monthNames = arrayOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
        val fullMonthNames = arrayOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        for (i in 11 downTo 0) {
            val targetCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, -i)
            }
            val targetM = targetCal.get(Calendar.MONTH)
            val targetY = targetCal.get(Calendar.YEAR)
            val label = monthNames[targetM]
            val fullName = "${fullMonthNames[targetM]} $targetY"

            val inc = txList.filter {
                val c = Calendar.getInstance().apply { timeInMillis = it.dateEpochMillis }
                c.get(Calendar.MONTH) == targetM && c.get(Calendar.YEAR) == targetY && it.type == TransactionType.INCOME && it.status == TransactionStatus.PAID
            }.sumOf { it.amount }

            val exp = txList.filter {
                val c = Calendar.getInstance().apply { timeInMillis = it.dateEpochMillis }
                c.get(Calendar.MONTH) == targetM && c.get(Calendar.YEAR) == targetY && it.type == TransactionType.EXPENSE && it.status == TransactionStatus.PAID
            }.sumOf { it.amount }

            monthBars.add(
                MonthlyBarData(
                    monthLabel = label,
                    income = inc,
                    expense = exp,
                    net = inc - exp,
                    year = targetY,
                    monthIndex = targetM,
                    fullMonthName = fullName
                )
            )
        }

        // Category breakdown
        val totalMonthExpense = categoryExpenseMap.values.sum()
        val palette = listOf(
            Color(0xFFEA580C), Color(0xFFDC2626), Color(0xFFD97706),
            Color(0xFF7C3AED), Color(0xFFBE123C), Color(0xFF2563EB),
            Color(0xFF0891B2), Color(0xFF64748B)
        )
        val categoryShares = categoryExpenseMap.entries.mapIndexed { index, entry ->
            val pct = if (totalMonthExpense > 0) (entry.value / totalMonthExpense) * 100.0 else 0.0
            CategoryShare(
                categoryName = entry.key,
                amount = entry.value,
                percentage = pct,
                color = palette[index % palette.size]
            )
        }.sortedByDescending { it.amount }

        // Filter transactions for Cash Flow list
        val filtered = txList.filter { tx ->
            val matchesPeriod = when (filter.period) {
                PeriodFilter.ALL -> true
                PeriodFilter.THIS_MONTH -> {
                    val c = Calendar.getInstance().apply { timeInMillis = tx.dateEpochMillis }
                    c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
                }
                PeriodFilter.LAST_30_DAYS -> {
                    val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                    tx.dateEpochMillis >= thirtyDaysAgo
                }
            }

            val matchesType = filter.type == null || tx.type == filter.type
            val matchesStatus = filter.status == null || tx.status == filter.status
            val matchesCategory = filter.category == null || tx.category.equals(filter.category, ignoreCase = true)
            val matchesQuery = filter.query.isBlank() || tx.description.contains(filter.query, ignoreCase = true) || tx.category.contains(filter.query, ignoreCase = true)

            matchesPeriod && matchesType && matchesStatus && matchesCategory && matchesQuery
        }

        return CashFlowUiState(
            transactions = txList,
            filteredTransactions = filtered,
            currentBalance = currentBalance,
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            monthNetProfit = monthNetProfit,
            monthNetMarginPercent = monthNetMargin,
            pendingIncome = pendingIncome,
            pendingExpense = pendingExpense,
            monthlyBars = monthBars,
            categoryShares = categoryShares,
            periodFilter = filter.period,
            typeFilter = filter.type,
            statusFilter = filter.status,
            categoryFilter = filter.category,
            searchQuery = filter.query
        )
    }

    class Factory(
        private val cashFlowRepository: CashFlowRepository,
        private val budgetRepository: BudgetRepository,
        private val invoiceRepository: InvoiceRepository,
        private val currencyRepository: CurrencyPreferenceRepository,
        private val subscriptionRepository: SubscriptionRepository,
        private val mcxRepository: MulticaixaExpressRepository,
        private val clientRepository: ClientRepository,
        private val customizationRepository: InvoiceCustomizationRepository,
        private val themePreferenceRepository: ThemePreferenceRepository,
        private val accountRepository: AccountRepository,
        private val supplierRepository: SupplierRepository,
        private val productRepository: ProductRepository,
        private val saleRepository: SaleRepository,
        private val projectRepository: ProjectRepository,
        private val notificationRepository: NotificationRepository,
        private val categoryRepository: CategoryRepository,
        private val categoryBudgetRepository: CategoryBudgetRepository,
        private val financialGoalRepository: FinancialGoalRepository,
        private val appointmentRepository: AppointmentRepository,
        private val companyRepository: CompanyRepository,
        private val adsConfigurationRepository: AdsConfigurationRepository,
        private val localePreferenceRepository: LocalePreferenceRepository,
        private val userPointsDao: UserPointsDao,
        private val pointsTransactionDao: PointsTransactionDao,
        private val userEntitlementDao: com.example.data.local.dao.UserEntitlementDao,
        private val userFeatureUnlockDao: UserFeatureUnlockDao? = null,
        private val welcomeSplashPreferenceRepository: WelcomeSplashPreferenceRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(
                cashFlowRepository,
                budgetRepository,
                invoiceRepository,
                currencyRepository,
                subscriptionRepository,
                mcxRepository,
                clientRepository,
                customizationRepository,
                themePreferenceRepository,
                accountRepository,
                supplierRepository,
                productRepository,
                saleRepository,
                projectRepository,
                notificationRepository,
                categoryRepository,
                categoryBudgetRepository,
                financialGoalRepository,
                appointmentRepository,
                companyRepository,
                adsConfigurationRepository,
                localePreferenceRepository,
                userPointsDao,
                pointsTransactionDao,
                userEntitlementDao,
                userFeatureUnlockDao,
                welcomeSplashPreferenceRepository
            ) as T
        }
    }
}
