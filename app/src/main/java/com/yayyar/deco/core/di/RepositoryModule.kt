package com.yayyar.deco.core.di

import com.yayyar.deco.core.data.repository.CategoryRepository
import com.yayyar.deco.core.data.repository.CategoryRepositoryImpl
import com.yayyar.deco.core.data.repository.OrderRepository
import com.yayyar.deco.core.data.repository.OrderRepositoryImpl
import com.yayyar.deco.core.data.repository.ProductRepository
import com.yayyar.deco.core.data.repository.ProductRepositoryImpl
import com.yayyar.deco.core.data.repository.ShiftRepository
import com.yayyar.deco.core.data.repository.ShiftRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        impl: OrderRepositoryImpl
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindShiftRepository(
        impl: ShiftRepositoryImpl
    ): ShiftRepository
}
