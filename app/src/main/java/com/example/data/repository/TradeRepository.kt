package com.example.data.repository

import android.content.Context
import android.content.Intent
import com.example.data.auth.GoogleAuthManager
import com.example.data.cloud.CloudDataStore
import com.example.data.local.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.DailyBatchEntry
import com.example.data.model.GoogleAccountInfo
import com.example.data.model.ProductItem
import com.example.data.model.SaleTransaction
import com.example.data.model.SyncStatus
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TradeRepository(
    private val database: AppDatabase,
    private val notificationHelper: NotificationHelper,
    private val context: Context
) {
    private val batchDao = database.dailyBatchDao()
    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val saleDao = database.saleDao()

    val authManager = GoogleAuthManager(context)
    val cloudDataStore = CloudDataStore(context)

    val googleAccount: StateFlow<GoogleAccountInfo> = authManager.accountInfo

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus = _syncStatus.asStateFlow()

    private val _lastSyncLog = MutableStateFlow("Google Account cloud backup ready")
    val lastSyncLog = _lastSyncLog.asStateFlow()

    private val repoScope = CoroutineScope(Dispatchers.IO)

    init {
        // If user is already linked with Google account, trigger silent initial sync
        repoScope.launch {
            val account = authManager.accountInfo.value
            if (account.isLinked && account.email.isNotBlank()) {
                pullDataFromCloud(account.email)
                triggerCloudSync(notifyUser = false)
            }
        }
    }

    // Flows
    val allBatches: Flow<List<DailyBatchEntry>> = batchDao.getAllBatches()
    val allProducts: Flow<List<ProductItem>> = productDao.getAllProducts()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allSales: Flow<List<SaleTransaction>> = saleDao.getAllSales()

    fun getBatchesBetween(startDate: String, endDate: String): Flow<List<DailyBatchEntry>> {
        return batchDao.getBatchesBetween(startDate, endDate)
    }

    fun getSalesBetween(startDate: String, endDate: String): Flow<List<SaleTransaction>> {
        return saleDao.getSalesBetween(startDate, endDate)
    }

    fun getSalesForCustomer(customerId: Long): Flow<List<SaleTransaction>> {
        return saleDao.getSalesForCustomer(customerId)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        saleDao.deleteAllSales()
        batchDao.deleteAllBatches()
        customerDao.deleteAllCustomers()
        productDao.deleteAllProducts()
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.clearUserCloudData(account.email)
        }
    }

    // Daily Batch Operations
    suspend fun addBatch(
        dateString: String,
        name: String,
        pieces: Int,
        weightKg: Double,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val entry = DailyBatchEntry(
            dateString = dateString,
            name = name,
            pieces = pieces,
            weightKg = weightKg,
            notes = notes,
            isSynced = true
        )
        val id = batchDao.insertBatch(entry)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveBatchToCloud(account.email, entry.copy(id = id))
        }
        autoSyncIfEnabled()
    }

    suspend fun deleteBatch(entry: DailyBatchEntry) = withContext(Dispatchers.IO) {
        batchDao.deleteBatch(entry)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.deleteBatchFromCloud(account.email, entry.id)
        }
        autoSyncIfEnabled()
    }

    // Product & Inventory Operations
    suspend fun addProduct(
        name: String,
        pricePerKg: Double,
        stockPieces: Int,
        stockWeightKg: Double,
        category: String
    ) = withContext(Dispatchers.IO) {
        val product = ProductItem(
            name = name,
            pricePerKg = pricePerKg,
            stockPieces = stockPieces,
            stockWeightKg = stockWeightKg,
            category = category
        )
        val id = productDao.insertProduct(product)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveProductToCloud(account.email, product.copy(id = id))
        }
        autoSyncIfEnabled()
    }

    suspend fun updateProduct(product: ProductItem) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveProductToCloud(account.email, product)
        }
        autoSyncIfEnabled()
    }

    suspend fun deleteProduct(product: ProductItem) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.deleteProductFromCloud(account.email, product.id)
        }
        autoSyncIfEnabled()
    }

    // Customer CRM Operations
    suspend fun addCustomer(
        name: String,
        phoneNumber: String,
        address: String,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        val customer = Customer(
            name = name,
            phoneNumber = phoneNumber,
            address = address,
            notes = notes
        )
        val id = customerDao.insertCustomer(customer)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveCustomerToCloud(account.email, customer.copy(id = id))
        }
        autoSyncIfEnabled()
        id
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        customerDao.updateCustomer(customer)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveCustomerToCloud(account.email, customer)
        }
        autoSyncIfEnabled()
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        customerDao.deleteCustomer(customer)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.deleteCustomerFromCloud(account.email, customer.id)
        }
        autoSyncIfEnabled()
    }

    // Sales & Automatic Inventory Deduction
    suspend fun recordSale(
        customerId: Long?,
        customerName: String,
        productId: Long?,
        itemName: String,
        pieces: Int,
        weightKg: Double,
        pricePerKg: Double,
        amountPaid: Double,
        dateString: String
    ) = withContext(Dispatchers.IO) {
        val totalPrice = weightKg * pricePerKg // Strictly weight-based calculation
        val sale = SaleTransaction(
            customerId = customerId,
            customerName = customerName,
            productId = productId,
            itemName = itemName,
            pieces = pieces,
            weightKg = weightKg,
            pricePerKg = pricePerKg,
            totalPrice = totalPrice,
            dateString = dateString,
            amountPaid = amountPaid,
            isSynced = true
        )
        val id = saleDao.insertSale(sale)
        val createdSale = sale.copy(id = id)

        // Automatic Inventory Deduction
        if (productId != null && productId > 0) {
            productDao.deductInventory(productId, pieces, weightKg)
            val updatedProd = productDao.getProductByIdDirect(productId)
            if (updatedProd != null && authManager.accountInfo.value.isLinked) {
                cloudDataStore.saveProductToCloud(authManager.accountInfo.value.email, updatedProd)
            }
        }

        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveSaleToCloud(account.email, createdSale)
        }

        notificationHelper.sendSaleRecordedNotification(customerName, totalPrice, weightKg)
        autoSyncIfEnabled()
    }

    suspend fun updateSale(sale: SaleTransaction) = withContext(Dispatchers.IO) {
        val calculatedTotal = sale.weightKg * sale.pricePerKg
        val updatedSale = sale.copy(
            totalPrice = calculatedTotal,
            isSynced = true
        )
        saleDao.updateSale(updatedSale)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveSaleToCloud(account.email, updatedSale)
        }
        autoSyncIfEnabled()
    }

    suspend fun deleteSale(sale: SaleTransaction) = withContext(Dispatchers.IO) {
        saleDao.deleteSale(sale)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.deleteSaleFromCloud(account.email, sale.id)
        }
        autoSyncIfEnabled()
    }

    private fun autoSyncIfEnabled() {
        if (authManager.accountInfo.value.isLinked && authManager.accountInfo.value.autoSyncEnabled) {
            repoScope.launch {
                triggerCloudSync(notifyUser = false)
            }
        }
    }

    // Google Account Linking & Auth
    fun getDeviceGoogleAccounts(): List<String> {
        return authManager.getDeviceGoogleAccounts()
    }

    fun createSystemAccountPickerIntent(): Intent {
        return authManager.createSystemAccountPickerIntent()
    }

    suspend fun signInWithGoogleAccount(email: String, customName: String? = null) = withContext(Dispatchers.IO) {
        val info = authManager.completeSignInWithEmail(email, customName)
        // Clear local cache before loading account data so only this account's cloud data is loaded
        saleDao.deleteAllSales()
        batchDao.deleteAllBatches()
        customerDao.deleteAllCustomers()
        productDao.deleteAllProducts()

        pullDataFromCloud(info.email)
        triggerCloudSync(notifyUser = false)
    }

    suspend fun signInWithCredentialManager(activityContext: Context, webClientId: String? = null): Result<GoogleAccountInfo> = withContext(Dispatchers.IO) {
        val res = authManager.signInWithCredentialManager(activityContext, webClientId)
        res.onSuccess { info ->
            saleDao.deleteAllSales()
            batchDao.deleteAllBatches()
            customerDao.deleteAllCustomers()
            productDao.deleteAllProducts()

            pullDataFromCloud(info.email)
            triggerCloudSync(notifyUser = false)
        }
        res
    }

    fun signOutGoogleAccount() {
        repoScope.launch {
            saleDao.deleteAllSales()
            batchDao.deleteAllBatches()
            customerDao.deleteAllCustomers()
            productDao.deleteAllProducts()
        }
        authManager.signOut()
        _lastSyncLog.value = "Signed out. Cloud data safely saved in Google account."
    }

    fun toggleAutoSync(enabled: Boolean) {
        authManager.toggleAutoSync(enabled)
    }

    // Cross-device Cloud Sync Engine: Pull data from online store
    suspend fun pullDataFromCloud(email: String): Int = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext 0
        try {
            val payload = cloudDataStore.fetchCloudData(email) ?: return@withContext 0
            var restoredCount = 0

            if (payload.products.isNotEmpty()) {
                productDao.deleteAllProducts()
                productDao.insertAll(payload.products)
                restoredCount += payload.products.size
            }
            if (payload.customers.isNotEmpty()) {
                customerDao.deleteAllCustomers()
                customerDao.insertAll(payload.customers)
                restoredCount += payload.customers.size
            }
            if (payload.batches.isNotEmpty()) {
                batchDao.deleteAllBatches()
                batchDao.insertAll(payload.batches)
                restoredCount += payload.batches.size
            }
            if (payload.sales.isNotEmpty()) {
                saleDao.deleteAllSales()
                saleDao.insertAll(payload.sales)
                restoredCount += payload.sales.size
            }

            if (restoredCount > 0) {
                authManager.updateSyncMetadata(System.currentTimeMillis(), restoredCount)
                _lastSyncLog.value = "Restored $restoredCount records from Google Cloud"
            }
            return@withContext restoredCount
        } catch (e: Exception) {
            return@withContext 0
        }
    }

    // Dual-write Cloud Synchronization Engine
    suspend fun triggerCloudSync(notifyUser: Boolean = true): Boolean = withContext(Dispatchers.IO) {
        val account = authManager.accountInfo.value
        if (!account.isLinked || account.email.isBlank()) {
            _lastSyncLog.value = "Please sign in with Google to sync data online"
            return@withContext false
        }

        _syncStatus.value = SyncStatus.SYNCING
        _lastSyncLog.value = "Syncing with Google Cloud..."

        try {
            // 1. Pull remote updates first so nothing is overwritten
            val cloudPayload = cloudDataStore.fetchCloudData(account.email)
            if (cloudPayload != null) {
                if (cloudPayload.products.isNotEmpty()) productDao.insertAll(cloudPayload.products)
                if (cloudPayload.customers.isNotEmpty()) customerDao.insertAll(cloudPayload.customers)
                if (cloudPayload.batches.isNotEmpty()) batchDao.insertAll(cloudPayload.batches)
                if (cloudPayload.sales.isNotEmpty()) saleDao.insertAll(cloudPayload.sales)
            }

            // 2. Fetch combined local records
            val currentProducts = productDao.getAllProductsList()
            val currentCustomers = customerDao.getAllCustomersList()
            val currentBatches = batchDao.getAllBatchesList()
            val currentSales = saleDao.getAllSalesList()

            // 3. Upload combined records to Cloud
            val success = cloudDataStore.uploadToCloud(
                email = account.email,
                products = currentProducts,
                customers = currentCustomers,
                batches = currentBatches,
                sales = currentSales
            )

            val now = System.currentTimeMillis()
            batchDao.markAllAsSynced(now)
            saleDao.markAllAsSynced()

            val totalSynced = currentProducts.size + currentCustomers.size + currentBatches.size + currentSales.size
            authManager.updateSyncMetadata(now, totalSynced)

            _syncStatus.value = SyncStatus.SUCCESS
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now))
            _lastSyncLog.value = "Synced with Google Cloud at $timeStr ($totalSynced records)"

            if (notifyUser) {
                notificationHelper.sendSyncNotification(
                    "Google Cloud Synced",
                    "Stored $totalSynced records online for ${account.email}"
                )
            }
            return@withContext success
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            _lastSyncLog.value = "Sync failed: ${e.localizedMessage ?: "Unknown error"}"
            return@withContext false
        } finally {
            delay(1200)
            _syncStatus.value = SyncStatus.IDLE
        }
    }

    // CSV Generation for Manual Google Drive Export & Backup
    fun generateCsvContent(
        batches: List<DailyBatchEntry>,
        sales: List<SaleTransaction>,
        products: List<ProductItem>,
        customers: List<Customer>
    ): String {
        val sb = StringBuilder()
        sb.append("RAI FISH Cloud Export Data\n")
        sb.append("Generated At,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        sb.append("Google Account,${authManager.accountInfo.value.email}\n\n")

        // 1. Daily Batches
        sb.append("--- DAILY INVENTORY & BATCH METRICS ---\n")
        sb.append("ID,Date,Batch Name,Pieces,Weight (kg),Notes,Synced Status\n")
        batches.forEach { b ->
            sb.append("${b.id},\"${b.dateString}\",\"${b.name.replace("\"", "\"\"")}\",${b.pieces},${b.weightKg},\"${b.notes.replace("\"", "\"\"")}\",${if (b.isSynced) "YES" else "PENDING"}\n")
        }
        sb.append("\n")

        // 2. Sales Transactions
        sb.append("--- SALES & CUSTOMER TRANSACTIONS ---\n")
        sb.append("Sale ID,Date,Customer Name,Item Name,Pieces,Weight (kg),Price per kg (₹),Total Amount (₹),Sync Status\n")
        sales.forEach { s ->
            sb.append("${s.id},\"${s.dateString}\",\"${s.customerName.replace("\"", "\"\"")}\",\"${s.itemName.replace("\"", "\"\"")}\",${s.pieces},${s.weightKg},${s.pricePerKg},${s.totalPrice},${if (s.isSynced) "YES" else "PENDING"}\n")
        }
        sb.append("\n")

        // 3. Products Catalog & Price basis
        sb.append("--- PRODUCTS CATALOG (FISH SPECIES) ---\n")
        sb.append("Product ID,Item Name,Category,Price Basis (₹/kg),Stock Pieces,Stock Weight (kg)\n")
        products.forEach { p ->
            sb.append("${p.id},\"${p.name.replace("\"", "\"\"")}\",\"${p.category}\",${p.pricePerKg},${p.stockPieces},${p.stockWeightKg}\n")
        }
        sb.append("\n")

        // 4. Customers Directory
        sb.append("--- CUSTOMERS DIRECTORY ---\n")
        sb.append("Customer ID,Name,Phone Number,Address,Notes\n")
        customers.forEach { c ->
            sb.append("${c.id},\"${c.name.replace("\"", "\"\"")}\",\"${c.phoneNumber}\",\"${c.address.replace("\"", "\"\"")}\",\"${c.notes.replace("\"", "\"\"")}\"\n")
        }

        return sb.toString()
    }

    fun isCloudConfigured(): Boolean = cloudDataStore.isCloudConfigured()

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val products = productDao.getAllProductsList()
        val customers = customerDao.getAllCustomersList()
        val batches = batchDao.getAllBatchesList()
        val sales = saleDao.getAllSalesList()
        val email = googleAccount.value.email.ifBlank { "offline_user@raifish.local" }
        cloudDataStore.serializePayloadToJson(email, products, customers, batches, sales)
    }

    suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        val payload = cloudDataStore.parsePayloadFromJson(jsonString) ?: return@withContext false
        try {
            if (payload.products.isNotEmpty()) {
                productDao.insertAll(payload.products)
            }
            if (payload.customers.isNotEmpty()) {
                customerDao.insertAll(payload.customers)
            }
            if (payload.batches.isNotEmpty()) {
                batchDao.insertAll(payload.batches)
            }
            if (payload.sales.isNotEmpty()) {
                saleDao.insertAll(payload.sales)
            }
            _lastSyncLog.value = "Restored ${payload.sales.size} sales, ${payload.products.size} products from file"
            true
        } catch (e: Exception) {
            false
        }
    }
}
