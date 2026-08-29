package com.yayyar.deco.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yayyar.deco.core.database.entity.OrderEntity
import com.yayyar.deco.core.database.entity.OrderItemEntity
import com.yayyar.deco.core.database.model.CategorySalesSummary
import com.yayyar.deco.core.database.model.DailySalesSummary
import com.yayyar.deco.core.database.model.OrderWithItems
import com.yayyar.deco.core.database.model.TopSellingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders ORDER BY created_at DESC")
    fun getAllOrdersWithItemsFlow(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE shift_id = :shiftId ORDER BY created_at DESC")
    fun getOrdersByShiftFlow(shiftId: String): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderWithItemsById(orderId: String): OrderWithItems?

    @Transaction
    @Query("SELECT * FROM orders WHERE receipt_number = :receiptNumber")
    suspend fun getOrderByReceiptNumber(receiptNumber: String): OrderWithItems?

    @Query("SELECT * FROM orders WHERE created_at >= :startTime AND created_at <= :endTime ORDER BY created_at DESC")
    fun getOrdersBetweenFlow(startTime: Long, endTime: Long): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("""
        SELECT 
            COUNT(id) as totalOrders,
            COALESCE(SUM(grand_total), 0.0) as totalSales,
            COALESCE(SUM(discount_amount), 0.0) as totalDiscount,
            COALESCE(SUM(deli_fee), 0.0) as totalDeli,
            COALESCE(SUM(CASE WHEN payment_type = 'CASH' THEN grand_total WHEN payment_type = 'SPLIT' THEN cash_received - change_returned ELSE 0.0 END), 0.0) as totalCash,
            COALESCE(SUM(CASE WHEN payment_type = 'KPAY' THEN grand_total WHEN payment_type = 'SPLIT' THEN kpay_amount ELSE 0.0 END), 0.0) as totalKpay,
            COALESCE(SUM(CASE WHEN payment_type = 'WAVEPAY' THEN grand_total WHEN payment_type = 'SPLIT' THEN wave_amount ELSE 0.0 END), 0.0) as totalWave
        FROM orders 
        WHERE created_at >= :startTime AND created_at <= :endTime AND order_status = 'COMPLETED'
    """)
    fun getDailySalesSummaryFlow(startTime: Long, endTime: Long): Flow<DailySalesSummary>

    @Query("""
        SELECT 
            oi.variant_id as variantId,
            oi.product_name as productName,
            oi.variant_name as variantName,
            SUM(oi.quantity) as totalQuantitySold,
            SUM(oi.total_price) as totalRevenue
        FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        WHERE o.created_at >= :startTime AND o.created_at <= :endTime AND o.order_status = 'COMPLETED'
        GROUP BY oi.variant_id
        ORDER BY totalQuantitySold DESC
        LIMIT :limit
    """)
    fun getTopSellingItemsFlow(startTime: Long, endTime: Long, limit: Int): Flow<List<TopSellingItem>>

    @Query("""
        SELECT 
            COALESCE(c.name, 'Uncategorized') as categoryName,
            SUM(oi.quantity) as totalQuantity,
            SUM(oi.total_price) as totalRevenue
        FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        LEFT JOIN product_variants v ON oi.variant_id = v.id
        LEFT JOIN products p ON v.product_id = p.id
        LEFT JOIN categories c ON p.category_id = c.id
        WHERE o.created_at >= :startTime AND o.created_at <= :endTime AND o.order_status = 'COMPLETED'
        GROUP BY c.name
        ORDER BY totalRevenue DESC
    """)
    fun getCategorySalesSummaryFlow(startTime: Long, endTime: Long): Flow<List<CategorySalesSummary>>
}
