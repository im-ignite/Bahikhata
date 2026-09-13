package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyBatchEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyBatchDao {
    @Query("SELECT * FROM daily_batches ORDER BY timestamp DESC")
    fun getAllBatches(): Flow<List<DailyBatchEntry>>

    @Query("SELECT * FROM daily_batches WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getBatchesForDate(dateString: String): Flow<List<DailyBatchEntry>>

    @Query("SELECT * FROM daily_batches WHERE dateString >= :startDate AND dateString <= :endDate ORDER BY dateString ASC, timestamp ASC")
    fun getBatchesBetween(startDate: String, endDate: String): Flow<List<DailyBatchEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(entry: DailyBatchEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DailyBatchEntry>)

    @Update
    suspend fun updateBatch(entry: DailyBatchEntry)

    @Delete
    suspend fun deleteBatch(entry: DailyBatchEntry)

    @Query("UPDATE daily_batches SET isSynced = 1, cloudSyncTimestamp = :syncTime")
    suspend fun markAllAsSynced(syncTime: Long)

    @Query("SELECT COUNT(*) FROM daily_batches")
    suspend fun getCount(): Int
}
