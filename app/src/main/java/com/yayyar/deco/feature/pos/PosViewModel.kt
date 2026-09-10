package com.yayyar.deco.feature.pos

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayyar.deco.core.common.Formatters
import com.yayyar.deco.core.common.Resource
import com.yayyar.deco.core.data.repository.CategoryRepository
import com.yayyar.deco.core.data.repository.OrderRepository
import com.yayyar.deco.core.data.repository.PaymentMethodRepository
import com.yayyar.deco.core.data.repository.ProductRepository
import com.yayyar.deco.core.database.entity.CategoryEntity
import com.yayyar.deco.core.database.entity.OrderEntity
import com.yayyar.deco.core.database.entity.OrderItemEntity
import com.yayyar.deco.core.database.entity.PaymentMethodEntity
import com.yayyar.deco.core.database.entity.ProductEntity
import com.yayyar.deco.core.database.entity.ProductVariantEntity
import com.yayyar.deco.core.database.model.OrderWithItems
import com.yayyar.deco.core.database.model.ProductWithVariants
import com.yayyar.deco.core.printer.PrinterManager
import com.yayyar.deco.core.printer.ReceiptData
import com.yayyar.deco.core.printer.ReceiptItem
import com.yayyar.deco.core.printer.SlipShareManager
import com.yayyar.deco.core.printer.StoreConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PosViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val orderRepository: OrderRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePaymentMethods: StateFlow<List<PaymentMethodEntity>> = paymentMethodRepository.getActivePaymentMethodsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val draftOrders: StateFlow<List<OrderWithItems>> = orderRepository.getDraftOrdersWithItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val catalogProducts: StateFlow<List<ProductWithVariants>> = combine(
        productRepository.getActiveProductsWithVariantsFlow(),
        _selectedCategoryId,
        _searchQuery
    ) { products, catId, query ->
        products.filter { item ->
            val matchesCat = catId == null || item.product.categoryId == catId
            val matchesQuery = query.isBlank() ||
                    item.product.name.contains(query, ignoreCase = true) ||
                    item.variants.any {
                        (it.barcode?.contains(query, ignoreCase = true) == true) ||
                                (it.sku?.contains(query, ignoreCase = true) == true) ||
                                it.colorPattern.contains(query, ignoreCase = true)
                    }
            matchesCat && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    private val _checkoutState = MutableStateFlow<Resource<ReceiptData>?>(null)
    val checkoutState: StateFlow<Resource<ReceiptData>?> = _checkoutState.asStateFlow()

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: ProductEntity, variant: ProductVariantEntity) {
        _cartState.update { current ->
            val existingIndex = current.items.indexOfFirst { it.variant.id == variant.id }
            val newItems = current.items.toMutableList()
            if (existingIndex >= 0) {
                val existing = newItems[existingIndex]
                if (existing.quantity < variant.stockQty) {
                    newItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                }
            } else {
                if (variant.stockQty > 0) {
                    newItems.add(CartItem(product, variant, 1))
                }
            }
            current.copy(items = newItems)
        }
    }

    fun updateCartItemQuantity(variantId: String, newQuantity: Int) {
        _cartState.update { current ->
            val newItems = current.items.toMutableList()
            val index = newItems.indexOfFirst { it.variant.id == variantId }
            if (index >= 0) {
                if (newQuantity <= 0) {
                    newItems.removeAt(index)
                } else {
                    val maxStock = newItems[index].variant.stockQty
                    newItems[index] = newItems[index].copy(quantity = newQuantity.coerceAtMost(maxStock))
                }
            }
            current.copy(items = newItems)
        }
    }

    fun removeCartItem(variantId: String) {
        updateCartItemQuantity(variantId, 0)
    }

    fun clearCart() {
        _cartState.value = CartState()
    }

    fun setDiscount(type: DiscountType, value: Double) {
        _cartState.update { it.copy(discountType = type, discountValue = value) }
    }

    fun setDeliFee(fee: Double) {
        _cartState.update { it.copy(deliFee = fee) }
    }

    fun setCustomerInfo(name: String?, phone: String?, address: String?) {
        _cartState.update {
            it.copy(
                customerName = name?.ifBlank { null },
                customerPhone = phone?.ifBlank { null },
                customerAddress = address?.ifBlank { null }
            )
        }
    }

    fun saveDraftSale(onSaved: () -> Unit = {}) {
        val currentCart = _cartState.value
        if (currentCart.items.isEmpty()) return

        viewModelScope.launch {
            val orderId = UUID.randomUUID().toString()
            val receiptNumber = "DRAFT-${Formatters.generateReceiptNumber()}"
            val orderEntity = OrderEntity(
                id = orderId,
                receiptNumber = receiptNumber,
                subtotal = currentCart.subtotal,
                discountAmount = currentCart.discountAmount,
                discountType = currentCart.discountType.name,
                deliFee = currentCart.deliFee,
                grandTotal = currentCart.grandTotal,
                paymentType = "CASH",
                customerName = currentCart.customerName,
                customerPhone = currentCart.customerPhone,
                customerAddress = currentCart.customerAddress,
                orderStatus = "DRAFT"
            )

            val orderItems = currentCart.items.map { item ->
                OrderItemEntity(
                    id = UUID.randomUUID().toString(),
                    orderId = orderId,
                    variantId = item.variant.id,
                    productName = item.product.name,
                    variantName = item.variant.displayName,
                    quantity = item.quantity,
                    unitPrice = item.variant.sellPrice,
                    totalPrice = item.totalPrice
                )
            }

            val result = orderRepository.saveDraftOrder(orderEntity, orderItems)
            if (result is Resource.Success) {
                clearCart()
                onSaved()
            }
        }
    }

    fun restoreDraftSale(draftOrder: OrderWithItems, onRestored: () -> Unit = {}) {
        viewModelScope.launch {
            val cartItems = mutableListOf<CartItem>()
            for (item in draftOrder.items) {
                val variant = productRepository.getVariantById(item.variantId)
                val product = variant?.let { productRepository.getProductById(it.productId) }
                if (product != null && variant != null) {
                    cartItems.add(CartItem(product, variant, item.quantity))
                } else {
                    val fallbackProd = ProductEntity(
                        id = "prod_${item.variantId}",
                        name = item.productName,
                        categoryId = ""
                    )
                    val fallbackVar = ProductVariantEntity(
                        id = item.variantId,
                        productId = fallbackProd.id,
                        sku = null,
                        barcode = null,
                        size = "",
                        colorPattern = item.variantName,
                        basePrice = item.unitPrice,
                        sellPrice = item.unitPrice,
                        stockQty = 999
                    )
                    cartItems.add(CartItem(fallbackProd, fallbackVar, item.quantity))
                }
            }

            val discType = try {
                DiscountType.valueOf(draftOrder.order.discountType)
            } catch (_: Exception) {
                DiscountType.FIXED
            }

            _cartState.value = CartState(
                items = cartItems,
                discountType = discType,
                discountValue = draftOrder.order.discountAmount,
                deliFee = draftOrder.order.deliFee,
                customerName = draftOrder.order.customerName,
                customerPhone = draftOrder.order.customerPhone,
                customerAddress = draftOrder.order.customerAddress
            )

            orderRepository.deleteDraftOrder(draftOrder.order.id)
            onRestored()
        }
    }

    fun deleteDraftSale(orderId: String) {
        viewModelScope.launch {
            orderRepository.deleteDraftOrder(orderId)
        }
    }

    fun performCheckout(
        paymentType: String,
        cashReceived: Double = 0.0,
        kpayAmount: Double = 0.0,
        waveAmount: Double = 0.0,
        paymentNotes: String? = null
    ) {
        val currentCart = _cartState.value
        if (currentCart.items.isEmpty()) return

        _checkoutState.value = Resource.Loading

        viewModelScope.launch {
            try {
                val orderId = UUID.randomUUID().toString()
                val receiptNumber = Formatters.generateReceiptNumber()

                val grandTotal = currentCart.grandTotal
                val changeReturned = if (paymentType == "CASH" && cashReceived > grandTotal) {
                    cashReceived - grandTotal
                } else 0.0

                val orderEntity = OrderEntity(
                    id = orderId,
                    receiptNumber = receiptNumber,
                    subtotal = currentCart.subtotal,
                    discountAmount = currentCart.discountAmount,
                    discountType = currentCart.discountType.name,
                    deliFee = currentCart.deliFee,
                    grandTotal = grandTotal,
                    paymentType = paymentType,
                    cashReceived = if (paymentType == "CASH") cashReceived else 0.0,
                    changeReturned = changeReturned,
                    kpayAmount = if (paymentType == "KPAY") grandTotal else kpayAmount,
                    waveAmount = if (paymentType == "WAVEPAY") grandTotal else waveAmount,
                    paymentNotes = paymentNotes,
                    customerName = currentCart.customerName,
                    customerPhone = currentCart.customerPhone,
                    customerAddress = currentCart.customerAddress,
                    orderStatus = "COMPLETED"
                )

                val orderItems = currentCart.items.map { item ->
                    OrderItemEntity(
                        id = UUID.randomUUID().toString(),
                        orderId = orderId,
                        variantId = item.variant.id,
                        productName = item.product.name,
                        variantName = item.variant.displayName,
                        quantity = item.quantity,
                        unitPrice = item.variant.sellPrice,
                        totalPrice = item.totalPrice
                    )
                }

                // Execute atomic checkout through repository
                val checkoutResult = orderRepository.checkoutOrder(orderEntity, orderItems)
                if (checkoutResult is Resource.Error) {
                    _checkoutState.value = Resource.Error(checkoutResult.message)
                    return@launch
                }

                // Prepare Receipt Data Model for Printing / Sharing
                val receiptData = ReceiptData(
                    receiptNumber = receiptNumber,
                    dateFormatted = Formatters.formatDateTime(System.currentTimeMillis()),
                    customerName = currentCart.customerName,
                    customerPhone = currentCart.customerPhone,
                    items = currentCart.items.map {
                        ReceiptItem(
                            productName = it.product.name,
                            variantName = it.variant.displayName,
                            quantity = it.quantity,
                            unitPrice = it.variant.sellPrice,
                            totalPrice = it.totalPrice
                        )
                    },
                    subtotal = currentCart.subtotal,
                    discountAmount = currentCart.discountAmount,
                    deliFee = currentCart.deliFee,
                    grandTotal = grandTotal,
                    paymentType = paymentType,
                    cashReceived = cashReceived,
                    changeReturned = changeReturned,
                    paymentNotes = paymentNotes,
                    storeConfig = StoreConfig()
                )

                // Reset Cart & notify success
                clearCart()
                _checkoutState.value = Resource.Success(receiptData)

            } catch (e: Exception) {
                _checkoutState.value = Resource.Error(e.message ?: "Checkout failed. Insufficient stock.")
            }
        }
    }

    fun dismissCheckoutState() {
        _checkoutState.value = null
    }

    fun shareReceipt(receiptData: ReceiptData) {
        SlipShareManager.shareReceiptImage(context, receiptData)
    }

    fun printThermalReceipt(receiptData: ReceiptData, deviceAddress: String? = null) {
        viewModelScope.launch {
            PrinterManager.printReceipt(context, receiptData, deviceAddress)
        }
    }
}
