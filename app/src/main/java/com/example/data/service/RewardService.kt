package com.example.data.service

data class RewardItem(
    val id: String,
    val code: String, // reports_advanced, invoice_premium, subscription_discount, whatsapp_automation, vip_support, pro_plan
    val title: String,
    val description: String,
    val pointsRequired: Int,
    val durationDays: Int? = 30,
    val category: String,
    val iconName: String,
    val isAvailable: Boolean = true
)

class RewardService(
    private val pointsService: PointsService,
    private val entitlementService: EntitlementService? = null
) {

    val catalog: List<RewardItem> = listOf(
        RewardItem(
            id = "rew_invoices",
            code = "invoices",
            title = "Faturas",
            description = "Emita e gerencie suas faturas comerciais e fiscais sem limites.",
            pointsRequired = 50,
            durationDays = 30,
            category = "feature_unlock",
            iconName = "ReceiptLong"
        ),
        RewardItem(
            id = "rew_customers",
            code = "customers",
            title = "Clientes",
            description = "Gerencie sua carteira de clientes, contatos e dados fiscais.",
            pointsRequired = 30,
            durationDays = 30,
            category = "feature_unlock",
            iconName = "People"
        ),
        RewardItem(
            id = "rew_appointments",
            code = "appointments",
            title = "Agendamentos",
            description = "Gerencie seus compromissos e marcações na sua agenda comercial.",
            pointsRequired = 30,
            durationDays = 30,
            category = "feature_unlock",
            iconName = "CalendarMonth"
        ),
        RewardItem(
            id = "rew_reports",
            code = "reports",
            title = "Relatórios",
            description = "Relatórios financeiros avançados, DRE, análises e exportações em PDF.",
            pointsRequired = 50,
            durationDays = 30,
            category = "feature_unlock",
            iconName = "Assessment"
        ),
        RewardItem(
            id = "rew_fluxoai",
            code = "fluxoai",
            title = "FluxoAI",
            description = "Consultoria e assistente financeiro inteligente em tempo real.",
            pointsRequired = 80,
            durationDays = 30,
            category = "feature_unlock",
            iconName = "AutoAwesome"
        ),
        RewardItem(
            id = "rew_whatsapp_automation",
            code = "whatsapp_automation",
            title = "Automação WhatsApp",
            description = "Desbloqueie automações avançadas de envio e lembretes por WhatsApp.",
            pointsRequired = 150,
            durationDays = 30,
            category = "feature_unlock",
            iconName = "Chat"
        )
    )

    suspend fun redeemReward(userId: String = "default_user", reward: RewardItem): AwardPointsResult {
        // Check if feature is already active and valid
        if (entitlementService != null && entitlementService.isFeatureActive(reward.code, userId)) {
            return AwardPointsResult.AlreadyAwarded("Você já possui este benefício ativo na sua conta!")
        }

        val result = pointsService.spendPoints(
            userId = userId,
            pointsToSpend = reward.pointsRequired,
            rewardId = reward.id,
            rewardTitle = reward.title
        )

        if (result is AwardPointsResult.Success && entitlementService != null) {
            entitlementService.grantEntitlement(
                rewardCode = reward.code,
                rewardTitle = reward.title,
                rewardId = reward.id,
                durationDays = reward.durationDays,
                userId = userId
            )
        }

        return result
    }
}
