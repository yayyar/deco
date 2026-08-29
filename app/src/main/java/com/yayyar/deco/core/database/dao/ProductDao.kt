package com.yayyar.deco.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yayyar.deco.core.database.entity.ProductEntity
import com.yayyar.deco.core.database.model.ProductWithVariants
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Transaction
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY updated_at DESC")
    fun getActiveProductsWithVariantsFlow(): Flow<List<ProductWithVariants>>

    @Transaction
    @Query("SELECT * FROM products ORDER BY updated_at DESC")
    fun getAllProductsWithVariantsFlow(): Flow<List<ProductWithVariants>>

    @Transaction
    @Query("SELECT * FROM products WHERE category_id = :categoryId AND is_active = 1 ORDER BY name ASC")
    fun getProductsByCategoryFlow(categoryId: String): Flow<List<ProductWithVariants>>

    @Transaction
    @Query("""
        SELECT DISTINCT p.* FROM products p 
        LEFT JOIN product_variants v ON p.id = v.product_id 
        WHERE p.is_active = 1 AND (
            p.name LIKE '%' || :query || '%' OR 
            v.barcode LIKE '%' || :query || '%' OR 
            v.sku LIKE '%' || :query || '%' OR
            v.color_pattern LIKE '%' || :query || '%'
        )
        ORDER BY p.name ASC
    """)
    fun searchProductsFlow(query: String): Flow<List<ProductWithVariants>>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductWithVariantsById(id: String): ProductWithVariants?

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String): Int
}
