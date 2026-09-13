package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductItem>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<ProductItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductItem>)

    @Update
    suspend fun updateProduct(product: ProductItem)

    @Delete
    suspend fun deleteProduct(product: ProductItem)

    @Query("UPDATE products SET stockPieces = MAX(0, stockPieces - :deductPieces), stockWeightKg = MAX(0.0, stockWeightKg - :deductWeight), lastUpdated = :timestamp WHERE id = :productId")
    suspend fun deductInventory(productId: Long, deductPieces: Int, deductWeight: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stockPieces = stockPieces + :addPieces, stockWeightKg = stockWeightKg + :addWeight, lastUpdated = :timestamp WHERE id = :productId")
    suspend fun restockInventory(productId: Long, addPieces: Int, addWeight: Double, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int
}
