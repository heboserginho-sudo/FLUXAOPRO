package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Query("SELECT * FROM appointments ORDER BY dateEpochMillis ASC, time ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE clientId = :clientId ORDER BY dateEpochMillis DESC, time DESC")
    fun getAppointmentsForClient(clientId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE status IN ('SOLICITADO', 'REAGENDAMENTO_SOLICITADO') ORDER BY dateEpochMillis ASC, time ASC")
    fun getPendingAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentById(id: Long): AppointmentEntity?

    @Query("SELECT * FROM appointments WHERE dateEpochMillis = :dateEpochMillis AND time = :time AND status NOT IN ('CANCELADO') AND id != :excludeId LIMIT 1")
    suspend fun findConflictingAppointment(dateEpochMillis: Long, time: String, excludeId: Long = 0L): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    @Query("SELECT * FROM appointments WHERE status NOT IN ('CANCELADO', 'CONCLUIDO') AND dateEpochMillis >= :minDateMillis")
    suspend fun getUpcomingAppointments(minDateMillis: Long): List<AppointmentEntity>

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointmentById(id: Long)
}
