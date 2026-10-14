package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.invoice.InvoiceStatus
import com.example.data.model.invoice.InvoiceType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String, // ex: FT 2026/001
    val invoiceType: InvoiceType = InvoiceType.FT,
    val status: InvoiceStatus = InvoiceStatus.PENDING,
    val dateCreatedEpoch: Long = System.currentTimeMillis(),
    val dueDateEpoch: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    // Dados do Cliente (Adquirente em Angola)
    val clientName: String,
    val clientNif: String = "Consumidor Final",
    val clientAddress: String = "Luanda, Angola",
    val clientCity: String = "Luanda",
    val clientPhone: String = "",
    val clientEmail: String = "",
    // Dados da Empresa Emissora
    val emitterName: String,
    val emitterNif: String,
    val emitterAddress: String = "Luanda, Angola",
    val emitterCity: String = "Luanda",
    val emitterIban: String = "",
    val emitterBank: String = "BAI / BFA",
    // Regime Fiscal e Pagamento
    val vatRegime: String = "Regime Geral (IVA 14%)",
    val paymentMethod: String = "Transferência Bancária / IBAN",
    val notes: String = "Os bens ou serviços foram colocados à disposição do adquirente na data do documento.",
    // Validação Fiscal AGT (Hash de assinatura digital 4 caracteres obrigatório em Angola)
    val fiscalValidationHash: String = "",
    // Totais calculados
    val totalGross: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val totalNet: Double = 0.0,
    val totalVat: Double = 0.0,
    val totalToPay: Double = 0.0,
    val companyId: String = "default_company",
    // Internacionalização & Jurisdição Fiscal Multi-País
    val countryCode: String = "AO",
    val currencyCode: String = "AOA",
    val taxIdType: String = "NIF",
    val fiscalProviderId: String = "AngolaAGTProvider",
    val fiscalStatus: String = "VALIDATED",
    val fiscalReference: String = "",
    val qrCodeData: String = "",
    val emissionMode: String = "FISCAL_INTEGRATED",
    val exchangeRate: Double = 1.0,
    val baseCurrency: String = "AOA"
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "AO"))
            return sdf.format(Date(dateCreatedEpoch))
        }

    val formattedDueDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "AO"))
            return sdf.format(Date(dueDateEpoch))
        }
}
