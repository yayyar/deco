package com.yayyar.deco.feature.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayyar.deco.core.data.repository.CategoryRepository
import com.yayyar.deco.core.data.repository.ProductRepository
import com.yayyar.deco.core.database.entity.CategoryEntity
import com.yayyar.deco.core.database.entity.ProductEntity
import com.yayyar.deco.core.database.entity.ProductVariantEntity
import com.yayyar.deco.core.database.model.ProductWithVariants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductWithVariants>> = productRepository.getAllProductsWithVariantsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categorySearchQuery = MutableStateFlow("")
    val categorySearchQuery: StateFlow<String> = _categorySearchQuery.asStateFlow()

    private val _showOnlyLowStock = MutableStateFlow(false)
    val showOnlyLowStock: StateFlow<Boolean> = _showOnlyLowStock.asStateFlow()

    val filteredCategories: StateFlow<List<CategoryEntity>> = combine(
        categories,
        _categorySearchQuery
    ) { allCats, query ->
        if (query.isBlank()) {
            allCats
        } else {
            allCats.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductWithVariants>> = combine(
        allProducts,
        _selectedCategoryId,
        _searchQuery,
        _showOnlyLowStock
    ) { allProds, catId, query, onlyLowStock ->
        allProds.filter { pwv ->
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

    fun setCategorySearchQuery(query: String) {
        _categorySearchQuery.value = query
    }

    fun toggleLowStockFilter() {
        _showOnlyLowStock.value = !_showOnlyLowStock.value
    }

    fun saveProductWithVariants(product: ProductEntity, variants: List<ProductVariantEntity>) {
        viewModelScope.launch {
            productRepository.saveProductWithVariants(product, variants)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.deleteProductWithVariants(product)
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val count = categoryRepository.getAllCategories().size
            categoryRepository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    sortOrder = count + 1
                )
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }

    fun adjustStock(variantId: String, deltaQty: Int) {
        viewModelScope.launch {
            productRepository.adjustVariantStock(variantId, deltaQty)
        }
    }

    fun updateStock(variantId: String, newStock: Int) {
        viewModelScope.launch {
            productRepository.setVariantStock(variantId, newStock)
        }
    }
}
