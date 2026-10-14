package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.receiver.AppointmentNotificationReceiver
import com.example.util.NotificationScheduler

class SnoozeReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SNOOZE_APPOINTMENT = "com.example.fluxao.ACTION_SNOOZE_APPOINTMENT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SNOOZE_APPOINTMENT) return

        val appointmentId = intent.getLongExtra(AppointmentNotificationReceiver.EXTRA_APPOINTMENT_ID, 0L)
        val clientName = intent.getStringExtra(AppointmentNotificationReceiver.EXTRA_CLIENT_NAME) ?: "Cliente"
        val serviceType = intent.getStringExtra(AppointmentNotificationReceiver.EXTRA_SERVICE_TYPE) ?: "Agendamento"
        val time = intent.getStringExtra(AppointmentNotificationReceiver.EXTRA_TIME) ?: ""
        val alertMessage = intent.getStringExtra(AppointmentNotificationReceiver.EXTRA_ALERT_MESSAGE) ?: "Agendamento com $clientName."
        val location = intent.getStringExtra(AppointmentNotificationReceiver.EXTRA_LOCATION) ?: ""

        val scheduler = NotificationScheduler.getInstance(context)
        scheduler.scheduleSnoozeReminder(
            appointmentId = appointmentId,
            clientName = clientName,
            serviceType = serviceType,
            time = time,
            alertMessage = alertMessage,
            location = location,
            snoozeMinutes = 15
        )

        Toast.makeText(context, "Lembrete adiado por 15 minutos.", Toast.LENGTH_SHORT).show()
    }
}
