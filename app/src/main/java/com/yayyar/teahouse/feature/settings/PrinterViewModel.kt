package com.yayyar.teahouse.feature.settings

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayyar.teahouse.core.data.repository.PreferencesRepository
import com.yayyar.teahouse.core.printer.PrinterManager
import com.yayyar.teahouse.core.printer.ReceiptData
import com.yayyar.teahouse.core.printer.ReceiptItem
import com.yayyar.teahouse.core.printer.StoreConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed interface PrintTestState {
    data object Idle : PrintTestState
    data object Printing : PrintTestState
    data class Success(val message: String) : PrintTestState
    data class Error(val errorMessage: String) : PrintTestState
}

data class DiscoveredPrinter(
    val name: String,
    val address: String,
    val isPaired: Boolean = true
)

@HiltViewModel
class PrinterViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val selectedPrinterAddress: StateFlow<String?> = preferencesRepository.selectedPrinterAddress
    val selectedPrinterName: StateFlow<String?> = preferencesRepository.selectedPrinterName

    private val _pairedPrinters = MutableStateFlow<List<DiscoveredPrinter>>(emptyList())
    val pairedPrinters: StateFlow<List<DiscoveredPrinter>> = _pairedPrinters.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _testPrintState = MutableStateFlow<PrintTestState>(PrintTestState.Idle)
    val testPrintState: StateFlow<PrintTestState> = _testPrintState.asStateFlow()

    @SuppressLint("MissingPermission")
    fun refreshPairedPrinters(context: Context) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val devices = PrinterManager.getPairedPrinters(context)
                _pairedPrinters.value = devices.map { device ->
                    DiscoveredPrinter(
                        name = device.name ?: "Unknown Printer",
                        address = device.address,
                        isPaired = true
                    )
                }
            } catch (e: Exception) {
                _pairedPrinters.value = emptyList()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun selectPrinter(address: String?, name: String?) {
        preferencesRepository.setSelectedPrinter(address, name)
    }

    fun addCustomPrinter(name: String, address: String) {
        val current = _pairedPrinters.value.toMutableList()
        if (current.none { it.address.equals(address, ignoreCase = true) }) {
            current.add(DiscoveredPrinter(name = name, address = address, isPaired = false))
            _pairedPrinters.value = current
        }
        selectPrinter(address, name)
    }

    fun runTestPrint(context: Context, targetAddress: String? = selectedPrinterAddress.value) {
        viewModelScope.launch {
            _testPrintState.value = PrintTestState.Printing
            try {
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val testReceipt = ReceiptData(
                    receiptNumber = "TEST-0001",
                    dateFormatted = now,
                    cashierName = "Admin / Test Mode",
                    customerName = "DeCo Test Customer",
                    items = listOf(
                        ReceiptItem(
                            productName = "Floral Summer Dress (နွေရာသီဂါဝန်)",
                            variantName = "S / ပန်းနီ",
                            quantity = 1,
                            unitPrice = 18500.0,
                            totalPrice = 18500.0
                        ),
                        ReceiptItem(
                            productName = "Cute Pattern T-Shirt (တီရှပ်)",
                            variantName = "Free Size / White",
                            quantity = 2,
                            unitPrice = 13500.0,
                            totalPrice = 27000.0
                        )
                    ),
                    subtotal = 45500.0,
                    discountAmount = 1500.0,
                    deliFee = 0.0,
                    grandTotal = 44000.0,
                    paymentType = "KPAY",
                    cashReceived = 0.0,
                    changeReturned = 0.0,
                    storeConfig = StoreConfig(
                        storeName = "DECO FASHION BOUTIQUE",
                        storeNameBurmese = "ဒေကို ဖက်ရှင် စမ်းသပ်စာရွက်",
                        footerMessage = "✓ Printer Connection & Burmese Font Test Passed!\nDeCo POS System v1.0"
                    )
                )

                val result = PrinterManager.printReceipt(context, testReceipt, targetAddress)
                if (result.isSuccess) {
                    _testPrintState.value = PrintTestState.Success(
                        if (targetAddress.isNullOrBlank()) {
                            "Test print simulation successful!"
                        } else {
                            "Test print sent to printer successfully!"
                        }
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Failed to connect or print"
                    _testPrintState.value = PrintTestState.Error(error)
                }
            } catch (e: Exception) {
                _testPrintState.value = PrintTestState.Error(e.message ?: "Unexpected error during printing")
            }
        }
    }

    fun clearTestPrintStatus() {
        _testPrintState.value = PrintTestState.Idle
    }
}
