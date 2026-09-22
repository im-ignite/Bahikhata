package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long? = null,
    val customerName: String,
    val productId: Long? = null,
    val itemName: String,
    val pieces: Int,
    val weightKg: Double,
    val pricePerKg: Double, // Price basis
    val totalPrice: Double, // Calculated: weightKg * pricePerKg
    val dateString: String, // YYYY-MM-DD
    val amountPaid: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
