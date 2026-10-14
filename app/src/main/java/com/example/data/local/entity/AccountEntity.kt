package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AccountType

/**
 * Contas bancárias e carteiras financeiras do negócio (BAI, BFA, Multicaixa Express, Dinheiro, etc.)
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // "Dinheiro / Caixa", "BAI", "BFA", "Multicaixa Express", "Unitel Money", "Afrimoney"
    val type: String = "BANK", // "CASH", "BANK", "WALLET", "MULTICAIXA_EXPRESS", "OTHER"
    val accountNumberOrIban: String = "",
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE"
    val colorHex: Long = 0xFF0F172A,
    val isDefault: Boolean = false,
    val companyId: String = "default_company",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
) {
    val isActive: Boolean
        get() = status.equals("ACTIVE", ignoreCase = true)

    val accountNumber: String
        get() = accountNumberOrIban

    val accountType: AccountType
        get() = try {
            AccountType.valueOf(type)
        } catch (_: Exception) {
            AccountType.BANK
        }
}
