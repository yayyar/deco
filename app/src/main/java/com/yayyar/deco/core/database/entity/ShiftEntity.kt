package com.yayyar.deco.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "shifts",
    indices = [
        Index(value = ["status"]),
        Index(value = ["opened_at"])
    ]
)
data class ShiftEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "opened_at")
    val openedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "closed_at")
    val closedAt: Long? = null,
    @ColumnInfo(name = "opening_float")
    val openingFloat: Double = 0.0,
    @ColumnInfo(name = "closing_cash_actual")
    val closingCashActual: Double? = null,
    @ColumnInfo(name = "closing_cash_expected")
    val closingCashExpected: Double? = null,
    @ColumnInfo(name = "total_sales_cash")
    val totalSalesCash: Double = 0.0,
    @ColumnInfo(name = "total_sales_kpay")
    val totalSalesKpay: Double = 0.0,
    @ColumnInfo(name = "total_sales_wave")
    val totalSalesWave: Double = 0.0,
    val notes: String? = null,
    val status: String = "OPEN" // OPEN, CLOSED
)

@Entity(
    tableName = "cash_movements",
    foreignKeys = [
        ForeignKey(
            entity = ShiftEntity::class,
            parentColumns = ["id"],
            childColumns = ["shift_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["shift_id"]),
        Index(value = ["timestamp"])
    ]
)
data class CashMovementEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "shift_id")
    val shiftId: String,
    val type: String, // CASH_IN, CASH_OUT
    val amount: Double,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
