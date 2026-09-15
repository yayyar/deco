package com.yayyar.teahouse.core.di

import com.yayyar.teahouse.core.data.repository.CategoryRepository
import com.yayyar.teahouse.core.data.repository.CategoryRepositoryImpl
import com.yayyar.teahouse.core.data.repository.OrderRepository
import com.yayyar.teahouse.core.data.repository.OrderRepositoryImpl
import com.yayyar.teahouse.core.data.repository.PaymentMethodRepository
import com.yayyar.teahouse.core.data.repository.PaymentMethodRepositoryImpl
import com.yayyar.teahouse.core.data.repository.PreferencesRepository
import com.yayyar.teahouse.core.data.repository.PreferencesRepositoryImpl
import com.yayyar.teahouse.core.data.repository.ProductRepository
import com.yayyar.teahouse.core.data.repository.ProductRepositoryImpl
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
    abstract fun bindPaymentMethodRepository(
        impl: PaymentMethodRepositoryImpl
    ): PaymentMethodRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository
}
