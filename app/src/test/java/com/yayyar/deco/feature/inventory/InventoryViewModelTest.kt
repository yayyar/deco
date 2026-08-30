package com.yayyar.deco.feature.inventory

import com.yayyar.deco.core.data.repository.CategoryRepository
import com.yayyar.deco.core.data.repository.ProductRepository
import com.yayyar.deco.core.database.entity.CategoryEntity
import com.yayyar.deco.core.database.entity.ProductEntity
import com.yayyar.deco.core.database.entity.ProductVariantEntity
import com.yayyar.deco.core.database.model.ProductWithVariants
import com.yayyar.deco.core.database.model.VariantWithProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeCategoryRepository : CategoryRepository {
    val categoriesFlow = MutableStateFlow<List<CategoryEntity>>(emptyList())

    override fun getAllCategoriesFlow(): Flow<List<CategoryEntity>> = categoriesFlow
    override suspend fun getAllCategories(): List<CategoryEntity> = categoriesFlow.value
    override suspend fun getCategoryById(id: String): CategoryEntity? = categoriesFlow.value.find { it.id == id }
    override suspend fun insertCategory(category: CategoryEntity): Long {
        categoriesFlow.value = categoriesFlow.value + category
        return 1L
    }
    override suspend fun insertCategories(categories: List<CategoryEntity>) {
        categoriesFlow.value = categoriesFlow.value + categories
    }
    override suspend fun updateCategory(category: CategoryEntity) {
        categoriesFlow.value = categoriesFlow.value.map { if (it.id == category.id) category else it }
    }
    override suspend fun deleteCategory(category: CategoryEntity) {
        categoriesFlow.value = categoriesFlow.value.filterNot { it.id == category.id }
    }
    override suspend fun deleteCategoryById(id: String): Int {
        val count = categoriesFlow.value.count { it.id == id }
        categoriesFlow.value = categoriesFlow.value.filterNot { it.id == id }
        return count
    }
}

class FakeProductRepository : ProductRepository {
    val productsFlow = MutableStateFlow<List<ProductWithVariants>>(emptyList())

    override fun getActiveProductsWithVariantsFlow(): Flow<List<ProductWithVariants>> = productsFlow
    override fun getAllProductsWithVariantsFlow(): Flow<List<ProductWithVariants>> = productsFlow
    override fun getProductsByCategoryFlow(categoryId: String): Flow<List<ProductWithVariants>> = productsFlow
    override fun searchProductsFlow(query: String): Flow<List<ProductWithVariants>> = productsFlow
    override suspend fun getProductWithVariantsById(id: String): ProductWithVariants? =
        productsFlow.value.find { it.product.id == id }
    override suspend fun getProductById(id: String): ProductEntity? =
        productsFlow.value.find { it.product.id == id }?.product
    override fun getVariantsByProductIdFlow(productId: String): Flow<List<ProductVariantEntity>> =
        MutableStateFlow(emptyList())
    override suspend fun getVariantsByProductId(productId: String): List<ProductVariantEntity> = emptyList()
    override suspend fun getVariantById(id: String): ProductVariantEntity? = null
    override suspend fun getVariantByBarcode(barcode: String): VariantWithProduct? = null
    override fun getLowStockVariantsFlow(): Flow<List<ProductVariantEntity>> = MutableStateFlow(emptyList())

    override suspend fun saveProductWithVariants(
        product: ProductEntity,
        variants: List<ProductVariantEntity>
    ) {
        val updated = productsFlow.value.filterNot { it.product.id == product.id } +
                ProductWithVariants(product, variants)
        productsFlow.value = updated
    }

    override suspend fun deleteProductWithVariants(product: ProductEntity) {
        productsFlow.value = productsFlow.value.filterNot { it.product.id == product.id }
    }

    override suspend fun adjustVariantStock(variantId: String, deltaQty: Int): Boolean = true
    override suspend fun setVariantStock(variantId: String, newStock: Int): Boolean = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var fakeProductRepo: FakeProductRepository
    private lateinit var viewModel: InventoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCategoryRepo = FakeCategoryRepository()
        fakeProductRepo = FakeProductRepository()
        viewModel = InventoryViewModel(fakeProductRepo, fakeCategoryRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addCategory_addsCategoryToRepository() = runTest {
        viewModel.addCategory("Traditional Silk")
        testDispatcher.scheduler.advanceUntilIdle()

        val categories = fakeCategoryRepo.getAllCategories()
        assertEquals(1, categories.size)
        assertEquals("Traditional Silk", categories[0].name)
    }

    @Test
    fun saveProductWithVariants_updatesProductsInRepository() = runTest {
        val product = ProductEntity(
            id = "prod_1",
            name = "Silk Longyi",
            categoryId = "cat_1"
        )
        val variant = ProductVariantEntity(
            id = "var_1",
            productId = "prod_1",
            colorPattern = "Gold",
            sellPrice = 45000.0,
            stockQty = 10
        )

        viewModel.saveProductWithVariants(product, listOf(variant))
        testDispatcher.scheduler.advanceUntilIdle()

        val products = fakeProductRepo.productsFlow.value
        assertEquals(1, products.size)
        assertEquals("Silk Longyi", products[0].product.name)
        assertEquals(1, products[0].variants.size)
    }
}
