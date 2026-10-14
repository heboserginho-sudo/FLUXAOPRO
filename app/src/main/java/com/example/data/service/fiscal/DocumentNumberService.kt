package com.example.data.service.fiscal

import com.example.data.model.invoice.InvoiceType
import java.util.Calendar
import java.util.Locale

class DocumentNumberService {

    fun generateDocumentNumber(
        countryCode: String,
        documentType: InvoiceType,
        seriesName: String = "",
        sequenceNumber: Long
    ): String {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val formattedSeq = String.format(Locale.ROOT, "%03d", sequenceNumber)

        return when (countryCode.uppercase()) {
            "AO" -> {
                val series = if (seriesName.isNotBlank()) seriesName else year.toString()
                "${documentType.code} $series/$formattedSeq"
            }
            "PT" -> {
                val series = if (seriesName.isNotBlank()) seriesName else "PT$year"
                "${documentType.code} $series/$formattedSeq"
            }
            "BR" -> {
                val series = if (seriesName.isNotBlank()) seriesName else "001"
                val longSeq = String.format(Locale.ROOT, "%06d", sequenceNumber)
                "NFE $series/$longSeq"
            }
            "FR" -> {
                "FA $year-$formattedSeq"
            }
            "ES" -> {
                "FAC $year/$formattedSeq"
            }
            else -> {
                val prefix = if (seriesName.isNotBlank()) seriesName else "${countryCode.uppercase()}$year"
                "${documentType.code} $prefix-$formattedSeq"
            }
        }
    }
}
