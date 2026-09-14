package com.yayyar.deco.feature.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yayyar.deco.R
import com.yayyar.deco.core.database.entity.PaymentMethodEntity
import com.yayyar.deco.ui.theme.AccentBlue
import com.yayyar.deco.ui.theme.AccentGreen
import com.yayyar.deco.ui.theme.AccentPurple
import com.yayyar.deco.ui.theme.AccentRed
import com.yayyar.deco.ui.theme.PrimaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    onBack: () -> Unit,
    viewModel: PaymentMethodViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val paymentMethods by viewModel.paymentMethods.collectAsState()

    var editingMethod by remember { mutableStateOf<PaymentMethodEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var deletingMethod by remember { mutableStateOf<PaymentMethodEntity?>(null) }

    BackHandler {
        onBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.payment_method_title)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.payment_method_add))
            }
        }
    ) { innerPadding ->
        if (paymentMethods.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = stringResource(R.string.payment_method_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.payment_method_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(paymentMethods, key = { it.id }) { method ->
                    PaymentMethodCard(
                        paymentMethod = method,
                        onToggleActive = { isActive ->
                            viewModel.toggleActiveStatus(method, isActive)
                        },
                        onEdit = { editingMethod = method },
                        onDelete = { deletingMethod = method }
                    )
                }
            }
        }
    }

    if (isAddingNew || editingMethod != null) {
        val methodToEdit = editingMethod
        PaymentMethodEditDialog(
            initialMethod = methodToEdit,
            onDismiss = {
                isAddingNew = false
                editingMethod = null
            },
            onSave = { name, code, accountName, accountNumber, qrCode, isActive ->
                viewModel.savePaymentMethod(
                    id = methodToEdit?.id,
                    name = name,
                    code = code,
                    accountName = accountName,
                    accountNumber = accountNumber,
                    qrCodeData = qrCode,
                    isActive = isActive
                )
                isAddingNew = false
                editingMethod = null
            }
        )
    }

    deletingMethod?.let { method ->
        AlertDialog(
            onDismissRequest = { deletingMethod = null },
            title = { Text(stringResource(R.string.payment_method_delete_title)) },
            text = { Text(stringResource(R.string.payment_method_delete_msg, method.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePaymentMethod(method)
                        deletingMethod = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = AccentRed
                    )
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingMethod = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun PaymentMethodCard(
    paymentMethod: PaymentMethodEntity,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, color) = getPaymentVisuals(paymentMethod.code)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = paymentMethod.name,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = paymentMethod.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = color.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = paymentMethod.code,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!paymentMethod.accountNumber.isNullOrBlank() || !paymentMethod.accountName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = buildString {
                                paymentMethod.accountName?.let { append(it) }
                                if (!paymentMethod.accountName.isNullOrBlank() && !paymentMethod.accountNumber.isNullOrBlank()) {
                                    append(" • ")
                                }
                                paymentMethod.accountNumber?.let { append(it) }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = paymentMethod.isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryLight
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.action_edit),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_edit), style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = AccentRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun getPaymentVisuals(code: String): Pair<ImageVector, Color> {
    return when (code.uppercase()) {
        "CASH" -> Icons.Outlined.AccountBalanceWallet to AccentGreen
        "KPAY" -> Icons.Outlined.PhoneAndroid to AccentBlue
        "WAVEPAY" -> Icons.Outlined.PhoneAndroid to AccentPurple
        "KBZ" -> Icons.Outlined.CreditCard to AccentBlue
        "CB" -> Icons.Outlined.CreditCard to Color(0xFFE11D48)
        "AYA" -> Icons.Outlined.CreditCard to Color(0xFFD97706)
        else -> Icons.Outlined.Payments to Color(0xFF0F766E)
    }
}

@Composable
private fun PaymentMethodEditDialog(
    initialMethod: PaymentMethodEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, code: String, accountName: String?, accountNumber: String?, qrCode: String?, isActive: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialMethod?.name ?: "") }
    var code by remember { mutableStateOf(initialMethod?.code ?: "") }
    var accountName by remember { mutableStateOf(initialMethod?.accountName ?: "") }
    var accountNumber by remember { mutableStateOf(initialMethod?.accountNumber ?: "") }
    var qrCodeData by remember { mutableStateOf(initialMethod?.qrCodeData ?: "") }
    var isActive by remember { mutableStateOf(initialMethod?.isActive ?: true) }

    var nameError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialMethod == null) stringResource(R.string.payment_method_add) else stringResource(R.string.payment_method_edit)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text(stringResource(R.string.payment_method_name_label)) },
                    placeholder = { Text(stringResource(R.string.payment_method_name_hint)) },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase()
                        codeError = false
                    },
                    label = { Text(stringResource(R.string.payment_method_code_label)) },
                    placeholder = { Text(stringResource(R.string.payment_method_code_hint)) },
                    isError = codeError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text(stringResource(R.string.payment_method_acc_name_label)) },
                    placeholder = { Text(stringResource(R.string.payment_method_acc_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text(stringResource(R.string.payment_method_acc_no_label)) },
                    placeholder = { Text(stringResource(R.string.payment_method_acc_no_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.payment_method_active_channel),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (code.isBlank()) {
                        codeError = true
                        return@Button
                    }
                    onSave(name, code, accountName, accountNumber, qrCodeData, isActive)
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
