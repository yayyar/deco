package com.yayyar.deco.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yayyar.deco.core.database.dao.CategoryDao
import com.yayyar.deco.core.database.dao.OrderDao
import com.yayyar.deco.core.database.dao.ProductDao
import com.yayyar.deco.core.database.dao.ShiftDao
import com.yayyar.deco.core.database.dao.VariantDao
import com.yayyar.deco.core.database.entity.CashMovementEntity
import com.yayyar.deco.core.database.entity.CategoryEntity
import com.yayyar.deco.core.database.entity.OrderEntity
import com.yayyar.deco.core.database.entity.OrderItemEntity
import com.yayyar.deco.core.database.entity.ProductEntity
import com.yayyar.deco.core.database.entity.ProductVariantEntity
import com.yayyar.deco.core.database.entity.ShiftEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        ProductVariantEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        ShiftEntity::class,
        CashMovementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DecoDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun variantDao(): VariantDao
    abstract fun orderDao(): OrderDao
    abstract fun shiftDao(): ShiftDao

    companion object {
        @Volatile
        private var INSTANCE: DecoDatabase? = null

        fun getInstance(context: Context): DecoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DecoDatabase::class.java,
                    "deco_pos.db"
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) // High performance WAL mode
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial seed data on background thread
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: DecoDatabase) {
            val catDao = db.categoryDao()
            val prodDao = db.productDao()
            val varDao = db.variantDao()

            val catDress = CategoryEntity(id = "cat_dress", name = "Dresses / ဂါဝန်", sortOrder = 1)
            val catTop = CategoryEntity(id = "cat_top", name = "Tops & Shirts / အင်္ကျီ", sortOrder = 2)
            val catSkirt = CategoryEntity(id = "cat_skirt", name = "Skirts & Pants / စကတ်နှင့်ဘောင်းဘီ", sortOrder = 3)
            val catAccessory = CategoryEntity(id = "cat_acc", name = "Accessories / အသုံးအဆောင်", sortOrder = 4)

            catDao.insertCategories(listOf(catDress, catTop, catSkirt, catAccessory))

            // Seed Product 1: Floral Print Summer Dress
            val p1 = ProductEntity(
                id = "p1",
                name = "Floral Summer Dress (နွေရာသီဂါဝန်)",
                categoryId = catDress.id,
                description = "Lightweight floral cotton dress"
            )
            val v1_1 = ProductVariantEntity(
                productId = p1.id,
                sku = "DRS-S-FLR",
                barcode = "8850001001",
                size = "S",
                colorPattern = "ပန်းနီ",
                basePrice = 12000.0,
                sellPrice = 18500.0,
                stockQty = 15
            )
            val v1_2 = ProductVariantEntity(
                productId = p1.id,
                sku = "DRS-M-FLR",
                barcode = "8850001002",
                size = "M",
                colorPattern = "ပန်းပြာ",
                basePrice = 12000.0,
                sellPrice = 18500.0,
                stockQty = 20
            )
            val v1_3 = ProductVariantEntity(
                productId = p1.id,
                sku = "DRS-L-FLR",
                barcode = "8850001003",
                size = "L",
                colorPattern = "ဝါရောင်",
                basePrice = 13000.0,
                sellPrice = 19500.0,
                stockQty = 4 // Low stock
            )
            prodDao.insertProduct(p1)
            varDao.deleteVariantsByProductId(p1.id)
            varDao.insertVariants(listOf(v1_1, v1_2, v1_3))

            // Seed Product 2: Cute Cat T-Shirt
            val p2 = ProductEntity(
                id = "p2",
                name = "Cute Pattern T-Shirt (တီရှပ်)",
                categoryId = catTop.id,
                description = "100% Premium Cotton Tee"
            )
            val v2_1 = ProductVariantEntity(
                productId = p2.id,
                sku = "TSH-CAT-WHT",
                barcode = "8850002001",
                size = "Free Size",
                colorPattern = "ကြောင်ရုပ် (White)",
                basePrice = 8000.0,
                sellPrice = 13500.0,
                stockQty = 30
            )
            val v2_2 = ProductVariantEntity(
                productId = p2.id,
                sku = "TSH-PIG-PNK",
                barcode = "8850002002",
                size = "Free Size",
                colorPattern = "ဝက်ရုပ် (Pink)",
                basePrice = 8000.0,
                sellPrice = 13500.0,
                stockQty = 12
            )
            val v2_3 = ProductVariantEntity(
                productId = p2.id,
                sku = "TSH-BEAR-BLK",
                barcode = "8850002003",
                size = "Free Size",
                colorPattern = "ဝက်ဝံရုပ် (Black)",
                basePrice = 8000.0,
                sellPrice = 13500.0,
                stockQty = 3 // Low stock
            )
            prodDao.insertProduct(p2)
            varDao.deleteVariantsByProductId(p2.id)
            varDao.insertVariants(listOf(v2_1, v2_2, v2_3))

            // Seed Product 3: Linen Wide Pants
            val p3 = ProductEntity(
                id = "p3",
                name = "Linen Wide Leg Pants (ချည်ဘောင်းဘီရှည်)",
                categoryId = catSkirt.id,
                description = "High-waist comfortable linen pants"
            )
            val v3_1 = ProductVariantEntity(
                productId = p3.id,
                sku = "PNT-LIN-BEI",
                barcode = "8850003001",
                size = "M",
                colorPattern = "Beige",
                basePrice = 15000.0,
                sellPrice = 22000.0,
                stockQty = 8
            )
            val v3_2 = ProductVariantEntity(
                productId = p3.id,
                sku = "PNT-LIN-NVY",
                barcode = "8850003002",
                size = "L",
                colorPattern = "Navy Blue",
                basePrice = 15000.0,
                sellPrice = 22000.0,
                stockQty = 10
            )
            prodDao.insertProduct(p3)
            varDao.deleteVariantsByProductId(p3.id)
            varDao.insertVariants(listOf(v3_1, v3_2))

            // Seed initial active shift
            val shift = ShiftEntity(
                id = "shift_init_1",
                openingFloat = 100000.0, // 100,000 MMK float
                notes = "Morning Shift Register 01"
            )
            db.shiftDao().insertShift(shift)
        }
    }
}
