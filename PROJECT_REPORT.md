# DeCo POS - Comprehensive Project Architecture & Technical Report

---

## 1. Executive Summary

**DeCo** is a modern, offline-first Point-of-Sale (POS) and Inventory Management Android application built for retail fashion boutiques and apparel shops. The app is tailored for the Myanmar retail market, featuring native Myanmar Kyat (MMK) currency formatting, bilingual support (English & Burmese Unicode), customizable payment channels (Cash, KBZPay, WavePay, AYA Pay, Custom Wallets, Split payments), Bluetooth ESC/POS thermal receipt printing, draft/parked sales management, granular sales intelligence, and instant digital receipt slip sharing via social/chat apps (Viber, Telegram, Messenger).

The codebase is built entirely with **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, **Kotlin Coroutines & StateFlow**, and **Dagger Hilt** following modern Android Architecture and Clean Architecture principles.

---

## 2. Technology Stack & Dependencies

| Component | Technology / Library | Version | Description |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | 2.2.10 | Modern static typing & coroutine primitives |
| **UI Toolkit** | Jetpack Compose (BOM) | 2026.02.01 | Declarative UI, Material 3 design system |
| **Dependency Injection** | Dagger Hilt | 2.55 | Compile-time dependency injection |
| **Local Database** | Room (SQLite) | 2.7.2 | Reactive persistence, WAL mode, migrations (Schema v4) |
| **Asynchronous Stream** | Kotlinx Coroutines & Flow | 1.9.0 | Reactive UDF pipelines and background processing |
| **Preferences & Settings** | SharedPreferences / StateFlow | Core KTX | Persistent application & hardware configurations |
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
│   │       ├── OrderRepository.kt      # Order lifecycle, atomic checkout transactions & drafts
│   │       ├── PaymentMethodRepository.kt # Custom payment method CRUD & active status management
│   │       ├── PreferencesRepository.kt   # Theme mode, language & default printer configuration
│   │       └── ProductRepository.kt    # Product & Variant management contracts
│   ├── database/
│   │   ├── DecoDatabase.kt             # Room database configuration (v3), WAL mode, migrations & seed data
│   │   ├── dao/
│   │   │   ├── CategoryDao.kt          # Reactive category queries & mutations
│   │   │   ├── OrderDao.kt             # Order records, daily aggregations, drafts & sales reporting
│   │   │   ├── PaymentMethodDao.kt     # Dynamic payment method management & queries
│   │   │   ├── ProductDao.kt           # Product search, catalog filtering & CRUD
│   │   │   └── VariantDao.kt           # Atomic stock deductions, additions & low-stock alerts
│   │   ├── entity/
│   │   │   ├── CategoryEntity.kt       # 'categories' table definition
│   │   │   ├── OrderEntity.kt          # 'orders' and 'order_items' table definitions
│   │   │   ├── PaymentMethodEntity.kt  # 'payment_methods' table definition
│   │   │   ├── ProductEntity.kt        # 'products' table definition
│   │   │   └── ProductVariantEntity.kt # 'product_variants' table definition
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
│   │   ├── AllSalesScreen.kt           # Comprehensive sales ledger with search, filtering & details
│   │   ├── AllSalesViewModel.kt        # Sales ledger state management & period aggregations
│   │   ├── AllSellingProductsScreen.kt # Full ranked best-selling products leaderboard & metrics
│   │   ├── AllSellingProductsViewModel.kt # Top-selling products calculation & time filters
│   │   ├── AnalyticsScreen.kt          # Revenue overview, charts, quick sales list & shortcuts
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
│   ├── pos/
│   │   ├── CheckoutDialog.kt           # Payment modal (Cash, KPay, WavePay, Custom methods, Split, Discounts)
│   │   ├── DraftSalesDialog.kt         # Parked/Draft sales manager (resume cart, delete draft, badge counter)
│   │   ├── PosModels.kt                # CartState, CartItem, DiscountType models
│   │   ├── PosScreen.kt                # Responsive POS layout (Master-detail on Tablet, Flow on Phone)
│   │   ├── PosViewModel.kt             # Cart operations, barcode search, drafts & checkout orchestration
│   │   └── ReceiptSuccessDialog.kt     # Post-checkout modal with Print & Share actions
│   └── settings/
│       ├── PaymentMethodScreen.kt      # Dynamic payment channel CRUD, default toggle & QR code settings
│       ├── PaymentMethodViewModel.kt   # Payment methods state & database mutations
│       ├── PrinterScreen.kt            # Bluetooth printer pairing, scanning, connection test & receipt width setup
│       ├── PrinterViewModel.kt         # Device discovery, test printing & preference persistence
│       ├── SettingScreen.kt            # Settings hub (Dark Mode, Language, Payment Methods, Printer)
│       └── SettingViewModel.kt         # Theme, language & peripheral preference management
│
└── ui/
    └── theme/                          # Material Design 3 Design System
        ├── Color.kt                    # Brand color definitions (Teal, Slate, Accent Gold/Green/Red)
        ├── Theme.kt                    # Dynamic color & Dark/Light mode configuration
        └── Type.kt                     # Typography definitions supporting Burmese & Latin scripts
```

---

## 4. Database Architecture & Schema Design

The application uses **Room Database (`deco_pos.db`)** at **Schema Version 4**, configured with **Write-Ahead Logging (WAL)** for high concurrency and transaction safety.

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
                                 │ wholesale_price  │
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
│ sale_type        │             └──────────────────┘
│ cash_received    │
│ change_returned  │
│ kpay_amount      │
│ wave_amount      │
│ payment_notes    │
│ customer_name    │
│ customer_phone   │
│ customer_address │
│ order_status     │             ┌──────────────────┐
│ sync_status      │             │ payment_methods  │
│ created_at       │             ├──────────────────┤
└──────────────────┘             │ id (PK)          │
                                 │ name             │
                                 │ code             │
                                 │ account_name     │
                                 │ account_number   │
                                 │ qr_code_data     │
                                 │ is_active        │
                                 │ is_default       │
                                 │ sort_order       │
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
- **Fields**: `productId`, `sku`, `barcode` (UNIQUE), `size` (e.g. S, M, L, Free), `colorPattern`, `basePrice`, `sellPrice` (Retail Price), `wholesalePrice` (Whole Sale Price), `stockQty`, `lowStockThreshold`, `syncStatus`, `updatedAt`.
- **Indices**: `product_id`, `barcode`, `sku`, `sync_status`.

#### 4. `orders`
- **Primary Key**: `id: String` (UUID)
- **Fields**: `receiptNumber` (UNIQUE, e.g. `REC-20260901-0001`), `subtotal`, `discountAmount`, `discountType` (`FIXED` / `PERCENT`), `deliFee`, `grandTotal`, `paymentType` (`CASH`, `KPAY`, `WAVEPAY`, `SPLIT`, or dynamic codes), `saleType` (`RETAIL` / `WHOLESALE`), `cashReceived`, `changeReturned`, `kpayAmount`, `waveAmount`, `paymentNotes`, `customerName`, `customerPhone`, `customerAddress`, `orderStatus` (`COMPLETED`, `DRAFT`, `CANCELLED`, `REFUNDED`), `syncStatus`, `createdAt`.
- **Indices**: `receipt_number` (UNIQUE), `created_at`, `sync_status`.

#### 5. `order_items`
- **Primary Key**: `id: String` (UUID)
- **Foreign Key**: `order_id` -> `orders(id)` ON DELETE `CASCADE`.
- **Fields**: `orderId`, `variantId`, `productName`, `variantName`, `quantity`, `unitPrice` (applied rate), `totalPrice`.
- **Indices**: `order_id`, `variant_id`.

#### 6. `payment_methods`
- **Primary Key**: `id: String` (UUID)
- **Fields**: `name`, `code`, `accountName`, `accountNumber`, `qrCodeData`, `isActive` (Boolean), `isDefault` (Boolean), `sortOrder` (Int), `createdAt` (Long).

### 4.3. Database Migrations
- **`MIGRATION_1_2`**: Restructured `orders` table to remove deprecated shift references, established unified order indexing, and removed legacy shift tables.
- **`MIGRATION_2_3`**: Created `payment_methods` table for dynamic payment channel configuration and seeded initial payment methods.
- **`MIGRATION_3_4`**: Added `wholesale_price` to `product_variants` (backfilling from `sell_price`), and added `sale_type` to `orders`.

### 4.4. Offline-Ready Sync Status Flags
Every core entity contains a `sync_status` field designed for cloud synchronization:
- `0`: **Synced** (Matches remote cloud database)
- `1`: **Created** (Created locally, pending upload)
- `2`: **Updated** (Modified locally, pending upload)
- `3`: **Deleted** (Soft-deleted locally, pending remote delete)

---

## 5. Application Architecture & Core Workflows

### 5.1. Navigation & State Persistence
The application uses an in-place sealed interface navigation model with complete **Configuration Change & Screen Rotation Survival** via `rememberSaveable`:
- **`AppDestination.Main(PosDestination)`**: Hosts core tabs (`POS` / Sales, `INVENTORY` / Items, `ANALYTICS` / Reports, `SETTINGS` / Settings) within the `ModalNavigationDrawer`.
- **Sub-Destinations**:
  - `ProductList`, `ProductAdd(productToEdit)`
  - `CategoryList`, `CategoryAdd`
  - `AllSellingProducts` (Full top products leaderboard)
  - `AllSales` (Full historical sales transaction ledger)
  - `Settings`, `PaymentMethods`, `Printers`
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
                ├──▶ Option: "Save as Draft" -> Inserts order as status 'DRAFT' (Holds cart for later)
                │
                ▼
OrderRepository.checkoutOrder(order, items)
                │
                ├──▶ [Inside database.withTransaction]
                │    1. Deduct stock atomically: UPDATE product_variants SET stock_qty = stock_qty - qty WHERE id = ? AND stock_qty >= qty
                │    2. If affected rows == 0 -> Rollback & Throw "Insufficient stock"
                │    3. Insert OrderEntity into 'orders' (status = 'COMPLETED')
                │    4. Insert OrderItemEntity records into 'order_items'
                │    5. If restoring a draft order -> Delete original draft order and draft items
                │
                ▼
ReceiptSuccessDialog displayed
    ├──▶ Bluetooth ESC/POS Print (via PrinterManager / default configured printer)
    └──▶ Share Image Slip (via SlipShareManager & Android Sharesheet)
```

### 5.3. Draft & Parked Sales Management
1. **Parking a Sale**: When a customer needs time to decide, the cashier can park the sale directly from checkout. The cart state, items, and customer info are saved as an order with `orderStatus = 'DRAFT'`.
2. **Visual Notification**: The TopAppBar displays an active draft badge with the count of currently parked orders.
3. **Resuming / Discarding Drafts**: Tapping the Draft icon opens `DraftSalesDialog`, allowing one-tap restoration of any parked sale directly back into the POS cart or permanent deletion.

### 5.4. Settings & Peripheral Configuration
1. **Theme Mode**: Reactive Dark / Light / System Default theme toggle backed by `PreferencesRepository`.
2. **Language Switching**: English and Myanmar language selection dialog.
3. **Payment Methods Management**:
   - Add, edit, reorder, or toggle active/inactive states for payment channels.
   - Configure account names, account numbers, and QR code identifiers for digital wallet payments.
   - Designate default payment methods for one-tap checkouts.
4. **Bluetooth ESC/POS Printer Management**:
   - Real-time scanning and discovery of paired and nearby Bluetooth printers.
   - Dynamic Bluetooth permission handling for Android 12+ (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`) and legacy versions.
   - One-tap test receipt printing to verify connection and print alignment.
   - Selection and persistence of default printer address and receipt paper width (58mm / 80mm).

### 5.5. ESC/POS Thermal Printing & Slip Sharing Pipeline

#### The Burmese Unicode Challenge
Standard thermal POS printers (58mm / 80mm ESC/POS) lack built-in Burmese Unicode fonts and rendering engines, causing direct text transmissions to appear corrupted or broken.

#### The DeCo Solution: Canvas Bitmap Rasterization
1. **`ReceiptBitmapRenderer`**: Measures and draws the receipt onto an Android `android.graphics.Bitmap` using Android's native font rendering pipeline. This guarantees 100% correct glyph clustering, tone markers, and Burmese sub-scripts.
2. **`EscPosRasterEncoder`**: Converts the Android ARGB bitmap into a 1-bit monochrome byte stream encoded using the standard ESC/POS raster command:
   $$\text{Command: } \text{GS } v\text{ } 0\text{ } m\text{ } x_L\text{ } x_H\text{ } y_L\text{ } y_H\text{ } [d_1 \dots d_k]$$
3. **`PrinterManager`**: Transmits the byte stream over standard Bluetooth Serial Port Profile (SPP - UUID `00001101-0000-1000-8000-00805F9B34FB`).
4. **`SlipShareManager`**: Simultaneously allows exporting the exact rendered receipt as a PNG image to cache and sharing it directly to messaging platforms (Viber, Telegram, Messenger).

### 5.6. Analytics & Sales Intelligence Flow
1. **Executive Dashboard (`AnalyticsScreen`)**:
   - Time-filtered revenue summaries (`Today`, `This Week`, `This Month`, `All Time`).
   - Gross Revenue, Net Revenue, Total Discounts, Delivery Fees Collected, Payment Channel Breakdown (Cash vs. KPay vs. WavePay vs. Others).
   - Category contribution percentage breakdown.
   - CSV generation directly exported to Android `Downloads` via `MediaStore`.
2. **All Sales Transaction Ledger (`AllSalesScreen`)**:
   - Searchable by receipt number, customer name, and customer phone.
   - Filter chips by time period and payment channel.
   - Pull-to-refresh reactive list displaying comprehensive receipt breakdowns.
3. **Best-Selling Products Leaderboard (`AllSellingProductsScreen`)**:
   - Filterable ranking of products by sales volume and generated revenue.
   - Search by product name or category.
   - Podium badges (#1 Gold, #2 Silver, #3 Bronze) and variant-level sales volume indicators.

---

## 6. Key Features & Business Capabilities

1. **Dual-Pane Tablet & Single-Pane Phone Adaptive Layout**:
   - On screens $\ge 720\text{dp}$ width (tablets in landscape), the POS interface dynamically splits into a side-by-side Catalog Grid and Active Cart pane.
   - On mobile screens, smooth collapsible sliding panels optimize viewport space.
2. **Multi-Variant Apparel Matrix**:
   - Products support arbitrary combinations of Sizes (S, M, L, XL, Free Size) and Color/Patterns (e.g. Floral Red, Cat Graphic White).
   - Each variant possesses its own barcode, SKU, cost price, sell price, and real-time inventory count.
3. **Barcode Scanning Support**:
   - Instant search bar listening for barcode inputs from external Bluetooth/USB laser scanners and software keyboards.
4. **Dynamic Myanmar Payment Matrix**:
   - Flexible support for Cash, Mobile Wallets (KBZPay, WavePay, AYA Pay), Custom Payment Channels, and Split payments (e.g. Cash + KBZPay).
5. **Dual Selling Modes (Retail Sale & Whole Sale)**:
   - Dedicated retail and wholesale pricing per product variant.
   - Interactive toggle on POS interface dynamically recalculating cart line items, pricing tiers, and totals.
   - Distinct receipt labeling and sales ledger analytics badges for wholesale orders.
6. **Draft / Parked Sales Workflow**:
   - Instant parking and restoring of open orders with active notification badges in the navigation bar.
7. **Real-time Low Stock Guard**:
   - Threshold-based visual warnings when inventory dips below safe operational levels.
8. **Comprehensive Settings & Hardware Setup**:
   - Integrated Bluetooth printer setup with automated pairing, test printing, and paper size switching.
   - Dark mode toggle, localized language selection, and payment channel configuration.

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
- **Preferences & Hardware State**: Store persistent user configurations through `PreferencesRepository` using reactive `StateFlow` primitives to ensure instant UI reactivity across all composables.
