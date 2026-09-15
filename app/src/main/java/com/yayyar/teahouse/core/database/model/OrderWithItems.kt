package com.yayyar.teahouse.core.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.yayyar.teahouse.core.database.entity.OrderEntity
import com.yayyar.teahouse.core.database.entity.OrderItemEntity

data class OrderWithItems(
    @Embedded
    val order: OrderEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "order_id"
    )
    val items: List<OrderItemEntity> = emptyList()
)
