package com.example.domain.ai

import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AppCurrency
import com.example.data.model.BudgetStatus
import com.example.data.model.CompanySubscription
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.invoice.InvoiceStatus
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.ui.viewmodel.CashFlowUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LocalAiFinancialEngine {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "AO"))

    fun formatMoney(amount: Double, currency: AppCurrency): String {
        val formatter = NumberFormat.getNumberInstance(Locale("pt", "AO")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "${formatter.format(amount)} ${currency.symbol}"
    }

    /**
     * Builds the comprehensive financial context string representing current real business data.
     * Injected into Gemini 3.5 Flash system prompt.
     */
    fun buildFinancialContext(
        uiState: CashFlowUiState,
        invoices: List<InvoiceEntity>,
        clients: List<ClientEntity>,
        budgets: List<BudgetEntity>,
        subscription: CompanySubscription,
        currency: AppCurrency
    ): String {
        val sb = StringBuilder()
        sb.appendLine("=== CONTEXTO FINANCEIRO EM TEMPO REAL (FLUXAO) ===")
        sb.appendLine("Empresa: ${subscription.companyName.ifBlank { "Minha Empresa" }} | NIF: ${subscription.nif.ifBlank { "Não informado" }}")
        sb.appendLine("Moeda Selecionada: ${currency.name} (${currency.symbol} - ${currency.code})")
        sb.appendLine("Saldo Atual em Caixa: ${formatMoney(uiState.currentBalance, currency)}")
        sb.appendLine("Receitas do Mês Atual: ${formatMoney(uiState.monthIncome, currency)}")
        sb.appendLine("Despesas do Mês Atual: ${formatMoney(uiState.monthExpense, currency)}")
        sb.appendLine("Resultado Líquido do Mês: ${formatMoney(uiState.monthNetProfit, currency)}")
        sb.appendLine("Margem Líquida do Mês: ${String.format(Locale.US, "%.1f", uiState.monthNetMarginPercent)}%")
        sb.appendLine("Valores a Receber Pendentes: ${formatMoney(uiState.pendingIncome, currency)}")
        sb.appendLine("Valores a Pagar Pendentes: ${formatMoney(uiState.pendingExpense, currency)}")

        // Category breakdown
        if (uiState.categoryShares.isNotEmpty()) {
            sb.appendLine("\n--- Maiores Despesas por Categoria (Mês Atual) ---")
            uiState.categoryShares.take(5).forEach { share ->
                sb.appendLine("- ${share.categoryName}: ${formatMoney(share.amount, currency)} (${String.format(Locale.US, "%.1f", share.percentage)}%)")
            }
        }

        // Pending invoices (who owes money)
        val pendingInvoices = invoices.filter { it.status == InvoiceStatus.PENDING }
        sb.appendLine("\n--- Faturas Pendentes / Contas a Receber (${pendingInvoices.size} faturas) ---")
        if (pendingInvoices.isEmpty()) {
            sb.appendLine("Nenhuma fatura pendente no momento.")
        } else {
            pendingInvoices.take(10).forEach { inv ->
                val dateStr = dateFormat.format(Date(inv.dateCreatedEpoch))
                sb.appendLine("- Fatura #${inv.invoiceNumber} (${inv.invoiceType.name}): ${inv.clientName} | Valor: ${formatMoney(inv.totalToPay, currency)} | Status: ${inv.status.name} | Data: $dateStr")
            }
        }

        // Clients overview
        sb.appendLine("\n--- Clientes Cadastrados (${clients.size}) ---")
        clients.take(10).forEach { client ->
            sb.appendLine("- ${client.name} | NIF: ${client.nif.ifBlank { "N/D" }} | Tel: ${client.phone.ifBlank { "N/D" }}")
        }

        // Budgets overview
        val approvedBudgets = budgets.filter { it.status == BudgetStatus.APPROVED }
        val pendingBudgets = budgets.filter { it.status == BudgetStatus.SENT || it.status == BudgetStatus.DRAFT }
        sb.appendLine("\n--- Orçamentos (Total: ${budgets.size}) ---")
        sb.appendLine("- Aprovados: ${approvedBudgets.size} | Em Aberto/Rascunho: ${pendingBudgets.size}")

        // Recent transactions
        sb.appendLine("\n--- Transações Recentes (Últimas 15) ---")
        uiState.transactions.take(15).forEach { tx ->
            val dateStr = dateFormat.format(Date(tx.dateEpochMillis))
            val sign = if (tx.type == TransactionType.INCOME) "+" else "-"
            sb.appendLine("- [$dateStr] $sign${formatMoney(tx.amount, currency)} | ${tx.description} | Categoria: ${tx.category} | Status: ${tx.status.name}")
        }

        sb.appendLine("=== FIM DO CONTEXTO ===")
        return sb.toString()
    }

    /**
     * Local intelligence processing engine for offline mode, zero-token situations, or immediate answers.
     * Follows the 14 fundamental guidelines strictly.
     */
    fun processQuery(
        query: String,
        uiState: CashFlowUiState,
        invoices: List<InvoiceEntity>,
        clients: List<ClientEntity>,
        budgets: List<BudgetEntity>,
        subscription: CompanySubscription,
        currency: AppCurrency
    ): String {
        val q = query.trim().lowercase(Locale.ROOT)

        // 1. Saldo / Quanto tenho em caixa
        if (q.contains("quanto tenho") || q.contains("em caixa") || q.contains("saldo atual") || q.contains("saldo")) {
            val netStr = if (uiState.monthNetProfit >= 0) "lucro" else "prejuízo"
            return """
                🏦 **Situação Atual do Caixa**
                
                • **Saldo em Caixa:** ${formatMoney(uiState.currentBalance, currency)}
                • **Entradas este mês:** ${formatMoney(uiState.monthIncome, currency)}
                • **Saídas este mês:** ${formatMoney(uiState.monthExpense, currency)}
                • **Resultado líquido do mês:** ${formatMoney(uiState.monthNetProfit, currency)} ($netStr)
                • **Margem líquida:** ${String.format(Locale.US, "%.1f", uiState.monthNetMarginPercent)}%
                
                📌 *Dica FluxAO:* Você tem ${formatMoney(uiState.pendingIncome, currency)} em valores pendentes a receber de faturas e clientes.
            """.trimIndent()
        }

        // 2. Despesas do mês / Quanto gastei
        if (q.contains("quanto gastei") || q.contains("minhas despesas") || q.contains("gasto este mês") || q.contains("despesa total")) {
            val expensePercent = if (uiState.monthIncome > 0) {
                (uiState.monthExpense / uiState.monthIncome) * 100.0
            } else 0.0

            val topCategory = uiState.categoryShares.firstOrNull()
            val categoryInsight = if (topCategory != null) {
                "\nA categoria com maior gasto foi **${topCategory.categoryName}**, consumindo ${formatMoney(topCategory.amount, currency)} (${String.format(Locale.US, "%.1f", topCategory.percentage)}% das despesas)."
            } else ""

            return """
                📉 **Análise de Despesas do Mês**
                
                • **Total Gasto:** ${formatMoney(uiState.monthExpense, currency)}
                • **Entradas Registradas:** ${formatMoney(uiState.monthIncome, currency)}
                • **Impacto no Faturamento:** As despesas consomem ${String.format(Locale.US, "%.1f", expensePercent)}% das receitas.$categoryInsight
                
                ${getTopCategoriesTable(uiState, currency)}
            """.trimIndent()
        }

        // 3. Maior despesa
        if (q.contains("maior despesa") || q.contains("maiores despesas") || q.contains("maior gasto")) {
            val expenses = uiState.transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sortedByDescending { it.amount }

            if (expenses.isEmpty()) {
                return "Não encontrei registros de despesas nos seus dados do FluxAO até o momento."
            }

            val top = expenses.first()
            val topDate = dateFormat.format(Date(top.dateEpochMillis))
            val otherTops = expenses.drop(1).take(3)

            val othersList = if (otherTops.isNotEmpty()) {
                "\n\n**Outras despesas significativas:**\n" + otherTops.joinToString("\n") {
                    "• ${it.description}: ${formatMoney(it.amount, currency)} (${it.category}) em ${dateFormat.format(Date(it.dateEpochMillis))}"
                }
            } else ""

            return """
                🔍 **Maior Despesa Registrada**
                
                • **Descrição:** ${top.description}
                • **Valor:** ${formatMoney(top.amount, currency)}
                • **Categoria:** ${top.category}
                • **Data:** $topDate
                • **Status:** ${if (top.status == TransactionStatus.PAID) "Pago" else "Pendente"}$othersList
            """.trimIndent()
        }

        // 4. Receitas / Quanto recebi
        if (q.contains("quanto recebi") || q.contains("minhas receitas") || q.contains("entradas") || q.contains("faturamento")) {
            val incomeTx = uiState.transactions.filter { it.type == TransactionType.INCOME }
            val avgIncome = if (incomeTx.isNotEmpty()) uiState.monthIncome / 30.0 else 0.0

            return """
                📈 **Análise de Receitas**
                
                • **Receita Total do Mês:** ${formatMoney(uiState.monthIncome, currency)}
                • **Média Diária Estimada:** ${formatMoney(avgIncome, currency)}/dia
                • **Lançamentos de Entrada:** ${incomeTx.size} transação(ões)
                • **Valores ainda a Receber (Pendentes):** ${formatMoney(uiState.pendingIncome, currency)}
                
                💡 O seu resultado líquido atual é de **${formatMoney(uiState.monthNetProfit, currency)}**.
            """.trimIndent()
        }

        // 5. Quem ainda me deve / Contas a receber
        if (q.contains("quem ainda me deve") || q.contains("quem me deve") || q.contains("a receber") || q.contains("contas a receber") || q.contains("devedores")) {
            val pendingInvoices = invoices.filter { it.status == InvoiceStatus.PENDING }

            if (pendingInvoices.isEmpty() && uiState.pendingIncome <= 0.0) {
                return "🎉 Parabéns! Não encontrei nenhuma conta ou fatura pendente a receber nos seus registros. Todos os clientes estão em dia."
            }

            val totalPending = pendingInvoices.sumOf { it.totalToPay }
            val list = pendingInvoices.joinToString("\n") { inv ->
                val dateStr = dateFormat.format(Date(inv.dateCreatedEpoch))
                "• **${inv.clientName}** — ${formatMoney(inv.totalToPay, currency)} (Fatura #${inv.invoiceNumber} emitida em $dateStr)"
            }

            val displayTotal = if (totalPending > 0.0) totalPending else uiState.pendingIncome

            return """
                🧾 **Contas a Receber & Clientes Pendentes**
                
                • **Total a Receber:** ${formatMoney(displayTotal, currency)}
                • **Faturas em Aberto:** ${pendingInvoices.size} fatura(s)
                
                **Detalhamento:**
                $list
                
                💡 *Ação sugerida:* Você pode acessar a aba **Faturas** ou **Clientes** para enviar uma lembrança amigável de cobrança via WhatsApp.
            """.trimIndent()
        }

        // 6. Contas a pagar
        if (q.contains("contas a pagar") || q.contains("a pagar") || q.contains("pendentes de pagamento") || q.contains("compromissos")) {
            val pendingExpenses = uiState.transactions.filter {
                it.type == TransactionType.EXPENSE && it.status == TransactionStatus.PENDING
            }

            if (pendingExpenses.isEmpty()) {
                return "Não encontrei despesas pendentes de pagamento registradas no momento. Todas as despesas lançadas constam como liquidadas."
            }

            val total = pendingExpenses.sumOf { it.amount }
            val list = pendingExpenses.joinToString("\n") { tx ->
                "• **${tx.description}** — ${formatMoney(tx.amount, currency)} (${tx.category})"
            }

            return """
                ⚠️ **Contas a Pagar Registradas**
                
                • **Total a Pagar:** ${formatMoney(total, currency)}
                • **Quantidade:** ${pendingExpenses.size} pagamento(s) pendente(s)
                
                **Compromissos:**
                $list
                
                Saldo disponível em caixa: **${formatMoney(uiState.currentBalance, currency)}**.
            """.trimIndent()
        }

        // 7. Relatório Financeiro (Mensal / Geral)
        if (q.contains("relatório") || q.contains("relatorio") || q.contains("resumo financeiro") || q.contains("dre")) {
            val netStatus = if (uiState.monthNetProfit >= 0) "Superávit (+)" else "Déficit (-)"
            return """
                📊 **Relatório Financeiro Executivo — ${subscription.companyName.ifBlank { "FluxAO" }}**
                
                **1. Demonstração de Resultados (Mês Vigente)**
                | Indicador | Valor |
                |---|---|
                | **Receita Bruta (Entradas)** | ${formatMoney(uiState.monthIncome, currency)} |
                | **Despesas Totais (Saídas)** | ${formatMoney(uiState.monthExpense, currency)} |
                | **Resultado Líquido** | ${formatMoney(uiState.monthNetProfit, currency)} ($netStatus) |
                | **Margem Líquida** | ${String.format(Locale.US, "%.1f", uiState.monthNetMarginPercent)}% |
                | **Saldo Atual em Caixa** | ${formatMoney(uiState.currentBalance, currency)} |
                
                **2. Posição de Créditos e Débitos**
                • Contas a Receber (Faturas/Clientes): **${formatMoney(uiState.pendingIncome, currency)}**
                • Contas a Pagar: **${formatMoney(uiState.pendingExpense, currency)}**
                
                **3. Diagnóstico do Assistente**
                ${if (uiState.monthNetProfit > 0) "A operação está rentável com geração positiva de caixa." else "Atenção: As despesas superaram as receitas no período analisado."}
            """.trimIndent()
        }

        // 8. Alertas e Insights
        if (q.contains("alerta") || q.contains("alertas") || q.contains("insight") || q.contains("insights") || q.contains("atenção") || q.contains("risco")) {
            val alerts = mutableListOf<String>()

            if (uiState.currentBalance < 0) {
                alerts.add("🚨 **Saldo Negativo:** O caixa está com saldo de ${formatMoney(uiState.currentBalance, currency)}. Evite novas despesas não essenciais.")
            } else if (uiState.currentBalance < uiState.monthExpense * 0.2 && uiState.monthExpense > 0) {
                alerts.add("⚠️ **Reserva Reduzida:** Seu saldo atual cobre menos de 20% das despesas mensais habituais.")
            }

            if (uiState.monthExpense > uiState.monthIncome && uiState.monthIncome > 0) {
                alerts.add("📉 **Despesas Superiores à Receita:** As saídas este mês (${formatMoney(uiState.monthExpense, currency)}) estão maiores que as entradas (${formatMoney(uiState.monthIncome, currency)}).")
            }

            if (uiState.pendingIncome > 0) {
                alerts.add("💰 **Recebíveis em Aberto:** Existem ${formatMoney(uiState.pendingIncome, currency)} a receber em faturas que podem reforçar seu caixa.")
            }

            val topCat = uiState.categoryShares.firstOrNull()
            if (topCat != null && topCat.percentage > 40.0) {
                alerts.add("📊 **Concentração de Custo:** A categoria **${topCat.categoryName}** representa ${String.format(Locale.US, "%.1f", topCat.percentage)}% de todos os seus gastos.")
            }

            if (alerts.isEmpty()) {
                alerts.add("✅ Todos os principais indicadores financeiros estão equilibrados! Fluxo de caixa com margem de ${String.format(Locale.US, "%.1f", uiState.monthNetMarginPercent)}% e saldo positivo de ${formatMoney(uiState.currentBalance, currency)}.")
            }

            return """
                💡 **Alertas & Insights Inteligentes do FluxAO**
                
                ${alerts.joinToString("\n\n")}
            """.trimIndent()
        }

        // 9. Previsão
        if (q.contains("previsão") || q.contains("previsao") || q.contains("estimativa") || q.contains("projeção") || q.contains("futuro") || q.contains("próximo mês")) {
            val projectedIncome = uiState.monthIncome * 1.05
            val projectedExpense = uiState.monthExpense * 1.02
            val projectedNet = projectedIncome - projectedExpense

            return """
                🔮 **Estimativa Financeira para o Próximo Mês**
                
                Com base na média histórica dos lançamentos:
                
                • **Estimativa de Receitas:** ~ ${formatMoney(projectedIncome, currency)}
                • **Estimativa de Despesas:** ~ ${formatMoney(projectedExpense, currency)}
                • **Resultado Projetado:** ~ ${formatMoney(projectedNet, currency)}
                
                ⚠️ *IMPORTANTE: Previsões são apenas estimativas matemáticas baseadas nos dados históricos disponíveis no aplicativo e não constituem certeza ou garantia financeira.*
            """.trimIndent()
        }

        // 10. Quanto posso gastar sem comprometer o caixa
        if (q.contains("quanto posso gastar") || q.contains("posso gastar") || q.contains("limite de gasto")) {
            val safeReserve = uiState.currentBalance * 0.30
            val safeToSpend = maxOf(0.0, uiState.currentBalance - safeReserve - uiState.pendingExpense)

            return """
                🛡️ **Capacidade de Gasto Seguro**
                
                • **Saldo Atual:** ${formatMoney(uiState.currentBalance, currency)}
                • **Reserva de Segurança Recomendada (30%):** ${formatMoney(safeReserve, currency)}
                • **Contas a Pagar Previstas:** ${formatMoney(uiState.pendingExpense, currency)}
                
                👉 **Valor sugerido para gastos não essenciais:** até **${formatMoney(safeToSpend, currency)}**.
                
                *Recomendação:* Mantenha sempre um fundo de reserva para imprevistos operacionais.
            """.trimIndent()
        }

        // 11. Comparativo / Como está meu fluxo
        if (q.contains("como está") || q.contains("como esta") || q.contains("comparar") || q.contains("evolução") || q.contains("fluxo de caixa")) {
            val healthText = when {
                uiState.monthNetProfit > 0 && uiState.monthNetMarginPercent >= 20.0 -> "🟢 **Excelente Saúde Financeira**: Margem líquida robusta de ${String.format(Locale.US, "%.1f", uiState.monthNetMarginPercent)}%."
                uiState.monthNetProfit > 0 -> "🟡 **Saúde Financeira Moderada**: Lucro positivo (${formatMoney(uiState.monthNetProfit, currency)}), mas recomenda-se otimizar despesas."
                else -> "🔴 **Atenção ao Caixa**: Operação em déficit de ${formatMoney(-uiState.monthNetProfit, currency)} no mês atual."
            }

            return """
                🌊 **Visão Geral do Fluxo de Caixa**
                
                $healthText
                
                • **Total Entradas:** ${formatMoney(uiState.monthIncome, currency)}
                • **Total Saídas:** ${formatMoney(uiState.monthExpense, currency)}
                • **Saldo Disponível:** ${formatMoney(uiState.currentBalance, currency)}
                • **Previsão de Recebíveis:** ${formatMoney(uiState.pendingIncome, currency)}
                
                Posso detalhar despesas por categoria ou mostrar devedores se desejar.
            """.trimIndent()
        }

        // 12. Orçamentos
        if (q.contains("orçamento") || q.contains("orcamento") || q.contains("proposta") || q.contains("propostas")) {
            val approved = budgets.filter { it.status == BudgetStatus.APPROVED }
            val sent = budgets.filter { it.status == BudgetStatus.SENT }
            val drafts = budgets.filter { it.status == BudgetStatus.DRAFT }

            val totalApproved = approved.sumOf { CostEngineeringCalculator.calculateForBudget(it).finalSellingPrice }
            val totalSent = sent.sumOf { CostEngineeringCalculator.calculateForBudget(it).finalSellingPrice }

            return """
                📐 **Resumo de Orçamentos & Propostas**
                
                • **Total de Orçamentos:** ${budgets.size}
                • **Aprovados:** ${approved.size} (${formatMoney(totalApproved, currency)})
                • **Enviados / Em Negociação:** ${sent.size} (${formatMoney(totalSent, currency)})
                • **Rascunhos:** ${drafts.size}
                
                💡 O método de formação de preço configurado é o **Método Angolano** (Mão de Obra = 85% da matéria-prima + BDI e Lucro Líquido).
            """.trimIndent()
        }

        // Generic fallback when query asks about unregistered data
        return """
            Olá! Sou o **FluxAO AI**, seu assistente financeiro inteligente.
            
            Para a sua consulta:
            Não encontrei esse dado específico nos seus registros do FluxAO.
            
            Você pode me perguntar:
            • *"Quanto tenho em caixa?"*
            • *"Quanto gastei este mês?"*
            • *"Qual foi minha maior despesa?"*
            • *"Quem ainda me deve?"*
            • *"Mostre minhas contas a pagar"*
            • *"Faça um relatório mensal"*
            • *"Alertas e insights financeiros"*
            • *"Previsão para o próximo mês"*
            
            Como posso ajudar sua empresa hoje?
        """.trimIndent()
    }

    private fun getTopCategoriesTable(uiState: CashFlowUiState, currency: AppCurrency): String {
        if (uiState.categoryShares.isEmpty()) return ""
        val rows = uiState.categoryShares.take(4).joinToString("\n") {
            "| ${it.categoryName} | ${formatMoney(it.amount, currency)} | ${String.format(Locale.US, "%.1f", it.percentage)}% |"
        }
        return """
            **Detalhamento por Categoria:**
            | Categoria | Valor Gasto | Participação |
            |---|---|---|
            $rows
        """.trimIndent()
    }
}
