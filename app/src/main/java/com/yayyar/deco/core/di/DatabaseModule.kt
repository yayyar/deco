package com.yayyar.deco.core.di

import android.content.Context
import com.yayyar.deco.core.database.DecoDatabase
import com.yayyar.deco.core.database.dao.CategoryDao
import com.yayyar.deco.core.database.dao.OrderDao
import com.yayyar.deco.core.database.dao.ProductDao
import com.yayyar.deco.core.database.dao.ShiftDao
import com.yayyar.deco.core.database.dao.VariantDao
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
    fun provideShiftDao(db: DecoDatabase): ShiftDao = db.shiftDao()
}
