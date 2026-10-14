package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entity.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppointmentNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "AppointmentNotification"
        const val CHANNEL_ID = "fluxao_appointments_channel"
        const val CHANNEL_NAME = "Agendamentos FluxAO"

        const val ACTION_TRIGGER_APPOINTMENT_NOTIFICATION = "com.example.fluxao.ACTION_TRIGGER_APPOINTMENT_NOTIFICATION"
        const val EXTRA_APPOINTMENT_ID = "extra_appointment_id"
        const val EXTRA_CLIENT_NAME = "extra_client_name"
        const val EXTRA_SERVICE_TYPE = "extra_service_type"
        const val EXTRA_TIME = "extra_time"
        const val EXTRA_ALERT_TITLE = "extra_alert_title"
        const val EXTRA_ALERT_MESSAGE = "extra_alert_message"
        const val EXTRA_LOCATION = "extra_location"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRIGGER_APPOINTMENT_NOTIFICATION) return

        val appointmentId = intent.getLongExtra(EXTRA_APPOINTMENT_ID, 0L)
        val clientName = intent.getStringExtra(EXTRA_CLIENT_NAME) ?: "Cliente"
        val serviceType = intent.getStringExtra(EXTRA_SERVICE_TYPE) ?: "Agendamento"
        val time = intent.getStringExtra(EXTRA_TIME) ?: ""
        val alertTitle = intent.getStringExtra(EXTRA_ALERT_TITLE) ?: "Lembrete de Agendamento"
        val alertMessage = intent.getStringExtra(EXTRA_ALERT_MESSAGE) ?: "Você tem um agendamento com $clientName."
        val location = intent.getStringExtra(EXTRA_LOCATION) ?: ""

        Log.d(TAG, "Exibindo notificação nativa para o agendamento #$appointmentId: $alertTitle")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Create Channel on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações e alertas nativos para agendamentos do FluxAO"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open App Intent when notification clicked
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = "OPEN_APPOINTMENT"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_APPOINTMENT_ID, appointmentId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            (appointmentId * 10 + 1).toInt(),
            openAppIntent,
            flags
        )

        // Action 1: Deep link to View Appointment
        val viewAction = NotificationCompat.Action.Builder(
            0,
            "Ver Agendamento",
            contentPendingIntent
        ).build()

        // Action 2: Snooze 15 Minutes
        val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
            action = SnoozeReceiver.ACTION_SNOOZE_APPOINTMENT
            putExtra(EXTRA_APPOINTMENT_ID, appointmentId)
            putExtra(EXTRA_CLIENT_NAME, clientName)
            putExtra(EXTRA_SERVICE_TYPE, serviceType)
            putExtra(EXTRA_TIME, time)
            putExtra(EXTRA_ALERT_TITLE, "Agendamento Adiado (15 min)")
            putExtra(EXTRA_ALERT_MESSAGE, alertMessage)
            putExtra(EXTRA_LOCATION, location)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (appointmentId * 10 + 2).toInt(),
            snoozeIntent,
            flags
        )
        val snoozeAction = NotificationCompat.Action.Builder(
            0,
            "Adiar 15 min",
            snoozePendingIntent
        ).build()

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val fullMessage = if (location.isNotBlank()) "$alertMessage\n📍 Local: $location" else alertMessage

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(alertTitle)
            .setContentText(alertMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setContentIntent(contentPendingIntent)
            .addAction(viewAction)
            .addAction(snoozeAction)

        val notificationId = (System.currentTimeMillis() % 1000000).toInt()
        notificationManager.notify(notificationId, builder.build())

        // Save in Room Notifications DB
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context, this)
                val notificationEntity = NotificationEntity(
                    title = alertTitle,
                    message = fullMessage,
                    type = "APPOINTMENT_REMINDER",
                    timestampMillis = System.currentTimeMillis(),
                    isRead = false,
                    relatedEntityId = appointmentId,
                    actionRoute = "AGENDA"
                )
                db.notificationDao().insertNotification(notificationEntity)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar notificação no BD: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
