package com.example.data.model

data class FinancialCategory(
    val id: String,
    val name: String,
    val type: TransactionType,
    val iconName: String,
    val colorHex: Long
) {
    companion object {
        val DEFAULT_INCOME_CATEGORIES = listOf(
            FinancialCategory("inc_venda", "Venda", TransactionType.INCOME, "ShoppingBag", 0xFF059669),
            FinancialCategory("inc_pag_cliente", "Pagamento de cliente", TransactionType.INCOME, "CheckCircle", 0xFF0D9488),
            FinancialCategory("inc_sinal", "Sinal", TransactionType.INCOME, "MonetizationOn", 0xFF0284C7),
            FinancialCategory("inc_servico", "Serviço", TransactionType.INCOME, "Engineering", 0xFF4F46E5),
            FinancialCategory("inc_emprestimo", "Empréstimo", TransactionType.INCOME, "AccountBalance", 0xFF7C3AED),
            FinancialCategory("inc_investimento", "Investimento", TransactionType.INCOME, "TrendingUp", 0xFF10B981),
            FinancialCategory("inc_outros", "Outros", TransactionType.INCOME, "AddCircle", 0xFF64748B)
        )

        val DEFAULT_EXPENSE_CATEGORIES = listOf(
            FinancialCategory("exp_material", "Material", TransactionType.EXPENSE, "Construction", 0xFFEA580C),
            FinancialCategory("exp_fornecedor", "Fornecedor", TransactionType.EXPENSE, "LocalShipping", 0xFFDC2626),
            FinancialCategory("exp_transporte", "Transporte", TransactionType.EXPENSE, "DirectionsCar", 0xFFD97706),
            FinancialCategory("exp_salario", "Salário", TransactionType.EXPENSE, "Badge", 0xFFE11D48),
            FinancialCategory("exp_alimentacao", "Alimentação", TransactionType.EXPENSE, "Restaurant", 0xFFF59E0B),
            FinancialCategory("exp_renda", "Renda", TransactionType.EXPENSE, "Home", 0xFF9333EA),
            FinancialCategory("exp_energia", "Energia", TransactionType.EXPENSE, "Bolt", 0xFFCA8A04),
            FinancialCategory("exp_agua", "Água", TransactionType.EXPENSE, "WaterDrop", 0xFF0284C7),
            FinancialCategory("exp_internet", "Internet", TransactionType.EXPENSE, "Wifi", 0xFF2563EB),
            FinancialCategory("exp_marketing", "Marketing", TransactionType.EXPENSE, "Campaign", 0xFF4F46E5),
            FinancialCategory("exp_equipamento", "Equipamento", TransactionType.EXPENSE, "Build", 0xFF0891B2),
            FinancialCategory("exp_manutencao", "Manutenção", TransactionType.EXPENSE, "Handyman", 0xFFD97706),
            FinancialCategory("exp_impostos", "Impostos", TransactionType.EXPENSE, "ReceiptLong", 0xFFBE123C),
            FinancialCategory("exp_outros", "Outros", TransactionType.EXPENSE, "MoreHoriz", 0xFF64748B)
        )

        val ALL_CATEGORIES = DEFAULT_INCOME_CATEGORIES + DEFAULT_EXPENSE_CATEGORIES

        fun findByName(name: String): FinancialCategory? {
            return ALL_CATEGORIES.firstOrNull { it.name.equals(name, ignoreCase = true) }
        }
    }
}
