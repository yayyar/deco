package com.yayyar.teahouse.core.data.repository

import com.yayyar.teahouse.core.database.dao.PaymentMethodDao
import com.yayyar.teahouse.core.database.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface PaymentMethodRepository {
    fun getAllPaymentMethodsFlow(): Flow<List<PaymentMethodEntity>>
    suspend fun getAllPaymentMethods(): List<PaymentMethodEntity>
    fun getActivePaymentMethodsFlow(): Flow<List<PaymentMethodEntity>>
    suspend fun getActivePaymentMethods(): List<PaymentMethodEntity>
    suspend fun getPaymentMethodById(id: String): PaymentMethodEntity?
    suspend fun getPaymentMethodByCode(code: String): PaymentMethodEntity?
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity): Long
    suspend fun insertPaymentMethods(paymentMethods: List<PaymentMethodEntity>)
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethodEntity)
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethodEntity)
    suspend fun deletePaymentMethodById(id: String): Int
    suspend fun updateActiveStatus(id: String, isActive: Boolean)
}

@Singleton
class PaymentMethodRepositoryImpl @Inject constructor(
    private val paymentMethodDao: PaymentMethodDao
) : PaymentMethodRepository {

    override fun getAllPaymentMethodsFlow(): Flow<List<PaymentMethodEntity>> =
        paymentMethodDao.getAllPaymentMethodsFlow()

    override suspend fun getAllPaymentMethods(): List<PaymentMethodEntity> =
        paymentMethodDao.getAllPaymentMethods()

    override fun getActivePaymentMethodsFlow(): Flow<List<PaymentMethodEntity>> =
        paymentMethodDao.getActivePaymentMethodsFlow()

    override suspend fun getActivePaymentMethods(): List<PaymentMethodEntity> =
        paymentMethodDao.getActivePaymentMethods()

    override suspend fun getPaymentMethodById(id: String): PaymentMethodEntity? =
        paymentMethodDao.getPaymentMethodById(id)

    override suspend fun getPaymentMethodByCode(code: String): PaymentMethodEntity? =
        paymentMethodDao.getPaymentMethodByCode(code)

    override suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity): Long =
        paymentMethodDao.insertPaymentMethod(paymentMethod)

    override suspend fun insertPaymentMethods(paymentMethods: List<PaymentMethodEntity>) =
        paymentMethodDao.insertPaymentMethods(paymentMethods)

    override suspend fun updatePaymentMethod(paymentMethod: PaymentMethodEntity) =
        paymentMethodDao.updatePaymentMethod(paymentMethod)

    override suspend fun deletePaymentMethod(paymentMethod: PaymentMethodEntity) =
        paymentMethodDao.deletePaymentMethod(paymentMethod)

    override suspend fun deletePaymentMethodById(id: String): Int =
        paymentMethodDao.deletePaymentMethodById(id)

    override suspend fun updateActiveStatus(id: String, isActive: Boolean) =
        paymentMethodDao.updateActiveStatus(id, isActive)
}
