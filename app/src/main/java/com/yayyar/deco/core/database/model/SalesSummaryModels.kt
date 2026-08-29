package com.yayyar.deco.core.database.model

data class DailySalesSummary(
    val totalOrders: Int = 0,
    val totalSales: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val totalDeli: Double = 0.0,
    val totalCash: Double = 0.0,
    val totalKpay: Double = 0.0,
    val totalWave: Double = 0.0
)

data class TopSellingItem(
    val variantId: String,
    val productName: String,
    val variantName: String,
    val totalQuantitySold: Int,
    val totalRevenue: Double
)

data class CategorySalesSummary(
    val categoryName: String,
    val totalQuantity: Int,
    val totalRevenue: Double
)
