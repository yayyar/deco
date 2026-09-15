package com.yayyar.teahouse.feature.analytics

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yayyar.teahouse.R
import com.yayyar.teahouse.core.database.model.TopSellingItem
import com.yayyar.teahouse.core.ui.components.CurrencyText
import com.yayyar.teahouse.ui.theme.AccentBlue
import com.yayyar.teahouse.ui.theme.AccentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllSellingProductsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AllSellingProductsViewModel = hiltViewModel()
) {
    val timeRange by viewModel.timeRange.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val canLoadMore by viewModel.canLoadMore.collectAsState()

    var isSearchActive by remember { mutableStateOf(searchQuery.isNotBlank()) }
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()

    BackHandler {
        if (isSearchActive) {
            isSearchActive = false
            viewModel.setSearchQuery("")
        } else {
            onBack()
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    // Pagination: detect when user scrolls near the bottom
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItemCount > 0 && lastVisibleItemIndex >= totalItemCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && canLoadMore && !isLoadingMore && !isLoading && !isRefreshing) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text(stringResource(R.string.top_sellers_search_hint), fontSize = 16.sp) },
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.top_sellers_title),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSearchActive) {
                                isSearchActive = false
                                viewModel.setSearchQuery("")
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.action_search)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Time Range Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
                ) {
                    items(TimeRange.entries.size) { index ->
                        val range = TimeRange.entries[index]
                        FilterChip(
                            selected = timeRange == range,
                            onClick = { viewModel.setTimeRange(range) },
                            label = { Text(stringResource(range.titleRes)) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                if (isLoading && items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                } else if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank())
                                    stringResource(R.string.top_sellers_no_match, searchQuery)
                                else
                                    stringResource(R.string.sales_no_orders),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Summary Stats Banner
                        item {
                            val totalUnits = items.sumOf { it.totalQuantitySold }
                            val totalRevenue = items.sumOf { it.totalRevenue }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.top_sellers_total_units),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = stringResource(R.string.top_sellers_units_count, totalUnits),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentBlue
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.sales_total_revenue),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        CurrencyText(
                                            amount = totalRevenue,
                                            fontSize = 16.sp,
                                            color = AccentGreen
                                        )
                                    }
                                }
                            }
                        }

                        // Product Items
                        itemsIndexed(
                            items = items,
                            key = { _, item -> item.variantId }
                        ) { index, item ->
                            TopSellingItemCard(rank = index + 1, item = item)
                        }

                        // Pagination Loading / End footer
                        item {
                            if (isLoadingMore) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Text(
                                            text = stringResource(R.string.top_sellers_loading_more),
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else if (!canLoadMore && items.size >= 10) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.top_sellers_showing_all, items.size),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopSellingItemCard(
    rank: Int,
    item: TopSellingItem,
    modifier: Modifier = Modifier
) {
    val rankBadgeColor = when (rank) {
        1 -> Color(0xFFEAB308) // Gold
        2 -> Color(0xFF94A3B8) // Silver
        3 -> Color(0xFFD97706) // Bronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val rankTextColor = when (rank) {
        1, 2, 3 -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Rank Circle + Product & Variant Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(rankBadgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        color = rankTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.variantName.isNotBlank() && item.variantName != "Default") {
                        Text(
                            text = item.variantName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Bottom Row: Total Quantity Sold + Total Revenue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(R.string.top_sellers_sold_count, item.totalQuantitySold),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                CurrencyText(
                    amount = item.totalRevenue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
