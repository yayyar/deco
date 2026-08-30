package com.yayyar.deco.core.data.repository

import androidx.room.withTransaction
import com.yayyar.deco.core.common.Resource
import com.yayyar.deco.core.database.DecoDatabase
import com.yayyar.deco.core.database.dao.OrderDao
import com.yayyar.deco.core.database.dao.VariantDao
import com.yayyar.deco.core.database.entity.OrderEntity
import com.yayyar.deco.core.database.entity.OrderItemEntity
import com.yayyar.deco.core.database.model.CategorySalesSummary
import com.yayyar.deco.core.database.model.DailySalesSummary
import com.yayyar.deco.core.database.model.OrderWithItems
import com.yayyar.deco.core.database.model.TopSellingItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface OrderRepository {
    fun getAllOrdersWithItemsFlow(): Flow<List<OrderWithItems>>
    fun getOrdersByShiftFlow(shiftId: String): Flow<List<OrderWithItems>>
    suspend fun getOrderWithItemsById(orderId: String): OrderWithItems?
    suspend fun getOrderByReceiptNumber(receiptNumber: String): OrderWithItems?
    fun getOrdersBetweenFlow(startTime: Long, endTime: Long): Flow<List<OrderEntity>>
    fun getDailySalesSummaryFlow(startTime: Long, endTime: Long): Flow<DailySalesSummary>
    fun getTopSellingItemsFlow(startTime: Long, endTime: Long, limit: Int = 10): Flow<List<TopSellingItem>>
    fun getCategorySalesSummaryFlow(startTime: Long, endTime: Long): Flow<List<CategorySalesSummary>>
    suspend fun checkoutOrder(order: OrderEntity, items: List<OrderItemEntity>): Resource<Unit>
}

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val database: DecoDatabase,
    private val orderDao: OrderDao,
    private val variantDao: VariantDao
) : OrderRepository {

    override fun getAllOrdersWithItemsFlow(): Flow<List<OrderWithItems>> =
        orderDao.getAllOrdersWithItemsFlow()

    override fun getOrdersByShiftFlow(shiftId: String): Flow<List<OrderWithItems>> =
        orderDao.getOrdersByShiftFlow(shiftId)

    override suspend fun getOrderWithItemsById(orderId: String): OrderWithItems? =
        orderDao.getOrderWithItemsById(orderId)

    override suspend fun getOrderByReceiptNumber(receiptNumber: String): OrderWithItems? =
        orderDao.getOrderByReceiptNumber(receiptNumber)

    override fun getOrdersBetweenFlow(startTime: Long, endTime: Long): Flow<List<OrderEntity>> =
        orderDao.getOrdersBetweenFlow(startTime, endTime)

    override fun getDailySalesSummaryFlow(startTime: Long, endTime: Long): Flow<DailySalesSummary> =
        orderDao.getDailySalesSummaryFlow(startTime, endTime)

    override fun getTopSellingItemsFlow(
        startTime: Long,
        endTime: Long,
        limit: Int
    ): Flow<List<TopSellingItem>> =
        orderDao.getTopSellingItemsFlow(startTime, endTime, limit)

    override fun getCategorySalesSummaryFlow(
        startTime: Long,
        endTime: Long
    ): Flow<List<CategorySalesSummary>> =
        orderDao.getCategorySalesSummaryFlow(startTime, endTime)

    override suspend fun checkoutOrder(
        order: OrderEntity,
        items: List<OrderItemEntity>
    ): Resource<Unit> {
        return try {
            database.withTransaction {
                val timestamp = System.currentTimeMillis()
                for (item in items) {
                    val affected = variantDao.deductStockAtomic(item.variantId, item.quantity, timestamp)
                    if (affected == 0) {
                        throw IllegalStateException("Insufficient stock for: ${item.productName} (${item.variantName})")
                    }
                }
                orderDao.insertOrder(order)
                orderDao.insertOrderItems(items)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to process checkout transaction")
        }
    }
}
