package com.yayyar.deco.feature.pos

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalLayoutApi::class)
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
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val accountCopiedMsg = stringResource(R.string.checkout_account_copied)

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
    var paymentNote by remember { mutableStateOf("") }

    val grandTotal = cartState.grandTotal
    val cashReceived = cashReceivedText.toDoubleOrNull() ?: 0.0
    val isCash = selectedPaymentType == "CASH"
    val change = if (isCash && cashReceived >= grandTotal) {
        cashReceived - grandTotal
    } else 0.0
    val isInsufficientCash = isCash && cashReceived < grandTotal

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (Title, mode chip, items count, and close button)
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        Text(
//                            text = stringResource(R.string.checkout_title),
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 20.sp,
//                            color = MaterialTheme.colorScheme.onSurface
//                        )
//                        Surface(
//                            shape = RoundedCornerShape(6.dp),
//                            color = if (cartState.saleType == SaleType.WHOLESALE)
//                                MaterialTheme.colorScheme.primaryContainer
//                            else
//                                MaterialTheme.colorScheme.secondaryContainer
//                        ) {
//                            Text(
//                                text = if (cartState.saleType == SaleType.WHOLESALE)
//                                    stringResource(R.string.pos_wholesale_mode)
//                                else
//                                    stringResource(R.string.pos_retail_mode),
//                                fontSize = 11.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = if (cartState.saleType == SaleType.WHOLESALE)
//                                    MaterialTheme.colorScheme.onPrimaryContainer
//                                else
//                                    MaterialTheme.colorScheme.onSecondaryContainer,
//                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
//                            )
//                        }
//                        Surface(
//                            shape = RoundedCornerShape(6.dp),
//                            color = MaterialTheme.colorScheme.surfaceVariant
//                        ) {
//                            Text(
//                                text = stringResource(R.string.pos_items_count, cartState.totalItemCount),
//                                fontSize = 11.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
//                            )
//                        }
//                    }
//
//                    IconButton(
//                        onClick = onDismiss,
//                        modifier = Modifier.size(32.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = stringResource(R.string.action_cancel),
//                            tint = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }

                // Customer Info (if any)
//                if (!cartState.customerName.isNullOrBlank() || !cartState.customerPhone.isNullOrBlank()) {
//                    CustomerInfoCard(
//                        name = cartState.customerName,
//                        phone = cartState.customerPhone,
//                        address = cartState.customerAddress
//                    )
//                }

                // Amount Due / Grand Total Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.checkout_amount_due),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            CurrencyText(
                                amount = grandTotal,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

//                        if (cartState.discountAmount > 0 || cartState.deliFee > 0) {
//                            HorizontalDivider(
//                                modifier = Modifier.padding(vertical = 4.dp),
//                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
//                            )
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ) {
//                                val discountText = if (cartState.discountAmount > 0) "Discount: -${Formatters.formatMmk(cartState.discountAmount)}" else ""
//                                val deliText = if (cartState.deliFee > 0) "Deli: +${Formatters.formatMmk(cartState.deliFee)}" else ""
//                                val extraInfo = listOf(
//                                    "${stringResource(R.string.pos_subtotal)}: ${Formatters.formatMmk(cartState.subtotal)}",
//                                    discountText,
//                                    deliText
//                                ).filter { it.isNotBlank() }.joinToString(" • ")
//
//                                Text(
//                                    text = extraInfo,
//                                    fontSize = 11.sp,
//                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
//                                )
//                            }
//                        }
                    }
                }

                // Payment Method Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.checkout_payment_method),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

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

                // Payment Details Panel
                val currentMethod = paymentMethodsToDisplay.find { it.code == selectedPaymentType }
                if (isCash) {
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
                            OutlinedTextField(
                                value = cashReceivedText,
                                onValueChange = { cashReceivedText = it },
                                label = { Text(stringResource(R.string.checkout_cash_received)) },
                                trailingIcon = {
                                    if (cashReceivedText.isNotEmpty() && cashReceivedText != grandTotal.toInt().toString()) {
                                        TextButton(onClick = { cashReceivedText = grandTotal.toInt().toString() }) {
                                            Text(
                                                stringResource(R.string.checkout_exact_amount),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (isInsufficientCash) {
                                Text(
                                    text = stringResource(R.string.checkout_insufficient_cash),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else if (change > 0) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = AccentGreen.copy(alpha = 0.12f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.checkout_change_returned),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = AccentGreen
                                        )
                                        CurrencyText(
                                            amount = change,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    DigitalPaymentPanel(
                        method = currentMethod,
                        paymentType = selectedPaymentType,
                        grandTotal = grandTotal,
                        paymentNote = paymentNote,
                        onPaymentNoteChange = { paymentNote = it },
                        onCopyAccount = { acc ->
                            clipboardManager.setText(AnnotatedString(acc))
                            Toast.makeText(context, accountCopiedMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            onConfirmCheckout(
                                selectedPaymentType,
                                if (isCash) cashReceived else 0.0,
                                if (selectedPaymentType == "KPAY") grandTotal else 0.0,
                                if (selectedPaymentType == "WAVEPAY") grandTotal else 0.0,
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
                            .weight(1.5f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.checkout_complete_sale),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerInfoCard(name: String?, phone: String?, address: String?) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
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
//                    Box(
//                        modifier = Modifier
//                            .size(30.dp)
//                            .clip(CircleShape)
//                            .background(if (isSelected) color else color.copy(alpha = 0.2f)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            imageVector = icon,
//                            contentDescription = null,
//                            tint = if (isSelected) Color.White else color,
//                            modifier = Modifier.size(16.dp)
//                        )
//                    }
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
                    fontSize = 15.sp,
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
