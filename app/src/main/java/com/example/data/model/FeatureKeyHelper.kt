package com.example.data.model

/**
 * Centralized feature keys and alias resolution for FluxAO.
 * Ensures consistent checking across "agenda" vs "appointments",
 * "clients" vs "customers", "invoices" vs "faturas",
 * and "reports" vs "advanced_reports".
 */
object FeatureKeyHelper {
    const val CASHFLOW = "cashflow_basic"
    const val INVOICES = "invoices"
    const val CLIENTS = "clients"
    const val AGENDA = "agenda"
    const val ADVANCED_REPORTS = "advanced_reports"
    const val FLUXOAI = "fluxoai"
    const val WHATSAPP = "whatsapp_automation"

    fun normalize(key: String): String {
        return when (key.trim().lowercase()) {
            "agenda", "appointments", "agendamentos", "compromissos" -> AGENDA
            "invoices", "faturas", "fatura", "invoice" -> INVOICES
            "clients", "customers", "clientes", "cliente" -> CLIENTS
            "reports", "advanced_reports", "relatorios", "relatorios_avancados" -> ADVANCED_REPORTS
            "fluxoai", "ai", "ia", "assistente", "assistente_ia" -> FLUXOAI
            "whatsapp", "whatsapp_automation", "whatsapp_avancado" -> WHATSAPP
            "cashflow", "cashflow_basic", "fluxo_caixa", "fluxo_de_caixa" -> CASHFLOW
            else -> key.trim().lowercase()
        }
    }

    fun getAllAliases(key: String): List<String> {
        val canonical = normalize(key)
        val list = mutableListOf(canonical, key)
        when (canonical) {
            AGENDA -> list.addAll(listOf("agenda", "appointments", "agendamentos"))
            INVOICES -> list.addAll(listOf("invoices", "faturas", "invoice"))
            CLIENTS -> list.addAll(listOf("clients", "customers", "clientes"))
            ADVANCED_REPORTS -> list.addAll(listOf("reports", "advanced_reports", "relatorios"))
            FLUXOAI -> list.addAll(listOf("fluxoai", "ai", "ia", "assistente"))
            WHATSAPP -> list.addAll(listOf("whatsapp", "whatsapp_automation"))
            CASHFLOW -> list.addAll(listOf("cashflow", "cashflow_basic", "fluxo_caixa"))
        }
        return list.distinct()
    }

    fun isUnlocked(unlockedSet: Set<String>, featureKey: String): Boolean {
        val canonical = normalize(featureKey)
        if (canonical == CASHFLOW) return true
        val aliases = getAllAliases(featureKey)
        return aliases.any { unlockedSet.contains(it) }
    }
}
