package com.yayyar.teahouse.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yayyar.teahouse.core.database.dao.CategoryDao
import com.yayyar.teahouse.core.database.dao.OrderDao
import com.yayyar.teahouse.core.database.dao.PaymentMethodDao
import com.yayyar.teahouse.core.database.dao.ProductDao
import com.yayyar.teahouse.core.database.dao.VariantDao
import com.yayyar.teahouse.core.database.entity.CategoryEntity
import com.yayyar.teahouse.core.database.entity.OrderEntity
import com.yayyar.teahouse.core.database.entity.OrderItemEntity
import com.yayyar.teahouse.core.database.entity.PaymentMethodEntity
import com.yayyar.teahouse.core.database.entity.ProductEntity
import com.yayyar.teahouse.core.database.entity.ProductVariantEntity
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
        PaymentMethodEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class DecoDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun variantDao(): VariantDao
    abstract fun orderDao(): OrderDao
    abstract fun paymentMethodDao(): PaymentMethodDao

    companion object {
        @Volatile
        private var INSTANCE: DecoDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create new orders table matching OrderEntity without shift_id
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `orders_new` (
                        `id` TEXT NOT NULL,
                        `receipt_number` TEXT NOT NULL,
                        `subtotal` REAL NOT NULL,
                        `discount_amount` REAL NOT NULL,
                        `discount_type` TEXT NOT NULL,
                        `deli_fee` REAL NOT NULL,
                        `grand_total` REAL NOT NULL,
                        `payment_type` TEXT NOT NULL,
                        `cash_received` REAL NOT NULL,
                        `change_returned` REAL NOT NULL,
                        `kpay_amount` REAL NOT NULL,
                        `wave_amount` REAL NOT NULL,
                        `payment_notes` TEXT,
                        `customer_name` TEXT,
                        `customer_phone` TEXT,
                        `customer_address` TEXT,
                        `order_status` TEXT NOT NULL,
                        `sync_status` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 2. Copy data from old orders table to orders_new
                db.execSQL("""
                    INSERT INTO `orders_new` (
                        `id`, `receipt_number`, `subtotal`, `discount_amount`, `discount_type`,
                        `deli_fee`, `grand_total`, `payment_type`, `cash_received`, `change_returned`,
                        `kpay_amount`, `wave_amount`, `payment_notes`, `customer_name`, `customer_phone`,
                        `customer_address`, `order_status`, `sync_status`, `created_at`
                    )
                    SELECT 
                        `id`, `receipt_number`, `subtotal`, `discount_amount`, `discount_type`,
                        `deli_fee`, `grand_total`, `payment_type`, `cash_received`, `change_returned`,
                        `kpay_amount`, `wave_amount`, `payment_notes`, `customer_name`, `customer_phone`,
                        `customer_address`, `order_status`, `sync_status`, `created_at`
                    FROM `orders`
                """.trimIndent())

                // 3. Drop old orders table
                db.execSQL("DROP TABLE `orders`")

                // 4. Rename orders_new to orders
                db.execSQL("ALTER TABLE `orders_new` RENAME TO `orders`")

                // 5. Recreate indexes for orders table
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_orders_receipt_number` ON `orders` (`receipt_number`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_created_at` ON `orders` (`created_at`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_sync_status` ON `orders` (`sync_status`)")

                // 6. Drop obsolete shifts and cash_movements tables
                db.execSQL("DROP TABLE IF EXISTS `cash_movements`")
                db.execSQL("DROP TABLE IF EXISTS `shifts`")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `payment_methods` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `code` TEXT NOT NULL,
                        `account_name` TEXT,
                        `account_number` TEXT,
                        `qr_code_data` TEXT,
                        `is_active` INTEGER NOT NULL,
                        `is_default` INTEGER NOT NULL,
                        `sort_order` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                // Seed initial payment methods for existing installs
                db.execSQL("""
                    INSERT OR IGNORE INTO `payment_methods` (`id`, `name`, `code`, `account_name`, `account_number`, `qr_code_data`, `is_active`, `is_default`, `sort_order`, `created_at`)
                    VALUES 
                    ('pm_cash', 'Cash', 'CASH', NULL, NULL, NULL, 1, 1, 1, 1700000000000),
                    ('pm_kpay', 'KBZPay', 'KPAY', 'Store Merchant', '09123456789', NULL, 1, 0, 2, 1700000000001),
                    ('pm_wave', 'WavePay', 'WAVEPAY', 'Store Merchant', '09987654321', NULL, 1, 0, 3, 1700000000002)
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `product_variants` ADD COLUMN `wholesale_price` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("UPDATE `product_variants` SET `wholesale_price` = `sell_price` WHERE `wholesale_price` = 0.0")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `sale_type` TEXT NOT NULL DEFAULT 'RETAIL'")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `product_variants` ADD COLUMN `image_uri` TEXT")
            }
        }

        fun getInstance(context: Context): DecoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DecoDatabase::class.java,
                    "deco_pos.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
            val payDao = db.paymentMethodDao()

            val pmCash = PaymentMethodEntity(id = "pm_cash", name = "Cash", code = "CASH", isDefault = true, sortOrder = 1)
            val pmKpay = PaymentMethodEntity(id = "pm_kpay", name = "KBZPay", code = "KPAY", accountName = "Store Merchant", accountNumber = "09123456789", sortOrder = 2)
            val pmWave = PaymentMethodEntity(id = "pm_wave", name = "WavePay", code = "WAVEPAY", accountName = "Store Merchant", accountNumber = "09987654321", sortOrder = 3)
            payDao.insertPaymentMethods(listOf(pmCash, pmKpay, pmWave))

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
                wholesalePrice = 16000.0,
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
                wholesalePrice = 16000.0,
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
                wholesalePrice = 17000.0,
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
                wholesalePrice = 11000.0,
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
                wholesalePrice = 11000.0,
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
                wholesalePrice = 11000.0,
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
                wholesalePrice = 19000.0,
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
                wholesalePrice = 19000.0,
                stockQty = 10
            )
            prodDao.insertProduct(p3)
            varDao.deleteVariantsByProductId(p3.id)
            varDao.insertVariants(listOf(v3_1, v3_2))
        }
    }
}
