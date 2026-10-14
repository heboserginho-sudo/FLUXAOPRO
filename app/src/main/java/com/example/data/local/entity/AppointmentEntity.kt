package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long = 0,
    val clientName: String = "",
    val clientPhone: String = "",
    val serviceType: String = "", // Tipo de serviço
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val time: String = "10:00", // "HH:mm"
    val durationMinutes: Int = 60,
    val address: String = "", // Endereço do agendamento
    val observations: String = "", // Observações
    val attachmentUri: String = "", // Fotos/Anexos
    val status: String = "SOLICITADO", // "SOLICITADO", "CONFIRMADO", "EM_ANDAMENTO", "CONCLUIDO", "REAGENDAMENTO_SOLICITADO", "CANCELADO"
    val requestedRescheduleDateMillis: Long? = null,
    val requestedRescheduleTime: String? = null,
    val cancellationReason: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "SERVICE",
    val historyLog: String = "", // Timeline de alterações
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val reminderEnabled: Boolean = true,
    val reminderMinutes: Int = 60,
    val companyId: String = "default_company"
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dateEpochMillis))

    val displayStatusLabel: String
        get() = when (status) {
            "SOLICITADO" -> "Solicitado"
            "CONFIRMADO" -> "Confirmado"
            "EM_ANDAMENTO" -> "Em Andamento"
            "CONCLUIDO" -> "Concluído"
            "REAGENDAMENTO_SOLICITADO" -> "Reagendamento Solicitado"
            "CANCELADO" -> "Cancelado"
            else -> status
        }
}
