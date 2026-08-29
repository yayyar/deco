package com.yayyar.deco

import com.yayyar.deco.core.common.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EscPosRasterEncoderTest {

    @Test
    fun testReceiptNumberGeneration() {
        val receipt1 = Formatters.generateReceiptNumber(101)
        val receipt2 = Formatters.generateReceiptNumber(102)

        assertTrue(receipt1.startsWith("REC-"))
        assertTrue(receipt2.startsWith("REC-"))
        assertEquals(receipt1.length, receipt2.length)
    }

    @Test
    fun testDateFormatters() {
        val timestamp = 1724950000000L
        val formattedDate = Formatters.formatDate(timestamp)
        val formattedDateTime = Formatters.formatDateTime(timestamp)

        assertTrue(formattedDate.isNotEmpty())
        assertTrue(formattedDateTime.contains(formattedDate))
    }
}
