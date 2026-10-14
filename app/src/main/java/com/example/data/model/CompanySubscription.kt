package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CompanySubscription(
    val companyName: String = "",
    val nif: String = "",
    val contactPhone: String = "",
    val isActivated: Boolean = false,
    val activationDateEpoch: Long = 0L,
    val expirationDateEpoch: Long = 0L,
    val paymentPhone: String = "",
    val paymentReference: String = "",
    val lastTransactionId: String = "",
    val destinationIban: String = "0006 0000 43818047301 44"
) {
    val isValid: Boolean
        get() = isActivated && expirationDateEpoch > System.currentTimeMillis()

    val daysRemaining: Long
        get() {
            if (!isValid) return 0L
            val diff = expirationDateEpoch - System.currentTimeMillis()
            return if (diff > 0) (diff / (1000L * 60 * 60 * 24)) + 1 else 0L
        }

    val formattedExpirationDate: String
        get() {
            if (expirationDateEpoch == 0L) return "Não ativado"
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "AO"))
            return sdf.format(Date(expirationDateEpoch))
        }

    val formattedActivationDate: String
        get() {
            if (activationDateEpoch == 0L) return "Não ativado"
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "AO"))
            return sdf.format(Date(activationDateEpoch))
        }
}
