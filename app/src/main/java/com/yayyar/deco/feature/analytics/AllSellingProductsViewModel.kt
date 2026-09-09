package com.yayyar.deco.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayyar.deco.core.data.repository.OrderRepository
import com.yayyar.deco.core.database.model.TopSellingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllSellingProductsViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    companion object {
        const val PAGE_SIZE = 20
    }

    private val _timeRange = MutableStateFlow(TimeRange.TODAY)
    val timeRange: StateFlow<TimeRange> = _timeRange.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _rawItems = MutableStateFlow<List<TopSellingItem>>(emptyList())
    private val _filteredItems = MutableStateFlow<List<TopSellingItem>>(emptyList())
    val items: StateFlow<List<TopSellingItem>> = _filteredItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private var currentOffset = 0
    private var loadJob: Job? = null

    init {
        loadInitial(isRefresh = false)
    }

    fun setTimeRange(range: TimeRange) {
        if (_timeRange.value == range) return
        _timeRange.value = range
        loadInitial(isRefresh = false)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterItems()
    }

    private fun filterItems() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) {
            _filteredItems.value = _rawItems.value
        } else {
            _filteredItems.value = _rawItems.value.filter { item ->
                item.productName.contains(query, ignoreCase = true) ||
                        item.variantName.contains(query, ignoreCase = true)
            }
        }
    }

    fun refresh() {
        loadInitial(isRefresh = true)
    }

    fun loadInitial(isRefresh: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _isLoading.value = true
            }

            currentOffset = 0
            val (start, end) = _timeRange.value.getTimestamps()

            try {
                val results = orderRepository.getTopSellingItemsPaged(
                    startTime = start,
                    endTime = end,
                    limit = PAGE_SIZE,
                    offset = 0
                )
                _rawItems.value = results
                currentOffset = results.size
                _canLoadMore.value = results.size >= PAGE_SIZE
                filterItems()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoading.value || _isRefreshing.value || _isLoadingMore.value || !_canLoadMore.value) {
            return
        }

        viewModelScope.launch {
            _isLoadingMore.value = true
            val (start, end) = _timeRange.value.getTimestamps()

            try {
                val results = orderRepository.getTopSellingItemsPaged(
                    startTime = start,
                    endTime = end,
                    limit = PAGE_SIZE,
                    offset = currentOffset
                )
                if (results.isNotEmpty()) {
                    val updated = _rawItems.value + results
                    _rawItems.value = updated
                    currentOffset += results.size
                    filterItems()
                }
                _canLoadMore.value = results.size >= PAGE_SIZE
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingMore.value = false
            }
        }
    }
}
