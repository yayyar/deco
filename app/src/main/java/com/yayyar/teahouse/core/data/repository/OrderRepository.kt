package com.yayyar.teahouse.core.data.repository

import androidx.room.withTransaction
import com.yayyar.teahouse.core.common.Resource
import com.yayyar.teahouse.core.database.DecoDatabase
import com.yayyar.teahouse.core.database.dao.OrderDao
import com.yayyar.teahouse.core.database.dao.VariantDao
import com.yayyar.teahouse.core.database.entity.OrderEntity
import com.yayyar.teahouse.core.database.entity.OrderItemEntity
import com.yayyar.teahouse.core.database.model.CategorySalesSummary
import com.yayyar.teahouse.core.database.model.DailySalesSummary
import com.yayyar.teahouse.core.database.model.OrderWithItems
import com.yayyar.teahouse.core.database.model.PaymentMethodSalesSummary
import com.yayyar.teahouse.core.database.model.TopSellingItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface OrderRepository {
    fun getAllOrdersWithItemsFlow(): Flow<List<OrderWithItems>>
    suspend fun getOrdersWithItemsPaged(startTime: Long, endTime: Long, limit: Int, offset: Int): List<OrderWithItems>
    suspend fun getOrderWithItemsById(orderId: String): OrderWithItems?
    suspend fun getOrderByReceiptNumber(receiptNumber: String): OrderWithItems?
    fun getOrdersBetweenFlow(startTime: Long, endTime: Long): Flow<List<OrderEntity>>
    fun getDailySalesSummaryFlow(startTime: Long, endTime: Long): Flow<DailySalesSummary>
    fun getPaymentMethodSalesSummaryFlow(startTime: Long, endTime: Long): Flow<List<PaymentMethodSalesSummary>>
    fun getTopSellingItemsFlow(startTime: Long, endTime: Long, limit: Int = 10): Flow<List<TopSellingItem>>
    suspend fun getTopSellingItemsPaged(startTime: Long, endTime: Long, limit: Int, offset: Int): List<TopSellingItem>
    fun getDraftOrdersWithItemsFlow(): Flow<List<OrderWithItems>>
    fun getCategorySalesSummaryFlow(startTime: Long, endTime: Long): Flow<List<CategorySalesSummary>>
    suspend fun checkoutOrder(order: OrderEntity, items: List<OrderItemEntity>): Resource<Unit>
    suspend fun saveDraftOrder(order: OrderEntity, items: List<OrderItemEntity>): Resource<Unit>
    suspend fun deleteDraftOrder(orderId: String): Resource<Unit>
}

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val database: DecoDatabase,
    private val orderDao: OrderDao,
    private val variantDao: VariantDao
) : OrderRepository {

    override fun getAllOrdersWithItemsFlow(): Flow<List<OrderWithItems>> =
        orderDao.getAllOrdersWithItemsFlow()

    override fun getDraftOrdersWithItemsFlow(): Flow<List<OrderWithItems>> =
        orderDao.getDraftOrdersWithItemsFlow()

    override suspend fun getOrdersWithItemsPaged(
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int
    ): List<OrderWithItems> =
        orderDao.getOrdersWithItemsPaged(startTime, endTime, limit, offset)

    override suspend fun getOrderWithItemsById(orderId: String): OrderWithItems? =
        orderDao.getOrderWithItemsById(orderId)

    override suspend fun getOrderByReceiptNumber(receiptNumber: String): OrderWithItems? =
        orderDao.getOrderByReceiptNumber(receiptNumber)

    override fun getOrdersBetweenFlow(startTime: Long, endTime: Long): Flow<List<OrderEntity>> =
        orderDao.getOrdersBetweenFlow(startTime, endTime)

    override fun getDailySalesSummaryFlow(startTime: Long, endTime: Long): Flow<DailySalesSummary> =
        orderDao.getDailySalesSummaryFlow(startTime, endTime)

    override fun getPaymentMethodSalesSummaryFlow(
        startTime: Long,
        endTime: Long
    ): Flow<List<PaymentMethodSalesSummary>> =
        orderDao.getPaymentMethodSalesSummaryFlow(startTime, endTime)

    override fun getTopSellingItemsFlow(
        startTime: Long,
        endTime: Long,
        limit: Int
    ): Flow<List<TopSellingItem>> =
        orderDao.getTopSellingItemsFlow(startTime, endTime, limit)

    override suspend fun getTopSellingItemsPaged(
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int
    ): List<TopSellingItem> =
        orderDao.getTopSellingItemsPaged(startTime, endTime, limit, offset)

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

    override suspend fun saveDraftOrder(
        order: OrderEntity,
        items: List<OrderItemEntity>
    ): Resource<Unit> {
        return try {
            database.withTransaction {
                orderDao.insertOrder(order.copy(orderStatus = "DRAFT"))
                orderDao.insertOrderItems(items)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save draft order")
        }
    }

    override suspend fun deleteDraftOrder(orderId: String): Resource<Unit> {
        return try {
            orderDao.deleteDraftOrder(orderId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete draft order")
        }
    }
}
