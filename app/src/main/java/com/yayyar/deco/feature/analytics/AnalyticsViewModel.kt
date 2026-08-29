package com.yayyar.deco.feature.analytics

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yayyar.deco.core.common.Formatters
import com.yayyar.deco.core.database.DecoDatabase
import com.yayyar.deco.core.database.model.CategorySalesSummary
import com.yayyar.deco.core.database.model.DailySalesSummary
import com.yayyar.deco.core.database.model.OrderWithItems
import com.yayyar.deco.core.database.model.TopSellingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.util.Calendar

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DecoDatabase.getInstance(application)
    private val orderDao = db.orderDao()

    private val _timeRange = MutableStateFlow(TimeRange.TODAY)
    val timeRange: StateFlow<TimeRange> = _timeRange.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val salesSummary: StateFlow<DailySalesSummary> = _timeRange.flatMapLatest { range ->
        val (start, end) = range.getTimestamps()
        orderDao.getDailySalesSummaryFlow(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySalesSummary())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val topSellingItems: StateFlow<List<TopSellingItem>> = _timeRange.flatMapLatest { range ->
        val (start, end) = range.getTimestamps()
        orderDao.getTopSellingItemsFlow(start, end, limit = 10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val categorySales: StateFlow<List<CategorySalesSummary>> = _timeRange.flatMapLatest { range ->
        val (start, end) = range.getTimestamps()
        orderDao.getCategorySalesSummaryFlow(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderWithItems>> = orderDao.getAllOrdersWithItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
    }

    suspend fun exportOrdersCsv(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val orders = allOrders.value
            val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(cacheDir, "Deco_Sales_Report_${System.currentTimeMillis()}.csv")

            FileWriter(file).use { writer ->
                writer.append("Receipt Number,Date,Customer,Subtotal,Discount,Delivery Fee,Grand Total,Payment Type,Status\n")
                orders.forEach { orderWithItems ->
                    val o = orderWithItems.order
                    val date = Formatters.formatDateTime(o.createdAt)
                    writer.append("\"${o.receiptNumber}\",\"$date\",\"${o.customerName ?: ""}\",${o.subtotal},${o.discountAmount},${o.deliFee},${o.grandTotal},\"${o.paymentType}\",\"${o.orderStatus}\"\n")
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Deco Sales Report Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Export Sales Report CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

enum class TimeRange(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time");

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
                cal.timeInMillis to end
            }
            THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.timeInMillis to end
            }
            ALL_TIME -> 0L to end
        }
    }
}
