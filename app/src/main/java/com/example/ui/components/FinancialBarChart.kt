package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.AppCurrency

data class MonthlyBarData(
    val monthLabel: String,
    val income: Double,
    val expense: Double,
    val net: Double = income - expense,
    val year: Int = 2026,
    val monthIndex: Int = 0,
    val fullMonthName: String = monthLabel
)

/**
 * Backward-compatible entry point delegating to the interactive Recharts-inspired [CashFlowEvolutionChart].
 */
@Composable
fun FinancialBarChart(
    data: List<MonthlyBarData>,
    modifier: Modifier = Modifier,
    currency: AppCurrency = AppCurrency.DEFAULT
) {
    CashFlowEvolutionChart(
        data = data,
        currency = currency,
        modifier = modifier
    )
}
