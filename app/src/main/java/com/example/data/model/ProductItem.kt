package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val pricePerKg: Double, // Strict requirement: price on weight basis only
    val stockPieces: Int = 0,
    val stockWeightKg: Double = 0.0,
    val unit: String = "kg",
    val category: String = "General",
    val lastUpdated: Long = System.currentTimeMillis()
)
