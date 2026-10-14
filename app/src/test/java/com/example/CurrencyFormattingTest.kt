package com.example

import com.example.data.model.AppCurrency
import com.example.domain.calculator.CostEngineeringCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyFormattingTest {

    @Test
    fun testAngolanKwanzaFormatting() {
        val currency = AppCurrency.AOA
        assertEquals("AOA", currency.code)
        assertEquals("Kz", currency.symbol)

        val formatted = currency.format(150000.50)
        // Should contain symbol "Kz" and digits
        assertTrue(formatted.startsWith("Kz "))
        assertTrue(formatted.contains("150"))
        assertTrue(formatted.contains("50"))
    }

    @Test
    fun testCalculatorCurrencySwitch() {
        CostEngineeringCalculator.setCurrency(AppCurrency.AOA)
        val kwanzaString = CostEngineeringCalculator.formatCurrency(2500.0)
        assertTrue(kwanzaString.startsWith("Kz "))

        CostEngineeringCalculator.setCurrency(AppCurrency.USD)
        val usdString = CostEngineeringCalculator.formatCurrency(2500.0)
        assertTrue(usdString.startsWith("$ "))

        CostEngineeringCalculator.setCurrency(AppCurrency.BRL)
        val realString = CostEngineeringCalculator.formatCurrency(2500.0)
        assertTrue(realString.startsWith("R$ "))
    }

    @Test
    fun testCurrencyFromCode() {
        assertEquals(AppCurrency.AOA, AppCurrency.fromCode("AOA"))
        assertEquals(AppCurrency.BRL, AppCurrency.fromCode("brl"))
        assertEquals(AppCurrency.USD, AppCurrency.fromCode("USD"))
        assertEquals(AppCurrency.EUR, AppCurrency.fromCode("eur"))
        assertEquals(AppCurrency.MZN, AppCurrency.fromCode("MZN"))
        // Unknown defaults to AOA
        assertEquals(AppCurrency.AOA, AppCurrency.fromCode("INVALID"))
    }
}
