package com.yayyar.teahouse.core.di

import android.content.Context
import com.yayyar.teahouse.core.database.DecoDatabase
import com.yayyar.teahouse.core.database.dao.CategoryDao
import com.yayyar.teahouse.core.database.dao.OrderDao
import com.yayyar.teahouse.core.database.dao.PaymentMethodDao
import com.yayyar.teahouse.core.database.dao.ProductDao
import com.yayyar.teahouse.core.database.dao.VariantDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDecoDatabase(
        @ApplicationContext context: Context
    ): DecoDatabase = DecoDatabase.getInstance(context)

    @Provides
    fun provideProductDao(db: DecoDatabase): ProductDao = db.productDao()

    @Provides
    fun provideCategoryDao(db: DecoDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideVariantDao(db: DecoDatabase): VariantDao = db.variantDao()

    @Provides
    fun provideOrderDao(db: DecoDatabase): OrderDao = db.orderDao()

    @Provides
    fun providePaymentMethodDao(db: DecoDatabase): PaymentMethodDao = db.paymentMethodDao()
}
