package com.example.data.repository

import com.example.data.local.dao.AppointmentDao
import com.example.data.local.dao.BlockedSlotDao
import com.example.data.local.entity.AppointmentEntity
import com.example.data.local.entity.BlockedSlotEntity
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(
    private val appointmentDao: AppointmentDao,
    private val blockedSlotDao: BlockedSlotDao? = null
) {

    val allAppointments: Flow<List<AppointmentEntity>> = appointmentDao.getAllAppointments()
    val pendingAppointments: Flow<List<AppointmentEntity>> = appointmentDao.getPendingAppointments()

    fun getAppointmentsForClient(clientId: Long): Flow<List<AppointmentEntity>> =
        appointmentDao.getAppointmentsForClient(clientId)

    suspend fun getAppointmentById(id: Long): AppointmentEntity? =
        appointmentDao.getAppointmentById(id)

    suspend fun getUpcomingAppointments(minDateMillis: Long = System.currentTimeMillis() - 86400000L): List<AppointmentEntity> =
        appointmentDao.getUpcomingAppointments(minDateMillis)

    suspend fun findConflictingAppointment(dateEpochMillis: Long, time: String, excludeId: Long = 0L): AppointmentEntity? =
        appointmentDao.findConflictingAppointment(dateEpochMillis, time, excludeId)

    suspend fun saveAppointment(appointment: AppointmentEntity): Long =
        appointmentDao.insertAppointment(appointment)

    suspend fun updateAppointment(appointment: AppointmentEntity) =
        appointmentDao.updateAppointment(appointment)

    suspend fun deleteAppointment(appointment: AppointmentEntity) =
        appointmentDao.deleteAppointment(appointment)

    suspend fun deleteAppointmentById(id: Long) =
        appointmentDao.deleteAppointmentById(id)

    fun getAllBlockedSlots(): Flow<List<BlockedSlotEntity>>? =
        blockedSlotDao?.getAllBlockedSlots()

    suspend fun saveBlockedSlot(slot: BlockedSlotEntity): Long? =
        blockedSlotDao?.insertBlockedSlot(slot)

    suspend fun deleteBlockedSlot(slot: BlockedSlotEntity) {
        blockedSlotDao?.deleteBlockedSlot(slot)
    }
}
