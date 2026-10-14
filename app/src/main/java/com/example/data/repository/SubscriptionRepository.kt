package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CompanySubscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class SubscriptionRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("company_subscription_prefs", Context.MODE_PRIVATE)

    private val _subscription = MutableStateFlow(loadSubscription())
    val subscription: StateFlow<CompanySubscription> = _subscription.asStateFlow()

    private fun loadSubscription(): CompanySubscription {
        val companyName = prefs.getString(KEY_COMPANY_NAME, "Minha Empresa Pro")?.ifBlank { "Minha Empresa Pro" } ?: "Minha Empresa Pro"
        val nif = prefs.getString(KEY_NIF, "5412345678")?.ifBlank { "5412345678" } ?: "5412345678"
        val contactPhone = prefs.getString(KEY_CONTACT_PHONE, "+244 923 000 000")?.ifBlank { "+244 923 000 000" } ?: "+244 923 000 000"
        val isActivated = true
        val activationDate = prefs.getLong(KEY_ACTIVATION_DATE, System.currentTimeMillis())
        val expirationDate = Long.MAX_VALUE // Licença Vitalícia Pro
        val paymentPhone = prefs.getString(KEY_PAYMENT_PHONE, "+244 923 000 000") ?: "+244 923 000 000"
        val paymentRef = prefs.getString(KEY_PAYMENT_REF, "PRO-FLUXAO-LIFETIME") ?: "PRO-FLUXAO-LIFETIME"
        val lastTxId = prefs.getString(KEY_LAST_TX_ID, "TX_PRO_LIFETIME") ?: "TX_PRO_LIFETIME"
        val destinationIban = prefs.getString(KEY_DESTINATION_IBAN, DEFAULT_DESTINATION_IBAN) ?: DEFAULT_DESTINATION_IBAN

        return CompanySubscription(
            companyName = companyName,
            nif = nif,
            contactPhone = contactPhone,
            isActivated = true,
            activationDateEpoch = activationDate,
            expirationDateEpoch = expirationDate,
            paymentPhone = paymentPhone,
            paymentReference = paymentRef,
            lastTransactionId = lastTxId,
            destinationIban = destinationIban
        )
    }

    fun updateCompanyInfo(companyName: String, nif: String = "", contactPhone: String = "", destinationIban: String = DEFAULT_DESTINATION_IBAN) {
        prefs.edit()
            .putString(KEY_COMPANY_NAME, companyName)
            .putString(KEY_NIF, nif)
            .putString(KEY_CONTACT_PHONE, contactPhone)
            .putString(KEY_DESTINATION_IBAN, destinationIban)
            .apply()

        _subscription.value = _subscription.value.copy(
            companyName = companyName,
            nif = nif,
            contactPhone = contactPhone,
            destinationIban = destinationIban
        )
    }

    fun activateMonthlyPlan(paymentPhone: String, paymentRef: String, transactionId: String, destinationIban: String = DEFAULT_DESTINATION_IBAN): CompanySubscription {
        val now = System.currentTimeMillis()
        val currentExpiration = _subscription.value.expirationDateEpoch
        // If current subscription is still active, add 30 days to existing expiration; otherwise start 30 days from now
        val baseDate = if (currentExpiration > now) currentExpiration else now
        val thirtyDaysMillis = TimeUnit.DAYS.toMillis(30)
        val newExpiration = baseDate + thirtyDaysMillis

        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, true)
            .putLong(KEY_ACTIVATION_DATE, now)
            .putLong(KEY_EXPIRATION_DATE, newExpiration)
            .putString(KEY_PAYMENT_PHONE, paymentPhone)
            .putString(KEY_PAYMENT_REF, paymentRef)
            .putString(KEY_LAST_TX_ID, transactionId)
            .putString(KEY_DESTINATION_IBAN, destinationIban)
            .apply()

        val updated = _subscription.value.copy(
            isActivated = true,
            activationDateEpoch = now,
            expirationDateEpoch = newExpiration,
            paymentPhone = paymentPhone,
            paymentReference = paymentRef,
            lastTransactionId = transactionId,
            destinationIban = destinationIban
        )
        _subscription.value = updated
        return updated
    }

    fun resetSubscription() {
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, false)
            .putLong(KEY_ACTIVATION_DATE, 0L)
            .putLong(KEY_EXPIRATION_DATE, 0L)
            .apply()

        _subscription.value = _subscription.value.copy(
            isActivated = false,
            activationDateEpoch = 0L,
            expirationDateEpoch = 0L
        )
    }

    companion object {
        private const val KEY_COMPANY_NAME = "company_name"
        private const val KEY_NIF = "company_nif"
        private const val KEY_CONTACT_PHONE = "company_contact_phone"
        private const val KEY_IS_ACTIVATED = "is_activated"
        private const val KEY_ACTIVATION_DATE = "activation_date"
        private const val KEY_EXPIRATION_DATE = "expiration_date"
        private const val KEY_PAYMENT_PHONE = "payment_phone"
        private const val KEY_PAYMENT_REF = "payment_ref"
        private const val KEY_LAST_TX_ID = "last_tx_id"
        private const val KEY_DESTINATION_IBAN = "destination_iban"
        const val DEFAULT_DESTINATION_IBAN = "0006 0000 43818047301 44"
    }
}
