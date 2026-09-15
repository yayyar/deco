package com.yayyar.teahouse.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yayyar.teahouse.core.database.entity.ProductVariantEntity
import com.yayyar.teahouse.core.database.model.VariantWithProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface VariantDao {
    @Query("SELECT * FROM product_variants WHERE product_id = :productId")
    fun getVariantsByProductIdFlow(productId: String): Flow<List<ProductVariantEntity>>

    @Query("SELECT * FROM product_variants WHERE product_id = :productId")
    suspend fun getVariantsByProductId(productId: String): List<ProductVariantEntity>

    @Query("SELECT * FROM product_variants WHERE id = :id")
    suspend fun getVariantById(id: String): ProductVariantEntity?

    @Transaction
    @Query("SELECT * FROM product_variants WHERE barcode = :barcode LIMIT 1")
    suspend fun getVariantByBarcode(barcode: String): VariantWithProduct?

    @Query("SELECT * FROM product_variants WHERE stock_qty <= low_stock_threshold ORDER BY stock_qty ASC")
    fun getLowStockVariantsFlow(): Flow<List<ProductVariantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariant(variant: ProductVariantEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ProductVariantEntity>)

    @Update
    suspend fun updateVariant(variant: ProductVariantEntity)

    @Delete
    suspend fun deleteVariant(variant: ProductVariantEntity)

    @Query("DELETE FROM product_variants WHERE product_id = :productId")
    suspend fun deleteVariantsByProductId(productId: String): Int

    @Query("""
        UPDATE product_variants 
        SET stock_qty = stock_qty - :quantity, 
            updated_at = :timestamp, 
            sync_status = 2 
        WHERE id = :variantId AND stock_qty >= :quantity
    """)
    suspend fun deductStockAtomic(variantId: String, quantity: Int, timestamp: Long): Int

    @Query("""
        UPDATE product_variants 
        SET stock_qty = stock_qty + :quantity, 
            updated_at = :timestamp, 
            sync_status = 2 
        WHERE id = :variantId
    """)
    suspend fun restockAtomic(variantId: String, quantity: Int, timestamp: Long): Int

    @Query("""
        UPDATE product_variants 
        SET stock_qty = :newStock, 
            updated_at = :timestamp, 
            sync_status = 2 
        WHERE id = :variantId
    """)
    suspend fun setStockDirect(variantId: String, newStock: Int, timestamp: Long): Int
}
