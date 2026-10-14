package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.AppointmentDao
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.BlockedSlotDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.CategoryBudgetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ClientDao
import com.example.data.local.dao.CompanyBrandingDao
import com.example.data.local.dao.CompanyDao
import com.example.data.local.dao.CompanySettingsDao
import com.example.data.local.dao.FinancialGoalDao
import com.example.data.local.dao.FiscalAuditLogDao
import com.example.data.local.dao.InvoiceDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.PlanDao
import com.example.data.local.dao.PointsTransactionDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.SubscriptionDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserEntitlementDao
import com.example.data.local.dao.UserFeatureUnlockDao
import com.example.data.local.dao.UserPointsDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AppointmentEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.BlockedSlotEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BudgetItemEntity
import com.example.data.local.entity.CategoryBudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.CompanyBrandingEntity
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.CompanySettingsEntity
import com.example.data.local.entity.FinancialGoalEntity
import com.example.data.local.entity.FiscalAuditLogEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PlanEntity
import com.example.data.local.entity.PointsTransactionEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SubscriptionEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntitlementEntity
import com.example.data.local.entity.UserFeatureUnlockEntity
import com.example.data.local.entity.UserPointsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Banco de dados central persistente Room (SQLite) do FluxoAO.
 *
 * Fornece armazenamento local reativo e durável para:
 * - Movimentações financeiras e fluxos de caixa (TransactionEntity / TransactionDao)
 * - Orçamentos e itens detalhados (BudgetEntity, BudgetItemEntity / BudgetDao)
 * - Orçamentos por categoria (CategoryBudgetEntity / CategoryBudgetDao)
 * - Contas bancárias, caixas e carteiras digitais (AccountEntity / AccountDao)
 * - Metas financeiras, faturas, clientes, vendas, planos e pontuação de desbloqueio.
 */
@Database(
    entities = [
        CompanyEntity::class,
        CompanySettingsEntity::class,
        CompanyBrandingEntity::class,
        ProfileEntity::class,
        PlanEntity::class,
        SubscriptionEntity::class,
        AuditLogEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        BudgetItemEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        ClientEntity::class,
        AccountEntity::class,
        SupplierEntity::class,
        ProductEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        ProjectEntity::class,
        NotificationEntity::class,
        CategoryEntity::class,
        CategoryBudgetEntity::class,
        FinancialGoalEntity::class,
        AppointmentEntity::class,
        BlockedSlotEntity::class,
        UserPointsEntity::class,
        PointsTransactionEntity::class,
        FiscalAuditLogEntity::class,
        UserEntitlementEntity::class,
        UserFeatureUnlockEntity::class
    ],
    version = 15,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // ==========================================
    // EXPOSIÇÃO DOS DATA ACCESS OBJECTS (DAOs)
    // ==========================================

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun financialGoalDao(): FinancialGoalDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun clientDao(): ClientDao
    abstract fun supplierDao(): SupplierDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun projectDao(): ProjectDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun blockedSlotDao(): BlockedSlotDao
    abstract fun notificationDao(): NotificationDao
    abstract fun companyDao(): CompanyDao
    abstract fun companySettingsDao(): CompanySettingsDao
    abstract fun companyBrandingDao(): CompanyBrandingDao
    abstract fun profileDao(): ProfileDao
    abstract fun planDao(): PlanDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun userPointsDao(): UserPointsDao
    abstract fun pointsTransactionDao(): PointsTransactionDao
    abstract fun fiscalAuditLogDao(): FiscalAuditLogDao
    abstract fun userEntitlementDao(): UserEntitlementDao
    abstract fun userFeatureUnlockDao(): UserFeatureUnlockDao

    companion object {
        private const val DATABASE_NAME = "fluxo_custos_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retorna a instância singleton do AppDatabase com um escopo de corrotina padrão.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(
                    context.applicationContext,
                    CoroutineScope(Dispatchers.IO + SupervisorJob())
                ).also { INSTANCE = it }
            }
        }

        /**
         * Retorna a instância singleton do AppDatabase permitindo injetar o CoroutineScope de inicialização.
         */
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext, scope).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context, scope: CoroutineScope): AppDatabase {
            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedDatabase(database)
                    }
                }
            }
        }

        /**
         * Inicialização opcional de dados padrão essenciais na criação inicial do banco.
         */
        suspend fun seedDatabase(db: AppDatabase) {
            val categoryDao = db.categoryDao()
            val defaultCategories = listOf(
                // Receitas
                CategoryEntity(name = "Vendas", type = "INCOME", iconName = "ShoppingBag", colorHex = 0xFF10B981, isDefault = true),
                CategoryEntity(name = "Serviços", type = "INCOME", iconName = "Handyman", colorHex = 0xFF059669, isDefault = true),
                CategoryEntity(name = "Projetos", type = "INCOME", iconName = "Assignment", colorHex = 0xFF047857, isDefault = true),
                CategoryEntity(name = "Pagamentos de clientes", type = "INCOME", iconName = "Payments", colorHex = 0xFF34D399, isDefault = true),
                CategoryEntity(name = "Outros", type = "INCOME", iconName = "MoreHoriz", colorHex = 0xFF6EE7B7, isDefault = true),
                // Despesas
                CategoryEntity(name = "Material", type = "EXPENSE", iconName = "Inventory2", colorHex = 0xFFEF4444, isDefault = true),
                CategoryEntity(name = "Transporte", type = "EXPENSE", iconName = "LocalShipping", colorHex = 0xFFF97316, isDefault = true),
                CategoryEntity(name = "Ferramentas", type = "EXPENSE", iconName = "Build", colorHex = 0xFFF59E0B, isDefault = true),
                CategoryEntity(name = "Alimentação", type = "EXPENSE", iconName = "Restaurant", colorHex = 0xFFEC4899, isDefault = true),
                CategoryEntity(name = "Salários", type = "EXPENSE", iconName = "Badge", colorHex = 0xFF8B5CF6, isDefault = true),
                CategoryEntity(name = "Renda", type = "EXPENSE", iconName = "HomeWork", colorHex = 0xFF6366F1, isDefault = true),
                CategoryEntity(name = "Marketing", type = "EXPENSE", iconName = "Campaign", colorHex = 0xFF3B82F6, isDefault = true),
                CategoryEntity(name = "Energia", type = "EXPENSE", iconName = "Bolt", colorHex = 0xFFEAB308, isDefault = true),
                CategoryEntity(name = "Internet", type = "EXPENSE", iconName = "Wifi", colorHex = 0xFF06B6D4, isDefault = true),
                CategoryEntity(name = "Manutenção", type = "EXPENSE", iconName = "Handyman", colorHex = 0xFF64748B, isDefault = true),
                CategoryEntity(name = "Outros", type = "EXPENSE", iconName = "MoreHoriz", colorHex = 0xFF94A3B8, isDefault = true)
            )
            categoryDao.insertCategories(defaultCategories)

            // Contas Padrão zeradas
            val accountDao = db.accountDao()
            val sampleAccounts = listOf(
                AccountEntity(name = "Dinheiro / Caixa", type = "CASH", initialBalance = 0.0, currentBalance = 0.0, colorHex = 0xFF059669, isDefault = true),
                AccountEntity(name = "BAI Directo", type = "BANK", accountNumberOrIban = "", initialBalance = 0.0, currentBalance = 0.0, colorHex = 0xFF1E3A8A),
                AccountEntity(name = "BFA Net", type = "BANK", accountNumberOrIban = "", initialBalance = 0.0, currentBalance = 0.0, colorHex = 0xFFEA580C),
                AccountEntity(name = "Multicaixa Express", type = "DIGITAL_WALLET", accountNumberOrIban = "", initialBalance = 0.0, currentBalance = 0.0, colorHex = 0xFFD97706)
            )
            accountDao.insertAccounts(sampleAccounts)
        }
    }
}
