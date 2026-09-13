package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SaleTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleTransaction>>

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    suspend fun getAllSalesList(): List<SaleTransaction>

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getSalesForCustomer(customerId: Long): Flow<List<SaleTransaction>>

    @Query("SELECT * FROM sales WHERE dateString >= :startDate AND dateString <= :endDate ORDER BY dateString ASC, timestamp ASC")
    fun getSalesBetween(startDate: String, endDate: String): Flow<List<SaleTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sales: List<SaleTransaction>)

    @Update
    suspend fun updateSale(sale: SaleTransaction)

    @Delete
    suspend fun deleteSale(sale: SaleTransaction)

    @Query("UPDATE sales SET isSynced = 1")
    suspend fun markAllAsSynced()

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getCount(): Int

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()
}
