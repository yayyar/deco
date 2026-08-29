package com.yayyar.deco.feature.pos

import com.yayyar.deco.core.database.entity.ProductEntity
import com.yayyar.deco.core.database.entity.ProductVariantEntity

data class CartItem(
    val product: ProductEntity,
    val variant: ProductVariantEntity,
    val quantity: Int = 1
) {
    val totalPrice: Double
        get() = variant.sellPrice * quantity
}

enum class DiscountType {
    FIXED, PERCENT
}

data class CartState(
    val items: List<CartItem> = emptyList(),
    val discountType: DiscountType = DiscountType.FIXED,
    val discountValue: Double = 0.0,
    val deliFee: Double = 0.0,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val customerAddress: String? = null
) {
    val subtotal: Double
        get() = items.sumOf { it.totalPrice }

    val discountAmount: Double
        get() = when (discountType) {
            DiscountType.FIXED -> discountValue.coerceAtMost(subtotal)
            DiscountType.PERCENT -> (subtotal * (discountValue.coerceIn(0.0, 100.0) / 100.0))
        }

    val grandTotal: Double
        get() = (subtotal - discountAmount + deliFee).coerceAtLeast(0.0)

    val totalItemCount: Int
        get() = items.sumOf { it.quantity }
}
