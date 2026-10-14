package com.example.data.model

enum class TransactionType(val label: String) {
    INCOME("Entrada"),
    EXPENSE("Saída"),
    TRANSFER("Transferência")
}

enum class TransactionStatus(val label: String) {
    PAID("Pago"),
    PENDING("Pendente")
}

enum class PaymentMethod(val label: String) {
    MULTICAIXA_EXPRESS("Multicaixa Express"),
    BAI_DIRECTO("Transferência BAI"),
    BFA_NET("Transferência BFA"),
    BANK_TRANSFER("Transferência Bancária"),
    TPA("TPA / Cartão Multicaixa"),
    CASH("Dinheiro"),
    UNITEL_MONEY("Unitel Money"),
    AFRIMONEY("Afrimoney"),
    PIX("PIX"),
    CREDIT_CARD("Cartão de Crédito"),
    DEBIT_CARD("Cartão de Débito"),
    BANK_SLIP("Boleto / Guia"),
    OTHER("Outro Método")
}

enum class BudgetStatus(val label: String) {
    DRAFT("Rascunho"),
    SENT("Enviado"),
    WAITING_APPROVAL("Aguardando aprovação"),
    APPROVED("Aprovado"),
    IN_EXECUTION("Em execução"),
    COMPLETED("Concluído"),
    CANCELLED("Cancelado"),
    REJECTED("Recusado")
}

enum class BudgetItemType(val label: String) {
    MATERIAL("Material / Insumo"),
    LABOR("Mão de Obra"),
    OUTSOURCED("Serviço Terceirizado"),
    OTHER("Outros Custos")
}

enum class BreakEvenStatus(val label: String) {
    HEALTHY("Excelente Viabilidade"),
    VIABLE("Viável / Equilibrado"),
    WARNING("Atenção: Margem Reduzida"),
    DEFICIT("Crítico: Prejuízo Operacional")
}

enum class AccountType(val label: String) {
    CASH("Dinheiro / Caixa"),
    BANK("Conta Bancária"),
    WALLET("Carteira Móvel"),
    MULTICAIXA_EXPRESS("Multicaixa Express"),
    OTHER("Outra Conta")
}
