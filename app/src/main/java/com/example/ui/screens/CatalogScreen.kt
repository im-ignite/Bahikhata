package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriceCheck
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
import com.example.data.model.GoogleAccountInfo
import com.example.data.model.ProductItem
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary
import com.example.ui.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<ProductItem>,
    googleAccount: GoogleAccountInfo,
    onAddProduct: (name: String, pricePerKg: Double, pieces: Int, weightKg: Double, category: String) -> Unit,
    onUpdateProduct: (ProductItem) -> Unit,
    onDeleteProduct: (ProductItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductItem?>(null) }

    // Quick Calculator tool state on profile
    var calcSelectedProduct by remember { mutableStateOf<ProductItem?>(products.firstOrNull()) }
    var calcPiecesText by remember { mutableStateOf("10") }
    var calcWeightText by remember { mutableStateOf("25.0") }
    var calcDropdownExpanded by remember { mutableStateOf(false) }

    val calcWeight = calcWeightText.toDoubleOrNull() ?: 0.0
    val calcPricePerKg = calcSelectedProduct?.pricePerKg ?: 0.0
    val calcResultPrice = calcWeight * calcPricePerKg

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Products & Business Info Header
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(TealPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = googleAccount.displayName.ifEmpty { strings.productsCatalogTitle },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = googleAccount.email.ifEmpty { strings.productsSubtitle },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.syncStatusDriveSynced,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Weight-based Pricing Calculator Tool on Products (Requirements 5 & 6)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(AmberAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = strings.liveCalculatorTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.liveCalculatorSub,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Product Selector
                        ExposedDropdownMenuBox(
                            expanded = calcDropdownExpanded,
                            onExpandedChange = { calcDropdownExpanded = !calcDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = calcSelectedProduct?.let { "${it.name} (@ ₹${it.pricePerKg}/kg)" }
                                    ?: if (strings.isHindi) "गणना के लिए उत्पाद चुनें" else "Select product to calculate",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.sellingProductLabel) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = calcDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("calc_product_picker")
                            )

                            ExposedDropdownMenu(
                                expanded = calcDropdownExpanded,
                                onDismissRequest = { calcDropdownExpanded = false }
                            ) {
                                products.forEach { prod ->
                                    DropdownMenuItem(
                                        text = { Text("${prod.name} • ₹${prod.pricePerKg}/kg") },
                                        onClick = {
                                            calcSelectedProduct = prod
                                            calcDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = calcPiecesText,
                                onValueChange = { calcPiecesText = it },
                                label = { Text(strings.piecesLabel) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("calc_pieces_input")
                            )

                            OutlinedTextField(
                                value = calcWeightText,
                                onValueChange = { calcWeightText = it },
                                label = { Text(strings.weightLabel) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("calc_weight_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Calculation Output
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AmberAccent.copy(alpha = 0.15f))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Formula: ${String.format("%.2f", calcWeight)} kg × ₹${String.format("%.2f", calcPricePerKg)}/kg",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Auto Price:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "₹${String.format("%.2f", calcResultPrice)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmberAccent
                                )
                            }
                        }
                    }
                }
            }

            // Products Catalog Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${strings.productsCatalogTitle} (${products.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = strings.priceBasisWeightOnly,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Products List
            items(products, key = { it.id }) { product ->
                ProductCatalogCard(
                    product = product,
                    onEdit = { productToEdit = product },
                    onDelete = { onDeleteProduct(product) }
                )
            }
        }

        // FAB to add product
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = TealPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_product_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = strings.addProductButton)
        }
    }

    // Add Product Dialog
    if (showAddDialog) {
        AddEditProductDialog(
            product = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, pricePerKg, pcs, wt, cat ->
                onAddProduct(name, pricePerKg, pcs, wt, cat)
                showAddDialog = false
            }
        )
    }

    // Edit Product Dialog
    productToEdit?.let { prod ->
        AddEditProductDialog(
            product = prod,
            onDismiss = { productToEdit = null },
            onConfirm = { name, pricePerKg, pcs, wt, cat ->
                onUpdateProduct(prod.copy(
                    name = name,
                    pricePerKg = pricePerKg,
                    stockPieces = pcs,
                    stockWeightKg = wt,
                    category = cat
                ))
                productToEdit = null
            }
        )
    }
}

@Composable
fun ProductCatalogCard(
    product: ProductItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_item_${product.id}"),
        shape = RoundedCornerShape(16.dp),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Category: ${product.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TealPrimary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Price basis badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "₹${String.format("%.2f", product.pricePerKg)} / kg",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberAccent
                    )
                }

                // Pieces in stock
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanSecondary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${product.stockPieces} pcs in stock",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanSecondary
                    )
                }

                // Weight in stock
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", product.stockWeightKg)} kg in stock",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    product: ProductItem?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, pricePerKg: Double, pieces: Int, weight: Double, category: String) -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(product?.name ?: "") }
    var pricePerKgText by remember { mutableStateOf(product?.pricePerKg?.toString() ?: "") }
    var piecesText by remember { mutableStateOf(product?.stockPieces?.toString() ?: "100") }
    var weightText by remember { mutableStateOf(product?.stockWeightKg?.toString() ?: "200.0") }
    var category by remember { mutableStateOf(product?.category ?: "Metals") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) (if (strings.isHindi) "मछली किस्म और दर जोड़ें" else "Add Fish Species") else (if (strings.isHindi) "मछली दर संपादित करें" else "Edit Fish Rate"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (strings.isHindi) "बिक्री और इन्वेंट्री कटौती के दौरान स्वचालित गणना के लिए दर केवल वजन के आधार पर (₹/किग्रा) जोड़ी जाती है।" else "Price is added on weight basis only (₹/kg) for automatic calculation during sales and inventory deduction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.itemNameLabelText) },
                    placeholder = { Text(strings.itemNamePlaceholderText) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                )

                OutlinedTextField(
                    value = pricePerKgText,
                    onValueChange = { pricePerKgText = it },
                    label = { Text(strings.priceOnWeightBasisLabel) },
                    placeholder = { Text(strings.pricePlaceholderText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("product_price_per_kg_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = piecesText,
                        onValueChange = { piecesText = it },
                        label = { Text(strings.stockPiecesLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("product_stock_pcs_input")
                    )

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text(strings.stockWeightLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("product_stock_weight_input")
                    )
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(strings.categoryLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("product_category_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = pricePerKgText.toDoubleOrNull() ?: 0.0
                    val pcs = piecesText.toIntOrNull() ?: 0
                    val wt = weightText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && price > 0) {
                        onConfirm(name.trim(), price, pcs, wt, category.trim())
                    }
                },
                enabled = name.isNotBlank() && (pricePerKgText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_save_product_btn")
            ) {
                Text(strings.saveToProfileButton)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelButton)
            }
        }
    )
}
