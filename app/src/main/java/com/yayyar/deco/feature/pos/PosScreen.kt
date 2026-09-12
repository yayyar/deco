package com.yayyar.deco.feature.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yayyar.deco.core.common.Formatters
import com.yayyar.deco.core.common.Resource
import com.yayyar.deco.core.database.entity.ProductVariantEntity
import com.yayyar.deco.core.database.model.ProductWithVariants
import com.yayyar.deco.core.printer.ReceiptData
import com.yayyar.deco.core.ui.components.CurrencyText
import com.yayyar.deco.core.ui.components.ProductThumbnail
import com.yayyar.deco.core.ui.components.StockBadge
import com.yayyar.deco.ui.theme.AccentGreen
import com.yayyar.deco.ui.theme.AccentRed
import kotlinx.coroutines.launch

@Composable
fun PosScreen(
    viewModel: PosViewModel,
    isGridView: Boolean = true,
    topBar: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val catalogProducts by viewModel.catalogProducts.collectAsState()
    val activePaymentMethods by viewModel.activePaymentMethods.collectAsState()
    val cartState by viewModel.cartState.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()

    var showCheckoutDialog by remember { mutableStateOf(false) }
    var selectedProductForVariants by remember { mutableStateOf<ProductWithVariants?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val configuration = LocalConfiguration.current
        val isTablet = configuration.smallestScreenWidthDp >= 530
        val isTabletLandscape = isTablet && maxWidth >= 720.dp

        if (isTabletLandscape) {
            // Tablet 2-Pane Master-Detail Layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left 65%: TopAppBar + Catalog
                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 0.dp, end = 3.dp, top = 0.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        topBar()
                    }
                    CatalogPane(
                        categories = categories,
                        selectedCatId = selectedCatId,
                        catalogProducts = catalogProducts,
                        saleType = cartState.saleType,
                        isGridView = isGridView,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onProductClick = { selectedProductForVariants = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                    )
                }

                // Right 35%: Cart & Checkout Summary (Aligned with TopAppBar, below Status Bar)
                CartPane(
                    cartState = cartState,
                    onSetSaleType = { viewModel.setSaleType(it) },
                    onUpdateQty = { variantId, qty -> viewModel.updateCartItemQuantity(variantId, qty) },
                    onRemoveItem = { viewModel.removeCartItem(it) },
                    onClearCart = { viewModel.clearCart() },
                    onSetDiscount = { type, value -> viewModel.setDiscount(type, value) },
                    onSetDeliFee = { viewModel.setDeliFee(it) },
                    onInitiateCheckout = { showCheckoutDialog = true },
                    onSaveDraft = { viewModel.saveDraftSale() },
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.End))
                        .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                )
            }
        } else {
            // Mobile Layout with Sticky Bottom Cart Bar
            MobilePosLayout(
                categories = categories,
                selectedCatId = selectedCatId,
                catalogProducts = catalogProducts,
                cartState = cartState,
                isGridView = isGridView,
                onSelectCategory = { viewModel.selectCategory(it) },
                onProductClick = { selectedProductForVariants = it },
                onSetSaleType = { viewModel.setSaleType(it) },
                onUpdateQty = { variantId, qty -> viewModel.updateCartItemQuantity(variantId, qty) },
                onRemoveItem = { viewModel.removeCartItem(it) },
                onClearCart = { viewModel.clearCart() },
                onSetDiscount = { type, value -> viewModel.setDiscount(type, value) },
                onSetDeliFee = { viewModel.setDeliFee(it) },
                onInitiateCheckout = { showCheckoutDialog = true },
                onSaveDraft = { viewModel.saveDraftSale() }
            )
        }
    }

    if (showCheckoutDialog) {
        CheckoutDialog(
            cartState = cartState,
            activePaymentMethods = activePaymentMethods,
            onDismiss = { showCheckoutDialog = false },
            onConfirmCheckout = { pType, cashRec, kpayAmt, waveAmt, notes ->
                showCheckoutDialog = false
                viewModel.performCheckout(pType, cashRec, kpayAmt, waveAmt, notes)
            }
        )
    }

    selectedProductForVariants?.let { productWithVariants ->
        ProductVariantSelectionDialog(
            productWithVariants = productWithVariants,
            saleType = cartState.saleType,
            onDismiss = { selectedProductForVariants = null },
            onSelectVariant = { variant ->
                viewModel.addToCart(productWithVariants.product, variant)
                selectedProductForVariants = null
            }
        )
    }

    when (val state = checkoutState) {
        is Resource.Loading -> {
            androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text("Deducting inventory and completing sale...")
                    }
                }
            }
        }

        is Resource.Success -> {
            ReceiptSuccessDialog(
                receiptData = state.data,
                onDismiss = { viewModel.dismissCheckoutState() },
                onPrint = { viewModel.printThermalReceipt(it) },
                onShareSlip = { viewModel.shareReceipt(it) }
            )
        }

        is Resource.Error -> {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.dismissCheckoutState() },
                title = { Text("Checkout Error", fontWeight = FontWeight.Bold, color = AccentRed) },
                text = { Text(state.message) },
                confirmButton = {
                    Button(onClick = { viewModel.dismissCheckoutState() }) {
                        Text("OK")
                    }
                }
            )
        }

        null -> {}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CatalogPane(
    categories: List<com.yayyar.deco.core.database.entity.CategoryEntity>,
    selectedCatId: String?,
    catalogProducts: List<ProductWithVariants>,
    saleType: SaleType = SaleType.RETAIL,
    isGridView: Boolean = true,
    onSelectCategory: (String?) -> Unit,
    onProductClick: (ProductWithVariants) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Category Filter Chips
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCatId == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text("All / အားလုံး") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                items(categories, key = { it.id }) { cat ->
                    FilterChip(
                        selected = selectedCatId == cat.id,
                        onClick = { onSelectCategory(cat.id) },
                        label = { Text(cat.name) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Product Catalog (Grid or List Layout based on Global Setting)
        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(catalogProducts, key = { it.product.id }) { item ->
                    ProductCatalogCard(
                        productWithVariants = item,
                        saleType = saleType,
                        onClick = { onProductClick(item) }
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(catalogProducts, key = { it.product.id }) { item ->
                    ProductCatalogCard(
                        productWithVariants = item,
                        saleType = saleType,
                        onClick = { onProductClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductCatalogCard(
    productWithVariants: ProductWithVariants,
    saleType: SaleType = SaleType.RETAIL,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minPrice = if (saleType == SaleType.WHOLESALE) productWithVariants.minWholesalePrice else productWithVariants.minPrice
    val maxPrice = if (saleType == SaleType.WHOLESALE) productWithVariants.maxWholesalePrice else productWithVariants.maxPrice

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Product Thumbnail (falls back to first variant photo)
            val firstVariantImg = productWithVariants.variants.firstOrNull { !it.imageUri.isNullOrBlank() }?.imageUri
            if(!productWithVariants.product.imageUri.isNullOrBlank() || !firstVariantImg.isNullOrBlank()){
                ProductThumbnail(
                    imageUri = productWithVariants.product.imageUri,
                    fallbackImageUri = firstVariantImg,
                    size = 56.dp,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = productWithVariants.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2
                )
                if (productWithVariants.category != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = productWithVariants.category.name,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(6.dp))

                if (productWithVariants.variants.isNotEmpty()) {
                    if (minPrice == maxPrice) {
                        CurrencyText(
                            amount = minPrice,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "${Formatters.formatMmk(minPrice)} ~ ${Formatters.formatMmk(maxPrice)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    if (productWithVariants.variants.isNotEmpty()) {
//                        if (minPrice == maxPrice) {
//                            CurrencyText(
//                                amount = minPrice,
//                                fontSize = 13.sp,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//                        } else {
//                            Text(
//                                text = "${Formatters.formatMmk(minPrice)} ~ ${Formatters.formatMmk(maxPrice)}",
//                                fontSize = 11.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//                        }
//                    } else {
//                        Text(
//                            text = "No variants",
//                            fontSize = 11.sp,
//                            color = Color.Gray
//                        )
//                    }
//
//                    Text(
//                        text = "${productWithVariants.variants.size} var",
//                        fontSize = 11.sp,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
//                    )
//                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductVariantSelectionDialog(
    productWithVariants: ProductWithVariants,
    saleType: SaleType = SaleType.RETAIL,
    onDismiss: () -> Unit,
    onSelectVariant: (ProductVariantEntity) -> Unit
) {
    var previewVariant by remember {
        mutableStateOf(productWithVariants.variants.firstOrNull())
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Product/Variant Image
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if(!productWithVariants.product.imageUri.isNullOrBlank() || !previewVariant?.imageUri.isNullOrBlank()){
                        ProductThumbnail(
                            imageUri =  productWithVariants.product.imageUri, //previewVariant?.imageUri,
                            fallbackImageUri = previewVariant?.imageUri,
                            size = 64.dp,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = productWithVariants.product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (productWithVariants.category != null) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = productWithVariants.category.name,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (saleType == SaleType.WHOLESALE) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Whole Sale",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (productWithVariants.variants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No variants available for this product", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // Variant Buttons for Quick Tap Entry
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        productWithVariants.variants.forEach { variant ->
                            val isOutOfStock = variant.stockQty <= 0
                            val priceToShow = if (saleType == SaleType.WHOLESALE && variant.wholesalePrice > 0) {
                                variant.wholesalePrice
                            } else {
                                variant.sellPrice
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isOutOfStock) Color(0xFFE2E8F0)
                                        else MaterialTheme.colorScheme.primaryContainer
                                    )
                                    .clickable(enabled = !isOutOfStock) {
                                        previewVariant = variant
                                        onSelectVariant(variant)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (!variant.imageUri.isNullOrBlank()) {
                                        ProductThumbnail(
                                            imageUri = variant.imageUri,
                                            size = 28.dp,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = variant.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOutOfStock) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(Modifier.height(1.dp))
                                        CurrencyText(
                                            amount = priceToShow,
                                            fontSize = 11.sp,
                                            color = if (isOutOfStock) Color.Gray else MaterialTheme.colorScheme.primary
                                        )
//                                        if (variant.isLowStock || isOutOfStock) {
//                                            Text(
//                                                text = if (isOutOfStock) "Out" else "${variant.stockQty} left",
//                                                fontSize = 9.sp,
//                                                color = if (isOutOfStock) AccentRed else Color(0xFFD97706),
//                                                fontWeight = FontWeight.SemiBold
//                                            )
//                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun CartPane(
    cartState: CartState,
    onSetSaleType: (SaleType) -> Unit = {},
    onUpdateQty: (variantId: String, newQty: Int) -> Unit,
    onRemoveItem: (variantId: String) -> Unit,
    onClearCart: () -> Unit,
    onSetDiscount: (type: DiscountType, value: Double) -> Unit,
    onSetDeliFee: (fee: Double) -> Unit,
    onInitiateCheckout: () -> Unit,
    onSaveDraft: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDiscountInput by remember { mutableStateOf(false) }
    var discountText by remember { mutableStateOf("") }
    var deliText by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cart Header & Items
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "Cart (${cartState.totalItemCount})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
                if (cartState.items.isNotEmpty()) {
                    TextButton(onClick = onClearCart) {
                        Text("CLEAN", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            if (cartState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.LocalMall,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Cart is empty",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cartState.items, key = { it.variant.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Variant/Product Thumbnail
                                if(!item.variant.imageUri.isNullOrBlank() || !item.product.imageUri.isNullOrBlank()){
                                    ProductThumbnail(
                                        imageUri = item.variant.imageUri,
                                        fallbackImageUri = item.product.imageUri,
                                        size = 44.dp,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = item.variant.displayName,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        CurrencyText(
                                            amount = item.unitPrice,
                                            fontSize = 12.sp
                                        )
                                        if (cartState.saleType == SaleType.WHOLESALE) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Whole",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { onUpdateQty(item.variant.id, item.quantity - 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = "${item.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )

                                    IconButton(
                                        onClick = { onUpdateQty(item.variant.id, item.quantity + 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cart Summary & Checkout Trigger
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal:", fontSize = 13.sp)
                    CurrencyText(amount = cartState.subtotal, fontSize = 13.sp)
                }

                if (cartState.discountAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Discount:", fontSize = 13.sp, color = AccentGreen)
                        CurrencyText(amount = cartState.discountAmount, fontSize = 13.sp, color = AccentGreen)
                    }
                }

                if (cartState.deliFee > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delivery Fee:", fontSize = 13.sp)
                        CurrencyText(amount = cartState.deliFee, fontSize = 13.sp)
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Grand Total:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    CurrencyText(
                        amount = cartState.grandTotal,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 19.sp
                    )
                }

                // Whole Sale / Retail Sale Mode Switch
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isRetail = cartState.saleType == SaleType.RETAIL
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isRetail) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSetSaleType(SaleType.RETAIL) }
//                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Retail Sale",
                                fontSize = 12.sp,
                                fontWeight = if (isRetail) FontWeight.Bold else FontWeight.Medium,
                                color = if (isRetail) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }

                        val isWholesale = cartState.saleType == SaleType.WHOLESALE
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isWholesale) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSetSaleType(SaleType.WHOLESALE) }
//                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Whole Sale",
                                fontSize = 12.sp,
                                fontWeight = if (isWholesale) FontWeight.Bold else FontWeight.Medium,
                                color = if (isWholesale) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onSaveDraft,
                        enabled = cartState.items.isNotEmpty(),
                        modifier = Modifier
                            .weight(0.2f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Draft", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onInitiateCheckout,
                        enabled = cartState.items.isNotEmpty(),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Place Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MobilePosLayout(
    categories: List<com.yayyar.deco.core.database.entity.CategoryEntity>,
    selectedCatId: String?,
    catalogProducts: List<ProductWithVariants>,
    cartState: CartState,
    isGridView: Boolean = true,
    onSelectCategory: (String?) -> Unit,
    onProductClick: (ProductWithVariants) -> Unit,
    onSetSaleType: (SaleType) -> Unit = {},
    onUpdateQty: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onSetDiscount: (DiscountType, Double) -> Unit,
    onSetDeliFee: (Double) -> Unit,
    onInitiateCheckout: () -> Unit,
    onSaveDraft: () -> Unit = {}
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = if (cartState.items.isNotEmpty()) 80.dp else 30.dp,
        sheetContent = {
            CartPane(
                cartState = cartState,
                onSetSaleType = onSetSaleType,
                onUpdateQty = onUpdateQty,
                onRemoveItem = onRemoveItem,
                onClearCart = onClearCart,
                onSetDiscount = onSetDiscount,
                onSetDeliFee = onSetDeliFee,
                onInitiateCheckout = onInitiateCheckout,
                onSaveDraft = onSaveDraft,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 8.dp)
            )
        }
    ) { innerPadding ->
        CatalogPane(
            categories = categories,
            selectedCatId = selectedCatId,
            catalogProducts = catalogProducts,
            saleType = cartState.saleType,
            isGridView = isGridView,
            onSelectCategory = onSelectCategory,
            onProductClick = onProductClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 12.dp)
        )
    }
}

