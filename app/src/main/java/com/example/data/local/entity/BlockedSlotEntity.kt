package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_slots")
data class BlockedSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochMillis: Long,
    val timeSlot: String, // e.g., "09:00"
    val isFullDay: Boolean = false,
    val reason: String = ""
)
