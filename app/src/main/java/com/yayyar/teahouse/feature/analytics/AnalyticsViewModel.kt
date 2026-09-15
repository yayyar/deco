package com.yayyar.teahouse.feature.analytics

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayyar.teahouse.core.common.Formatters
import com.yayyar.teahouse.core.data.repository.OrderRepository
import com.yayyar.teahouse.core.data.repository.PaymentMethodRepository
import com.yayyar.teahouse.core.database.model.CategorySalesSummary
import com.yayyar.teahouse.core.database.model.DailySalesSummary
import com.yayyar.teahouse.core.database.model.OrderWithItems
import com.yayyar.teahouse.core.database.model.PaymentMethodSalesSummary
import com.yayyar.teahouse.core.database.model.PaymentMethodSalesUiModel
import com.yayyar.teahouse.core.database.model.TopSellingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import com.yayyar.teahouse.R
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import javax.inject.Inject

enum class TimeRange(val displayName: String, val titleRes: Int) {
    TODAY("Today", R.string.analytics_period_today),
    THIS_WEEK("This Week", R.string.analytics_period_week),
    THIS_MONTH("This Month", R.string.analytics_period_month),
    ALL_TIME("All Time", R.string.analytics_period_all);

    fun getTimestamps(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val end = cal.timeInMillis
        return when (this) {
            TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to end
            }
            THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to end
            }
            THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to end
            }
            ALL_TIME -> 0L to end
        }
    }
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val _timeRange = MutableStateFlow(TimeRange.TODAY)
    val timeRange: StateFlow<TimeRange> = _timeRange.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val salesSummary: StateFlow<DailySalesSummary> = _timeRange.flatMapLatest { range ->
        val (start, end) = range.getTimestamps()
        orderRepository.getDailySalesSummaryFlow(start, end)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailySalesSummary(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val topSellingItems: StateFlow<List<TopSellingItem>> = _timeRange.flatMapLatest { range ->
        val (start, end) = range.getTimestamps()
        orderRepository.getTopSellingItemsFlow(start, end, limit = 10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val categorySales: StateFlow<List<CategorySalesSummary>> = _timeRange.flatMapLatest { range ->
        val (start, end) = range.getTimestamps()
        orderRepository.getCategorySalesSummaryFlow(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val paymentMethodSales: StateFlow<List<PaymentMethodSalesUiModel>> = _timeRange.flatMapLatest { range ->
        val (start, end) = range.getTimestamps()
        combine(
            orderRepository.getPaymentMethodSalesSummaryFlow(start, end),
            paymentMethodRepository.getAllPaymentMethodsFlow()
        ) { salesBreakdown, configuredMethods ->
            val salesMap = salesBreakdown.associateBy { it.paymentType.uppercase() }
            val result = mutableListOf<PaymentMethodSalesUiModel>()
            val seenCodes = mutableSetOf<String>()

            // 1. Add configured methods from database
            configuredMethods.forEach { method ->
                val codeUpper = method.code.uppercase()
                seenCodes.add(codeUpper)
                val sales = salesMap[codeUpper]
                result.add(
                    PaymentMethodSalesUiModel(
                        paymentType = method.code,
                        totalOrders = sales?.totalOrders ?: 0,
                        totalAmount = sales?.totalAmount ?: 0.0,
                        label = method.name
                    )
                )
            }

            // 2. Add any other payment types present in completed orders
            salesBreakdown.forEach { sales ->
                val codeUpper = sales.paymentType.uppercase()
                if (codeUpper !in seenCodes) {
                    seenCodes.add(codeUpper)
                    result.add(
                        PaymentMethodSalesUiModel(
                            paymentType = sales.paymentType,
                            totalOrders = sales.totalOrders,
                            totalAmount = sales.totalAmount,
                            label = sales.paymentType
                        )
                    )
                }
            }

            result
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderWithItems>> = orderRepository.getAllOrdersWithItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
    }

    suspend fun exportOrdersCsv(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val orders = allOrders.value
            val fileName = "Deco_Sales_Report_${System.currentTimeMillis()}.csv"

            val csvContent = buildString {
                append("Receipt Number,Date,Customer,Subtotal,Discount,Delivery Fee,Grand Total,Payment Type,Status\n")
                orders.forEach { orderWithItems ->
                    val o = orderWithItems.order
                    val date = Formatters.formatDateTime(o.createdAt)
                    append("\"${o.receiptNumber}\",\"$date\",\"${o.customerName ?: ""}\",${o.subtotal},${o.discountAmount},${o.deliFee},${o.grandTotal},\"${o.paymentType}\",\"${o.orderStatus}\"\n")
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/DeCo Export")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IllegalStateException("Could not create file in Downloads")

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val decoExportDir = File(downloadsDir, "DeCo Export").apply { mkdirs() }
                val file = File(decoExportDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Saved to Download/DeCo Export/$fileName", Toast.LENGTH_LONG).show()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to export CSV: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
}
