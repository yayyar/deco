package com.yayyar.deco.core.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

object PrinterManager {

    // Standard Bluetooth Serial Port Profile (SPP) UUID
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    suspend fun getPairedPrinters(context: Context): List<BluetoothDevice> = withContext(Dispatchers.IO) {
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return@withContext emptyList()
            adapter.bondedDevices.filter { device ->
                // Filter printers or SPP devices
                device.bluetoothClass?.majorDeviceClass == 1536 || // Imaging device (Printer)
                        device.name?.contains("pos", ignoreCase = true) == true ||
                        device.name?.contains("printer", ignoreCase = true) == true ||
                        device.name?.contains("mpt", ignoreCase = true) == true ||
                        device.name?.contains("rpp", ignoreCase = true) == true
            }.toList().ifEmpty { adapter.bondedDevices.toList() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Prints receipt data using the bitmap rasterization pipeline to guarantee
     * crisp output and flawless Burmese Unicode rendering.
     */
    @SuppressLint("MissingPermission")
    suspend fun printReceipt(
        context: Context,
        receiptData: ReceiptData,
        deviceAddress: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 1. Generate Bitmap and encode to ESC/POS raster byte stream
            val bitmap = ReceiptBitmapRenderer.renderReceiptBitmap(receiptData)
            val escPosBytes = EscPosRasterEncoder.encodeBitmapToEscPos(bitmap)

            if (deviceAddress.isNullOrBlank()) {
                // Simulated print output for preview / testing
                return@withContext Result.success(true)
            }

            val adapter = BluetoothAdapter.getDefaultAdapter()
                ?: return@withContext Result.failure(IllegalStateException("Bluetooth not supported"))

            val device = adapter.getRemoteDevice(deviceAddress)
            val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)

            try {
                socket.connect()
                val outputStream: OutputStream = socket.outputStream
                outputStream.write(escPosBytes)
                outputStream.flush()
                Result.success(true)
            } finally {
                try { socket.close() } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
