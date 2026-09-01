# DeCo POS - Comprehensive Project Architecture & Technical Report

---

## 1. Executive Summary

**DeCo** is a modern, offline-first Point-of-Sale (POS) and Inventory Management Android application built for retail fashion boutiques and apparel shops. The app is tailored for the Myanmar retail market, featuring native Myanmar Kyat (MMK) currency formatting, bilingual support (English & Burmese Unicode), localized payment channels (Cash, KBZPay, WavePay, Split payments), Bluetooth ESC/POS thermal receipt printing, and instant digital receipt slip sharing via social/chat apps (Viber, Telegram, Messenger).

The codebase is built entirely with **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, **Kotlin Coroutines & StateFlow**, and **Dagger Hilt** following modern Android Architecture and Clean Architecture principles.

---

## 2. Technology Stack & Dependencies

| Component | Technology / Library | Version | Description |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | 2.2.10 | Modern static typing & coroutine primitives |
| **UI Toolkit** | Jetpack Compose (BOM) | 2026.02.01 | Declarative UI, Material 3 design system |
| **Dependency Injection** | Dagger Hilt | 2.55 | Compile-time dependency injection |
| **Local Database** | Room (SQLite) | 2.7.2 | Reactive persistence, WAL mode, migrations |
| **Asynchronous Stream** | Kotlinx Coroutines & Flow | 1.9.0 | Reactive UDF pipelines and background processing |
| **Architecture** | MVVM + Unidirectional Data Flow (UDF) | Clean Architecture | Clear separation between Core, Data, and Feature layers |
| **Hardware Integration** | Android Bluetooth SPP + Canvas Bitmap Raster | ESC/POS (58mm/80mm) | Crisp bitmap printing guaranteeing Burmese Unicode rendering |
| **Build System** | Gradle (Kotlin DSL + Version Catalog) | AGP 9.3.2 | Automated dependency management |

---

## 3. Project Structure & Package Hierarchy

```
com.yayyar.deco/
├── DecoApplication.kt                  # Application class with @HiltAndroidApp
├── MainActivity.kt                     # Single Activity host, TopAppBar, Drawer & Root Navigation
│
├── core/                               # Shared modules and infrastructural components
│   ├── common/
│   │   ├── Formatters.kt               # Currency (MMK), date/time, and receipt number formatters
│   │   └── Resource.kt                 # Generic State sealed class (Success, Error, Loading)
│   ├── data/
│   │   └── repository/
│   │       ├── CategoryRepository.kt   # Category data contracts and implementations
│   │       ├── OrderRepository.kt      # Order lifecycle & atomic checkout transactions
│   │       └── ProductRepository.kt    # Product & Variant management contracts
│   ├── database/
│   │   ├── DecoDatabase.kt             # Room database configuration, WAL mode, migrations & seed data
│   │   ├── dao/
│   │   │   ├── CategoryDao.kt          # Reactive category queries & mutations
│   │   │   ├── OrderDao.kt             # Order records, daily aggregations & sales reporting
│   │   │   ├── ProductDao.kt           # Product search, catalog filtering & CRUD
│   │   │   └── VariantDao.kt           # Atomic stock deductions, additions & low-stock alerts
│   │   ├── entity/
│   │   │   ├── CategoryEntity.kt       # 'categories' table definition
│   │   │   ├── OrderEntity.kt          # 'orders' and 'order_items' table definitions
│   │   │   └── ProductEntity.kt        # 'products' and 'product_variants' table definitions
│   │   └── model/
│   │       ├── OrderWithItems.kt       # 1-to-N Relation model for Order + OrderItems
│   │       ├── ProductWithVariants.kt  # 1-to-N Relation model for Product + Variants + Category
│   │       └── SalesSummaryModels.kt   # Analytical DTOs (Daily, Category, Top-selling items)
│   ├── di/
│   │   ├── DatabaseModule.kt           # Hilt provider for DecoDatabase and DAOs
│   │   └── RepositoryModule.kt         # Hilt binder for Repository interfaces to implementations
│   ├── printer/
│   │   ├── EscPosRasterEncoder.kt      # Monochrome bit-packing encoder for thermal printers
│   │   ├── PrinterManager.kt           # Bluetooth SPP connection & device discovery
│   │   ├── ReceiptBitmapRenderer.kt    # Canvas-based receipt generator (flawless Unicode)
│   │   ├── ReceiptModels.kt            # StoreConfig, PaperWidth (58mm/80mm), ReceiptData
│   │   └── SlipShareManager.kt         # FileProvider image export for Viber/Messenger/Telegram
│   └── ui/
│       └── components/
│           └── CommonComponents.kt     # Shared UI elements (badges, state screens, buttons)
│
├── feature/                            # Feature-driven UI and ViewModels
│   ├── analytics/
│   │   ├── AnalyticsScreen.kt          # Revenue metrics, breakdown charts, recent sales list
│   │   └── AnalyticsViewModel.kt       # Time-filtered sales aggregation & CSV export engine
│   ├── inventory/
│   │   ├── CategoryAddScreen.kt        # Category creation form
│   │   ├── CategoryListScreen.kt       # Category listing & reordering
│   │   ├── CategoryManageDialog.kt     # Quick category popup dialog
│   │   ├── InventoryScreen.kt          # Main inventory overview (List/Grid view, Low stock toggle)
│   │   ├── InventoryViewModel.kt       # Filter, search, and stock level state management
│   │   ├── ProductAddScreen.kt         # Multi-variant product builder (Size, Color, SKU, Barcode)
│   │   ├── ProductEditDialog.kt        # Quick product editing dialog
│   │   ├── ProductListScreen.kt        # Comprehensive product inventory list
│   │   └── StockAdjustmentDialog.kt    # Instant atomic stock increment/decrement dialog
│   └── pos/
│       ├── CheckoutDialog.kt           # Payment modal (Cash, KPay, WavePay, Split payment, Discounts)
│       ├── PosModels.kt                # CartState, CartItem, PaymentType, DiscountType models
│       ├── PosScreen.kt                # Responsive POS layout (Master-detail on Tablet, Flow on Phone)
│       ├── PosViewModel.kt             # Cart operations, barcode search, checkout orchestration
│       └── ReceiptSuccessDialog.kt     # Post-checkout modal with Print & Share actions
│
└── ui/
    └── theme/                          # Material Design 3 Design System
        ├── Color.kt                    # Brand color definitions (Primary Teal/Slate aesthetic)
        ├── Theme.kt                    # Dynamic color & Dark/Light mode configuration
        └── Type.kt                     # Typography definitions supporting Burmese & Latin scripts
```

---

## 4. Database Architecture & Schema Design

The application uses **Room Database (`deco_pos.db`)** configured with **Write-Ahead Logging (WAL)** for high concurrency and transaction safety.

### 4.1. Entity Relationship Diagram (ERD)

```
┌──────────────────┐             ┌──────────────────┐
│    categories    │ 1         N │     products     │
├──────────────────┼────────────<├──────────────────┤
│ id (PK)          │             │ id (PK)          │
│ name             │             │ category_id (FK) │
│ sort_order       │             │ name             │
│ sync_status      │             │ description      │
│ updated_at       │             │ is_active        │
└──────────────────┘             │ sync_status      │
                                 │ updated_at       │
                                 └─────────┬────────┘
                                           │ 1
                                           │
                                           │ N
                                 ┌─────────┴────────┐
                                 │ product_variants │
                                 ├──────────────────┤
                                 │ id (PK)          │
                                 │ product_id (FK)  │
                                 │ sku              │
                                 │ barcode (UNIQUE) │
                                 │ size             │
                                 │ color_pattern    │
                                 │ base_price       │
                                 │ sell_price       │
                                 │ stock_qty        │
                                 │ low_stock_thresh │
                                 │ sync_status      │
                                 │ updated_at       │
                                 └─────────┬────────┘
                                           │ 1
                                           │
                                           │ N
┌──────────────────┐ 1         N ┌─────────┴────────┐
│      orders      │────────────<│   order_items    │
├──────────────────┤             ├──────────────────┤
│ id (PK)          │             │ id (PK)          │
│ receipt_number   │             │ order_id (FK)    │
│ subtotal         │             │ variant_id       │
│ discount_amount  │             │ product_name     │
│ discount_type    │             │ variant_name     │
│ deli_fee         │             │ quantity         │
│ grand_total      │             │ unit_price       │
│ payment_type     │             │ total_price      │
│ cash_received    │             └──────────────────┘
│ change_returned  │
│ kpay_amount      │
│ wave_amount      │
│ payment_notes    │
│ customer_name    │
│ customer_phone   │
│ order_status     │
│ sync_status      │
│ created_at       │
└──────────────────┘
```

### 4.2. Database Tables & Specifications

#### 1. `categories`
- **Primary Key**: `id: String` (UUID)
- **Fields**: `name` (UNIQUE), `sort_order` (Int), `sync_status` (Int), `updated_at` (Long).
- **Indices**: `name`, `sort_order`.

#### 2. `products`
- **Primary Key**: `id: String` (UUID)
- **Foreign Key**: `category_id` -> `categories(id)` ON DELETE `SET NULL`.
- **Fields**: `name`, `categoryId`, `description`, `imageUri`, `isActive`, `syncStatus`, `updatedAt`.
- **Indices**: `name`, `category_id`, `sync_status`.

#### 3. `product_variants`
- **Primary Key**: `id: String` (UUID)
- **Foreign Key**: `product_id` -> `products(id)` ON DELETE `CASCADE`.
- **Fields**: `productId`, `sku`, `barcode` (UNIQUE), `size` (e.g. S, M, L, Free), `colorPattern`, `basePrice`, `sellPrice`, `stockQty`, `lowStockThreshold`, `syncStatus`, `updatedAt`.
- **Indices**: `product_id`, `barcode`, `sku`, `sync_status`.

#### 4. `orders`
- **Primary Key**: `id: String` (UUID)
- **Fields**: `receiptNumber` (UNIQUE, e.g. `REC-20260901-0001`), `subtotal`, `discountAmount`, `discountType` (`FIXED` / `PERCENT`), `deliFee`, `grandTotal`, `paymentType` (`CASH`, `KPAY`, `WAVEPAY`, `SPLIT`), `cashReceived`, `changeReturned`, `kpayAmount`, `waveAmount`, `paymentNotes`, `customerName`, `customerPhone`, `customerAddress`, `orderStatus` (`COMPLETED`, `CANCELLED`, `REFUNDED`), `syncStatus`, `createdAt`.
- **Indices**: `receipt_number` (UNIQUE), `created_at`, `sync_status`.

#### 5. `order_items`
- **Primary Key**: `id: String` (UUID)
- **Foreign Key**: `order_id` -> `orders(id)` ON DELETE `CASCADE`.
- **Fields**: `orderId`, `variantId`, `productName`, `variantName`, `quantity`, `unitPrice`, `totalPrice`.
- **Indices**: `order_id`, `variant_id`.

### 4.3. Offline-Ready Sync Status Flags
Every core entity contains a `sync_status` field designed for cloud synchronization:
- `0`: **Synced** (Matches remote cloud database)
- `1`: **Created** (Created locally, pending upload)
- `2`: **Updated** (Modified locally, pending upload)
- `3`: **Deleted** (Soft-deleted locally, pending remote delete)

---

## 5. Application Architecture & Core Workflows

### 5.1. Navigation & State Persistence
The application uses an in-place sealed interface navigation model with complete **Configuration Change & Screen Rotation Survival** via `rememberSaveable`:
- **`AppDestination.Main(PosDestination)`**: Hosts the core tabs (`POS`, `INVENTORY`, `ANALYTICS`) within the `ModalNavigationDrawer`.
- **`AppDestination.ProductList`**, **`ProductAdd`**, **`CategoryList`**, **`CategoryAdd`**: Sub-destinations with back-stack support.
- All destinations and data models implement `java.io.Serializable` to guarantee seamless persistence in Android's `SavedStateRegistry`.

### 5.2. POS Checkout & Atomic Stock Deduction Flow
```
User selects items from Catalog / Scans Barcode
                │
                ▼
Cart updates (Validates variant stockQty)
                │
                ▼
Checkout Dialog opened (Discount, Delivery fee, Payment type selection)
                │
                ▼
OrderRepository.checkoutOrder(order, items)
                │
                ├──▶ [Inside database.withTransaction]
                │    1. Deduct stock atomically: UPDATE product_variants SET stock_qty = stock_qty - qty WHERE id = ? AND stock_qty >= qty
                │    2. If affected rows == 0 -> Rollback & Throw "Insufficient stock"
                │    3. Insert OrderEntity into 'orders'
                │    4. Insert OrderItemEntity records into 'order_items'
                │
                ▼
ReceiptSuccessDialog displayed
    ├──▶ Bluetooth ESC/POS Print (via PrinterManager)
    └──▶ Share Image Slip (via SlipShareManager & Android Sharesheet)
```

### 5.3. ESC/POS Thermal Printing & Slip Sharing Pipeline

#### The Burmese Unicode Challenge
Standard thermal POS printers (58mm / 80mm ESC/POS) lack built-in Burmese Unicode fonts and rendering engines, causing direct text transmissions to appear corrupted or broken.

#### The DeCo Solution: Canvas Bitmap Rasterization
1. **`ReceiptBitmapRenderer`**: Measures and draws the receipt onto an Android `android.graphics.Bitmap` using Android's native font rendering pipeline. This guarantees 100% correct glyph clustering, tone markers, and Burmese sub-scripts.
2. **`EscPosRasterEncoder`**: Converts the Android ARGB bitmap into a 1-bit monochrome byte stream encoded using the standard ESC/POS raster command:
   $$\text{Command: } \text{GS } v\text{ } 0\text{ } m\text{ } x_L\text{ } x_H\text{ } y_L\text{ } y_H\text{ } [d_1 \dots d_k]$$
3. **`PrinterManager`**: Transmits the byte stream over standard Bluetooth Serial Port Profile (SPP - UUID `00001101-0000-1000-8000-00805F9B34FB`).
4. **`SlipShareManager`**: Simultaneously allows exporting the exact rendered receipt as a PNG image to cache and sharing it directly to messaging platforms (Viber, Telegram, Messenger).

### 5.4. Analytics & Sales Intelligence Flow
The `AnalyticsViewModel` provides reactive financial metrics filtered by dynamic time windows:
- **Time Windows**: `Today`, `This Week`, `This Month`, `All Time`.
- **Metrics Aggregated**: Gross Revenue, Net Revenue, Total Discounts, Delivery Fees Collected, Payment Channel Split (Cash vs. KPay vs. WavePay).
- **Visual Breakdowns**: Top 10 Best-Selling Items by Volume and Revenue, Category Contribution Breakdown, Recent Transactions Ledger.
- **Data Export**: Built-in CSV generation saved directly to the Android `Downloads` directory via `MediaStore`.

---

## 6. Key Features & Business Capabilities

1. **Dual-Pane Tablet & Single-Pane Phone Adaptive Layout**:
   - On screens $\ge 720\text{dp}$ width (tablets in landscape), the POS interface dynamically splits into a side-by-side Catalog Grid and Active Cart pane.
   - On mobile screens, smooth collapsible sliding panels optimize viewport space.
2. **Multi-Variant Apparel Matrix**:
   - Products support arbitrary combinations of Sizes (S, M, L, XL, Free Size) and Color/Patterns (e.g. Floral Red, Cat Graphic White).
   - Each variant possesses its own barcode, SKU, cost price, sell price, and real-time inventory count.
3. **Barcode Scanning Support**:
   - Instant search bar listening for barcode inputs from external Bluetooth/USB laser scanners.
4. **Localized Myanmar Payment Matrix**:
   - Support for pure Cash, Mobile Wallets (KBZPay, WavePay), and Split payments (e.g. Cash + KBZPay).
5. **Real-time Low Stock Guard**:
   - Threshold-based visual warnings when inventory dips below safe operational levels.

---

## 7. Developer & Maintenance Reference

### Running Unit Tests & Verification
```bash
./gradlew testDebugUnitTest
```

### Building Debug APK
```bash
./gradlew assembleDebug
```

### Key Architectural Guidelines for Future Additions
- **State Preservation**: When adding new navigation destinations or composable screen arguments, ensure all classes implement `java.io.Serializable` or provide a custom Compose `Saver`.
- **Database Schema Changes**: Always increment `version` in `DecoDatabase` and provide explicit `Migration(from, to)` objects rather than relying on destructive fallbacks.
- **Stock Updates**: Never modify `stock_qty` in memory before writing; always use the atomic SQL methods `deductStockAtomic` and `restockAtomic` inside transactions to prevent race conditions during checkout.
