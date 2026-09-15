package com.yayyar.teahouse.core.data.repository

import com.yayyar.teahouse.core.database.dao.CategoryDao
import com.yayyar.teahouse.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface CategoryRepository {
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>
    suspend fun getAllCategories(): List<CategoryEntity>
    suspend fun getCategoryById(id: String): CategoryEntity?
    suspend fun insertCategory(category: CategoryEntity): Long
    suspend fun insertCategories(categories: List<CategoryEntity>)
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(category: CategoryEntity)
    suspend fun deleteCategoryById(id: String): Int
}

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategoriesFlow(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategoriesFlow()

    override suspend fun getAllCategories(): List<CategoryEntity> =
        categoryDao.getAllCategories()

    override suspend fun getCategoryById(id: String): CategoryEntity? =
        categoryDao.getCategoryById(id)

    override suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    override suspend fun insertCategories(categories: List<CategoryEntity>) =
        categoryDao.insertCategories(categories)

    override suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    override suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.deleteCategory(category)

    override suspend fun deleteCategoryById(id: String): Int =
        categoryDao.deleteCategoryById(id)
}
