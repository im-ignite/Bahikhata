package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Customer
import com.example.data.model.DailyBatchEntry
import com.example.data.model.ProductItem
import com.example.data.model.SaleTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        DailyBatchEntry::class,
        ProductItem::class,
        Customer::class,
        SaleTransaction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyBatchDao(): DailyBatchDao
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trade_sync_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val productDao = database.productDao()
            val customerDao = database.customerDao()
            val batchDao = database.dailyBatchDao()
            val saleDao = database.saleDao()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())

            val sampleProducts = listOf(
                ProductItem(
                    name = "Copper Wire Gauge 8",
                    pricePerKg = 12.50,
                    stockPieces = 450,
                    stockWeightKg = 850.0,
                    unit = "kg",
                    category = "Metals"
                ),
                ProductItem(
                    name = "Industrial Steel Rods 12mm",
                    pricePerKg = 4.20,
                    stockPieces = 300,
                    stockWeightKg = 1200.0,
                    unit = "kg",
                    category = "Construction"
                ),
                ProductItem(
                    name = "Alloy Brass Ingots",
                    pricePerKg = 9.80,
                    stockPieces = 180,
                    stockWeightKg = 540.0,
                    unit = "kg",
                    category = "Foundry"
                ),
                ProductItem(
                    name = "Aluminum Sheet 3mm",
                    pricePerKg = 6.40,
                    stockPieces = 220,
                    stockWeightKg = 660.0,
                    unit = "kg",
                    category = "Fabrication"
                ),
                ProductItem(
                    name = "High-Tensile Fasteners",
                    pricePerKg = 15.00,
                    stockPieces = 800,
                    stockWeightKg = 240.0,
                    unit = "kg",
                    category = "Hardware"
                )
            )
            productDao.insertAll(sampleProducts)

            val sampleCustomers = listOf(
                Customer(
                    name = "Apex Fabrication Ltd",
                    phoneNumber = "+1 (555) 234-8901",
                    address = "450 Industrial Parkway, Suite 12, Chicago IL",
                    notes = "VIP buyer. Preferred delivery Mondays."
                ),
                Customer(
                    name = "Metro Builders & Co",
                    phoneNumber = "+1 (555) 876-5432",
                    address = "89 Construction Ave, Dallas TX",
                    notes = "Regular orders of steel rods and aluminum."
                ),
                Customer(
                    name = "Keystone Electricals",
                    phoneNumber = "+1 (555) 345-6789",
                    address = "120 Spark Boulevard, San Jose CA",
                    notes = "High-volume buyer for copper wire."
                ),
                Customer(
                    name = "Summit Metal Works",
                    phoneNumber = "+1 (555) 901-2345",
                    address = "77 Forge Street, Pittsburgh PA",
                    notes = "Specializes in brass castings."
                )
            )
            customerDao.insertAll(sampleCustomers)

            val sampleBatches = listOf(
                DailyBatchEntry(
                    dateString = today,
                    name = "Morning Warehouse Intake - Copper",
                    pieces = 120,
                    weightKg = 340.5,
                    notes = "Inspected and sorted into Bay 3",
                    isSynced = true,
                    cloudSyncTimestamp = System.currentTimeMillis() - 3600000
                ),
                DailyBatchEntry(
                    dateString = today,
                    name = "Steel Rod Mill Delivery",
                    pieces = 85,
                    weightKg = 410.0,
                    notes = "Batch grade A certificate verified",
                    isSynced = true,
                    cloudSyncTimestamp = System.currentTimeMillis() - 1800000
                ),
                DailyBatchEntry(
                    dateString = today,
                    name = "Brass Ingot Shipment #4",
                    pieces = 60,
                    weightKg = 180.25,
                    notes = "Foundry intake",
                    isSynced = false
                )
            )
            batchDao.insertAll(sampleBatches)

            val sampleSales = listOf(
                SaleTransaction(
                    customerId = 1,
                    customerName = "Apex Fabrication Ltd",
                    productId = 1,
                    itemName = "Copper Wire Gauge 8",
                    pieces = 30,
                    weightKg = 90.0,
                    pricePerKg = 12.50,
                    totalPrice = 90.0 * 12.50,
                    dateString = today,
                    isSynced = true
                ),
                SaleTransaction(
                    customerId = 2,
                    customerName = "Metro Builders & Co",
                    productId = 2,
                    itemName = "Industrial Steel Rods 12mm",
                    pieces = 50,
                    weightKg = 200.0,
                    pricePerKg = 4.20,
                    totalPrice = 200.0 * 4.20,
                    dateString = today,
                    isSynced = true
                )
            )
            saleDao.insertAll(sampleSales)
        }
    }
}
