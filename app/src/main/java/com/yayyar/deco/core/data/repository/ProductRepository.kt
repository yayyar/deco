package com.yayyar.deco.core.data.repository

import androidx.room.withTransaction
import com.yayyar.deco.core.database.DecoDatabase
import com.yayyar.deco.core.database.dao.ProductDao
import com.yayyar.deco.core.database.dao.VariantDao
import com.yayyar.deco.core.database.entity.ProductEntity
import com.yayyar.deco.core.database.entity.ProductVariantEntity
import com.yayyar.deco.core.database.model.ProductWithVariants
import com.yayyar.deco.core.database.model.VariantWithProduct
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface ProductRepository {
    fun getActiveProductsWithVariantsFlow(): Flow<List<ProductWithVariants>>
    fun getAllProductsWithVariantsFlow(): Flow<List<ProductWithVariants>>
    fun getProductsByCategoryFlow(categoryId: String): Flow<List<ProductWithVariants>>
    fun searchProductsFlow(query: String): Flow<List<ProductWithVariants>>
    suspend fun getProductWithVariantsById(id: String): ProductWithVariants?
    suspend fun getProductById(id: String): ProductEntity?
    fun getVariantsByProductIdFlow(productId: String): Flow<List<ProductVariantEntity>>
    suspend fun getVariantsByProductId(productId: String): List<ProductVariantEntity>
    suspend fun getVariantById(id: String): ProductVariantEntity?
    suspend fun getVariantByBarcode(barcode: String): VariantWithProduct?
    fun getLowStockVariantsFlow(): Flow<List<ProductVariantEntity>>
    suspend fun saveProductWithVariants(product: ProductEntity, variants: List<ProductVariantEntity>)
    suspend fun deleteProductWithVariants(product: ProductEntity)
    suspend fun adjustVariantStock(variantId: String, deltaQty: Int): Boolean
    suspend fun setVariantStock(variantId: String, newStock: Int): Boolean
}

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val database: DecoDatabase,
    private val productDao: ProductDao,
    private val variantDao: VariantDao
) : ProductRepository {

    override fun getActiveProductsWithVariantsFlow(): Flow<List<ProductWithVariants>> =
        productDao.getActiveProductsWithVariantsFlow()

    override fun getAllProductsWithVariantsFlow(): Flow<List<ProductWithVariants>> =
        productDao.getAllProductsWithVariantsFlow()

    override fun getProductsByCategoryFlow(categoryId: String): Flow<List<ProductWithVariants>> =
        productDao.getProductsByCategoryFlow(categoryId)

    override fun searchProductsFlow(query: String): Flow<List<ProductWithVariants>> =
        productDao.searchProductsFlow(query)

    override suspend fun getProductWithVariantsById(id: String): ProductWithVariants? =
        productDao.getProductWithVariantsById(id)

    override suspend fun getProductById(id: String): ProductEntity? =
        productDao.getProductById(id)

    override fun getVariantsByProductIdFlow(productId: String): Flow<List<ProductVariantEntity>> =
        variantDao.getVariantsByProductIdFlow(productId)

    override suspend fun getVariantsByProductId(productId: String): List<ProductVariantEntity> =
        variantDao.getVariantsByProductId(productId)

    override suspend fun getVariantById(id: String): ProductVariantEntity? =
        variantDao.getVariantById(id)

    override suspend fun getVariantByBarcode(barcode: String): VariantWithProduct? =
        variantDao.getVariantByBarcode(barcode)

    override fun getLowStockVariantsFlow(): Flow<List<ProductVariantEntity>> =
        variantDao.getLowStockVariantsFlow()

    override suspend fun saveProductWithVariants(
        product: ProductEntity,
        variants: List<ProductVariantEntity>
    ) {
        database.withTransaction {
            productDao.insertProduct(product)
            variantDao.deleteVariantsByProductId(product.id)
            variantDao.insertVariants(variants)
        }
    }

    override suspend fun deleteProductWithVariants(product: ProductEntity) {
        database.withTransaction {
            variantDao.deleteVariantsByProductId(product.id)
            productDao.deleteProduct(product)
        }
    }

    override suspend fun adjustVariantStock(variantId: String, deltaQty: Int): Boolean {
        val timestamp = System.currentTimeMillis()
        val affected = if (deltaQty > 0) {
            variantDao.restockAtomic(variantId, deltaQty, timestamp)
        } else {
            variantDao.deductStockAtomic(variantId, -deltaQty, timestamp)
        }
        return affected > 0
    }

    override suspend fun setVariantStock(variantId: String, newStock: Int): Boolean {
        val timestamp = System.currentTimeMillis()
        val affected = variantDao.setStockDirect(variantId, newStock, timestamp)
        return affected > 0
    }
}
