package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_batches")
data class DailyBatchEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val name: String,
    val pieces: Int,
    val weightKg: Double,
    val notes: String = "",
    val isSynced: Boolean = false,
    val cloudSyncTimestamp: Long? = null
)
