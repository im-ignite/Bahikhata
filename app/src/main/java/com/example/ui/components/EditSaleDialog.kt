package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Customer
import com.example.data.model.ProductItem
import com.example.data.model.SaleTransaction
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSaleDialog(
    sale: SaleTransaction,
    products: List<ProductItem> = emptyList(),
    customers: List<Customer> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (SaleTransaction) -> Unit
) {
    var customerName by remember { mutableStateOf(sale.customerName) }
    var selectedCustomerId by remember { mutableStateOf(sale.customerId) }
    var itemName by remember { mutableStateOf(sale.itemName) }
    var selectedProductId by remember { mutableStateOf(sale.productId) }
    var piecesText by remember { mutableStateOf(sale.pieces.toString()) }
    var weightText by remember { mutableStateOf(sale.weightKg.toString()) }
    var pricePerKgText by remember { mutableStateOf(sale.pricePerKg.toString()) }
    var dateString by remember { mutableStateOf(sale.dateString) }

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    val inputWeight = weightText.toDoubleOrNull() ?: 0.0
    val inputPricePerKg = pricePerKgText.toDoubleOrNull() ?: 0.0
    val calculatedTotalPrice = inputWeight * inputPricePerKg

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Sale Record",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Modify sale metrics, client details, weight, and price in Rupees (₹).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Customer selection / edit
                if (customers.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = customerDropdownExpanded,
                        onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {
                                customerName = it
                                selectedCustomerId = null
                            },
                            label = { Text("Client / Customer Name *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("edit_sale_customer_input")
                        )

                        ExposedDropdownMenu(
                            expanded = customerDropdownExpanded,
                            onDismissRequest = { customerDropdownExpanded = false }
                        ) {
                            customers.forEach { cust ->
                                DropdownMenuItem(
                                    text = { Text("${cust.name} (${cust.phoneNumber})") },
                                    onClick = {
                                        customerName = cust.name
                                        selectedCustomerId = cust.id
                                        customerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Client / Customer Name *") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_sale_customer_input")
                    )
                }

                // Item Name / Product selection
                if (products.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = productDropdownExpanded,
                        onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = {
                                itemName = it
                                selectedProductId = null
                            },
                            label = { Text("Item / Commodity *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("edit_sale_item_input")
                        )

                        ExposedDropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false }
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text("${prod.name} • ₹${prod.pricePerKg}/kg") },
                                    onClick = {
                                        itemName = prod.name
                                        selectedProductId = prod.id
                                        pricePerKgText = prod.pricePerKg.toString()
                                        productDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name *") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_sale_item_input")
                    )
                }

                // Pieces and Weight Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = piecesText,
                        onValueChange = { piecesText = it },
                        label = { Text("Pieces *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_sale_pieces_input")
                    )

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (kg) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_sale_weight_input")
                    )
                }

                // Price per kg in Rupees (₹)
                OutlinedTextField(
                    value = pricePerKgText,
                    onValueChange = { pricePerKgText = it },
                    label = { Text("Price per kg (₹ / kg) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_sale_price_per_kg_input")
                )

                // Date
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_sale_date_input")
                )

                // Live price calculation in Rupees
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberAccent.copy(alpha = 0.12f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Auto Price (${String.format("%.2f", inputWeight)} kg × ₹${String.format("%.2f", inputPricePerKg)}):",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Updated Total:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "₹${String.format("%.2f", calculatedTotalPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberAccent
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pieces = piecesText.toIntOrNull() ?: sale.pieces
                    val weight = weightText.toDoubleOrNull() ?: sale.weightKg
                    val pricePerKg = pricePerKgText.toDoubleOrNull() ?: sale.pricePerKg

                    if (customerName.isNotBlank() && itemName.isNotBlank() && weight > 0 && pricePerKg > 0) {
                        val updated = sale.copy(
                            customerId = selectedCustomerId,
                            customerName = customerName.trim(),
                            productId = selectedProductId,
                            itemName = itemName.trim(),
                            pieces = pieces,
                            weightKg = weight,
                            pricePerKg = pricePerKg,
                            totalPrice = weight * pricePerKg,
                            dateString = dateString.trim().ifEmpty { sale.dateString },
                            isSynced = false
                        )
                        onConfirm(updated)
                    }
                },
                enabled = customerName.isNotBlank() &&
                        itemName.isNotBlank() &&
                        (weightText.toDoubleOrNull() ?: 0.0) > 0 &&
                        (pricePerKgText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_edit_sale_btn")
            ) {
                Text("Update Sale")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_edit_sale_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}
