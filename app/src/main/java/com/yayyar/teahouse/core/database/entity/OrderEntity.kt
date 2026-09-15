package com.yayyar.teahouse.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["receipt_number"], unique = true),
        Index(value = ["created_at"]),
        Index(value = ["sync_status"])
    ]
)
data class OrderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "receipt_number")
    val receiptNumber: String,
    val subtotal: Double,
    @ColumnInfo(name = "discount_amount")
    val discountAmount: Double = 0.0,
    @ColumnInfo(name = "discount_type")
    val discountType: String = "FIXED", // FIXED, PERCENT
    @ColumnInfo(name = "deli_fee")
    val deliFee: Double = 0.0,
    @ColumnInfo(name = "grand_total")
    val grandTotal: Double,
    @ColumnInfo(name = "payment_type")
    val paymentType: String = "CASH", // CASH, KPAY, WAVEPAY, SPLIT
    @ColumnInfo(name = "cash_received")
    val cashReceived: Double = 0.0,
    @ColumnInfo(name = "change_returned")
    val changeReturned: Double = 0.0,
    @ColumnInfo(name = "kpay_amount")
    val kpayAmount: Double = 0.0,
    @ColumnInfo(name = "wave_amount")
    val waveAmount: Double = 0.0,
    @ColumnInfo(name = "payment_notes")
    val paymentNotes: String? = null,
    @ColumnInfo(name = "customer_name")
    val customerName: String? = null,
    @ColumnInfo(name = "customer_phone")
    val customerPhone: String? = null,
    @ColumnInfo(name = "customer_address")
    val customerAddress: String? = null,
    @ColumnInfo(name = "order_status")
    val orderStatus: String = "COMPLETED", // COMPLETED, CANCELLED, REFUNDED
    @ColumnInfo(name = "sale_type")
    val saleType: String = "RETAIL", // RETAIL, WHOLESALE
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int = 1,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["order_id"]),
        Index(value = ["variant_id"])
    ]
)
data class OrderItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "order_id")
    val orderId: String,
    @ColumnInfo(name = "variant_id")
    val variantId: String,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "variant_name")
    val variantName: String,
    val quantity: Int,
    @ColumnInfo(name = "unit_price")
    val unitPrice: Double,
    @ColumnInfo(name = "total_price")
    val totalPrice: Double
)
