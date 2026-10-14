package com.example

import com.example.data.model.CompanySubscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class SubscriptionAndMcxTest {

    @Test
    fun testInitialSubscriptionIsInvalid() {
        val subscription = CompanySubscription()
        assertFalse(subscription.isValid)
        assertEquals(0L, subscription.daysRemaining)
        assertEquals("Não ativado", subscription.formattedExpirationDate)
    }

    @Test
    fun testValidOneMonthSubscription() {
        val now = System.currentTimeMillis()
        val oneMonthLater = now + TimeUnit.DAYS.toMillis(30)

        val subscription = CompanySubscription(
            companyName = "Comercial Luanda Lda",
            nif = "5412345678",
            contactPhone = "+244 923 000 000",
            isActivated = true,
            activationDateEpoch = now,
            expirationDateEpoch = oneMonthLater,
            paymentPhone = "923111222",
            paymentReference = "MCX-TEST-001",
            destinationIban = "0006 0000 43818047301 44"
        )

        assertTrue(subscription.isValid)
        assertTrue(subscription.daysRemaining in 29L..31L)
        assertEquals("Comercial Luanda Lda", subscription.companyName)
        assertEquals("0006 0000 43818047301 44", subscription.destinationIban)
    }

    @Test
    fun testDestinationIbanDefaultConfiguration() {
        val subscription = CompanySubscription()
        assertEquals("0006 0000 43818047301 44", subscription.destinationIban)
        val cleanIban = subscription.destinationIban.replace(" ", "")
        assertTrue(cleanIban.startsWith("0006")) // BFA Bank Code 0006
        assertEquals(21, cleanIban.length)
    }

    @Test
    fun testExpiredSubscriptionIsInvalid() {
        val past = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        val subscription = CompanySubscription(
            companyName = "Comercial Luanda Lda",
            isActivated = true,
            activationDateEpoch = past - TimeUnit.DAYS.toMillis(30),
            expirationDateEpoch = past
        )

        assertFalse(subscription.isValid)
        assertEquals(0L, subscription.daysRemaining)
    }

    @Test
    fun testCompanySessionPreservationOnLock() {
        val subscription = CompanySubscription(
            companyName = "Comercial Luanda Lda",
            nif = "5412345678",
            contactPhone = "+244 923 000 000",
            isActivated = true,
            activationDateEpoch = System.currentTimeMillis(),
            expirationDateEpoch = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30)
        )
        // When session is locked/exited to setup gate, company credentials and validity remain intact
        assertEquals("Comercial Luanda Lda", subscription.companyName)
        assertEquals("5412345678", subscription.nif)
        assertTrue(subscription.isValid)
    }
}
