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
import com.example.data.model.ProductItem
import com.example.data.model.SaleTransaction
import com.example.data.model.SyncStatus
import com.example.data.repository.TradeRepository
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
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

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
    val chartPoints: List<DailyChartPoint> = emptyList()
)

data class TradeUiState(
    val isDarkMode: Boolean = false,
    val selectedDateRangePreset: DateRangePreset = DateRangePreset.LAST_7_DAYS,
    val customStartDate: String = "",
    val customEndDate: String = "",
    val searchFilter: String = "",
    val activeTab: Int = 0 // 0: Daily Batches, 1: Sales, 2: Catalog/Profile, 3: Reports, 4: Customers
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

    // Visual Reports combined flow
    val reportMetrics: StateFlow<VisualReportMetrics> = combine(
        allBatches,
        allSales,
        _uiState
    ) { batches, sales, state ->
        calculateMetrics(batches, sales, state.selectedDateRangePreset)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VisualReportMetrics())

    fun setActiveTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    fun setDarkMode(isDark: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = isDark)
    }

    fun setDateRangePreset(preset: DateRangePreset) {
        _uiState.value = _uiState.value.copy(selectedDateRangePreset = preset)
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

    // Sales recording with weight-based price calculation & inventory deduction
    fun recordSale(
        customerId: Long?,
        customerName: String,
        productId: Long?,
        itemName: String,
        pieces: Int,
        weightKg: Double,
        pricePerKg: Double,
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
                dateString = dateString
            )
        }
    }

    // Cloud & Google Drive sync
    fun triggerCloudSync() {
        viewModelScope.launch {
            repository.triggerCloudSync(notifyUser = true)
        }
    }

    fun toggleAutoSync(enabled: Boolean) {
        repository.toggleAutoSync(enabled)
    }

    fun linkGoogleAccount(email: String, displayName: String) {
        repository.linkGoogleAccount(email, displayName)
    }

    fun unlinkGoogleAccount() {
        repository.unlinkGoogleAccount()
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

            val fileName = "TradeSync_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
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
                putExtra(Intent.EXTRA_SUBJECT, "TradeSync Backup & Export (Google Drive Ready)")
                putExtra(Intent.EXTRA_TEXT, "Attached is your TradeSync real-time data export for Google Drive cloud backup.")
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
        batches: List<DailyBatchEntry>,
        sales: List<SaleTransaction>,
        preset: DateRangePreset
    ): VisualReportMetrics {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val now = cal.time

        val filteredBatches = when (preset) {
            DateRangePreset.TODAY -> {
                val todayStr = dateFormat.format(now)
                batches.filter { it.dateString == todayStr }
            }
            DateRangePreset.LAST_7_DAYS -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val cutoff = dateFormat.format(cal.time)
                batches.filter { it.dateString >= cutoff }
            }
            DateRangePreset.LAST_30_DAYS -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                val cutoff = dateFormat.format(cal.time)
                batches.filter { it.dateString >= cutoff }
            }
            DateRangePreset.THIS_MONTH -> {
                val monthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now)
                batches.filter { it.dateString.startsWith(monthPrefix) }
            }
            DateRangePreset.ALL_TIME -> batches
        }

        cal.time = now
        val filteredSales = when (preset) {
            DateRangePreset.TODAY -> {
                val todayStr = dateFormat.format(now)
                sales.filter { it.dateString == todayStr }
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
        }

        val totalWeight = filteredBatches.sumOf { it.weightKg }
        val totalPieces = filteredBatches.sumOf { it.pieces }
        val totalSalesAmount = filteredSales.sumOf { it.totalPrice }
        val transactionsCount = filteredBatches.size + filteredSales.size
        val avgWeight = if (totalPieces > 0) totalWeight / totalPieces else 0.0

        // Daily chart points aggregation
        val dailyMap = mutableMapOf<String, DailyChartPoint>()
        filteredBatches.forEach { b ->
            val existing = dailyMap[b.dateString] ?: DailyChartPoint(b.dateString, 0.0, 0, 0.0)
            dailyMap[b.dateString] = existing.copy(
                weightKg = existing.weightKg + b.weightKg,
                pieces = existing.pieces + b.pieces
            )
        }
        filteredSales.forEach { s ->
            val existing = dailyMap[s.dateString] ?: DailyChartPoint(s.dateString, 0.0, 0, 0.0)
            dailyMap[s.dateString] = existing.copy(
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
            chartPoints = sortedPoints
        )
    }

    class Factory(private val repository: TradeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TradeViewModel(repository) as T
        }
    }
}
