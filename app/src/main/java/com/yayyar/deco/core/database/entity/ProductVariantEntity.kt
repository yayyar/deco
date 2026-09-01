package com.yayyar.deco.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "product_variants",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["product_id"]),
        Index(value = ["barcode"], unique = true),
        Index(value = ["sku"]),
        Index(value = ["sync_status"])
    ]
)
data class ProductVariantEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "product_id")
    val productId: String,
    val sku: String? = null,
    val barcode: String? = null,
    val size: String = "Free", // e.g. S, M, L, XL, Free
    @ColumnInfo(name = "color_pattern")
    val colorPattern: String = "Default", // e.g. "ကြောင်", "ဝက်", "Floral Red", "Black"
    @ColumnInfo(name = "base_price")
    val basePrice: Double = 0.0,
    @ColumnInfo(name = "sell_price")
    val sellPrice: Double,
    @ColumnInfo(name = "stock_qty")
    val stockQty: Int = 0,
    @ColumnInfo(name = "low_stock_threshold")
    val lowStockThreshold: Int = 5,
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int = 1,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) : java.io.Serializable {
    val displayName: String
        get() = when {
            size.isNotBlank() && colorPattern.isNotBlank() && colorPattern != "Default" -> "$size / $colorPattern"
            colorPattern.isNotBlank() && colorPattern != "Default" -> colorPattern
            size.isNotBlank() -> size
            else -> "Standard"
        }

    val isLowStock: Boolean
        get() = stockQty <= lowStockThreshold
}
