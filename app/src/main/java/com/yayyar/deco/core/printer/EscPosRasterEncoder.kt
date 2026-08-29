package com.yayyar.deco.core.printer

import android.graphics.Bitmap
import android.graphics.Color
import java.io.ByteArrayOutputStream

object EscPosRasterEncoder {

    // ESC/POS Commands
    private val ESC_INIT = byteArrayOf(0x1B, 0x40) // Initialize printer
    private val GS_CUT = byteArrayOf(0x1D, 0x56, 0x42, 0x00) // Partial Cut with feed
    private val LINE_FEED = byteArrayOf(0x0A)

    /**
     * Converts a Bitmap into an ESC/POS raster bit image command stream (GS v 0).
     * Works with all standard 58mm and 80mm ESC/POS thermal receipt printers.
     */
    fun encodeBitmapToEscPos(bitmap: Bitmap, autoCut: Boolean = true): ByteArray {
        val width = bitmap.width
        val height = bitmap.height

        // Width in bytes (must be multiple of 8)
        val widthBytes = (width + 7) / 8
        val output = ByteArrayOutputStream()

        // 1. Initialize printer
        output.write(ESC_INIT)

        // 2. Line spacing standard
        output.write(byteArrayOf(0x1B, 0x33, 0x00))

        // 3. Raster bit image command: GS v 0 m xL xH yL yH
        // m = 0 (Normal mode)
        val xL = (widthBytes and 0xFF).toByte()
        val xH = ((widthBytes shr 8) and 0xFF).toByte()
        val yL = (height and 0xFF).toByte()
        val yH = ((height shr 8) and 0xFF).toByte()

        output.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00, xL, xH, yL, yH))

        // 4. Pixel data encoding (1 = black dot, 0 = white dot)
        for (y in 0 until height) {
            for (byteX in 0 until widthBytes) {
                var byteVal = 0
                for (bit in 0 until 8) {
                    val px = (byteX * 8) + bit
                    if (px < width) {
                        val pixel = bitmap.getPixel(px, y)
                        // Luminance calculation
                        val red = Color.red(pixel)
                        val green = Color.green(pixel)
                        val blue = Color.blue(pixel)
                        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)

                        // Threshold: dark pixels get printed
                        if (luminance < 160) {
                            byteVal = byteVal or (1 shl (7 - bit))
                        }
                    }
                }
                output.write(byteVal)
            }
        }

        // 5. Feed lines
        output.write(LINE_FEED)
        output.write(LINE_FEED)
        output.write(LINE_FEED)

        // 6. Cut paper if requested
        if (autoCut) {
            output.write(GS_CUT)
        }

        return output.toByteArray()
    }
}
