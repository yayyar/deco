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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yayyar.deco.core.common.Formatters
import com.yayyar.deco.core.common.Resource
import com.yayyar.deco.core.database.entity.ProductVariantEntity
import com.yayyar.deco.core.database.model.ProductWithVariants
import com.yayyar.deco.core.printer.ReceiptData
import com.yayyar.deco.core.ui.components.CurrencyText
import com.yayyar.deco.core.ui.components.StockBadge
import com.yayyar.deco.ui.theme.AccentGreen
import com.yayyar.deco.ui.theme.AccentRed
import kotlinx.coroutines.launch

@Composable
fun PosScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val catalogProducts by viewModel.catalogProducts.collectAsState()
    val cartState by viewModel.cartState.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()

    var showCheckoutDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isTabletLandscape = maxWidth >= 720.dp

        if (isTabletLandscape) {
            // Tablet 2-Pane Master-Detail Layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left 65%: Catalog
                CatalogPane(
                    categories = categories,
                    selectedCatId = selectedCatId,
                    catalogProducts = catalogProducts,
                    onSelectCategory = { viewModel.selectCategory(it) },
                    onAddToCart = { prod, variant -> viewModel.addToCart(prod, variant) },
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                        .padding(16.dp)
                )

                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                // Right 35%: Cart & Checkout Summary
                CartPane(
                    cartState = cartState,
                    onUpdateQty = { variantId, qty -> viewModel.updateCartItemQuantity(variantId, qty) },
                    onRemoveItem = { viewModel.removeCartItem(it) },
                    onClearCart = { viewModel.clearCart() },
                    onSetDiscount = { type, value -> viewModel.setDiscount(type, value) },
                    onSetDeliFee = { viewModel.setDeliFee(it) },
                    onInitiateCheckout = { showCheckoutDialog = true },
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp)
                )
            }
        } else {
            // Mobile Layout with Sticky Bottom Cart Bar
            MobilePosLayout(
                categories = categories,
                selectedCatId = selectedCatId,
                catalogProducts = catalogProducts,
                cartState = cartState,
                onSelectCategory = { viewModel.selectCategory(it) },
                onAddToCart = { prod, variant -> viewModel.addToCart(prod, variant) },
                onUpdateQty = { variantId, qty -> viewModel.updateCartItemQuantity(variantId, qty) },
                onRemoveItem = { viewModel.removeCartItem(it) },
                onClearCart = { viewModel.clearCart() },
                onSetDiscount = { type, value -> viewModel.setDiscount(type, value) },
                onSetDeliFee = { viewModel.setDeliFee(it) },
                onInitiateCheckout = { showCheckoutDialog = true }
            )
        }
    }

    if (showCheckoutDialog) {
        CheckoutDialog(
            cartState = cartState,
            onDismiss = { showCheckoutDialog = false },
            onConfirmCheckout = { pType, cashRec, kpayAmt, waveAmt, notes ->
                showCheckoutDialog = false
                viewModel.performCheckout(pType, cashRec, kpayAmt, waveAmt, notes)
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
    onSelectCategory: (String?) -> Unit,
    onAddToCart: (com.yayyar.deco.core.database.entity.ProductEntity, ProductVariantEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Category Filter Chips
        LazyRow(
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

        Spacer(Modifier.height(12.dp))

        // Product Catalog Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 200.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(catalogProducts, key = { it.product.id }) { item ->
                ProductCatalogCard(
                    productWithVariants = item,
                    onAddToCart = onAddToCart
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductCatalogCard(
    productWithVariants: ProductWithVariants,
    onAddToCart: (com.yayyar.deco.core.database.entity.ProductEntity, ProductVariantEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = productWithVariants.product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 2
            )
            if (productWithVariants.category != null) {
                Text(
                    text = productWithVariants.category.name,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(8.dp))

            // Variant Buttons for Quick Tap Entry
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                productWithVariants.variants.forEach { variant ->
                    val isOutOfStock = variant.stockQty <= 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isOutOfStock) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.primaryContainer)
                            .clickable(enabled = !isOutOfStock) {
                                onAddToCart(productWithVariants.product, variant)
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = variant.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOutOfStock) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            CurrencyText(
                                amount = variant.sellPrice,
                                fontSize = 11.sp,
                                color = if (isOutOfStock) Color.Gray else MaterialTheme.colorScheme.primary
                            )
                            if (variant.isLowStock) {
                                Text(
                                    text = if (isOutOfStock) "Out" else "${variant.stockQty} left",
                                    fontSize = 9.sp,
                                    color = if (isOutOfStock) AccentRed else Color(0xFFD97706),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartPane(
    cartState: CartState,
    onUpdateQty: (variantId: String, newQty: Int) -> Unit,
    onRemoveItem: (variantId: String) -> Unit,
    onClearCart: () -> Unit,
    onSetDiscount: (type: DiscountType, value: Double) -> Unit,
    onSetDeliFee: (fee: Double) -> Unit,
    onInitiateCheckout: () -> Unit,
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Current Cart (${cartState.totalItemCount})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
                if (cartState.items.isNotEmpty()) {
                    TextButton(onClick = onClearCart) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

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
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Cart is empty\nTap variants to add items",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                    CurrencyText(
                                        amount = item.variant.sellPrice,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                modifier = Modifier.padding(12.dp),
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
                    Text("Grand Total:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    CurrencyText(
                        amount = cartState.grandTotal,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 19.sp
                    )
                }

                Button(
                    onClick = onInitiateCheckout,
                    enabled = cartState.items.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Quick Checkout (${Formatters.formatMmk(cartState.grandTotal)})", fontWeight = FontWeight.Bold)
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
    onSelectCategory: (String?) -> Unit,
    onAddToCart: (com.yayyar.deco.core.database.entity.ProductEntity, ProductVariantEntity) -> Unit,
    onUpdateQty: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onSetDiscount: (DiscountType, Double) -> Unit,
    onSetDeliFee: (Double) -> Unit,
    onInitiateCheckout: () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = if (cartState.items.isNotEmpty()) 80.dp else 0.dp,
        sheetContent = {
            CartPane(
                cartState = cartState,
                onUpdateQty = onUpdateQty,
                onRemoveItem = onRemoveItem,
                onClearCart = onClearCart,
                onSetDiscount = onSetDiscount,
                onSetDeliFee = onSetDeliFee,
                onInitiateCheckout = onInitiateCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .padding(16.dp)
            )
        }
    ) { innerPadding ->
        CatalogPane(
            categories = categories,
            selectedCatId = selectedCatId,
            catalogProducts = catalogProducts,
            onSelectCategory = onSelectCategory,
            onAddToCart = onAddToCart,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
        )
    }
}
