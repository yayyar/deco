package com.yayyar.deco.feature.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.yayyar.deco.core.database.DecoDatabase
import com.yayyar.deco.core.database.entity.CategoryEntity
import com.yayyar.deco.core.database.entity.ProductEntity
import com.yayyar.deco.core.database.entity.ProductVariantEntity
import com.yayyar.deco.core.database.model.ProductWithVariants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DecoDatabase.getInstance(application)
    private val categoryDao = db.categoryDao()
    private val productDao = db.productDao()
    private val variantDao = db.variantDao()

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showOnlyLowStock = MutableStateFlow(false)
    val showOnlyLowStock: StateFlow<Boolean> = _showOnlyLowStock.asStateFlow()

    val products: StateFlow<List<ProductWithVariants>> = combine(
        productDao.getAllProductsWithVariantsFlow(),
        _selectedCategoryId,
        _searchQuery,
        _showOnlyLowStock
    ) { allProducts, catId, query, onlyLowStock ->
        allProducts.filter { pwv ->
            val matchCat = catId == null || pwv.product.categoryId == catId
            val matchQuery = query.isBlank() ||
                    pwv.product.name.contains(query, ignoreCase = true) ||
                    pwv.variants.any {
                        (it.barcode?.contains(query, ignoreCase = true) == true) ||
                                (it.sku?.contains(query, ignoreCase = true) == true) ||
                                it.colorPattern.contains(query, ignoreCase = true)
                    }
            val matchLowStock = !onlyLowStock || pwv.hasLowStock
            matchCat && matchQuery && matchLowStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleLowStockFilter() {
        _showOnlyLowStock.value = !_showOnlyLowStock.value
    }

    fun saveProductWithVariants(product: ProductEntity, variants: List<ProductVariantEntity>) {
        viewModelScope.launch {
            db.withTransaction {
                productDao.insertProduct(product)
                variantDao.deleteVariantsByProductId(product.id)
                variantDao.insertVariants(variants)
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productDao.deleteProduct(product)
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val count = categoryDao.getAllCategories().size
            categoryDao.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    sortOrder = count + 1
                )
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
    }

    fun updateStock(variantId: String, newStock: Int) {
        viewModelScope.launch {
            variantDao.setStockDirect(variantId, newStock, System.currentTimeMillis())
        }
    }
}
