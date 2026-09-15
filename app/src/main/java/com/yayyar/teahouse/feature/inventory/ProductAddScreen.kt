package com.yayyar.teahouse.feature.inventory

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.derivedStateOf
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
import com.yayyar.teahouse.core.database.entity.CategoryEntity
import com.yayyar.teahouse.core.database.entity.ProductEntity
import androidx.compose.ui.graphics.Color
import com.yayyar.teahouse.core.database.entity.ProductVariantEntity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import com.yayyar.teahouse.core.common.ImageStorageHelper
import com.yayyar.teahouse.core.ui.components.ProductThumbnail
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.yayyar.teahouse.R
import com.yayyar.teahouse.core.database.model.ProductWithVariants
import java.io.File
import java.util.UUID

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
    val lowStockThreshold: String = "5",
    val imageUri: String? = null
)

private val NumberKeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
private val CardShape = RoundedCornerShape(12.dp)
private val FieldShape = RoundedCornerShape(8.dp)

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isEditing = productWithVariants != null
    val productId = remember { productWithVariants?.product?.id ?: UUID.randomUUID().toString() }
    var name by remember { mutableStateOf(productWithVariants?.product?.name ?: "") }
    var description by remember { mutableStateOf(productWithVariants?.product?.description ?: "") }
    var imageUri by remember { mutableStateOf(productWithVariants?.product?.imageUri) }
    var selectedCategoryId by remember {
        mutableStateOf(productWithVariants?.product?.categoryId ?: categories.firstOrNull()?.id)
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Launcher for main product image
    val productPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val savedPath = ImageStorageHelper.saveImageFromUri(context, it, prefix = "prod_${productId.take(8)}")
                if (savedPath != null) {
                    imageUri = savedPath
                }
            }
        }
    }

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
                            lowStockThreshold = it.lowStockThreshold.toString(),
                            imageUri = it.imageUri
                        )
                    }
                )
            } else {
                add(VariantDraft())
            }
        }
    }

    val isFormValid by remember {
        derivedStateOf {
            name.isNotBlank() && variants.isNotEmpty() && variants.all { it.sellPrice.isNotBlank() }
        }
    }

    val handleSave = {
        if (isFormValid) {
            val product = ProductEntity(
                id = productId,
                name = name.trim(),
                categoryId = selectedCategoryId,
                description = description.trim().ifBlank { null },
                imageUri = imageUri
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
                    lowStockThreshold = v.lowStockThreshold.toIntOrNull() ?: 5,
                    imageUri = v.imageUri
                )
            }
            onSave(product, variantEntities)
            onBack()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) stringResource(R.string.inv_edit_product) else stringResource(R.string.inv_add_product),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    if (isEditing && onDelete != null) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.action_delete),
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
                        shape = CardShape
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Button(
                        onClick = handleSave,
                        enabled = isFormValid,
                        modifier = Modifier.weight(1f),
                        shape = CardShape
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(contentType = "section_header") {
                Text(
                    text = "Basic Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Main Product Photo Picker Card
            item(contentType = "product_image_picker") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .clickable {
                            productPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    shape = CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    if (!imageUri.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = if (imageUri!!.startsWith("http") || imageUri!!.startsWith("content://")) imageUri else File(imageUri!!),
                                contentDescription = "Product Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Overlay action buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        productPhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Change Photo", color = Color.White, fontSize = 12.sp)
                                }

                                IconButton(
                                    onClick = { imageUri = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Remove Photo",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Add Photo",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "Add Main Product Photo",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            item(contentType = "product_name") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("${stringResource(R.string.inv_product_name)} *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = CardShape
                )
            }

            item(contentType = "category_dropdown") {
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it }
                ) {
                    val currentCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: stringResource(R.string.inv_select_category)
                    OutlinedTextField(
                        value = currentCategoryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.inv_category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = CardShape
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

            item(contentType = "product_description") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.inv_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = CardShape
                )
            }

            item(contentType = "variants_header") {
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
                            text = "Sizes, colors/patterns, photos, pricing & inventory stock",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { variants.add(VariantDraft()) },
                        shape = FieldShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", maxLines = 1)
                    }
                }
            }

            itemsIndexed(
                items = variants,
                key = { _, v -> v.id },
                contentType = { _, _ -> "variant_card" }
            ) { index, variant ->
                VariantCardItem(
                    index = index,
                    totalVariants = variants.size,
                    variant = variant,
                    fallbackProductImageUri = imageUri,
                    onUpdate = { updatedVariant ->
                        variants[index] = updatedVariant
                    },
                    onDelete = {
                        variants.removeAt(index)
                    }
                )
            }
        }
    }

    if (showDeleteConfirmation && productWithVariants != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.inv_delete_product_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete?.invoke(productWithVariants.product)
                        onBack()
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun VariantCardItem(
    index: Int,
    totalVariants: Int,
    variant: VariantDraft,
    fallbackProductImageUri: String? = null,
    onUpdate: (VariantDraft) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val variantPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val savedPath = ImageStorageHelper.saveImageFromUri(
                    context,
                    it,
                    prefix = "var_${variant.id.take(8)}"
                )
                if (savedPath != null) {
                    onUpdate(variant.copy(imageUri = savedPath))
                }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
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
                if (totalVariants > 1) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Variant",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Variant Photo Picker + Size/Color row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Compact Variant Photo Picker Thumbnail (64x64dp)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .clickable {
                            variantPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!variant.imageUri.isNullOrBlank()) {
                        ProductThumbnail(
                            imageUri = variant.imageUri,
                            size = 64.dp,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxSize()
                        )
                        // Small remove button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable { onUpdate(variant.copy(imageUri = null)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Remove variant photo",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add Variant Photo",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Photo",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = variant.size,
                        onValueChange = { onUpdate(variant.copy(size = it)) },
                        label = { Text(stringResource(R.string.pos_size_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FieldShape
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = variant.colorPattern,
                        onValueChange = { onUpdate(variant.copy(colorPattern = it)) },
                        label = { Text(stringResource(R.string.pos_color_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FieldShape
                    )
                }
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
                        onUpdate(
                            variant.copy(
                                sellPrice = newPrice,
                                wholesalePrice = newWholesale
                            )
                        )
                    },
                    label = { Text("${stringResource(R.string.inv_sell_price)} *") },
                    keyboardOptions = NumberKeyboardOptions,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = FieldShape
                )
                OutlinedTextField(
                    value = variant.wholesalePrice,
                    onValueChange = { onUpdate(variant.copy(wholesalePrice = it)) },
                    label = { Text(stringResource(R.string.inv_wholesale_price)) },
                    keyboardOptions = NumberKeyboardOptions,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = FieldShape
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = variant.stockQty,
                    onValueChange = { onUpdate(variant.copy(stockQty = it)) },
                    label = { Text(stringResource(R.string.inv_stock_qty)) },
                    keyboardOptions = NumberKeyboardOptions,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = FieldShape
                )
                OutlinedTextField(
                    value = variant.lowStockThreshold,
                    onValueChange = { onUpdate(variant.copy(lowStockThreshold = it)) },
                    label = { Text(stringResource(R.string.inv_low_stock_threshold)) },
                    keyboardOptions = NumberKeyboardOptions,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = FieldShape
                )
            }

            OutlinedTextField(
                value = variant.barcode,
                onValueChange = { onUpdate(variant.copy(barcode = it)) },
                label = { Text(stringResource(R.string.inv_barcode)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = FieldShape
            )
        }
    }
}
