package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fornecedores e parceiros comerciais de insumos e matérias-primas
 */
@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val address: String = "",
    val nif: String = "",
    val category: String = "Matéria-prima & Materiais",
    val notes: String = "",
    val companyId: String = "default_company",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(createdAtEpochMillis))

    val initials: String
        get() {
            val parts = name.trim().split(" ").filter { it.isNotBlank() }
            return when {
                parts.isEmpty() -> "FN"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts[0].first()}${parts[1].first()}".uppercase()
            }
        }

    val cleanPhoneForDial: String
        get() = phone.filter { it.isDigit() || it == '+' }

    val cleanPhoneForWhatsApp: String
        get() {
            val target = if (whatsapp.isNotBlank()) whatsapp else phone
            val digits = target.filter { it.isDigit() }
            return when {
                digits.startsWith("244") -> digits
                digits.length == 9 && (digits.startsWith("9") || digits.startsWith("2")) -> "244$digits"
                else -> digits
            }
        }
}
