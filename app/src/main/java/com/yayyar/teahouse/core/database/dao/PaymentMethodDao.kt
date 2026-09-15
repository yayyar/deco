package com.yayyar.teahouse.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yayyar.teahouse.core.database.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {

    @Query("SELECT * FROM payment_methods ORDER BY sort_order ASC, created_at ASC")
    fun getAllPaymentMethodsFlow(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods ORDER BY sort_order ASC, created_at ASC")
    suspend fun getAllPaymentMethods(): List<PaymentMethodEntity>

    @Query("SELECT * FROM payment_methods WHERE is_active = 1 ORDER BY sort_order ASC, created_at ASC")
    fun getActivePaymentMethodsFlow(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE is_active = 1 ORDER BY sort_order ASC, created_at ASC")
    suspend fun getActivePaymentMethods(): List<PaymentMethodEntity>

    @Query("SELECT * FROM payment_methods WHERE id = :id LIMIT 1")
    suspend fun getPaymentMethodById(id: String): PaymentMethodEntity?

    @Query("SELECT * FROM payment_methods WHERE code = :code LIMIT 1")
    suspend fun getPaymentMethodByCode(code: String): PaymentMethodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(paymentMethods: List<PaymentMethodEntity>)

    @Update
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethodEntity)

    @Delete
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethodEntity)

    @Query("DELETE FROM payment_methods WHERE id = :id")
    suspend fun deletePaymentMethodById(id: String): Int

    @Query("UPDATE payment_methods SET is_active = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: String, isActive: Boolean)
}
