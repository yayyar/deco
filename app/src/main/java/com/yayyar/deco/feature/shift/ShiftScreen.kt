package com.yayyar.deco.feature.shift

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.yayyar.deco.core.database.entity.ShiftEntity
import com.yayyar.deco.core.ui.components.CurrencyText
import com.yayyar.deco.ui.theme.AccentGreen
import com.yayyar.deco.ui.theme.AccentRed

@Composable
fun ShiftScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val activeShift by viewModel.activeShift.collectAsState()
    val cashMovements by viewModel.cashMovements.collectAsState()
    val shiftHistory by viewModel.shiftHistory.collectAsState()

    var showOpenShiftDialog by remember { mutableStateOf(false) }
    var showCashMovementDialog by remember { mutableStateOf<String?>(null) } // "CASH_IN" or "CASH_OUT"
    var showCloseShiftDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cash Register & Shift",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (activeShift != null) "Shift is OPEN" else "Register is CLOSED",
                    fontSize = 13.sp,
                    color = if (activeShift != null) AccentGreen else AccentRed,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (activeShift == null) {
                Button(
                    onClick = { showOpenShiftDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Open Shift")
                }
            } else {
                Button(
                    onClick = { showCloseShiftDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Close Shift")
                }
            }
        }

        // Active Shift Card
        activeShift?.let { shift ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Opened At:", fontSize = 13.sp)
                        Text(Formatters.formatDateTime(shift.openedAt), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Opening Float (မတည်ငွေ):", fontSize = 13.sp)
                        CurrencyText(amount = shift.openingFloat, fontSize = 13.sp)
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cash Sales:", fontSize = 13.sp)
                        CurrencyText(amount = shift.totalSalesCash, fontSize = 13.sp, color = AccentGreen)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("KPay Sales:", fontSize = 13.sp)
                        CurrencyText(amount = shift.totalSalesKpay, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("WavePay Sales:", fontSize = 13.sp)
                        CurrencyText(amount = shift.totalSalesWave, fontSize = 13.sp)
                    }

                    HorizontalDivider()

                    // Quick Cash In / Cash Out Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCashMovementDialog = "CASH_IN" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = AccentGreen)
                            Spacer(Modifier.width(4.dp))
                            Text("Cash In (ငွေသွင်း)", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showCashMovementDialog = "CASH_OUT" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = AccentRed)
                            Spacer(Modifier.width(4.dp))
                            Text("Cash Out (ငွေထုတ်)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Cash Movement Logs
        Text(
            text = "Cash Drawer Activity",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cashMovements, key = { it.id }) { movement ->
                val isCashIn = movement.type == "CASH_IN"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isCashIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isCashIn) AccentGreen else AccentRed
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isCashIn) "Cash In" else "Cash Out",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(movement.reason, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        CurrencyText(
                            amount = movement.amount,
                            color = if (isCashIn) AccentGreen else AccentRed,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    if (showOpenShiftDialog) {
        var floatText by remember { mutableStateOf("100000") }
        var notesText by remember { mutableStateOf("Main Cash Register") }

        AlertDialog(
            onDismissRequest = { showOpenShiftDialog = false },
            title = { Text("Open Shift Register", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = floatText,
                        onValueChange = { floatText = it },
                        label = { Text("Opening Cash Float (Ks)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = floatText.toDoubleOrNull() ?: 0.0
                    viewModel.openShift(amount, notesText.ifBlank { null })
                    showOpenShiftDialog = false
                }) {
                    Text("Open")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpenShiftDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    showCashMovementDialog?.let { movementType ->
        var amountText by remember { mutableStateOf("") }
        var reasonText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCashMovementDialog = null },
            title = {
                Text(
                    text = if (movementType == "CASH_IN") "Record Cash In" else "Record Cash Out",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (Ks)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Reason (e.g. Petty cash / Supplier payment)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && reasonText.isNotBlank()) {
                        viewModel.recordCashMovement(movementType, amount, reasonText)
                        showCashMovementDialog = null
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCashMovementDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCloseShiftDialog) {
        var countedCashText by remember { mutableStateOf("") }
        var closingNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCloseShiftDialog = false },
            title = { Text("End of Day - Close Shift", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Count physical cash in the drawer and input the actual total.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = countedCashText,
                        onValueChange = { countedCashText = it },
                        label = { Text("Actual Counted Cash (Ks)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = closingNotes,
                        onValueChange = { closingNotes = it },
                        label = { Text("Shift Handover Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val actual = countedCashText.toDoubleOrNull() ?: 0.0
                        viewModel.closeShift(actual, closingNotes.ifBlank { null })
                        showCloseShiftDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Close & Reconcile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseShiftDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
