package com.yayyar.deco.core.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.yayyar.deco.core.database.entity.CategoryEntity
import com.yayyar.deco.core.database.entity.ProductEntity
import com.yayyar.deco.core.database.entity.ProductVariantEntity

data class ProductWithVariants(
    @Embedded
    val product: ProductEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "product_id"
    )
    val variants: List<ProductVariantEntity> = emptyList(),

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: CategoryEntity? = null
) : java.io.Serializable {
    val minPrice: Double
        get() = variants.minOfOrNull { it.sellPrice } ?: 0.0

    val maxPrice: Double
        get() = variants.maxOfOrNull { it.sellPrice } ?: 0.0

    val minWholesalePrice: Double
        get() = variants.minOfOrNull { if (it.wholesalePrice > 0) it.wholesalePrice else it.sellPrice } ?: 0.0

    val maxWholesalePrice: Double
        get() = variants.maxOfOrNull { if (it.wholesalePrice > 0) it.wholesalePrice else it.sellPrice } ?: 0.0

    val totalStock: Int
        get() = variants.sumOf { it.stockQty }

    val hasLowStock: Boolean
        get() = variants.any { it.isLowStock }
}

data class VariantWithProduct(
    @Embedded
    val variant: ProductVariantEntity,

    @Relation(
        parentColumn = "product_id",
        entityColumn = "id"
    )
    val product: ProductEntity
) : java.io.Serializable
