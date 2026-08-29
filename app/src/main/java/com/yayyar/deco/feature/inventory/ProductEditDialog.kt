package com.yayyar.deco.feature.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yayyar.deco.core.database.entity.CategoryEntity
import com.yayyar.deco.core.database.entity.ProductEntity
import com.yayyar.deco.core.database.entity.ProductVariantEntity
import com.yayyar.deco.core.database.model.ProductWithVariants
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditDialog(
    productWithVariants: ProductWithVariants? = null,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (ProductEntity, List<ProductVariantEntity>) -> Unit
) {
    val isEditing = productWithVariants != null
    val productId = remember { productWithVariants?.product?.id ?: UUID.randomUUID().toString() }
    var name by remember { mutableStateOf(productWithVariants?.product?.name ?: "") }
    var description by remember { mutableStateOf(productWithVariants?.product?.description ?: "") }
    var selectedCategoryId by remember {
        mutableStateOf(productWithVariants?.product?.categoryId ?: categories.firstOrNull()?.id)
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    data class VariantDraft(
        val id: String = UUID.randomUUID().toString(),
        var size: String = "Free Size",
        var colorPattern: String = "Default",
        var sku: String = "",
        var barcode: String = "",
        var basePrice: String = "0",
        var sellPrice: String = "15000",
        var stockQty: String = "10",
        var lowStockThreshold: String = "5"
    )

    val variants = remember {
        mutableStateListOf<VariantDraft>().apply {
            if (productWithVariants != null && productWithVariants.variants.isNotEmpty()) {
                addAll(
                    productWithVariants.variants.map {
                        VariantDraft(
                            id = it.id,
                            size = it.size,
                            colorPattern = it.colorPattern,
                            sku = it.sku ?: "",
                            barcode = it.barcode ?: "",
                            basePrice = it.basePrice.toInt().toString(),
                            sellPrice = it.sellPrice.toInt().toString(),
                            stockQty = it.stockQty.toString(),
                            lowStockThreshold = it.lowStockThreshold.toString()
                        )
                    }
                )
            } else {
                add(VariantDraft())
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Product" else "Add New Fashion Item",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name (e.g. Floral Dress / ဂါဝန်)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val currentCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Select Category"
                        OutlinedTextField(
                            value = currentCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description / Fabric Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Variants (${variants.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        OutlinedButton(
                            onClick = { variants.add(VariantDraft()) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Add Variant")
                        }
                    }
                }

                itemsIndexed(variants) { index, variant ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Variant #${index + 1}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (variants.size > 1) {
                                    IconButton(onClick = { variants.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Variant", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = variant.size,
                                    onValueChange = { variant.size = it },
                                    label = { Text("Size (S, M, Free)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = variant.colorPattern,
                                    onValueChange = { variant.colorPattern = it },
                                    label = { Text("Pattern/Color (e.g. ကြောင်)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = variant.sellPrice,
                                    onValueChange = { variant.sellPrice = it },
                                    label = { Text("Selling Price (Ks)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = variant.stockQty,
                                    onValueChange = { variant.stockQty = it },
                                    label = { Text("Stock Qty") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = variant.barcode,
                                    onValueChange = { variant.barcode = it },
                                    label = { Text("Barcode / SKU (Optional)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = variant.lowStockThreshold,
                                    onValueChange = { variant.lowStockThreshold = it },
                                    label = { Text("Low Alert Qty") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || variants.isEmpty()) return@Button
                    val product = ProductEntity(
                        id = productId,
                        name = name.trim(),
                        categoryId = selectedCategoryId,
                        description = description.trim().ifBlank { null }
                    )
                    val variantEntities = variants.map { v ->
                        ProductVariantEntity(
                            id = v.id,
                            productId = productId,
                            size = v.size.trim().ifBlank { "Free Size" },
                            colorPattern = v.colorPattern.trim().ifBlank { "Default" },
                            sku = v.sku.trim().ifBlank { null },
                            barcode = v.barcode.trim().ifBlank { null },
                            basePrice = v.basePrice.toDoubleOrNull() ?: 0.0,
                            sellPrice = v.sellPrice.toDoubleOrNull() ?: 0.0,
                            stockQty = v.stockQty.toIntOrNull() ?: 0,
                            lowStockThreshold = v.lowStockThreshold.toIntOrNull() ?: 5
                        )
                    }
                    onSave(product, variantEntities)
                },
                enabled = name.isNotBlank() && variants.all { it.sellPrice.isNotBlank() }
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
