package com.example.util

import android.content.Context
import com.example.data.local.entity.AppointmentEntity

/**
 * Ponto de compatibilidade que delega diretamente para o [NotificationScheduler] centralizado.
 */
object AppointmentScheduler {

    fun calculateAppointmentExactTimeMillis(dateEpochMillis: Long, timeStr: String): Long {
        return NotificationScheduler.calculateAppointmentExactTimeMillis(dateEpochMillis, timeStr)
    }

    fun scheduleAppointmentAlarms(context: Context, appointment: AppointmentEntity) {
        NotificationScheduler.getInstance(context).scheduleAppointmentReminders(appointment)
    }

    fun cancelAppointmentAlarms(context: Context, appointmentId: Long) {
        NotificationScheduler.getInstance(context).cancelAppointmentReminders(appointmentId)
    }

    fun rescheduleAllUpcomingAlarms(context: Context, appointments: List<AppointmentEntity>) {
        NotificationScheduler.getInstance(context).rescheduleAllUpcoming(appointments)
    }
}
