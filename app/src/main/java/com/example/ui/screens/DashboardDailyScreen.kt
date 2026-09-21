package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.GoogleAccountInfo
import com.example.data.model.ProductItem
import com.example.data.model.SaleTransaction
import com.example.data.model.SyncStatus
import com.example.ui.components.EditSaleDialog


import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary
import com.example.ui.util.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardDailyScreen(
    sales: List<SaleTransaction>,
    customers: List<Customer>,
    products: List<ProductItem>,
    onRecordSale: (
        customerId: Long?,
        customerName: String,
        productId: Long?,
        itemName: String,
        pieces: Int,
        weightKg: Double,
        pricePerKg: Double,
        dateString: String
    ) -> Unit,
    onUpdateSale: (SaleTransaction) -> Unit,
    onDeleteSale: (SaleTransaction) -> Unit,
    onAddCustomer: (name: String, phone: String, address: String, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { dateFormat.format(Date()) }

    var selectedDate by remember { mutableStateOf(todayDate) }
    var searchQuery by remember { mutableStateOf("") }
    var showRecordSaleDialog by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    
    var editingSale by remember { mutableStateOf<SaleTransaction?>(null) }
    var saleToDelete by remember { mutableStateOf<SaleTransaction?>(null) }

    // Helper functions for date stepping
    fun stepDate(daysDelta: Int) {
        try {
            val parsed = dateFormat.parse(selectedDate) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = parsed
                add(Calendar.DAY_OF_YEAR, daysDelta)
            }
            selectedDate = dateFormat.format(cal.time)
        } catch (_: Exception) {
            selectedDate = todayDate
        }
    }

    // Filter sales for the selected date and search filter
    val dateSales = sales.filter { it.dateString == selectedDate }
    val filteredSales = if (searchQuery.isBlank()) {
        dateSales
    } else {
        val q = searchQuery.trim().lowercase()
        dateSales.filter {
            it.customerName.lowercase().contains(q) ||
            it.itemName.lowercase().contains(q)
        }
    }

    // Aggregates for the selected date
    val dayTotalSales = dateSales.sumOf { it.totalPrice }
    val dayTotalWeight = dateSales.sumOf { it.weightKg }
    val dayTotalPieces = dateSales.sumOf { it.pieces }
    val dayUniqueClients = dateSales.map { it.customerName }.distinct().size

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Google Drive Cloud Sync Card removed from front page per request

            // 2. Date Navigation & Picker Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.dateSelectorTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (selectedDate != todayDate) {
                                OutlinedButton(
                                    onClick = { selectedDate = todayDate },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Today,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.todayButton, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stepper Bar: [<] [ Date Badge / Picker ] [>]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { stepDate(-1) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    .testTag("date_step_prev_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Day",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Date Badge - clickable to pick any specific date
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(TealPrimary.copy(alpha = 0.14f))
                                    .clickable { showDatePickerDialog = true }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .testTag("date_picker_badge"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedDate == todayDate) "$selectedDate (Today)" else selectedDate,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { stepDate(1) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    .testTag("date_step_next_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Day",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 3. Daily Sales Overview KPIs for this date
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Sales Amount
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.daySalesAmountLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(AmberAccent.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyRupee,
                                        contentDescription = null,
                                        tint = AmberAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹${String.format("%.2f", dayTotalSales)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberAccent,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "$dayUniqueClients ${strings.dayClientsLabel.lowercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Total Fish Weight
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.dayFishWeightLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(TealPrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${String.format("%.1f", dayTotalWeight)} kg",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TealPrimary,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "$dayTotalPieces ${strings.dayPiecesLabel.lowercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 4. Search Filter for this Date
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter client name or fish type on this date...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_sales_search_input")
                )
            }

            // 5. Section Title: "Client Fish Sales for [Date]"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${strings.dateSalesTitle} ($selectedDate)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${filteredSales.size} sales",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                }
            }

            // 6. Client Sales List for Selected Date
            if (filteredSales.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(TealPrimary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = strings.noSalesOnDateMsg,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Selling directly from your pond? Record the sale here to track weight and client payments.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { showRecordSaleDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                modifier = Modifier.testTag("empty_state_record_sale_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.recordFishSaleButton, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredSales, key = { it.id }) { sale ->
                    val customer = customers.find { it.id == sale.customerId }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sale_item_${sale.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Client Info
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(TealPrimary.copy(alpha = 0.14f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = sale.customerName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (customer?.phoneNumber?.isNotBlank() == true) {
                                            Text(
                                                text = customer.phoneNumber,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Total Amount Badge
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹${String.format("%.2f", sale.totalPrice)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AmberAccent
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (sale.isSynced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                            contentDescription = if (sale.isSynced) "Synced to Drive" else "Pending Drive Sync",
                                            tint = if (sale.isSynced) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (sale.isSynced) "Drive Synced" else "Pending Sync",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (sale.isSynced) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Fish Details Pill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = sale.itemName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary
                                        )
                                        Text(
                                            text = "Rate: ₹${String.format("%.2f", sale.pricePerKg)} / kg",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${String.format("%.1f", sale.weightKg)} kg",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${sale.pieces} pcs",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (customer?.phoneNumber?.isNotBlank() == true) {
                                    TextButton(
                                        onClick = {
                                            try {
                                                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${customer.phoneNumber}")
                                                }
                                                context.startActivity(callIntent)
                                            } catch (_: Exception) {}
                                        },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call Client", color = SuccessGreen, style = MaterialTheme.typography.labelMedium)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { editingSale = sale },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { saleToDelete = sale },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = DangerRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to quickly record a sale for this date
        FloatingActionButton(
            onClick = { showRecordSaleDialog = true },
            containerColor = TealPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("fab_record_fish_sale")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.recordFishSaleButton
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.recordFishSaleButton,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Dialog: Record Fish Sale for Selected Date
    if (showRecordSaleDialog) {
        RecordFishSaleDialog(
            selectedDate = selectedDate,
            products = products,
            customers = customers,
            onDismiss = { showRecordSaleDialog = false },
            onConfirm = { custId, custName, prodId, itemName, pcs, wt, rate, dateStr ->
                onRecordSale(custId, custName, prodId, itemName, pcs, wt, rate, dateStr)
                showRecordSaleDialog = false
            },
            onAddNewCustomer = onAddCustomer
        )
    }

    // Dialog: Edit Sale
    editingSale?.let { sale ->
        EditSaleDialog(
            sale = sale,
            products = products,
            customers = customers,
            onDismiss = { editingSale = null },
            onConfirm = { updated ->
                onUpdateSale(updated)
                editingSale = null
            }
        )
    }

    // Dialog: Confirm Delete
    saleToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text("Delete Fish Sale Record?") },
            text = {
                Text("Are you sure you want to delete the sale of ${sale.weightKg} kg ${sale.itemName} to ${sale.customerName} on ${sale.dateString}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSale(sale)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Pick Any Specific Date
    if (showDatePickerDialog) {
        var inputPickerDate by remember { mutableStateOf(selectedDate) }
        AlertDialog(
            onDismissRequest = { showDatePickerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = TealPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.searchSpecificDateTitle, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter or choose date (YYYY-MM-DD) to view fish sales for that day:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputPickerDate,
                        onValueChange = { inputPickerDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("picker_date_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { inputPickerDate = todayDate },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Today ($todayDate)", style = MaterialTheme.typography.labelSmall)
                        }

                        val yDate = remember {
                            val c = Calendar.getInstance()
                            c.add(Calendar.DAY_OF_YEAR, -1)
                            dateFormat.format(c.time)
                        }
                        OutlinedButton(
                            onClick = { inputPickerDate = yDate },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Yesterday", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputPickerDate.isNotBlank()) {
                            selectedDate = inputPickerDate.trim()
                        }
                        showDatePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Select Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Removed Link Dialog
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordFishSaleDialog(
    selectedDate: String,
    products: List<ProductItem>,
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (
        customerId: Long?,
        customerName: String,
        productId: Long?,
        itemName: String,
        pieces: Int,
        weightKg: Double,
        pricePerKg: Double,
        dateString: String
    ) -> Unit,
    onAddNewCustomer: (name: String, phone: String, address: String, notes: String) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf(customers.firstOrNull()) }
    var isNewCustomerMode by remember { mutableStateOf(customers.isEmpty()) }
    var manualCustomerName by remember { mutableStateOf("") }
    var manualCustomerPhone by remember { mutableStateOf("") }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var isManualFishMode by remember { mutableStateOf(products.isEmpty()) }
    var manualFishName by remember { mutableStateOf("") }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    var weightInput by remember { mutableStateOf("") }
    var piecesInput by remember { mutableStateOf("1") }
    var rateInput by remember {
        mutableStateOf(selectedProduct?.pricePerKg?.toString() ?: "180.0")
    }
    var saleDate by remember { mutableStateOf(selectedDate) }

    // Live calculations
    val inputWeight = weightInput.toDoubleOrNull() ?: 0.0
    val inputPieces = piecesInput.toIntOrNull() ?: 1
    val inputRate = rateInput.toDoubleOrNull() ?: 0.0
    val calculatedTotal = inputWeight * inputRate

    val canSubmit = (selectedCustomer != null || manualCustomerName.isNotBlank()) &&
            (selectedProduct != null || manualFishName.isNotBlank()) &&
            inputWeight > 0.0 &&
            inputRate > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = AmberAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Fish Sale for $saleDate", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Customer Selector
                item {
                    if (!isNewCustomerMode && customers.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = customerDropdownExpanded,
                            onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: "Select Client *",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Client *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("pond_sale_customer_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = customerDropdownExpanded,
                                onDismissRequest = { customerDropdownExpanded = false }
                            ) {
                                customers.forEach { cust ->
                                    DropdownMenuItem(
                                        text = { Text("${cust.name} (${cust.phoneNumber})") },
                                        onClick = {
                                            selectedCustomer = cust
                                            customerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { isNewCustomerMode = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add New Client", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("New Client Info", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    if (customers.isNotEmpty()) {
                                        TextButton(onClick = { isNewCustomerMode = false }) {
                                            Text("Pick Existing", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = manualCustomerName,
                                    onValueChange = { manualCustomerName = it },
                                    label = { Text("Client Name *") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("new_client_name_input")
                                )

                                OutlinedTextField(
                                    value = manualCustomerPhone,
                                    onValueChange = { manualCustomerPhone = it },
                                    label = { Text("Phone Number") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("new_client_phone_input")
                                )
                            }
                        }
                    }
                }

                // Fish Variety Selector
                item {
                    if (!isManualFishMode && products.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = productDropdownExpanded,
                            onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedProduct?.let { "${it.name} (₹${it.pricePerKg}/kg)" } ?: "Select Fish Type *",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Fish Variety / Species *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("pond_sale_product_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = productDropdownExpanded,
                                onDismissRequest = { productDropdownExpanded = false }
                            ) {
                                products.forEach { prod ->
                                    DropdownMenuItem(
                                        text = { Text("${prod.name} • ₹${prod.pricePerKg}/kg") },
                                        onClick = {
                                            selectedProduct = prod
                                            rateInput = prod.pricePerKg.toString()
                                            productDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { isManualFishMode = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+ Type Other Fish Species", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = manualFishName,
                            onValueChange = { manualFishName = it },
                            label = { Text("Fish Species / Name *") },
                            placeholder = { Text("e.g. Rohu Fish, Catla, Tilapia") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (products.isNotEmpty()) {
                            TextButton(onClick = { isManualFishMode = false }) {
                                Text("Choose from Fish Species List", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Weight (kg) & Pieces
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (kg) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f).testTag("pond_sale_weight_input")
                        )

                        OutlinedTextField(
                            value = piecesInput,
                            onValueChange = { piecesInput = it },
                            label = { Text("Pieces") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.8f).testTag("pond_sale_pieces_input")
                        )
                    }
                }

                // Rate per kg & Date
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = rateInput,
                            onValueChange = { rateInput = it },
                            label = { Text("Rate (₹/kg) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("pond_sale_rate_input")
                        )

                        OutlinedTextField(
                            value = saleDate,
                            onValueChange = { saleDate = it },
                            label = { Text("Date") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("pond_sale_date_input")
                        )
                    }
                }

                // Total Calculation Box
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberAccent.copy(alpha = 0.15f))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Bill Amount:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${String.format("%.2f", calculatedTotal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberAccent
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalCustName = if (isNewCustomerMode || selectedCustomer == null) {
                        manualCustomerName.trim()
                    } else {
                        selectedCustomer!!.name
                    }

                    val finalFishName = if (isManualFishMode || selectedProduct == null) {
                        manualFishName.trim()
                    } else {
                        selectedProduct!!.name
                    }

                    if (isNewCustomerMode && manualCustomerName.isNotBlank()) {
                        onAddNewCustomer(manualCustomerName.trim(), manualCustomerPhone.trim(), "", "")
                    }

                    onConfirm(
                        if (isNewCustomerMode) null else selectedCustomer?.id,
                        finalCustName,
                        if (isManualFishMode) null else selectedProduct?.id,
                        finalFishName,
                        inputPieces,
                        inputWeight,
                        inputRate,
                        saleDate.trim()
                    )
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("save_pond_sale_btn")
            ) {
                Text("Save Sale Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
