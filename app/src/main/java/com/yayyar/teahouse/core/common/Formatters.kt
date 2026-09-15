package com.yayyar.teahouse.core.common

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val mmkFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatMmk(amount: Double): String {
        return "${mmkFormat.format(amount)} Ks"
    }

    fun formatMmk(amount: Long): String {
        return "${mmkFormat.format(amount)} Ks"
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun generateReceiptNumber(sequence: Long = System.currentTimeMillis() % 10000): String {
        val datePrefix = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val formattedSeq = String.format(Locale.US, "%04d", sequence % 10000)
        return "REC-$datePrefix-$formattedSeq"
    }
}
