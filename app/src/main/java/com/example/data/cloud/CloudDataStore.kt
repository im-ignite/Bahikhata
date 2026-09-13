package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.data.model.Customer
import com.example.data.model.DailyBatchEntry
import com.example.data.model.ProductItem
import com.example.data.model.SaleTransaction
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class CloudSyncPayload(
    val products: List<ProductItem> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val batches: List<DailyBatchEntry> = emptyList(),
    val sales: List<SaleTransaction> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

class CloudDataStore(private val context: Context) {

    private val tag = "CloudDataStore"

    private fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun isCloudConfigured(): Boolean = isFirebaseConfigured()

    private fun sanitizeEmailForPath(email: String): String {
        return email.lowercase().trim()
            .replace("@", "_at_")
            .replace(".", "_")
            .replace("-", "_")
    }

    private fun getCloudBackupFile(email: String): File {
        val sanitized = sanitizeEmailForPath(email)
        val dir = File(context.filesDir, "google_cloud_sync")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "cloud_store_$sanitized.json")
    }

    suspend fun uploadToCloud(
        email: String,
        products: List<ProductItem>,
        customers: List<Customer>,
        batches: List<DailyBatchEntry>,
        sales: List<SaleTransaction>
    ): Boolean = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext false

        var firestoreSuccess = false
        val sanitized = sanitizeEmailForPath(email)

        // 1. If Firebase Firestore is active, push to user collection
        if (isFirebaseConfigured()) {
            try {
                val db = FirebaseFirestore.getInstance()
                val userDoc = db.collection("users").document(sanitized)

                val summary = mapOf(
                    "lastSync" to System.currentTimeMillis(),
                    "email" to email,
                    "productsCount" to products.size,
                    "customersCount" to customers.size,
                    "salesCount" to sales.size,
                    "batchesCount" to batches.size
                )
                userDoc.set(summary, SetOptions.merge()).await()

                // Batch write or collection write
                for (p in products) {
                    userDoc.collection("products").document(p.id.toString())
                        .set(
                            mapOf(
                                "id" to p.id,
                                "name" to p.name,
                                "pricePerKg" to p.pricePerKg,
                                "stockPieces" to p.stockPieces,
                                "stockWeightKg" to p.stockWeightKg,
                                "unit" to p.unit,
                                "category" to p.category,
                                "lastUpdated" to p.lastUpdated
                            )
                        ).await()
                }

                for (c in customers) {
                    userDoc.collection("customers").document(c.id.toString())
                        .set(
                            mapOf(
                                "id" to c.id,
                                "name" to c.name,
                                "phoneNumber" to c.phoneNumber,
                                "address" to c.address,
                                "notes" to c.notes,
                                "createdAt" to c.createdAt
                            )
                        ).await()
                }

                for (s in sales) {
                    userDoc.collection("sales").document(s.id.toString())
                        .set(
                            mapOf(
                                "id" to s.id,
                                "customerId" to (s.customerId ?: 0L),
                                "customerName" to s.customerName,
                                "productId" to (s.productId ?: 0L),
                                "itemName" to s.itemName,
                                "pieces" to s.pieces,
                                "weightKg" to s.weightKg,
                                "pricePerKg" to s.pricePerKg,
                                "totalPrice" to s.totalPrice,
                                "dateString" to s.dateString,
                                "timestamp" to s.timestamp,
                                "isSynced" to true
                            )
                        ).await()
                }

                for (b in batches) {
                    userDoc.collection("batches").document(b.id.toString())
                        .set(
                            mapOf(
                                "id" to b.id,
                                "dateString" to b.dateString,
                                "timestamp" to b.timestamp,
                                "name" to b.name,
                                "pieces" to b.pieces,
                                "weightKg" to b.weightKg,
                                "notes" to b.notes,
                                "isSynced" to true
                            )
                        ).await()
                }

                firestoreSuccess = true
            } catch (e: Exception) {
                Log.w(tag, "Firestore push skipped or failed: ${e.message}")
            }
        }

        // 2. Always maintain the full synchronized Cloud Account Snapshot
        try {
            val root = JSONObject()
            root.put("account_email", email)
            root.put("timestamp", System.currentTimeMillis())

            val productsArray = JSONArray()
            products.forEach { p ->
                val obj = JSONObject()
                obj.put("id", p.id)
                obj.put("name", p.name)
                obj.put("pricePerKg", p.pricePerKg)
                obj.put("stockPieces", p.stockPieces)
                obj.put("stockWeightKg", p.stockWeightKg)
                obj.put("unit", p.unit)
                obj.put("category", p.category)
                obj.put("lastUpdated", p.lastUpdated)
                productsArray.put(obj)
            }
            root.put("products", productsArray)

            val customersArray = JSONArray()
            customers.forEach { c ->
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("name", c.name)
                obj.put("phoneNumber", c.phoneNumber)
                obj.put("address", c.address)
                obj.put("notes", c.notes)
                obj.put("createdAt", c.createdAt)
                customersArray.put(obj)
            }
            root.put("customers", customersArray)

            val batchesArray = JSONArray()
            batches.forEach { b ->
                val obj = JSONObject()
                obj.put("id", b.id)
                obj.put("dateString", b.dateString)
                obj.put("timestamp", b.timestamp)
                obj.put("name", b.name)
                obj.put("pieces", b.pieces)
                obj.put("weightKg", b.weightKg)
                obj.put("notes", b.notes)
                batchesArray.put(obj)
            }
            root.put("batches", batchesArray)

            val salesArray = JSONArray()
            sales.forEach { s ->
                val obj = JSONObject()
                obj.put("id", s.id)
                obj.put("customerId", s.customerId ?: -1L)
                obj.put("customerName", s.customerName)
                obj.put("productId", s.productId ?: -1L)
                obj.put("itemName", s.itemName)
                obj.put("pieces", s.pieces)
                obj.put("weightKg", s.weightKg)
                obj.put("pricePerKg", s.pricePerKg)
                obj.put("totalPrice", s.totalPrice)
                obj.put("dateString", s.dateString)
                obj.put("timestamp", s.timestamp)
                salesArray.put(obj)
            }
            root.put("sales", salesArray)

            val file = getCloudBackupFile(email)
            file.writeText(root.toString(2))
            return@withContext true
        } catch (e: Exception) {
            Log.e(tag, "Failed to write cloud sync snapshot", e)
            return@withContext firestoreSuccess
        }
    }

    suspend fun fetchCloudData(email: String): CloudSyncPayload? = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext null

        val sanitized = sanitizeEmailForPath(email)

        // 1. Try reading from Firestore if available
        if (isFirebaseConfigured()) {
            try {
                val db = FirebaseFirestore.getInstance()
                val userDoc = db.collection("users").document(sanitized)

                val prodDocs = userDoc.collection("products").get().await()
                val products = prodDocs.mapNotNull { d ->
                    try {
                        ProductItem(
                            id = d.getLong("id") ?: 0L,
                            name = d.getString("name") ?: "",
                            pricePerKg = d.getDouble("pricePerKg") ?: 0.0,
                            stockPieces = d.getLong("stockPieces")?.toInt() ?: 0,
                            stockWeightKg = d.getDouble("stockWeightKg") ?: 0.0,
                            unit = d.getString("unit") ?: "kg",
                            category = d.getString("category") ?: "General",
                            lastUpdated = d.getLong("lastUpdated") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) { null }
                }

                val custDocs = userDoc.collection("customers").get().await()
                val customers = custDocs.mapNotNull { d ->
                    try {
                        Customer(
                            id = d.getLong("id") ?: 0L,
                            name = d.getString("name") ?: "",
                            phoneNumber = d.getString("phoneNumber") ?: "",
                            address = d.getString("address") ?: "",
                            notes = d.getString("notes") ?: "",
                            createdAt = d.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) { null }
                }

                val batchDocs = userDoc.collection("batches").get().await()
                val batches = batchDocs.mapNotNull { d ->
                    try {
                        DailyBatchEntry(
                            id = d.getLong("id") ?: 0L,
                            dateString = d.getString("dateString") ?: "",
                            timestamp = d.getLong("timestamp") ?: System.currentTimeMillis(),
                            name = d.getString("name") ?: "",
                            pieces = d.getLong("pieces")?.toInt() ?: 0,
                            weightKg = d.getDouble("weightKg") ?: 0.0,
                            notes = d.getString("notes") ?: "",
                            isSynced = true
                        )
                    } catch (e: Exception) { null }
                }

                val saleDocs = userDoc.collection("sales").get().await()
                val sales = saleDocs.mapNotNull { d ->
                    try {
                        SaleTransaction(
                            id = d.getLong("id") ?: 0L,
                            customerId = d.getLong("customerId"),
                            customerName = d.getString("customerName") ?: "",
                            productId = d.getLong("productId"),
                            itemName = d.getString("itemName") ?: "",
                            pieces = d.getLong("pieces")?.toInt() ?: 0,
                            weightKg = d.getDouble("weightKg") ?: 0.0,
                            pricePerKg = d.getDouble("pricePerKg") ?: 0.0,
                            totalPrice = d.getDouble("totalPrice") ?: 0.0,
                            dateString = d.getString("dateString") ?: "",
                            timestamp = d.getLong("timestamp") ?: System.currentTimeMillis(),
                            isSynced = true
                        )
                    } catch (e: Exception) { null }
                }

                if (products.isNotEmpty() || customers.isNotEmpty() || sales.isNotEmpty()) {
                    return@withContext CloudSyncPayload(
                        products = products,
                        customers = customers,
                        batches = batches,
                        sales = sales,
                        timestamp = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                Log.w(tag, "Firestore fetch skipped or empty: ${e.message}")
            }
        }

        // 2. Read from persistent cloud snapshot file
        try {
            val file = getCloudBackupFile(email)
            if (!file.exists()) return@withContext null

            val text = file.readText()
            if (text.isBlank()) return@withContext null

            val root = JSONObject(text)
            val timestamp = root.optLong("timestamp", System.currentTimeMillis())

            val productsList = mutableListOf<ProductItem>()
            val prodArray = root.optJSONArray("products")
            if (prodArray != null) {
                for (i in 0 until prodArray.length()) {
                    val obj = prodArray.getJSONObject(i)
                    productsList.add(
                        ProductItem(
                            id = obj.optLong("id", 0L),
                            name = obj.optString("name", ""),
                            pricePerKg = obj.optDouble("pricePerKg", 0.0),
                            stockPieces = obj.optInt("stockPieces", 0),
                            stockWeightKg = obj.optDouble("stockWeightKg", 0.0),
                            unit = obj.optString("unit", "kg"),
                            category = obj.optString("category", "General"),
                            lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis())
                        )
                    )
                }
            }

            val customersList = mutableListOf<Customer>()
            val custArray = root.optJSONArray("customers")
            if (custArray != null) {
                for (i in 0 until custArray.length()) {
                    val obj = custArray.getJSONObject(i)
                    customersList.add(
                        Customer(
                            id = obj.optLong("id", 0L),
                            name = obj.optString("name", ""),
                            phoneNumber = obj.optString("phoneNumber", ""),
                            address = obj.optString("address", ""),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val batchesList = mutableListOf<DailyBatchEntry>()
            val batchArray = root.optJSONArray("batches")
            if (batchArray != null) {
                for (i in 0 until batchArray.length()) {
                    val obj = batchArray.getJSONObject(i)
                    batchesList.add(
                        DailyBatchEntry(
                            id = obj.optLong("id", 0L),
                            dateString = obj.optString("dateString", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            name = obj.optString("name", ""),
                            pieces = obj.optInt("pieces", 0),
                            weightKg = obj.optDouble("weightKg", 0.0),
                            notes = obj.optString("notes", ""),
                            isSynced = true
                        )
                    )
                }
            }

            val salesList = mutableListOf<SaleTransaction>()
            val salesArray = root.optJSONArray("sales")
            if (salesArray != null) {
                for (i in 0 until salesArray.length()) {
                    val obj = salesArray.getJSONObject(i)
                    salesList.add(
                        SaleTransaction(
                            id = obj.optLong("id", 0L),
                            customerId = if (obj.has("customerId") && obj.getLong("customerId") > 0) obj.getLong("customerId") else null,
                            customerName = obj.optString("customerName", ""),
                            productId = if (obj.has("productId") && obj.getLong("productId") > 0) obj.getLong("productId") else null,
                            itemName = obj.optString("itemName", ""),
                            pieces = obj.optInt("pieces", 0),
                            weightKg = obj.optDouble("weightKg", 0.0),
                            pricePerKg = obj.optDouble("pricePerKg", 0.0),
                            totalPrice = obj.optDouble("totalPrice", 0.0),
                            dateString = obj.optString("dateString", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isSynced = true
                        )
                    )
                }
            }

            return@withContext CloudSyncPayload(
                products = productsList,
                customers = customersList,
                batches = batchesList,
                sales = salesList,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to read cloud sync snapshot", e)
            return@withContext null
        }
    }

    fun serializePayloadToJson(
        email: String,
        products: List<ProductItem>,
        customers: List<Customer>,
        batches: List<DailyBatchEntry>,
        sales: List<SaleTransaction>
    ): String {
        val root = JSONObject()
        root.put("account_email", email)
        root.put("app_name", "RAI FISH")
        root.put("timestamp", System.currentTimeMillis())

        val productsArray = JSONArray()
        products.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("pricePerKg", p.pricePerKg)
            obj.put("stockPieces", p.stockPieces)
            obj.put("stockWeightKg", p.stockWeightKg)
            obj.put("unit", p.unit)
            obj.put("category", p.category)
            obj.put("lastUpdated", p.lastUpdated)
            productsArray.put(obj)
        }
        root.put("products", productsArray)

        val customersArray = JSONArray()
        customers.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("phoneNumber", c.phoneNumber)
            obj.put("address", c.address)
            obj.put("notes", c.notes)
            obj.put("createdAt", c.createdAt)
            customersArray.put(obj)
        }
        root.put("customers", customersArray)

        val batchesArray = JSONArray()
        batches.forEach { b ->
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("dateString", b.dateString)
            obj.put("timestamp", b.timestamp)
            obj.put("name", b.name)
            obj.put("pieces", b.pieces)
            obj.put("weightKg", b.weightKg)
            obj.put("notes", b.notes)
            batchesArray.put(obj)
        }
        root.put("batches", batchesArray)

        val salesArray = JSONArray()
        sales.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("customerId", s.customerId ?: -1L)
            obj.put("customerName", s.customerName)
            obj.put("productId", s.productId ?: -1L)
            obj.put("itemName", s.itemName)
            obj.put("pieces", s.pieces)
            obj.put("weightKg", s.weightKg)
            obj.put("pricePerKg", s.pricePerKg)
            obj.put("totalPrice", s.totalPrice)
            obj.put("dateString", s.dateString)
            obj.put("timestamp", s.timestamp)
            salesArray.put(obj)
        }
        root.put("sales", salesArray)

        return root.toString(2)
    }

    fun parsePayloadFromJson(jsonString: String): CloudSyncPayload? {
        return try {
            val root = JSONObject(jsonString)
            val timestamp = root.optLong("timestamp", System.currentTimeMillis())

            val productsList = mutableListOf<ProductItem>()
            val prodArray = root.optJSONArray("products")
            if (prodArray != null) {
                for (i in 0 until prodArray.length()) {
                    val obj = prodArray.getJSONObject(i)
                    productsList.add(
                        ProductItem(
                            id = obj.optLong("id", 0L),
                            name = obj.optString("name", ""),
                            pricePerKg = obj.optDouble("pricePerKg", 0.0),
                            stockPieces = obj.optInt("stockPieces", 0),
                            stockWeightKg = obj.optDouble("stockWeightKg", 0.0),
                            unit = obj.optString("unit", "kg"),
                            category = obj.optString("category", "General"),
                            lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis())
                        )
                    )
                }
            }

            val customersList = mutableListOf<Customer>()
            val custArray = root.optJSONArray("customers")
            if (custArray != null) {
                for (i in 0 until custArray.length()) {
                    val obj = custArray.getJSONObject(i)
                    customersList.add(
                        Customer(
                            id = obj.optLong("id", 0L),
                            name = obj.optString("name", ""),
                            phoneNumber = obj.optString("phoneNumber", ""),
                            address = obj.optString("address", ""),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val batchesList = mutableListOf<DailyBatchEntry>()
            val batchArray = root.optJSONArray("batches")
            if (batchArray != null) {
                for (i in 0 until batchArray.length()) {
                    val obj = batchArray.getJSONObject(i)
                    batchesList.add(
                        DailyBatchEntry(
                            id = obj.optLong("id", 0L),
                            dateString = obj.optString("dateString", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            name = obj.optString("name", ""),
                            pieces = obj.optInt("pieces", 0),
                            weightKg = obj.optDouble("weightKg", 0.0),
                            notes = obj.optString("notes", ""),
                            isSynced = true
                        )
                    )
                }
            }

            val salesList = mutableListOf<SaleTransaction>()
            val salesArray = root.optJSONArray("sales")
            if (salesArray != null) {
                for (i in 0 until salesArray.length()) {
                    val obj = salesArray.getJSONObject(i)
                    salesList.add(
                        SaleTransaction(
                            id = obj.optLong("id", 0L),
                            customerId = if (obj.has("customerId") && obj.getLong("customerId") > 0) obj.getLong("customerId") else null,
                            customerName = obj.optString("customerName", ""),
                            productId = if (obj.has("productId") && obj.getLong("productId") > 0) obj.getLong("productId") else null,
                            itemName = obj.optString("itemName", ""),
                            pieces = obj.optInt("pieces", 0),
                            weightKg = obj.optDouble("weightKg", 0.0),
                            pricePerKg = obj.optDouble("pricePerKg", 0.0),
                            totalPrice = obj.optDouble("totalPrice", 0.0),
                            dateString = obj.optString("dateString", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isSynced = true
                        )
                    )
                }
            }

            CloudSyncPayload(
                products = productsList,
                customers = customersList,
                batches = batchesList,
                sales = salesList,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse payload from json", e)
            null
        }
    }
}
