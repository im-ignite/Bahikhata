package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Customer
import com.example.data.model.DailyBatchEntry
import com.example.data.model.GoogleAccountInfo
import com.example.data.model.PaymentTransaction
import com.example.data.model.ProductItem
import com.example.data.model.SaleTransaction
import com.example.data.model.SyncStatus
import com.example.data.repository.TradeRepository
import com.example.ui.util.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateRangePreset(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time"),
    SPECIFIC_DATE("Specific Date")
}

data class ClientSalesSummary(
    val customerName: String,
    val totalWeightKg: Double,
    val totalPieces: Int,
    val totalAmount: Double,
    val itemsSummary: String,
    val salesCount: Int,
    val transactions: List<SaleTransaction> = emptyList()
)

data class DailyChartPoint(
    val dateLabel: String,
    val weightKg: Double,
    val pieces: Int,
    val salesAmount: Double
)

data class VisualReportMetrics(
    val totalWeightKg: Double = 0.0,
    val totalPieces: Int = 0,
    val totalSalesAmount: Double = 0.0,
    val totalTransactions: Int = 0,
    val avgWeightPerPiece: Double = 0.0,
    val chartPoints: List<DailyChartPoint> = emptyList(),
    val filteredSales: List<SaleTransaction> = emptyList(),
    val clientSummaries: List<ClientSalesSummary> = emptyList()
)

data class TradeUiState(
    val isDarkMode: Boolean = false,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val selectedDateRangePreset: DateRangePreset = DateRangePreset.LAST_7_DAYS,
    val specificSearchDate: String = "",
    val customStartDate: String = "",
    val customEndDate: String = "",
    val searchFilter: String = "",
    val activeTab: Int = 0 // 0: Date Sales, 1: Sales, 2: Products, 3: Reports, 4: Customers
)

class TradeViewModel(
    private val repository: TradeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradeUiState())
    val uiState: StateFlow<TradeUiState> = _uiState.asStateFlow()

    val googleAccount: StateFlow<GoogleAccountInfo> = repository.googleAccount
    val syncStatus: StateFlow<SyncStatus> = repository.syncStatus
    val lastSyncLog: StateFlow<String> = repository.lastSyncLog

    val allBatches: StateFlow<List<DailyBatchEntry>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductItem>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<SaleTransaction>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Visual Reports combined flow (focused on fish sales to clients)
    val reportMetrics: StateFlow<VisualReportMetrics> = combine(
        allSales,
        _uiState
    ) { sales, state ->
        calculateMetrics(sales, state.selectedDateRangePreset, state.specificSearchDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VisualReportMetrics())

    fun setActiveTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun setDarkMode(isDark: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = isDark)
    }

    fun setDateRangePreset(preset: DateRangePreset) {
        _uiState.value = _uiState.value.copy(
            selectedDateRangePreset = preset,
            specificSearchDate = if (preset == DateRangePreset.SPECIFIC_DATE) _uiState.value.specificSearchDate else ""
        )
    }

    fun setSpecificDateSearch(dateStr: String) {
        _uiState.value = _uiState.value.copy(
            specificSearchDate = dateStr,
            selectedDateRangePreset = DateRangePreset.SPECIFIC_DATE
        )
    }

    fun clearSpecificDateSearch() {
        _uiState.value = _uiState.value.copy(
            specificSearchDate = "",
            selectedDateRangePreset = DateRangePreset.LAST_7_DAYS
        )
    }

    fun setSearchFilter(query: String) {
        _uiState.value = _uiState.value.copy(searchFilter = query)
    }

    // Daily batch operations
    fun addDailyBatch(name: String, pieces: Int, weightKg: Double, notes: String, dateString: String) {
        viewModelScope.launch {
            repository.addBatch(dateString, name, pieces, weightKg, notes)
        }
    }

    fun deleteDailyBatch(entry: DailyBatchEntry) {
        viewModelScope.launch {
            repository.deleteBatch(entry)
        }
    }

    // Products / Catalog operations
    fun addProduct(name: String, pricePerKg: Double, pieces: Int, weightKg: Double, category: String) {
        viewModelScope.launch {
            repository.addProduct(name, pricePerKg, pieces, weightKg, category)
        }
    }

    fun updateProduct(product: ProductItem) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: ProductItem) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // Customer operations
    fun addCustomer(name: String, phoneNumber: String, address: String, notes: String) {
        viewModelScope.launch {
            repository.addCustomer(name, phoneNumber, address, notes)
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Sales recording with weight-based price calculation & inventory deduction
    fun recordSale(
        customerId: Long?,
        customerName: String,
        productId: Long?,
        itemName: String,
        pieces: Int,
        weightKg: Double,
        pricePerKg: Double,
        amountPaid: Double,
        dateString: String
    ) {
        viewModelScope.launch {
            repository.recordSale(
                customerId = customerId,
                customerName = customerName,
                productId = productId,
                itemName = itemName,
                pieces = pieces,
                weightKg = weightKg,
                pricePerKg = pricePerKg,
                amountPaid = amountPaid,
                dateString = dateString
            )
        }
    }

    fun updateSale(sale: SaleTransaction) {
        viewModelScope.launch {
            repository.updateSale(sale)
        }
    }

    fun deleteSale(sale: SaleTransaction) {
        viewModelScope.launch {
            repository.deleteSale(sale)
        }
    }

    fun registerCustomerPayment(customerId: Long, amount: Double, dateString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (amount <= 0.0) return@launch

            // 1. Record the payment transaction
            val payment = PaymentTransaction(
                customerId = customerId,
                amountPaid = amount,
                dateString = dateString
            )
            repository.addPayment(payment)

            var remainingAmount = amount
            // Fetch all sales for this customer, sorted by oldest first
            val customerSales = allSales.value.filter { it.customerId == customerId }
                .sortedBy { it.timestamp }

            for (sale in customerSales) {
                val dueAmount = sale.totalPrice - sale.amountPaid
                if (dueAmount > 0) {
                    if (remainingAmount >= dueAmount) {
                        // Fully pay this sale
                        val updatedSale = sale.copy(amountPaid = sale.amountPaid + dueAmount)
                        repository.updateSale(updatedSale)
                        remainingAmount -= dueAmount
                    } else {
                        // Partially pay this sale
                        val updatedSale = sale.copy(amountPaid = sale.amountPaid + remainingAmount)
                        repository.updateSale(updatedSale)
                        remainingAmount = 0.0
                    }
                }
                
                if (remainingAmount <= 0) break
            }
        }
    }

    fun getPaymentsForCustomer(customerId: Long) = repository.getPaymentsForCustomer(customerId)

    // Cloud & Google Account Sync
    fun triggerCloudSync() {
        viewModelScope.launch {
            repository.triggerCloudSync(notifyUser = true)
        }
    }

    fun toggleAutoSync(enabled: Boolean) {
        repository.toggleAutoSync(enabled)
    }

    fun getDeviceGoogleAccounts(): List<String> {
        return repository.getDeviceGoogleAccounts()
    }

    fun createSystemAccountPickerIntent(): Intent {
        return repository.createSystemAccountPickerIntent()
    }

    fun signInWithGoogleAccount(email: String, customName: String? = null) {
        viewModelScope.launch {
            repository.signInWithGoogleAccount(email, customName)
        }
    }

    fun signInWithCredentialManager(activityContext: Context, webClientId: String? = null, onResult: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            val res = repository.signInWithCredentialManager(activityContext, webClientId)
            res.onSuccess {
                onResult?.invoke(true, null)
            }.onFailure { err ->
                onResult?.invoke(false, err.message)
            }
        }
    }

    fun linkGoogleAccount(email: String, customName: String? = null) {
        signInWithGoogleAccount(email, customName)
    }

    fun signOutGoogleAccount() {
        repository.signOutGoogleAccount()
    }

    fun unlinkGoogleAccount() {
        signOutGoogleAccount()
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun isCloudConfigured(): Boolean = repository.isCloudConfigured()

    fun exportBackupFile(context: Context): Boolean {
        return try {
            viewModelScope.launch {
                val json = repository.exportBackupJson()
                val fileName = "RAI_FISH_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
                val exportDir = File(context.cacheDir, "backups").apply { mkdirs() }
                val file = File(exportDir, fileName)
                FileOutputStream(file).use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "RAI FISH Backup - $fileName")
                    putExtra(Intent.EXTRA_TEXT, "Complete data backup for RAI FISH App. Save to Google Drive to keep your data safe across installs.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Save Backup to Google Drive / Files")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restoreBackupFromUri(context: Context, uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().use { it.readText() }
                } ?: ""

                if (jsonString.isBlank()) {
                    onResult(false, "Backup file is empty")
                    return@launch
                }

                val success = repository.importBackupJson(jsonString)
                if (success) {
                    onResult(true, "Data successfully restored from backup!")
                } else {
                    onResult(false, "Could not parse backup file")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to restore backup")
            }
        }
    }

    // Google Drive CSV Export & Share
    fun exportCsvForGoogleDrive(context: Context): Boolean {
        return try {
            val csvContent = repository.generateCsvContent(
                batches = allBatches.value,
                sales = allSales.value,
                products = allProducts.value,
                customers = allCustomers.value
            )

            val fileName = "RAI_FISH_Sales_Export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, fileName)
            FileOutputStream(file).use { out ->
                out.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "RAI FISH Client Sales Report (Google Drive Ready)")
                putExtra(Intent.EXTRA_TEXT, "Attached is your RAI FISH pond-to-client sales report and customer records.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Save or Export to Google Drive")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun calculateMetrics(
        sales: List<SaleTransaction>,
        preset: DateRangePreset,
        specificDate: String
    ): VisualReportMetrics {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val now = cal.time

        val filteredSales = when (preset) {
            DateRangePreset.TODAY -> {
                val todayStr = dateFormat.format(now)
                sales.filter { it.dateString == todayStr }
            }
            DateRangePreset.YESTERDAY -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = dateFormat.format(cal.time)
                sales.filter { it.dateString == yesterdayStr }
            }
            DateRangePreset.LAST_7_DAYS -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val cutoff = dateFormat.format(cal.time)
                sales.filter { it.dateString >= cutoff }
            }
            DateRangePreset.LAST_30_DAYS -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                val cutoff = dateFormat.format(cal.time)
                sales.filter { it.dateString >= cutoff }
            }
            DateRangePreset.THIS_MONTH -> {
                val monthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now)
                sales.filter { it.dateString.startsWith(monthPrefix) }
            }
            DateRangePreset.ALL_TIME -> sales
            DateRangePreset.SPECIFIC_DATE -> {
                if (specificDate.isNotBlank()) {
                    sales.filter { it.dateString == specificDate.trim() }
                } else {
                    sales
                }
            }
        }

        val totalWeight = filteredSales.sumOf { it.weightKg }
        val totalPieces = filteredSales.sumOf { it.pieces }
        val totalSalesAmount = filteredSales.sumOf { it.totalPrice }
        val transactionsCount = filteredSales.size
        val avgWeight = if (totalPieces > 0) totalWeight / totalPieces else 0.0

        // Client-wise breakdown of sales: to whom and which clients bought fish!
        val clientGroupMap = filteredSales.groupBy { it.customerName }
        val clientSummaries = clientGroupMap.map { (clientName, clientSales) ->
            val cWeight = clientSales.sumOf { it.weightKg }
            val cPieces = clientSales.sumOf { it.pieces }
            val cTotal = clientSales.sumOf { it.totalPrice }
            val itemsStr = clientSales.map { "${it.itemName} (${it.weightKg}kg)" }.distinct().joinToString(", ")
            ClientSalesSummary(
                customerName = clientName,
                totalWeightKg = cWeight,
                totalPieces = cPieces,
                totalAmount = cTotal,
                itemsSummary = itemsStr,
                salesCount = clientSales.size,
                transactions = clientSales
            )
        }.sortedByDescending { it.totalAmount }

        // Daily chart points aggregation for sales trends
        val dailyMap = mutableMapOf<String, DailyChartPoint>()
        filteredSales.forEach { s ->
            val existing = dailyMap[s.dateString] ?: DailyChartPoint(s.dateString, 0.0, 0, 0.0)
            dailyMap[s.dateString] = existing.copy(
                weightKg = existing.weightKg + s.weightKg,
                pieces = existing.pieces + s.pieces,
                salesAmount = existing.salesAmount + s.totalPrice
            )
        }

        val sortedPoints = dailyMap.values.sortedBy { it.dateLabel }

        return VisualReportMetrics(
            totalWeightKg = totalWeight,
            totalPieces = totalPieces,
            totalSalesAmount = totalSalesAmount,
            totalTransactions = transactionsCount,
            avgWeightPerPiece = avgWeight,
            chartPoints = sortedPoints,
            filteredSales = filteredSales.sortedByDescending { it.timestamp },
            clientSummaries = clientSummaries
        )
    }

    class Factory(private val repository: TradeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TradeViewModel(repository) as T
        }
    }
}
