package com.yayyar.deco.core.printer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.yayyar.deco.core.common.Formatters

object ReceiptBitmapRenderer {

    fun renderReceiptBitmap(data: ReceiptData): Bitmap {
        val width = data.storeConfig.paperWidth.pixelWidth
        val padding = 16f
        val contentWidth = width - (padding * 2)

        // Pre-calculate approximate height
        val baseHeaderHeight = 220f
        val itemHeight = 48f
        val itemsTotalHeight = data.items.size * itemHeight
        val totalsHeight = 260f
        val footerHeight = 140f
        val calculatedHeight = (baseHeaderHeight + itemsTotalHeight + totalsHeight + footerHeight + 100f).toInt()

        val bitmap = Bitmap.createBitmap(width, calculatedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        var y = 40f
        val centerX = width / 2f

        // 1. Store Header
        canvas.drawText(data.storeConfig.storeName, centerX, y, headerPaint)
        y += 28f
        if (data.storeConfig.storeNameBurmese.isNotBlank()) {
            val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 19f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(data.storeConfig.storeNameBurmese, centerX, y, subHeaderPaint)
            y += 26f
        }

        val smallCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 17f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(data.storeConfig.address, centerX, y, smallCenterPaint)
        y += 24f
        canvas.drawText("Tel: ${data.storeConfig.phone}", centerX, y, smallCenterPaint)
        y += 28f

        // Separator
        drawDashedLine(canvas, padding, y, width - padding, linePaint)
        y += 26f

        // 2. Receipt Meta
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Receipt #: ${data.receiptNumber}", padding, y, boldPaint)
        y += 24f
        canvas.drawText("Date: ${data.dateFormatted}", padding, y, textPaint)
        y += 24f
        if (!data.customerName.isNullOrBlank()) {
            canvas.drawText("Customer: ${data.customerName} (${data.customerPhone ?: ""})", padding, y, textPaint)
            y += 24f
        }
        canvas.drawText("Cashier: ${data.cashierName}", padding, y, textPaint)
        y += 28f

        drawDashedLine(canvas, padding, y, width - padding, linePaint)
        y += 24f

        // 3. Item Table Header
        canvas.drawText("Item / Description", padding, y, boldPaint)
        val rightAlignBold = Paint(boldPaint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("Total", width - padding, y, rightAlignBold)
        y += 20f
        drawDashedLine(canvas, padding, y, width - padding, linePaint)
        y += 26f

        // 4. Items List
        val rightAlignText = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }
        val smallTextPaint = Paint(textPaint).apply { textSize = 17f; color = Color.DKGRAY }

        for (item in data.items) {
            // Line 1: Product & Variant Name
            val itemTitle = if (item.variantName.isNotBlank() && item.variantName != "Standard") {
                "${item.productName} - ${item.variantName}"
            } else {
                item.productName
            }
            canvas.drawText(itemTitle, padding, y, textPaint)
            y += 22f

            // Line 2: Qty x UnitPrice -> Total
            val qtyUnitText = "  ${item.quantity} x ${Formatters.formatMmk(item.unitPrice)}"
            canvas.drawText(qtyUnitText, padding, y, smallTextPaint)
            canvas.drawText(Formatters.formatMmk(item.totalPrice), width - padding, y, rightAlignText)
            y += 28f
        }

        drawDashedLine(canvas, padding, y, width - padding, linePaint)
        y += 26f

        // 5. Totals & Payment Summary
        fun drawRow(label: String, value: String, isBold: Boolean = false) {
            val left = if (isBold) boldPaint else textPaint
            val right = if (isBold) rightAlignBold else rightAlignText
            canvas.drawText(label, padding, y, left)
            canvas.drawText(value, width - padding, y, right)
            y += 26f
        }

        drawRow("Subtotal:", Formatters.formatMmk(data.subtotal))
        if (data.discountAmount > 0) {
            drawRow("Discount:", "-${Formatters.formatMmk(data.discountAmount)}")
        }
        if (data.deliFee > 0) {
            drawRow("Delivery Fee:", "+${Formatters.formatMmk(data.deliFee)}")
        }

        drawDashedLine(canvas, padding, y, width - padding, linePaint)
        y += 28f

        // Grand Total in larger font
        val grandLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
        }
        val grandValuePaint = Paint(grandLabelPaint).apply {
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("GRAND TOTAL:", padding, y, grandLabelPaint)
        canvas.drawText(Formatters.formatMmk(data.grandTotal), width - padding, y, grandValuePaint)
        y += 32f

        drawDashedLine(canvas, padding, y, width - padding, linePaint)
        y += 26f

        // Payment Info
        drawRow("Payment Method:", data.paymentType)
        if (data.cashReceived > 0) {
            drawRow("Cash Received:", Formatters.formatMmk(data.cashReceived))
            drawRow("Change:", Formatters.formatMmk(data.changeReturned))
        }
        if (!data.paymentNotes.isNullOrBlank()) {
            drawRow("Payment Note:", data.paymentNotes)
        }
        y += 16f

        // 6. Footer message
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 17f
            textAlign = Paint.Align.CENTER
        }
        val lines = data.storeConfig.footerMessage.split("\n")
        for (line in lines) {
            canvas.drawText(line, centerX, y, footerPaint)
            y += 24f
        }
        y += 20f

        // Trim bitmap to actual content height
        val finalHeight = y.toInt().coerceAtLeast(100)
        return Bitmap.createBitmap(bitmap, 0, 0, width, finalHeight)
    }

    private fun drawDashedLine(canvas: Canvas, startX: Float, y: Float, endX: Float, paint: Paint) {
        var x = startX
        val dashLength = 8f
        val spaceLength = 5f
        while (x < endX) {
            canvas.drawLine(x, y, (x + dashLength).coerceAtMost(endX), y, paint)
            x += dashLength + spaceLength
        }
    }
}
