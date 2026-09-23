package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.ProductItem
import com.example.data.model.SaleTransaction
import androidx.compose.material3.Surface
import com.example.ui.components.EditSaleDialog
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary
import com.example.ui.util.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesScreen(
    sales: List<SaleTransaction>,
    products: List<ProductItem>,
    customers: List<Customer>,
    onRecordSale: (
        customerId: Long?,
        customerName: String,
        productId: Long?,
        itemName: String,
        pieces: Int,
        weightKg: Double,
        pricePerKg: Double,
        amountPaid: Double,
        dateString: String
    ) -> Unit,
    onAddCustomer: (name: String, phone: String, address: String, notes: String) -> Unit,
    onUpdateSale: (SaleTransaction) -> Unit = {},
    onDeleteSale: (SaleTransaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showRecordSaleDialog by remember { mutableStateOf(false) }
    var saleToEdit by remember { mutableStateOf<SaleTransaction?>(null) }
    var saleToDelete by remember { mutableStateOf<SaleTransaction?>(null) }
    val strings = LocalAppStrings.current

    val totalSalesRevenue = sales.sumOf { it.totalPrice }
    val totalWeightSold = sales.sumOf { it.weightKg }
    val totalPiecesSold = sales.sumOf { it.pieces }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Sales KPI Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Sales & Customer Orders",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Automatic weight-based pricing in Rupees (₹) & inventory deduction",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(AmberAccent.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AmberAccent.copy(alpha = 0.1f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Total Revenue",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${String.format("%.2f", totalSalesRevenue)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AmberAccent
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TealPrimary.copy(alpha = 0.1f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Weight Sold",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${String.format("%.1f", totalWeightSold)} kg",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TealPrimary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CyanSecondary.copy(alpha = 0.1f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Pieces Sold",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$totalPiecesSold pcs",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CyanSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sales History Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sales History (${sales.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Weight × ₹/kg calculation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Sales List
            if (sales.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No sales recorded yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap + to add customer, enter weight & pieces for auto-pricing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(sales, key = { it.id }) { sale ->
                    SaleTransactionCard(
                        sale = sale,
                        onEdit = { saleToEdit = sale },
                        onDelete = { saleToDelete = sale }
                    )
                }
            }
        }

        // Floating Action Button to record new sale
        FloatingActionButton(
            onClick = { showRecordSaleDialog = true },
            containerColor = AmberAccent,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("record_sale_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Record Sale")
        }
    }

    if (showRecordSaleDialog) {
        RecordSaleDialog(
            products = products,
            customers = customers,
            onDismiss = { showRecordSaleDialog = false },
            onConfirm = { custId, custName, prodId, itemName, pcs, wt, pricePerKg, amountPaid, date ->
                onRecordSale(custId, custName, prodId, itemName, pcs, wt, pricePerKg, amountPaid, date)
                showRecordSaleDialog = false
            },
            onAddCustomer = onAddCustomer
        )
    }

    // Confirm Delete Sale Dialog
    if (saleToDelete != null) {
        val saleItem = saleToDelete!!
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Delete Sale Record", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = if (strings.isHindi) "क्या आप ${saleItem.customerName} को ${saleItem.itemName} की ₹${String.format("%.2f", saleItem.totalPrice)} की बिक्री को हटाना चाहते हैं?" else "Are you sure you want to delete this sale of \"${saleItem.itemName}\" to ${saleItem.customerName} for ₹${String.format("%.2f", saleItem.totalPrice)}?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSale(saleItem)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_sale_btn")
                ) {
                    Text(strings.deleteButton)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { saleToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_sale_btn")
                ) {
                    Text(strings.cancelButton)
                }
            }
        )
    }

    // Edit Sale Dialog
    if (saleToEdit != null) {
        EditSaleDialog(
            sale = saleToEdit!!,
            products = products,
            customers = customers,
            onDismiss = { saleToEdit = null },
            onConfirm = { updated ->
                onUpdateSale(updated)
                saleToEdit = null
            }
        )
    }
}

@Composable
fun SaleTransactionCard(
    sale: SaleTransaction,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sale_item_${sale.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(TealPrimary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = sale.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = sale.dateString,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Calculated total price highlight
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format("%.2f", sale.totalPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberAccent
                    )
                    Text(
                        text = "@ ₹${String.format("%.2f", sale.pricePerKg)}/kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            PaymentStatusBadge(amountPaid = sale.amountPaid, totalPrice = sale.totalPrice)
            Spacer(modifier = Modifier.height(6.dp))

            // Item and weight metrics + Edit/Delete actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sale.itemName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TealPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${String.format("%.2f", sale.weightKg)} kg",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanSecondary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${sale.pieces} pcs",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Edit Sale Action
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("edit_sale_${sale.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Sale",
                            tint = TealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Delete Sale Action
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("delete_sale_${sale.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Sale",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentStatusBadge(amountPaid: Double, totalPrice: Double) {
    val dueAmount = totalPrice - amountPaid

    val (backgroundColor, textColor, text) = when {
        amountPaid >= totalPrice -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Paid / पूरा भुगतान")
        amountPaid > 0 -> Triple(Color(0xFFFFF3E0), Color(0xFFEF6C00), "Partial / आंशिक: ₹$amountPaid, Due: ₹${"%.2f".format(dueAmount)}")
        else -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Unpaid / बाकी: ₹${"%.2f".format(dueAmount)}")
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSaleDialog(
    products: List<ProductItem>,
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (
        customerId: Long?,
        customerName: String,
        productId: Long?,
        itemName: String,
        pieces: Int,
        weight: Double,
        pricePerKg: Double,
        amountPaid: Double,
        date: String
    ) -> Unit,
    onAddCustomer: (name: String, phone: String, address: String, notes: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedCustomer by remember { mutableStateOf<Customer?>(customers.firstOrNull()) }
    var manualCustomerName by remember { mutableStateOf("") }
    var isNewCustomerMode by remember { mutableStateOf(false) }
    var newCustomerPhone by remember { mutableStateOf("") }
    var newCustomerAddress by remember { mutableStateOf("") }

    var selectedProduct by remember { mutableStateOf<ProductItem?>(products.firstOrNull()) }
    var manualItemName by remember { mutableStateOf("") }
    var manualPricePerKgText by remember { mutableStateOf("") }

    var piecesText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf(today) }

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    // Live Automatic Price Calculation on the basis of weight
    val inputWeight = weightText.toDoubleOrNull() ?: 0.0
    val inputPieces = piecesText.toIntOrNull() ?: 0
    val activePricePerKg = selectedProduct?.pricePerKg ?: manualPricePerKgText.toDoubleOrNull() ?: 0.0
    val calculatedTotalPrice = inputWeight * activePricePerKg

    // Inventory Deduction Preview
    val availablePieces = selectedProduct?.stockPieces ?: 0
    val availableWeight = selectedProduct?.stockWeightKg ?: 0.0
    val remainingPieces = availablePieces - inputPieces
    val remainingWeight = availableWeight - inputWeight
    val isWeightExceeded = selectedProduct != null && remainingWeight < 0

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
                Text(strings.recordCustomerSaleTitle, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Customer Selector or Creator
                if (!isNewCustomerMode) {
                    ExposedDropdownMenuBox(
                        expanded = customerDropdownExpanded,
                        onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: (if (strings.isHindi) "ग्राहक चुनें या जोड़ें" else "Select or add customer"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.clientLabelText) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("sale_customer_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = customerDropdownExpanded,
                            onDismissRequest = { customerDropdownExpanded = false }
                        ) {
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { Text("${customer.name} (${customer.phoneNumber})") },
                                    onClick = {
                                        selectedCustomer = customer
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
                            Text(strings.addNewClientBtnText, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    // Inline New Customer Fields
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
                                Text(strings.newCustomerDetailsTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                TextButton(onClick = { isNewCustomerMode = false }) {
                                    Text(strings.pickExistingLabelText, style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            OutlinedTextField(
                                value = manualCustomerName,
                                onValueChange = { manualCustomerName = it },
                                label = { Text(strings.customerNameLabelText) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("new_customer_name_input")
                            )

                            OutlinedTextField(
                                value = newCustomerPhone,
                                onValueChange = { newCustomerPhone = it },
                                label = { Text(strings.phoneNumberLabelText) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("new_customer_phone_input")
                            )

                            OutlinedTextField(
                                value = newCustomerAddress,
                                onValueChange = { newCustomerAddress = it },
                                label = { Text(strings.addressLabelText) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("new_customer_address_input")
                            )
                        }
                    }
                }

                // Product Selector from Main Profile
                ExposedDropdownMenuBox(
                    expanded = productDropdownExpanded,
                    onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.let { "${it.name} (₹${it.pricePerKg}/kg)" } ?: (if (strings.isHindi) "प्रोफ़ाइल से उत्पाद चुनें" else "Select product from profile"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.productCommodityLabelText) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("sale_product_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = productDropdownExpanded,
                        onDismissRequest = { productDropdownExpanded = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${prod.name} • ₹${prod.pricePerKg}/kg", fontWeight = FontWeight.SemiBold)
                                        Text("Stock: ${prod.stockPieces} pcs, ${String.format("%.1f", prod.stockWeightKg)} kg", style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                onClick = {
                                    selectedProduct = prod
                                    productDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Pieces and Weight Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = piecesText,
                        onValueChange = { piecesText = it },
                        label = { Text(strings.piecesLabel) },
                        placeholder = { Text(if (strings.isHindi) "उदा. 10" else "e.g. 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sale_pieces_input")
                    )

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text(strings.weightLabel) },
                        placeholder = { Text(if (strings.isHindi) "उदा. 25.5" else "e.g. 25.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sale_weight_input")
                    )
                }

                // Live Price Calculation & Inventory Deduction Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AmberAccent.copy(alpha = 0.12f))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (strings.isHindi) "स्वचालित मूल्य (${String.format("%.2f", inputWeight)} किग्रा × ₹${String.format("%.2f", activePricePerKg)}):" else "Auto Price (${String.format("%.2f", inputWeight)} kg × ₹${String.format("%.2f", activePricePerKg)}):",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "₹${String.format("%.2f", calculatedTotalPrice)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberAccent
                            )
                        }

                        if (selectedProduct != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isWeightExceeded) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = DangerRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (strings.isHindi) "स्टॉक से अधिक: ${String.format("%.1f", availableWeight)} किग्रा उपलब्ध है" else "Exceeds stock: ${String.format("%.1f", availableWeight)} kg available",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DangerRed
                                    )
                                } else {
                                    Text(
                                        text = if (strings.isHindi) "इन्वेंट्री कटौती: ${availablePieces} → ${remainingPieces.coerceAtLeast(0)} पीस, ${String.format("%.1f", availableWeight)} → ${String.format("%.1f", remainingWeight.coerceAtLeast(0.0))} किग्रा" else "Inventory Deduction: ${availablePieces} → ${remainingPieces.coerceAtLeast(0)} pcs, ${String.format("%.1f", availableWeight)} → ${String.format("%.1f", remainingWeight.coerceAtLeast(0.0))} kg",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val custName = if (isNewCustomerMode) manualCustomerName.trim() else selectedCustomer?.name ?: "Walk-in Buyer"
                    val custId = if (isNewCustomerMode) {
                        onAddCustomer(manualCustomerName.trim(), newCustomerPhone.trim(), newCustomerAddress.trim(), "")
                        null
                    } else selectedCustomer?.id

                    val prodName = selectedProduct?.name ?: manualItemName.ifEmpty { "General Commodity" }
                    val prodId = selectedProduct?.id
                    val amountPaid = 0.0

                    onConfirm(
                        custId,
                        custName,
                        prodId,
                        prodName,
                        inputPieces,
                        inputWeight,
                        activePricePerKg,
                        amountPaid,
                        dateString
                    )
                },
                enabled = inputWeight > 0 && activePricePerKg > 0 &&
                        (if (isNewCustomerMode) manualCustomerName.isNotBlank() else selectedCustomer != null),
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_record_sale_btn")
            ) {
                Text(strings.confirmSaleButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelButton)
            }
        }
    )
}
