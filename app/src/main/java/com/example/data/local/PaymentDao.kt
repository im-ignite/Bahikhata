package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PaymentTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentTransaction): Long

    @Query("SELECT * FROM payment_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM payment_transactions")
    suspend fun getAllPaymentsList(): List<PaymentTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<PaymentTransaction>)
}
