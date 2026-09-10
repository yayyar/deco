package com.yayyar.deco.feature.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yayyar.deco.core.common.Formatters
import com.yayyar.deco.core.database.entity.PaymentMethodEntity
import com.yayyar.deco.core.ui.components.CurrencyText
import com.yayyar.deco.ui.theme.AccentBlue
import com.yayyar.deco.ui.theme.AccentGreen
import com.yayyar.deco.ui.theme.AccentPurple

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

    val grandTotal = cartState.grandTotal
    val cashReceived = cashReceivedText.toDoubleOrNull() ?: 0.0
    val change = if (selectedPaymentType == "CASH" && cashReceived >= grandTotal) {
        cashReceived - grandTotal
    } else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Complete Checkout", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                CurrencyText(
                    amount = grandTotal,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Payment Method Selector
                item {
                    Text(
                        text = "Payment Method",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paymentMethodsToDisplay.forEach { method ->
                            val color = getPaymentMethodColor(method.code)
                            PaymentTypeOption(
                                label = method.name,
                                isSelected = selectedPaymentType == method.code,
                                color = color,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                selectedPaymentType = method.code
                                if (method.code == "CASH") {
                                    cashReceivedText = grandTotal.toInt().toString()
                                }
                            }
                        }
                    }
                }

                // Payment Inputs
                item {
                    if (selectedPaymentType == "CASH") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = cashReceivedText,
                                    onValueChange = { cashReceivedText = it },
                                    label = { Text("Cash Amount (Ks)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                // Quick cash buttons
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    listOf(grandTotal, 20000.0, 50000.0, 100000.0).distinct().forEach { quickAmt ->
                                        if (quickAmt >= grandTotal) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .clickable { cashReceivedText = quickAmt.toInt().toString() }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = Formatters.formatMmk(quickAmt),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Change Return (ပြန်အမ်းငွေ):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    CurrencyText(
                                        amount = change,
                                        fontSize = 12.sp,
                                        color = if (change > 0) AccentGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    } else {
                        val currentMethod = paymentMethodsToDisplay.find { it.code == selectedPaymentType }
                        val color = getPaymentMethodColor(selectedPaymentType)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${currentMethod?.name ?: selectedPaymentType} Transfer",
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                                if (!currentMethod?.accountName.isNullOrBlank() || !currentMethod?.accountNumber.isNullOrBlank()) {
                                    Text(
                                        text = buildString {
                                            append("Account: ")
                                            currentMethod?.accountName?.let { append(it) }
                                            if (!currentMethod?.accountName.isNullOrBlank() && !currentMethod?.accountNumber.isNullOrBlank()) {
                                                append(" • ")
                                            }
                                            currentMethod?.accountNumber?.let { append(it) }
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Please verify payment transfer for ${Formatters.formatMmk(grandTotal)}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = paymentNote,
                                    onValueChange = { paymentNote = it },
                                    label = { Text("Transaction ID / Note") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // Summary details
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Items Count: ${cartState.totalItemCount}", fontSize = 13.sp)
                            Text("Subtotal: ${Formatters.formatMmk(cartState.subtotal)}", fontSize = 13.sp)
                            if (cartState.discountAmount > 0) {
                                Text("Discount: -${Formatters.formatMmk(cartState.discountAmount)}", fontSize = 13.sp, color = AccentGreen)
                            }
                            if (cartState.deliFee > 0) {
                                Text("Delivery Fee: +${Formatters.formatMmk(cartState.deliFee)}", fontSize = 13.sp)
                            }
                            if (!cartState.customerName.isNullOrBlank()) {
                                Text("Customer: ${cartState.customerName} (${cartState.customerPhone ?: ""})", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
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
                enabled = selectedPaymentType != "CASH" || cashReceived >= grandTotal,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm Sale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Back")
            }
        }
    )
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

@Composable
private fun PaymentTypeOption(
    label: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
        )
    }
}
