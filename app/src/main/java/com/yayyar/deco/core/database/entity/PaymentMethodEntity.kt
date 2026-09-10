package com.yayyar.deco.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "name")
    val name: String, // e.g. "Cash", "KBZPay", "WavePay", "AYA Pay"

    @ColumnInfo(name = "code")
    val code: String, // e.g. "CASH", "KPAY", "WAVEPAY", "AYAPAY", "OTHER"

    @ColumnInfo(name = "account_name")
    val accountName: String? = null,

    @ColumnInfo(name = "account_number")
    val accountNumber: String? = null,

    @ColumnInfo(name = "qr_code_data")
    val qrCodeData: String? = null,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
