package com.yayyar.deco.feature.pos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yayyar.deco.R
import com.yayyar.deco.core.common.Formatters
import com.yayyar.deco.core.database.entity.PaymentMethodEntity
import com.yayyar.deco.core.ui.components.CurrencyText
import com.yayyar.deco.ui.theme.AccentBlue
import com.yayyar.deco.ui.theme.AccentGreen
import com.yayyar.deco.ui.theme.AccentPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CheckoutDialog(
    cartState: CartState,
    activePaymentMethods: List<PaymentMethodEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirmCheckout: (
        paymentType: String,
        cashReceived: Double,
        kpayAmount: Double,
        waveAmount: Double,
        paymentNotes: String?
    ) -> Unit
) {
    val paymentMethodsToDisplay = remember(activePaymentMethods) {
        if (activePaymentMethods.isNotEmpty()) {
            activePaymentMethods
        } else {
            listOf(
                PaymentMethodEntity(id = "pm_cash", name = "Cash", code = "CASH", isDefault = true),
                PaymentMethodEntity(id = "pm_kpay", name = "KPay", code = "KPAY"),
                PaymentMethodEntity(id = "pm_wave", name = "WavePay", code = "WAVEPAY")
            )
        }
    }

    val defaultMethod = remember(paymentMethodsToDisplay) {
        paymentMethodsToDisplay.firstOrNull { it.isDefault }
            ?: paymentMethodsToDisplay.firstOrNull()
            ?: PaymentMethodEntity(id = "pm_cash", name = "Cash", code = "CASH", isDefault = true)
    }

    var selectedPaymentType by remember(defaultMethod) { mutableStateOf(defaultMethod.code) }
    var cashReceivedText by remember { mutableStateOf(cartState.grandTotal.toInt().toString()) }
    var kpayText by remember { mutableStateOf("0") }
    var waveText by remember { mutableStateOf("0") }
    var paymentNote by remember { mutableStateOf("") }
    var isItemsExpanded by remember { mutableStateOf(false) }

    val grandTotal = cartState.grandTotal
    val cashReceived = cashReceivedText.toDoubleOrNull() ?: 0.0
    val isCash = selectedPaymentType == "CASH"
    val change = if (isCash && cashReceived >= grandTotal) {
        cashReceived - grandTotal
    } else 0.0
    val isInsufficientCash = isCash && cashReceived < grandTotal

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val accountCopiedMsg = stringResource(R.string.checkout_account_copied)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.checkout_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (cartState.saleType == SaleType.WHOLESALE)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = if (cartState.saleType == SaleType.WHOLESALE)
                                        stringResource(R.string.pos_wholesale_mode)
                                    else
                                        stringResource(R.string.pos_retail_mode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cartState.saleType == SaleType.WHOLESALE)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = stringResource(R.string.pos_items_count, cartState.totalItemCount),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (isInsufficientCash) {
                            Text(
                                text = stringResource(R.string.checkout_insufficient_cash),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.checkout_grand_total_label),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                CurrencyText(
                                    amount = grandTotal,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (isCash && change > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.checkout_change_due) + ":",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentGreen
                                        )
                                        CurrencyText(
                                            amount = change,
                                            fontSize = 13.sp,
                                            color = AccentGreen
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    onConfirmCheckout(
                                        selectedPaymentType,
                                        cashReceived,
                                        kpayText.toDoubleOrNull() ?: 0.0,
                                        waveText.toDoubleOrNull() ?: 0.0,
                                        paymentNote.ifBlank { null }
                                    )
                                },
                                enabled = !isInsufficientCash,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(52.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.checkout_complete_sale),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val isTabletLandscape = maxWidth >= 720.dp

                if (isTabletLandscape) {
                    // Split screen for Tablets / Wide devices
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column: Order details & Summary
                        Card(
                            modifier = Modifier
                                .weight(0.42f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = stringResource(R.string.checkout_items_summary, cartState.totalItemCount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                if (!cartState.customerName.isNullOrBlank() || !cartState.customerPhone.isNullOrBlank()) {
                                    Spacer(Modifier.height(10.dp))
                                    CustomerInfoCard(
                                        name = cartState.customerName,
                                        phone = cartState.customerPhone,
                                        address = cartState.customerAddress
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Itemized Order List
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(cartState.items) { item ->
                                        OrderItemRow(item = item)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Totals Breakdown
                                OrderTotalsBreakdown(cartState = cartState)
                            }
                        }

                        // Right Column: Payment Methods & Keypad / Input
                        Card(
                            modifier = Modifier
                                .weight(0.58f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Text(
                                        text = stringResource(R.string.checkout_payment_method),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    PaymentMethodGrid(
                                        methods = paymentMethodsToDisplay,
                                        selectedMethodCode = selectedPaymentType,
                                        onSelectMethod = { code ->
                                            selectedPaymentType = code
                                            if (code == "CASH") {
                                                cashReceivedText = grandTotal.toInt().toString()
                                            }
                                        }
                                    )
                                }

                                item {
                                    val currentMethod = paymentMethodsToDisplay.find { it.code == selectedPaymentType }
                                    if (isCash) {
                                        CashPaymentPanel(
                                            grandTotal = grandTotal,
                                            cashReceivedText = cashReceivedText,
                                            change = change,
                                            onCashTextChange = { cashReceivedText = it },
                                            onAppendDigit = { digit ->
                                                cashReceivedText = if (cashReceivedText == "0" || cashReceivedText.isEmpty()) digit else cashReceivedText + digit
                                            },
                                            onBackspace = {
                                                cashReceivedText = if (cashReceivedText.length > 1) cashReceivedText.dropLast(1) else "0"
                                            },
                                            onClear = { cashReceivedText = "0" },
                                            onExact = { cashReceivedText = grandTotal.toInt().toString() }
                                        )
                                    } else {
                                        DigitalPaymentPanel(
                                            method = currentMethod,
                                            paymentType = selectedPaymentType,
                                            grandTotal = grandTotal,
                                            paymentNote = paymentNote,
                                            onPaymentNoteChange = { paymentNote = it },
                                            onCopyAccount = { acc ->
                                                clipboardManager.setText(AnnotatedString(acc))
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(accountCopiedMsg)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Mobile Portrait Layout
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Grand Total Hero Card
                        item {
                            HeroTotalCard(
                                grandTotal = grandTotal,
                                totalItems = cartState.totalItemCount,
                                isExpanded = isItemsExpanded,
                                onToggleExpand = { isItemsExpanded = !isItemsExpanded }
                            )
                        }

                        // Collapsible Order Details
                        item {
                            AnimatedVisibility(
                                visible = isItemsExpanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (!cartState.customerName.isNullOrBlank() || !cartState.customerPhone.isNullOrBlank()) {
                                            CustomerInfoCard(
                                                name = cartState.customerName,
                                                phone = cartState.customerPhone,
                                                address = cartState.customerAddress
                                            )
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        }

                                        cartState.items.forEach { item ->
                                            OrderItemRow(item = item)
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        OrderTotalsBreakdown(cartState = cartState)
                                    }
                                }
                            }
                        }

                        // Payment Method Selection
                        item {
                            Text(
                                text = stringResource(R.string.checkout_payment_method),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            PaymentMethodGrid(
                                methods = paymentMethodsToDisplay,
                                selectedMethodCode = selectedPaymentType,
                                onSelectMethod = { code ->
                                    selectedPaymentType = code
                                    if (code == "CASH") {
                                        cashReceivedText = grandTotal.toInt().toString()
                                    }
                                }
                            )
                        }

                        // Payment Inputs
                        item {
                            val currentMethod = paymentMethodsToDisplay.find { it.code == selectedPaymentType }
                            if (isCash) {
                                CashPaymentPanel(
                                    grandTotal = grandTotal,
                                    cashReceivedText = cashReceivedText,
                                    change = change,
                                    onCashTextChange = { cashReceivedText = it },
                                    onAppendDigit = { digit ->
                                        cashReceivedText = if (cashReceivedText == "0" || cashReceivedText.isEmpty()) digit else cashReceivedText + digit
                                    },
                                    onBackspace = {
                                        cashReceivedText = if (cashReceivedText.length > 1) cashReceivedText.dropLast(1) else "0"
                                    },
                                    onClear = { cashReceivedText = "0" },
                                    onExact = { cashReceivedText = grandTotal.toInt().toString() }
                                )
                            } else {
                                DigitalPaymentPanel(
                                    method = currentMethod,
                                    paymentType = selectedPaymentType,
                                    grandTotal = grandTotal,
                                    paymentNote = paymentNote,
                                    onPaymentNoteChange = { paymentNote = it },
                                    onCopyAccount = { acc ->
                                        clipboardManager.setText(AnnotatedString(acc))
                                        scope.launch {
                                            snackbarHostState.showSnackbar(accountCopiedMsg)
                                        }
                                    }
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
private fun HeroTotalCard(
    grandTotal: Double,
    totalItems: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.checkout_amount_due),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    CurrencyText(
                        amount = grandTotal,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    modifier = Modifier.clickable { onToggleExpand() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.pos_items_count, totalItems),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: CartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${item.variant.displayName} • ${item.quantity} × ${Formatters.formatMmk(item.unitPrice)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CurrencyText(
            amount = item.totalPrice,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CustomerInfoCard(name: String?, phone: String?, address: String?) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                if (!name.isNullOrBlank()) {
                    Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                if (!phone.isNullOrBlank()) {
                    Text(text = phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!address.isNullOrBlank()) {
                    Text(text = address, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun OrderTotalsBreakdown(cartState: CartState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.pos_subtotal), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            CurrencyText(amount = cartState.subtotal, fontSize = 13.sp)
        }
        if (cartState.discountAmount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.pos_discount)} (${if (cartState.discountType == DiscountType.PERCENT) "${cartState.discountValue.toInt()}%" else "Fixed"})",
                    fontSize = 13.sp,
                    color = AccentGreen
                )
                Text(
                    text = "-${Formatters.formatMmk(cartState.discountAmount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentGreen
                )
            }
        }
        if (cartState.deliFee > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.pos_delivery_fee), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "+${Formatters.formatMmk(cartState.deliFee)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.checkout_grand_total_label), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            CurrencyText(amount = cartState.grandTotal, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaymentMethodGrid(
    methods: List<PaymentMethodEntity>,
    selectedMethodCode: String,
    onSelectMethod: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        methods.forEach { method ->
            val isSelected = selectedMethodCode == method.code
            val color = getPaymentMethodColor(method.code)
            val icon = getPaymentMethodIcon(method.code)

            val animatedBorderColor by animateColorAsState(
                targetValue = if (isSelected) color else Color.Transparent,
                label = "border_color"
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = animatedBorderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectMethod(method.code) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) color else color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = method.name,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CashPaymentPanel(
    grandTotal: Double,
    cashReceivedText: String,
    change: Double,
    onCashTextChange: (String) -> Unit,
    onAppendDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onExact: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cash Received Input Row
            OutlinedTextField(
                value = cashReceivedText,
                onValueChange = onCashTextChange,
                label = { Text(stringResource(R.string.checkout_cash_received)) },
                trailingIcon = {
                    if (cashReceivedText.isNotEmpty() && cashReceivedText != "0") {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Quick Cash Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.checkout_quick_cash),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Exact button
                    QuickCashChip(
                        label = stringResource(R.string.checkout_exact_amount),
                        isExact = true,
                        onClick = onExact
                    )

                    // Standard rounded values (e.g., 5k, 10k, 20k, 50k, 100k)
                    val presets = listOf(5000.0, 10000.0, 20000.0, 50000.0, 100000.0, 200000.0)
                    presets.filter { it >= grandTotal }.distinct().take(4).forEach { amt ->
                        QuickCashChip(
                            label = Formatters.formatMmk(amt),
                            onClick = { onCashTextChange(amt.toInt().toString()) }
                        )
                    }

                    // Incremental chips (+1000, +5000, +10000)
                    listOf(1000.0, 5000.0, 10000.0).forEach { inc ->
                        val currentVal = cashReceivedText.toDoubleOrNull() ?: grandTotal
                        QuickCashChip(
                            label = "+${Formatters.formatMmk(inc)}",
                            onClick = { onCashTextChange((currentVal + inc).toInt().toString()) }
                        )
                    }
                }
            }

            // On-screen POS Numeric Keypad
            PosNumericKeypad(
                onAppendDigit = onAppendDigit,
                onBackspace = onBackspace,
                onClear = onClear,
                onExact = onExact
            )

            // Change Return Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (change > 0)
                        AccentGreen.copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.checkout_change_returned),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (change > 0) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CurrencyText(
                            amount = change,
                            fontSize = 18.sp,
                            color = if (change > 0) AccentGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (change > 0) {
                        Surface(
                            shape = CircleShape,
                            color = AccentGreen.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(20.dp)
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
private fun PosNumericKeypad(
    onAppendDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onExact: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("00", "0", "BACKSPACE")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                when (key) {
                                    "BACKSPACE" -> onBackspace()
                                    else -> onAppendDigit(key)
                                }
                            },
                        color = if (key == "BACKSPACE")
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (key == "BACKSPACE") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
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
private fun QuickCashChip(
    label: String,
    isExact: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isExact) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isExact) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DigitalPaymentPanel(
    method: PaymentMethodEntity?,
    paymentType: String,
    grandTotal: Double,
    paymentNote: String,
    onPaymentNoteChange: (String) -> Unit,
    onCopyAccount: (String) -> Unit
) {
    val color = getPaymentMethodColor(paymentType)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = method?.name ?: paymentType,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = color
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = color.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = Formatters.formatMmk(grandTotal),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Merchant Account Details
            if (!method?.accountName.isNullOrBlank() || !method?.accountNumber.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.checkout_merchant_account),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!method?.accountName.isNullOrBlank()) {
                                Text(
                                    text = method.accountName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (!method?.accountNumber.isNullOrBlank()) {
                                Text(
                                    text = method.accountNumber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = color
                                )
                            }
                        }

                        if (!method?.accountNumber.isNullOrBlank()) {
                            IconButton(onClick = { onCopyAccount(method.accountNumber) }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.checkout_copy_account),
                                    tint = color
                                )
                            }
                        }
                    }
                }
            }

            // Transaction Note Field
            OutlinedTextField(
                value = paymentNote,
                onValueChange = onPaymentNoteChange,
                label = { Text(stringResource(R.string.checkout_payment_notes)) },
                placeholder = { Text("e.g. Transaction ID / Ref #") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun getPaymentMethodColor(code: String): Color {
    return when (code.uppercase()) {
        "CASH" -> AccentGreen
        "KPAY" -> AccentBlue
        "WAVEPAY" -> AccentPurple
        "CB", "CBPAY" -> Color(0xFFE11D48)
        "AYA", "AYAPAY" -> Color(0xFFD97706)
        else -> Color(0xFF0F766E)
    }
}

private fun getPaymentMethodIcon(code: String): ImageVector {
    return when (code.uppercase()) {
        "CASH" -> Icons.Default.LocalAtm
        "KPAY" -> Icons.Default.AccountBalanceWallet
        "WAVEPAY" -> Icons.Default.PhoneAndroid
        "CB", "CBPAY" -> Icons.Default.AccountBalance
        "AYA", "AYAPAY" -> Icons.Default.CreditCard
        else -> Icons.Default.Payments
    }
}
