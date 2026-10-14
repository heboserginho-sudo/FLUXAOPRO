package com.example.data.ads

/**
 * Contextual classification of application screens for AdMob ad placement.
 * Ensures ads are strictly disallowed during sensitive or critical flows as required.
 */
enum class AdScreenContext(val isAllowed: Boolean, val description: String) {
    // Allowed positions
    DASHBOARD(true, "Página Inicial / Dashboard"),
    FEED_CATALOG(true, "Feed / Catálogo de Produtos e Serviços"),
    REPORTS(true, "Relatórios Gerais"),

    // Strictly Blocked Screens (Compliance & User Experience)
    LOGIN(false, "Autenticação e Login"),
    REGISTER(false, "Cadastro e Onboarding"),
    PAYMENT(false, "Processamento de Pagamento"),
    PAYMENT_CONFIRMATION(false, "Confirmação de Pagamento"),
    APPOINTMENT_BOOKING(false, "Agendamento de Compromisso"),
    BUDGET_SENDING(false, "Envio e Emissão de Orçamento"),
    IMAGE_UPLOAD(false, "Upload e Edição de Imagens"),
    CRITICAL_PROCESS(false, "Processos Críticos e Fiscais")
}
