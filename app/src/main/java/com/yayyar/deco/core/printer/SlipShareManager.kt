package com.yayyar.deco.core.printer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object SlipShareManager {

    /**
     * Saves rendered receipt bitmap to cache and triggers an Android Share Sheet
     * allowing direct dispatch to Viber, Messenger, Telegram, WhatsApp, or Gallery.
     */
    fun shareReceiptImage(context: Context, receiptData: ReceiptData): Boolean {
        return try {
            val bitmap = ReceiptBitmapRenderer.renderReceiptBitmap(receiptData)
            val cacheDir = File(context.cacheDir, "receipts").apply { mkdirs() }
            val file = File(cacheDir, "${receiptData.receiptNumber}.png")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Receipt - ${receiptData.receiptNumber}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Receipt: ${receiptData.receiptNumber}\nTotal: ${receiptData.grandTotal} Ks\nThank you for shopping at ${receiptData.storeConfig.storeName}!"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Receipt Slip")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
