package com.yayyar.teahouse.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.yayyar.teahouse.R
import com.yayyar.teahouse.ui.theme.AccentBlue
import com.yayyar.teahouse.ui.theme.AccentGreen
import com.yayyar.teahouse.ui.theme.AccentPurple
import com.yayyar.teahouse.ui.theme.AccentRed
import com.yayyar.teahouse.ui.theme.PrimaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterScreen(
    onBack: () -> Unit,
    viewModel: PrinterViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedAddress by viewModel.selectedPrinterAddress.collectAsState()
    val selectedName by viewModel.selectedPrinterName.collectAsState()
    val pairedPrinters by viewModel.pairedPrinters.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val testPrintState by viewModel.testPrintState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPairedPrinters(context)
    }

    val refreshPrinters: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnect = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            val hasScan = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED

            if (hasConnect && hasScan) {
                viewModel.refreshPairedPrinters(context)
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            }
        } else {
            viewModel.refreshPairedPrinters(context)
        }
    }

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        refreshPrinters()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.printer_title)
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
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isScanning,
            onRefresh = { refreshPrinters() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // Test Print Feedback Banner
            when (val state = testPrintState) {
                is PrintTestState.Printing -> {
                    StatusCard(
                        icon = Icons.Outlined.Print,
                        iconColor = AccentBlue,
                        title = stringResource(R.string.printer_status_printing_title),
                        subtitle = stringResource(R.string.printer_status_printing_sub),
                        isLoading = true
                    )
                }
                is PrintTestState.Success -> {
                    StatusCard(
                        icon = Icons.Outlined.CheckCircle,
                        iconColor = AccentGreen,
                        title = stringResource(R.string.printer_status_success_title),
                        subtitle = state.message,
                        onDismiss = { viewModel.clearTestPrintStatus() }
                    )
                }
                is PrintTestState.Error -> {
                    StatusCard(
                        icon = Icons.Outlined.ErrorOutline,
                        iconColor = AccentRed,
                        title = stringResource(R.string.printer_status_error_title),
                        subtitle = state.errorMessage,
                        onDismiss = { viewModel.clearTestPrintStatus() }
                    )
                }
                is PrintTestState.Idle -> Unit
            }

            // Paired / Discovered Printers Section
            Text(
                text = stringResource(R.string.printer_available_header),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            if (pairedPrinters.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bluetooth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = stringResource(R.string.printer_no_paired_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.printer_no_paired_sub),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pairedPrinters.forEach { printer ->
                        val isSelected = selectedAddress == printer.address
                        PrinterDeviceCard(
                            printer = printer,
                            isSelected = isSelected,
                            onSelect = {
                                if (isSelected) {
                                    viewModel.selectPrinter(null, null)
                                } else {
                                    viewModel.selectPrinter(printer.address, printer.name)
                                }
                            },
                            onTestPrint = {
                                viewModel.runTestPrint(context, printer.address)
                            }
                        )
                    }
                }
            }

            // Paper format & rendering specs card
//            Card(
//                shape = RoundedCornerShape(16.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
//                ),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text(
//                        text = stringResource(R.string.printer_capabilities_title),
//                        style = MaterialTheme.typography.labelLarge,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                    Spacer(modifier = Modifier.height(6.dp))
//                    Text(
//                        text = stringResource(R.string.printer_capabilities_sub),
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        lineHeight = 20.sp
//                    )
//                }
//            }
        }
    }
    }

    if (showAddDialog) {
        AddPrinterDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, address ->
                viewModel.addCustomPrinter(name, address)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PrinterDeviceCard(
    printer: DiscoveredPrinter,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTestPrint: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(
                    width = 2.dp,
                    color = PrimaryLight,
                    shape = RoundedCornerShape(14.dp)
                ) else Modifier
            )
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) PrimaryLight.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Print,
                    contentDescription = null,
                    tint = if (isSelected) PrimaryLight else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = printer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = AccentGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.printer_active_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = printer.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onTestPrint,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(stringResource(R.string.printer_test_short), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun StatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isLoading: Boolean = false,
    onDismiss: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = iconColor.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = iconColor
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        }
    }
}

@Composable
private fun AddPrinterDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.printer_add_manual_title), fontWeight = FontWeight.Bold) },
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
                    label = { Text(stringResource(R.string.printer_name_label)) },
                    placeholder = { Text(stringResource(R.string.printer_name_hint)) },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        addressError = false
                    },
                    label = { Text(stringResource(R.string.printer_mac_label)) },
                    placeholder = { Text(stringResource(R.string.printer_mac_hint)) },
                    isError = addressError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (address.isBlank()) {
                        addressError = true
                        return@Button
                    }
                    onAdd(name.trim(), address.trim())
                }
            ) {
                Text(stringResource(R.string.printer_add_select_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
