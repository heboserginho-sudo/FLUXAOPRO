package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BlockedSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedSlotDao {
    @Query("SELECT * FROM blocked_slots ORDER BY dateEpochMillis ASC")
    fun getAllBlockedSlots(): Flow<List<BlockedSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedSlot(slot: BlockedSlotEntity): Long

    @Delete
    suspend fun deleteBlockedSlot(slot: BlockedSlotEntity)
}
