package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.BudgetStatus

@Entity(
    tableName = "budgets",
    indices = [
        Index("dateCreatedEpochMillis"),
        Index("status"),
        Index("clientName")
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val budgetNumber: String = "",
    val clientName: String,
    val clientContact: String = "",
    val projectTitle: String,
    val projectScope: String = "",
    val dateCreatedEpochMillis: Long = System.currentTimeMillis(),
    val deadlineDays: Int = 15,
    val status: BudgetStatus = BudgetStatus.DRAFT,
    // Custos Diretos
    val directMaterialsTotal: Double = 0.0,
    val laborPercentageOnMaterials: Double = 85.0, // Método Angolano: Mão de obra = 85% da matéria-prima
    val laborHours: Double = 0.0,
    val laborHourlyRate: Double = 0.0,
    // Engenharia de Custos: Rateio & Taxas
    val overheadPercentage: Double = 0.0, // Custos Indiretos / Rateio Operacional adicional
    val taxesAndSalesFeePercent: Double = 14.0, // IVA AGT padrão de Angola (14%)
    val desiredProfitMarginPercent: Double = 80.0, // Margem de Lucro Líquido no Método Angolano (70% a 100%+)
    val discountAmount: Double = 0.0,
    // Condições Comerciais
    val paymentTerms: String = "50% de sinal e 50% na conclusão",
    val termsAndConditions: String = "Proposta válida por 15 dias. Conforme regulamentação comercial e fiscal em Angola.",
    val notes: String = "",
    val companyId: String = "default_company"
)
