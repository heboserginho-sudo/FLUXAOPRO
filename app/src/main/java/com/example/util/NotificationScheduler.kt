package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AppointmentEntity
import com.example.receiver.AppointmentNotificationReceiver
import com.example.receiver.SnoozeReceiver
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * NotificationScheduler centralizado para criação, cancelamento e atualização de
 * AlarmManager e notificações locais.
 *
 * Garante que o ciclo de vida do app não interfira nos lembretes através de:
 * 1. Uso exclusivo de ApplicationContext (desacoplado de Activities e Views).
 * 2. Suporte a AlarmClockInfo e setExactAndAllowWhileIdle com WAKEUP para ultrapassar Doze Mode.
 * 3. PendingIntents imutáveis e persistentes no sistema operacional Android.
 * 4. Tratamento compatível para Android 12+ (canScheduleExactAlarms) com fallback resiliente.
 * 5. Registro persistente em SharedPreferences de IDs de agendamentos com alarmes ativos.
 */
class NotificationScheduler(context: Context) {

    private val appContext: Context = context.applicationContext
    private val alarmManager: AlarmManager? = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val notificationManager: NotificationManager? = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        ensureNotificationChannels()
    }

    companion object {
        private const val TAG = "NotificationScheduler"
        private const val PREFS_NAME = "fluxao_notification_scheduler_prefs"
        private const val KEY_SCHEDULED_IDS = "key_scheduled_appointment_ids"

        const val CHANNEL_ID_APPOINTMENTS = "fluxao_appointments_channel"
        const val CHANNEL_NAME_APPOINTMENTS = "Agendamentos FluxAO"

        const val CHANNEL_ID_ALERTS = "fluxao_general_alerts_channel"
        const val CHANNEL_NAME_ALERTS = "Alertas Financeiros & Operacionais"

        @Volatile
        private var INSTANCE: NotificationScheduler? = null

        /**
         * Retorna a instância singleton centralizada do NotificationScheduler.
         */
        fun getInstance(context: Context): NotificationScheduler {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationScheduler(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Calcula os milissegundos exatos combinando data epoch e horário "HH:mm" (Fuso de Luanda/Angola).
         */
        fun calculateAppointmentExactTimeMillis(dateEpochMillis: Long, timeStr: String): Long {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Africa/Luanda"), Locale.getDefault()).apply {
                timeInMillis = dateEpochMillis
            }
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                val hour = parts[0].trim().toIntOrNull() ?: 10
                val minute = parts[1].trim().toIntOrNull() ?: 0
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }
    }

    data class NotificationScheduleRequest(
        val requestCode: Int,
        val triggerTimeMillis: Long,
        val title: String,
        val message: String,
        val category: String = "REMINDER",
        val payloadExtras: Map<String, String> = emptyMap(),
        val useAlarmClock: Boolean = false
    )

    data class AlertTrigger(
        val typeIndex: Int,
        val offsetMs: Long,
        val title: String,
        val messageFormat: (service: String, client: String, time: String) -> String
    )

    private val STANDARD_APPOINTMENT_TRIGGERS = listOf(
        AlertTrigger(
            typeIndex = 0,
            offsetMs = 7L * 24 * 60 * 60 * 1000L,
            title = "Agendamento Próximo (7 dias)",
            messageFormat = { service, client, time -> "Você tem um agendamento em 7 dias: $service com $client." }
        ),
        AlertTrigger(
            typeIndex = 1,
            offsetMs = 3L * 24 * 60 * 60 * 1000L,
            title = "Agendamento se Aproximando (3 dias)",
            messageFormat = { service, client, time -> "Seu agendamento de $service com $client é em 3 dias." }
        ),
        AlertTrigger(
            typeIndex = 2,
            offsetMs = 24L * 60 * 60 * 1000L,
            title = "Agendamento Amanhã",
            messageFormat = { service, client, time -> "Seu agendamento de $service com $client é amanhã às $time." }
        ),
        AlertTrigger(
            typeIndex = 3,
            offsetMs = 2L * 60 * 60 * 1000L,
            title = "Lembrete de Agendamento (2h)",
            messageFormat = { service, client, time -> "Seu agendamento de $service com $client acontece em 2 horas às $time." }
        ),
        AlertTrigger(
            typeIndex = 4,
            offsetMs = 1L * 60 * 60 * 1000L,
            title = "Lembrete de Agendamento (1h)",
            messageFormat = { service, client, time -> "Seu agendamento de $service com $client começa em 1 hora às $time." }
        ),
        AlertTrigger(
            typeIndex = 5,
            offsetMs = 0L,
            title = "Agendamento Começa Agora",
            messageFormat = { service, client, time -> "Seu agendamento de $service com $client começa agora às $time!" }
        )
    )

    // =========================================================================
    // 1. CRIAÇÃO DE ALARMES E NOTIFICAÇÕES
    // =========================================================================

    /**
     * Agenda todos os lembretes do ciclo de vida para um agendamento.
     */
    fun scheduleAppointmentReminders(appointment: AppointmentEntity) {
        if (appointment.status == "CANCELADO" || appointment.status == "CONCLUIDO") {
            cancelAppointmentReminders(appointment.id)
            return
        }

        // Cancelar prévios para evitar duplicação
        cancelAppointmentReminders(appointment.id)

        val exactAppointmentTime = calculateAppointmentExactTimeMillis(appointment.dateEpochMillis, appointment.time)
        val now = System.currentTimeMillis()
        val clientName = appointment.clientName.ifBlank { "Cliente" }
        val serviceType = appointment.serviceType.ifBlank { appointment.title.ifBlank { "Serviço" } }

        var scheduledCount = 0

        // Disparos automáticos pré-configurados
        for (trigger in STANDARD_APPOINTMENT_TRIGGERS) {
            val triggerTime = exactAppointmentTime - trigger.offsetMs
            if (triggerTime > now) {
                val message = trigger.messageFormat(serviceType, clientName, appointment.time)
                scheduleExactAlarm(
                    appointmentId = appointment.id,
                    typeIndex = trigger.typeIndex,
                    triggerTimeMillis = triggerTime,
                    title = trigger.title,
                    message = message,
                    serviceType = serviceType,
                    clientName = clientName,
                    time = appointment.time,
                    location = appointment.address
                )
                scheduledCount++
            }
        }

        // Alerta personalizado antecipado caso configurado
        if (appointment.reminderEnabled && appointment.reminderMinutes > 0) {
            val customOffsetMs = appointment.reminderMinutes * 60 * 1000L
            val customTriggerTime = exactAppointmentTime - customOffsetMs
            if (customTriggerTime > now) {
                val customTitle = "Lembrete Personalizado (${appointment.reminderMinutes} min)"
                val customMessage = "Seu agendamento de $serviceType com $clientName é em ${appointment.reminderMinutes} minutos às ${appointment.time}."
                scheduleExactAlarm(
                    appointmentId = appointment.id,
                    typeIndex = 6,
                    triggerTimeMillis = customTriggerTime,
                    title = customTitle,
                    message = customMessage,
                    serviceType = serviceType,
                    clientName = clientName,
                    time = appointment.time,
                    location = appointment.address
                )
                scheduledCount++
            }
        }

        markAppointmentAsScheduled(appointment.id)
        Log.d(TAG, "[$scheduledCount] lembretes agendados com sucesso para o compromisso #${appointment.id}")
    }

    /**
     * Agenda uma notificação pontual genérica com garantia de sobrevivência ao ciclo de vida.
     */
    fun scheduleGenericNotification(request: NotificationScheduleRequest) {
        val mgr = alarmManager ?: return
        if (request.triggerTimeMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Ignorando agendamento no passado: ${request.title}")
            return
        }

        val intent = Intent(appContext, AppointmentNotificationReceiver::class.java).apply {
            action = AppointmentNotificationReceiver.ACTION_TRIGGER_APPOINTMENT_NOTIFICATION
            putExtra(AppointmentNotificationReceiver.EXTRA_APPOINTMENT_ID, request.requestCode.toLong())
            putExtra(AppointmentNotificationReceiver.EXTRA_ALERT_TITLE, request.title)
            putExtra(AppointmentNotificationReceiver.EXTRA_ALERT_MESSAGE, request.message)
            request.payloadExtras.forEach { (k, v) -> putExtra(k, v) }
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(appContext, request.requestCode, intent, flags)

        dispatchAlarm(mgr, request.triggerTimeMillis, pendingIntent, request.useAlarmClock)
    }

    private fun scheduleExactAlarm(
        appointmentId: Long,
        typeIndex: Int,
        triggerTimeMillis: Long,
        title: String,
        message: String,
        serviceType: String,
        clientName: String,
        time: String,
        location: String
    ) {
        val mgr = alarmManager ?: return
        val requestCode = generateRequestCode(appointmentId, typeIndex)

        val intent = Intent(appContext, AppointmentNotificationReceiver::class.java).apply {
            action = AppointmentNotificationReceiver.ACTION_TRIGGER_APPOINTMENT_NOTIFICATION
            putExtra(AppointmentNotificationReceiver.EXTRA_APPOINTMENT_ID, appointmentId)
            putExtra(AppointmentNotificationReceiver.EXTRA_CLIENT_NAME, clientName)
            putExtra(AppointmentNotificationReceiver.EXTRA_SERVICE_TYPE, serviceType)
            putExtra(AppointmentNotificationReceiver.EXTRA_TIME, time)
            putExtra(AppointmentNotificationReceiver.EXTRA_ALERT_TITLE, title)
            putExtra(AppointmentNotificationReceiver.EXTRA_ALERT_MESSAGE, message)
            putExtra(AppointmentNotificationReceiver.EXTRA_LOCATION, location)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(appContext, requestCode, intent, flags)

        // Usar setAlarmClock para compromissos com horário exato (tipo 5), e setExactAndAllowWhileIdle para os demais
        val isExactTimeAlarm = (typeIndex == 5)
        dispatchAlarm(mgr, triggerTimeMillis, pendingIntent, useAlarmClock = isExactTimeAlarm)
    }

    /**
     * Agenda o adiamento (snooze) de uma notificação com segurança e precisão.
     */
    fun scheduleSnoozeReminder(
        appointmentId: Long,
        clientName: String,
        serviceType: String,
        time: String,
        alertMessage: String,
        location: String,
        snoozeMinutes: Int = 15
    ) {
        val snoozeTimeMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val title = "🔔 Lembrete Adiado (${snoozeMinutes} min)"
        scheduleExactAlarm(
            appointmentId = appointmentId,
            typeIndex = 99,
            triggerTimeMillis = snoozeTimeMillis,
            title = title,
            message = alertMessage,
            serviceType = serviceType,
            clientName = clientName,
            time = time,
            location = location
        )
    }

    private fun dispatchAlarm(
        mgr: AlarmManager,
        triggerTimeMillis: Long,
        pendingIntent: PendingIntent,
        useAlarmClock: Boolean
    ) {
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mgr.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (useAlarmClock && canScheduleExact) {
                val showIntent = Intent(appContext, MainActivity::class.java).apply {
                    action = "OPEN_APPOINTMENT"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val showPendingIntent = PendingIntent.getActivity(
                    appContext,
                    0,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, showPendingIntent)
                mgr.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (canScheduleExact) {
                mgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            } else {
                mgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Permissão de alarme exato não concedida, utilizando fallback resiliente: ${e.message}")
            try {
                mgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            } catch (ex: Exception) {
                mgr.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao definir alarme no AlarmManager: ${e.message}", e)
            try {
                mgr.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            } catch (ignored: Exception) {}
        }
    }

    // =========================================================================
    // 2. ATUALIZAÇÃO DE ALARMES E NOTIFICAÇÕES
    // =========================================================================

    /**
     * Atualiza os lembretes de um agendamento após edição ou reagendamento.
     */
    fun updateAppointmentReminders(appointment: AppointmentEntity) {
        scheduleAppointmentReminders(appointment)
    }

    /**
     * Reagenda em lote todos os agendamentos futuros (utilizado no BootReceiver ou restaurações).
     */
    fun rescheduleAllUpcoming(appointments: List<AppointmentEntity>) {
        var count = 0
        for (appointment in appointments) {
            if (appointment.status != "CANCELADO" && appointment.status != "CONCLUIDO") {
                scheduleAppointmentReminders(appointment)
                count++
            }
        }
        Log.i(TAG, "Reagendados com sucesso $count agendamentos futuros.")
    }

    /**
     * Carrega diretamente do Room Database e reagenda todos os agendamentos pendentes ou confirmados.
     * Usado principalmente após reinicialização do dispositivo (BootReceiver).
     */
    suspend fun reschedulePendingAppointmentsFromDatabase(database: AppDatabase) {
        val minDate = System.currentTimeMillis() - (86400000L) // Inclui o dia de hoje
        val upcomingAppointments = database.appointmentDao().getUpcomingAppointments(minDate)
        Log.d(TAG, "Reagendando ${upcomingAppointments.size} compromissos pendentes recuperados do banco de dados.")
        rescheduleAllUpcoming(upcomingAppointments)
    }

    // =========================================================================
    // 3. CANCELAMENTO DE ALARMES E NOTIFICAÇÕES
    // =========================================================================

    /**
     * Cancela todos os alarmes programados associados a um agendamento específico.
     */
    fun cancelAppointmentReminders(appointmentId: Long) {
        val mgr = alarmManager ?: return

        for (typeIndex in 0..10) {
            val requestCode = generateRequestCode(appointmentId, typeIndex)
            val intent = Intent(appContext, AppointmentNotificationReceiver::class.java).apply {
                action = AppointmentNotificationReceiver.ACTION_TRIGGER_APPOINTMENT_NOTIFICATION
            }
            val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getBroadcast(appContext, requestCode, intent, flags)
            if (pendingIntent != null) {
                mgr.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }

        unmarkAppointmentAsScheduled(appointmentId)
        Log.d(TAG, "Alarmes cancelados para o compromisso #$appointmentId")
    }

    /**
     * Cancela um requestCode genérico.
     */
    fun cancelGenericNotification(requestCode: Int) {
        val mgr = alarmManager ?: return
        val intent = Intent(appContext, AppointmentNotificationReceiver::class.java).apply {
            action = AppointmentNotificationReceiver.ACTION_TRIGGER_APPOINTMENT_NOTIFICATION
        }
        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(appContext, requestCode, intent, flags)
        if (pendingIntent != null) {
            mgr.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Descarta uma notificação que já esteja visível na bandeja do sistema.
     */
    fun dismissActiveNotification(notificationId: Int) {
        notificationManager?.cancel(notificationId)
    }

    // =========================================================================
    // 4. CANAIS DE NOTIFICAÇÃO E PERSISTÊNCIA AUXILIAR
    // =========================================================================

    private fun ensureNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val appointmentChannel = NotificationChannel(
                CHANNEL_ID_APPOINTMENTS,
                CHANNEL_NAME_APPOINTMENTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lembretes e alertas nativos de agendamentos e reuniões com clientes"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(soundUri, audioAttributes)
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                CHANNEL_NAME_ALERTS,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alertas de caixa, metas, faturas e contas a vencer"
                enableVibration(true)
            }

            notificationManager?.createNotificationChannel(appointmentChannel)
            notificationManager?.createNotificationChannel(alertsChannel)
        }
    }

    private fun generateRequestCode(appointmentId: Long, typeIndex: Int): Int {
        return (appointmentId * 100 + typeIndex).toInt()
    }

    private fun markAppointmentAsScheduled(appointmentId: Long) {
        val currentSet = prefs.getStringSet(KEY_SCHEDULED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(appointmentId.toString())
        prefs.edit().putStringSet(KEY_SCHEDULED_IDS, currentSet).apply()
    }

    private fun unmarkAppointmentAsScheduled(appointmentId: Long) {
        val currentSet = prefs.getStringSet(KEY_SCHEDULED_IDS, emptySet())?.toMutableSet() ?: return
        currentSet.remove(appointmentId.toString())
        prefs.edit().putStringSet(KEY_SCHEDULED_IDS, currentSet).apply()
    }

    fun getTrackedScheduledAppointmentIds(): Set<Long> {
        val set = prefs.getStringSet(KEY_SCHEDULED_IDS, emptySet()) ?: emptySet()
        return set.mapNotNull { it.toLongOrNull() }.toSet()
    }
}
