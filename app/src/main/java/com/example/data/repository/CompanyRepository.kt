package com.example.data.repository

import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.CompanyBrandingDao
import com.example.data.local.dao.CompanyDao
import com.example.data.local.dao.CompanySettingsDao
import com.example.data.local.dao.PlanDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.SubscriptionDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CompanyBrandingEntity
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.CompanySettingsEntity
import com.example.data.local.entity.PlanEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CompanyRepository(
    private val companyDao: CompanyDao,
    private val settingsDao: CompanySettingsDao,
    private val brandingDao: CompanyBrandingDao,
    private val profileDao: ProfileDao,
    private val planDao: PlanDao,
    private val subscriptionDao: SubscriptionDao,
    private val auditLogDao: AuditLogDao
) {
    val allCompanies: Flow<List<CompanyEntity>> = companyDao.getAllCompanies()
    val allProfiles: Flow<List<ProfileEntity>> = profileDao.getAllProfiles()
    val allPlans: Flow<List<PlanEntity>> = planDao.getAllPlans()
    val allSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()

    suspend fun getCompanyById(id: String): CompanyEntity? = companyDao.getCompanyById(id)

    suspend fun getCompanyBySlug(slug: String): CompanyEntity? = companyDao.getCompanyBySlug(slug)

    fun getSettingsByCompanyIdFlow(companyId: String): Flow<CompanySettingsEntity?> =
        settingsDao.getSettingsByCompanyIdFlow(companyId)

    suspend fun getSettingsByCompanyId(companyId: String): CompanySettingsEntity? =
        settingsDao.getSettingsByCompanyId(companyId)

    fun getBrandingByCompanyIdFlow(companyId: String): Flow<CompanyBrandingEntity?> =
        brandingDao.getBrandingByCompanyIdFlow(companyId)

    suspend fun getBrandingByCompanyId(companyId: String): CompanyBrandingEntity? =
        brandingDao.getBrandingByCompanyId(companyId)

    fun getSubscriptionByCompanyIdFlow(companyId: String): Flow<SubscriptionEntity?> =
        subscriptionDao.getSubscriptionByCompanyIdFlow(companyId)

    suspend fun getSubscriptionByCompanyId(companyId: String): SubscriptionEntity? =
        subscriptionDao.getSubscriptionByCompanyId(companyId)

    fun getProfileByIdFlow(id: String): Flow<ProfileEntity?> = profileDao.getProfileByIdFlow(id)

    suspend fun getProfileById(id: String): ProfileEntity? = profileDao.getProfileById(id)

    fun getProfilesByCompany(companyId: String): Flow<List<ProfileEntity>> =
        profileDao.getProfilesByCompany(companyId)

    suspend fun saveCompanyWithBranding(
        company: CompanyEntity,
        settings: CompanySettingsEntity? = null,
        branding: CompanyBrandingEntity? = null,
        planId: String = "plan_pro"
    ) {
        companyDao.insertCompany(company)
        val defaultSettings = settings ?: CompanySettingsEntity(companyId = company.id)
        settingsDao.insertSettings(defaultSettings)

        val defaultBranding = branding ?: CompanyBrandingEntity(
            companyId = company.id,
            appName = company.name
        )
        brandingDao.insertBranding(defaultBranding)

        val subscription = SubscriptionEntity(
            id = UUID.randomUUID().toString(),
            companyId = company.id,
            planId = planId,
            status = "active"
        )
        subscriptionDao.insertSubscription(subscription)

        logAuditAction(
            userId = "usr_super_admin",
            companyId = company.id,
            action = "create_company",
            entityType = "company",
            entityId = company.id,
            metadata = "Nova empresa criada: ${company.name}"
        )
    }

    suspend fun updateCompany(company: CompanyEntity) {
        companyDao.updateCompany(company)
        logAuditAction(
            userId = "usr_super_admin",
            companyId = company.id,
            action = "update_company",
            entityType = "company",
            entityId = company.id,
            metadata = "Empresa atualizada: ${company.name}"
        )
    }

    suspend fun updateCompanyStatus(companyId: String, status: String) {
        companyDao.updateCompanyStatus(companyId, status)
        logAuditAction(
            userId = "usr_super_admin",
            companyId = companyId,
            action = if (status == "suspended") "suspend_company" else "activate_company",
            entityType = "company",
            entityId = companyId,
            metadata = "Estado da empresa alterado para $status"
        )
    }

    suspend fun saveProfile(profile: ProfileEntity) {
        profileDao.insertProfile(profile)
    }

    suspend fun savePlan(plan: PlanEntity) {
        planDao.insertPlan(plan)
        logAuditAction(
            userId = "usr_super_admin",
            companyId = "",
            action = "save_plan",
            entityType = "plan",
            entityId = plan.id,
            metadata = "Plano atualizado/criado: ${plan.name}"
        )
    }

    suspend fun assignPlanToCompany(companyId: String, planId: String) {
        val existing = subscriptionDao.getSubscriptionByCompanyId(companyId)
        val sub = (existing ?: SubscriptionEntity(
            id = UUID.randomUUID().toString(),
            companyId = companyId,
            planId = planId
        )).copy(
            planId = planId,
            status = "active",
            updatedAtEpoch = System.currentTimeMillis()
        )
        subscriptionDao.insertSubscription(sub)
        logAuditAction(
            userId = "usr_super_admin",
            companyId = companyId,
            action = "subscription_change",
            entityType = "subscription",
            entityId = sub.id,
            metadata = "Plano alterado para $planId"
        )
    }

    suspend fun updateBrandingColors(
        companyId: String,
        primaryColor: String,
        secondaryColor: String,
        accentColor: String
    ) {
        val existing = brandingDao.getBrandingByCompanyId(companyId)
        val updated = (existing ?: CompanyBrandingEntity(companyId = companyId)).copy(
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            accentColor = accentColor,
            updatedAtEpoch = System.currentTimeMillis()
        )
        brandingDao.insertBranding(updated)
    }

    suspend fun logAuditAction(
        userId: String,
        companyId: String,
        action: String,
        entityType: String,
        entityId: String,
        metadata: String
    ) {
        val log = AuditLogEntity(
            userId = userId,
            companyId = companyId,
            action = action,
            entityType = entityType,
            entityId = entityId,
            metadata = metadata
        )
        auditLogDao.insertAuditLog(log)
    }

    suspend fun seedDefaultMultiCompanyData() {
        val count = companyDao.getCompanyById("company_sh_moveis")
        if (count == null) {
            // Seed SaaS Plans
            val planFree = PlanEntity(
                id = "plan_free",
                name = "FREE",
                description = "Plano gratuito com recursos essenciais",
                price = 0.0,
                maxUsers = 2,
                maxProducts = 20,
                maxCustomers = 25,
                maxOrders = 30,
                featuresJson = "[\"Gestão básica de caixa\", \"Até 25 clientes\", \"Orçamentos simples\"]"
            )
            val planStarter = PlanEntity(
                id = "plan_starter",
                name = "STARTER",
                description = "Ideal para pequenos negócios em crescimento",
                price = 15000.0,
                maxUsers = 5,
                maxProducts = 100,
                maxCustomers = 150,
                maxOrders = 200,
                featuresJson = "[\"Fluxo de Caixa Completo\", \"Emissão de Faturas\", \"Suporte Multicaixa Express\", \"Até 5 Utilizadores\"]"
            )
            val planPro = PlanEntity(
                id = "plan_pro",
                name = "PRO",
                description = "Plano profissional com inteligência financeira",
                price = 35000.0,
                maxUsers = 15,
                maxProducts = 500,
                maxCustomers = 1000,
                maxOrders = 1500,
                featuresJson = "[\"Assistente de IA\", \"Relatórios Financeiros Avançados\", \"Agendamentos & Lembretes\", \"Branding Personalizado\", \"Até 15 Utilizadores\"]"
            )
            val planBusiness = PlanEntity(
                id = "plan_business",
                name = "BUSINESS",
                description = "Recursos ilimitados para médias e grandes empresas",
                price = 75000.0,
                maxUsers = 100,
                maxProducts = 10000,
                maxCustomers = 50000,
                maxOrders = 100000,
                featuresJson = "[\"Recursos Ilimitados\", \"Suporte Prioritário 24/7\", \"Integração Supabase RLS\", \"Múltiplas Filiais & Logs de Auditoria\"]"
            )

            planDao.insertPlan(planFree)
            planDao.insertPlan(planStarter)
            planDao.insertPlan(planPro)
            planDao.insertPlan(planBusiness)

            val companyA = CompanyEntity(
                id = "company_sh_moveis",
                name = "SH Móveis & Projetos",
                slug = "sh-moveis",
                description = "Fabricação e design de móveis planejados em Luanda",
                phone = "+244 923 111 222",
                email = "contato@shmoveis.co.ao",
                address = "Talatona, Luanda, Angola",
                status = "active"
            )
            saveCompanyWithBranding(
                company = companyA,
                branding = CompanyBrandingEntity(
                    companyId = companyA.id,
                    primaryColor = "#0F172A",
                    secondaryColor = "#D4AF37",
                    accentColor = "#059669",
                    appName = "SH Móveis"
                ),
                planId = "plan_pro"
            )

            val companyB = CompanyEntity(
                id = "company_transporte",
                name = "Transporte & Logística Express",
                slug = "transporte-express",
                description = "Serviços de carga e frete interprovincial em Angola",
                phone = "+244 924 333 444",
                email = "geral@transporte-express.co.ao",
                address = "Viana, Luanda, Angola",
                status = "active"
            )
            saveCompanyWithBranding(
                company = companyB,
                branding = CompanyBrandingEntity(
                    companyId = companyB.id,
                    primaryColor = "#1E3A8A",
                    secondaryColor = "#F59E0B",
                    accentColor = "#2563EB",
                    appName = "Transporte Express"
                ),
                planId = "plan_starter"
            )

            val companyC = CompanyEntity(
                id = "company_barbearia",
                name = "Barbearia Vip Luanda",
                slug = "barbearia-vip",
                description = "Estilo, corte e barboterapia de alta precisão",
                phone = "+244 931 555 666",
                email = "agendamentos@barbeariavip.co.ao",
                address = "Maianga, Luanda, Angola",
                status = "active"
            )
            saveCompanyWithBranding(
                company = companyC,
                branding = CompanyBrandingEntity(
                    companyId = companyC.id,
                    primaryColor = "#312E81",
                    secondaryColor = "#EC4899",
                    accentColor = "#8B5CF6",
                    appName = "Barbearia Vip"
                ),
                planId = "plan_free"
            )

            // Seed Profiles
            val superAdminProfile = ProfileEntity(
                id = "usr_super_admin",
                companyId = "",
                fullName = "Super Administrador FluxAO",
                email = "superadmin@fluxao.co.ao",
                phone = "+244 900 000 000",
                role = "super_admin"
            )
            profileDao.insertProfile(superAdminProfile)

            val adminCompanyA = ProfileEntity(
                id = "usr_admin_sh_moveis",
                companyId = "company_sh_moveis",
                fullName = "João Manuel (Admin SH Móveis)",
                email = "admin@shmoveis.co.ao",
                phone = "+244 923 111 222",
                role = "company_admin"
            )
            profileDao.insertProfile(adminCompanyA)

            val empCompanyA = ProfileEntity(
                id = "usr_emp_sh_moveis",
                companyId = "company_sh_moveis",
                fullName = "Mateus Kalandula (Vendedor)",
                email = "mateus@shmoveis.co.ao",
                phone = "+244 923 888 999",
                role = "employee"
            )
            profileDao.insertProfile(empCompanyA)

            val customerCompanyA = ProfileEntity(
                id = "usr_cust_sh_moveis",
                companyId = "company_sh_moveis",
                fullName = "Carla Neto (Cliente)",
                email = "carla@gmail.com",
                phone = "+244 912 345 678",
                role = "customer"
            )
            profileDao.insertProfile(customerCompanyA)

            logAuditAction(
                userId = "usr_super_admin",
                companyId = "",
                action = "system_seed",
                entityType = "system",
                entityId = "seed",
                metadata = "Seeding inicial do sistema FluxAO Business SaaS concluído"
            )
        }
    }
}
