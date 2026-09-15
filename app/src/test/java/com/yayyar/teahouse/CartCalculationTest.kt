package com.yayyar.teahouse

import com.yayyar.teahouse.core.common.Formatters
import com.yayyar.teahouse.core.database.entity.ProductEntity
import com.yayyar.teahouse.core.database.entity.ProductVariantEntity
import com.yayyar.teahouse.feature.pos.CartItem
import com.yayyar.teahouse.feature.pos.CartState
import com.yayyar.teahouse.feature.pos.DiscountType
import org.junit.Assert.assertEquals
import org.junit.Test

class CartCalculationTest {

    private val sampleProduct = ProductEntity(
        id = "p1",
        name = "Floral Dress",
        categoryId = "cat1"
    )

    private val sampleVariant1 = ProductVariantEntity(
        id = "v1",
        productId = "p1",
        size = "M",
        colorPattern = "Red",
        sellPrice = 20000.0,
        wholesalePrice = 17000.0,
        stockQty = 10
    )

    private val sampleVariant2 = ProductVariantEntity(
        id = "v2",
        productId = "p1",
        size = "L",
        colorPattern = "Blue",
        sellPrice = 15000.0,
        wholesalePrice = 12000.0,
        stockQty = 5
    )

    @Test
    fun testCartSubtotalCalculation() {
        val cart = CartState(
            items = listOf(
                CartItem(sampleProduct, sampleVariant1, quantity = 2), // 40,000
                CartItem(sampleProduct, sampleVariant2, quantity = 1)  // 15,000
            )
        )

        assertEquals(55000.0, cart.subtotal, 0.01)
        assertEquals(3, cart.totalItemCount)
    }

    @Test
    fun testWholesaleCartSubtotalCalculation() {
        val cart = CartState(
            saleType = com.yayyar.teahouse.feature.pos.SaleType.WHOLESALE,
            items = listOf(
                CartItem(sampleProduct, sampleVariant1, quantity = 2, saleType = com.yayyar.teahouse.feature.pos.SaleType.WHOLESALE), // 17,000 * 2 = 34,000
                CartItem(sampleProduct, sampleVariant2, quantity = 1, saleType = com.yayyar.teahouse.feature.pos.SaleType.WHOLESALE)  // 12,000 * 1 = 12,000
            )
        )

        assertEquals(46000.0, cart.subtotal, 0.01) // 34,000 + 12,000
        assertEquals(3, cart.totalItemCount)
    }

    @Test
    fun testFixedDiscountCalculation() {
        val cart = CartState(
            items = listOf(
                CartItem(sampleProduct, sampleVariant1, quantity = 2) // 40,000
            ),
            discountType = DiscountType.FIXED,
            discountValue = 5000.0,
            deliFee = 2500.0
        )

        assertEquals(40000.0, cart.subtotal, 0.01)
        assertEquals(5000.0, cart.discountAmount, 0.01)
        assertEquals(37500.0, cart.grandTotal, 0.01) // 40000 - 5000 + 2500
    }

    @Test
    fun testPercentageDiscountCalculation() {
        val cart = CartState(
            items = listOf(
                CartItem(sampleProduct, sampleVariant1, quantity = 1) // 20,000
            ),
            discountType = DiscountType.PERCENT,
            discountValue = 10.0 // 10%
        )

        assertEquals(2000.0, cart.discountAmount, 0.01)
        assertEquals(18000.0, cart.grandTotal, 0.01)
    }

    @Test
    fun testCurrencyFormatting() {
        val formatted = Formatters.formatMmk(50000.0)
        assertEquals("50,000 Ks", formatted)
    }
}
