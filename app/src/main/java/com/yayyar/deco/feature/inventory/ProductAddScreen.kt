package com.yayyar.deco.feature.inventory

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
fun ProductAddScreen(
    productWithVariants: ProductWithVariants? = null,
    categories: List<CategoryEntity>,
    onBack: () -> Unit,
    onSave: (ProductEntity, List<ProductVariantEntity>) -> Unit,
    onDelete: ((ProductEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val isEditing = productWithVariants != null
    val productId = remember { productWithVariants?.product?.id ?: UUID.randomUUID().toString() }
    var name by remember { mutableStateOf(productWithVariants?.product?.name ?: "") }
    var description by remember { mutableStateOf(productWithVariants?.product?.description ?: "") }
    var selectedCategoryId by remember {
        mutableStateOf(productWithVariants?.product?.categoryId ?: categories.firstOrNull()?.id)
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    data class VariantDraft(
        val id: String = UUID.randomUUID().toString(),
        val size: String = "Free Size",
        val colorPattern: String = "Default",
        val sku: String = "",
        val barcode: String = "",
        val basePrice: String = "0",
        val sellPrice: String = "15000",
        val wholesalePrice: String = "13000",
        val stockQty: String = "10",
        val lowStockThreshold: String = "5"
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
                            wholesalePrice = (if (it.wholesalePrice > 0) it.wholesalePrice else it.sellPrice).toInt().toString(),
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

    val isFormValid = name.isNotBlank() && variants.isNotEmpty() && variants.all { it.sellPrice.isNotBlank() }

    val handleSave = {
        if (isFormValid) {
            val product = ProductEntity(
                id = productId,
                name = name.trim(),
                categoryId = selectedCategoryId,
                description = description.trim().ifBlank { null }
            )
            val variantEntities = variants.map { v ->
                val retail = v.sellPrice.toDoubleOrNull() ?: 0.0
                val wholesale = v.wholesalePrice.toDoubleOrNull() ?: retail
                ProductVariantEntity(
                    id = v.id,
                    productId = productId,
                    size = v.size.trim().ifBlank { "Free Size" },
                    colorPattern = v.colorPattern.trim().ifBlank { "Default" },
                    sku = v.sku.trim().ifBlank { null },
                    barcode = v.barcode.trim().ifBlank { null },
                    basePrice = v.basePrice.toDoubleOrNull() ?: 0.0,
                    sellPrice = retail,
                    wholesalePrice = wholesale,
                    stockQty = v.stockQty.toIntOrNull() ?: 0,
                    lowStockThreshold = v.lowStockThreshold.toIntOrNull() ?: 5
                )
            }
            onSave(product, variantEntities)
            onBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Product" else "New Product",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isEditing && onDelete != null) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Product",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = handleSave,
                        enabled = isFormValid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isEditing) "Update" else "Save")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Basic Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it }
                ) {
                    val currentCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Select Category"
                    OutlinedTextField(
                        value = currentCategoryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
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
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Product Variants (${variants.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Sizes, colors/patterns, pricing & inventory stock",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { variants.add(VariantDraft()) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", maxLines = 1)
                    }
                }
            }

            itemsIndexed(variants, key = { _, v -> v.id }) { index, variant ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Variant #${index + 1}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                            if (variants.size > 1) {
                                IconButton(onClick = { variants.removeAt(index) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Variant",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = variant.size,
                                onValueChange = { variants[index] = variant.copy(size = it) },
                                label = { Text("Size (S, M, Free)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = variant.colorPattern,
                                onValueChange = { variants[index] = variant.copy(colorPattern = it) },
                                label = { Text("Color / Pattern") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = variant.sellPrice,
                                onValueChange = { newPrice ->
                                    val newWholesale = if (variant.wholesalePrice.isBlank() || variant.wholesalePrice == variant.sellPrice) {
                                        newPrice
                                    } else {
                                        variant.wholesalePrice
                                    }
                                    variants[index] = variant.copy(
                                        sellPrice = newPrice,
                                        wholesalePrice = newWholesale
                                    )
                                },
                                label = { Text("Retail Price (Ks) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = variant.wholesalePrice,
                                onValueChange = { variants[index] = variant.copy(wholesalePrice = it) },
                                label = { Text("Wholesale Price (Ks)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = variant.stockQty,
                                onValueChange = { variants[index] = variant.copy(stockQty = it) },
                                label = { Text("Stock Qty") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = variant.lowStockThreshold,
                                onValueChange = { variants[index] = variant.copy(lowStockThreshold = it) },
                                label = { Text("Low Alert Qty") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = variant.barcode,
                            onValueChange = { variants[index] = variant.copy(barcode = it) },
                            label = { Text("Barcode / SKU") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showDeleteConfirmation && productWithVariants != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete '${productWithVariants.product.name}' and all its variants?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete?.invoke(productWithVariants.product)
                        onBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
