package com.yayyar.teahouse.core.printer

data class StoreConfig(
    val storeName: String = "DECO FASHION BOUTIQUE",
    val storeNameBurmese: String = "ဒေကို ဖက်ရှင် ဘောတစ်ခ်",
    val address: String = "No. 123, Bogyoke Aung San Road, Yangon",
    val phone: String = "09-770001122, 09-440003344",
    val footerMessage: String = "ဝယ်ယူအားပေးမှုအတွက် ကျေးဇူးတင်ပါသည်\nThank you for shopping with us!\nExchange within 3 days with receipt.",
    val paperWidth: PaperWidth = PaperWidth.WIDTH_58MM
)

enum class PaperWidth(val pixelWidth: Int, val charWidth: Int) {
    WIDTH_58MM(384, 32),
    WIDTH_80MM(576, 48)
}

data class ReceiptItem(
    val productName: String,
    val variantName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)

data class ReceiptData(
    val receiptNumber: String,
    val dateFormatted: String,
    val cashierName: String = "Cashier 01",
    val customerName: String? = null,
    val customerPhone: String? = null,
    val items: List<ReceiptItem>,
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val deliFee: Double = 0.0,
    val grandTotal: Double,
    val paymentType: String = "CASH",
    val saleType: String = "RETAIL",
    val cashReceived: Double = 0.0,
    val changeReturned: Double = 0.0,
    val paymentNotes: String? = null,
    val storeConfig: StoreConfig = StoreConfig()
)
