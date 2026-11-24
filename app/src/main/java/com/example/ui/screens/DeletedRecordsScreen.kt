package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletedRecordsScreen(
    deletedCustomersFlow: Flow<List<Customer>>,
    deletedProductsFlow: Flow<List<ProductItem>>,
    deletedSalesFlow: Flow<List<SaleTransaction>>,
    deletedBatchesFlow: Flow<List<DailyBatchEntry>>,
    onRestoreCustomer: (Customer) -> Unit,
    onRestoreProduct: (ProductItem) -> Unit,
    onRestoreSale: (SaleTransaction) -> Unit,
    onRestoreBatch: (DailyBatchEntry) -> Unit,
    onBack: () -> Unit
) {
    val customers by deletedCustomersFlow.collectAsState(initial = emptyList())
    val products by deletedProductsFlow.collectAsState(initial = emptyList())
    val sales by deletedSalesFlow.collectAsState(initial = emptyList())
    val batches by deletedBatchesFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recycle Bin", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (customers.isEmpty() && products.isEmpty() && sales.isEmpty() && batches.isEmpty()) {
                item {
                    Text(
                        text = "No deleted records.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (sales.isNotEmpty()) {
                item { SectionHeader("Deleted Sales") }
                items(sales) { sale ->
                    DeletedItemRow(
                        title = "${sale.customerName} - ${sale.itemName} (${sale.pieces} pcs)",
                        subtitle = sale.dateString,
                        onRestore = { onRestoreSale(sale) }
                    )
                }
            }

            if (customers.isNotEmpty()) {
                item { SectionHeader("Deleted Customers") }
                items(customers) { c ->
                    DeletedItemRow(
                        title = c.name,
                        subtitle = c.phoneNumber,
                        onRestore = { onRestoreCustomer(c) }
                    )
                }
            }

            if (products.isNotEmpty()) {
                item { SectionHeader("Deleted Products") }
                items(products) { p ->
                    DeletedItemRow(
                        title = p.name,
                        subtitle = p.category,
                        onRestore = { onRestoreProduct(p) }
                    )
                }
            }

            if (batches.isNotEmpty()) {
                item { SectionHeader("Deleted Batches") }
                items(batches) { b ->
                    DeletedItemRow(
                        title = b.name,
                        subtitle = b.dateString,
                        onRestore = { onRestoreBatch(b) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun DeletedItemRow(
    title: String,
    subtitle: String,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.Restore, contentDescription = "Restore")
            }
        }
    }
}
