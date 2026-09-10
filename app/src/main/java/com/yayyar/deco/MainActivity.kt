package com.yayyar.deco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.outlined.Notifications
import androidx.hilt.navigation.compose.hiltViewModel
import com.yayyar.deco.core.data.repository.PreferencesRepository
import com.yayyar.deco.core.database.model.ProductWithVariants
import com.yayyar.deco.feature.analytics.AllSalesScreen
import com.yayyar.deco.feature.analytics.AllSellingProductsScreen
import com.yayyar.deco.feature.analytics.AnalyticsScreen
import com.yayyar.deco.feature.analytics.AnalyticsViewModel
import com.yayyar.deco.feature.inventory.CategoryAddScreen
import com.yayyar.deco.feature.inventory.CategoryListScreen
import com.yayyar.deco.feature.inventory.InventoryScreen
import com.yayyar.deco.feature.inventory.InventoryViewModel
import com.yayyar.deco.feature.inventory.ProductAddScreen
import com.yayyar.deco.feature.inventory.ProductListScreen
import com.yayyar.deco.feature.pos.PosScreen
import com.yayyar.deco.feature.pos.PosViewModel
import com.yayyar.deco.feature.settings.PaymentMethodScreen
import com.yayyar.deco.feature.settings.PrinterScreen
import com.yayyar.deco.feature.settings.SettingScreen
import com.yayyar.deco.ui.theme.DecoTheme
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import com.yayyar.deco.feature.pos.DraftSalesDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PosDestination(
    val title: String,
    val icon: ImageVector
) {
    POS("Sales", Icons.Outlined.LocalMall),
    INVENTORY("Items", Icons.Outlined.Checkroom),
    ANALYTICS("Reports", Icons.Outlined.Analytics),
    SETTINGS("Settings", Icons.Outlined.Settings)
}

sealed interface AppDestination : java.io.Serializable {
    data class Main(val destination: PosDestination = PosDestination.POS) : AppDestination
    data object ProductList : AppDestination
    data class ProductAdd(val productToEdit: ProductWithVariants? = null) : AppDestination
    data object CategoryList : AppDestination
    data object CategoryAdd : AppDestination
    data object AllSellingProducts : AppDestination
    data object AllSales : AppDestination
    data object Settings : AppDestination
    data object PaymentMethods : AppDestination
    data object Printers : AppDestination
}

@AndroidEntryPoint(ComponentActivity::class)
class MainActivity : Hilt_MainActivity() {
    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkModePref by preferencesRepository.isDarkMode.collectAsState()
            val isDark = darkModePref ?: isSystemInDarkTheme()

            DecoTheme(darkTheme = isDark) {
                DecoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecoApp() {
    var appDestination by rememberSaveable { mutableStateOf<AppDestination>(AppDestination.Main(PosDestination.POS)) }
    var isInventoryGridView by rememberSaveable { mutableStateOf(false) }
    var isPosSearchActive by rememberSaveable { mutableStateOf(false) }

    val posViewModel: PosViewModel = hiltViewModel()
    val inventoryViewModel: InventoryViewModel = hiltViewModel()
    val analyticsViewModel: AnalyticsViewModel = hiltViewModel()

    val draftOrders by posViewModel.draftOrders.collectAsState()
    var showDraftSalesDialog by rememberSaveable { mutableStateOf(false) }

    val posSearchQuery by posViewModel.searchQuery.collectAsState()
    val posFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isPosSearchActive) {
        if (isPosSearchActive) {
            posFocusRequester.requestFocus()
        }
    }

    if (showDraftSalesDialog) {
        DraftSalesDialog(
            draftOrders = draftOrders,
            onDismiss = { showDraftSalesDialog = false },
            onRestoreDraft = { draftOrder ->
                posViewModel.restoreDraftSale(draftOrder)
                appDestination = AppDestination.Main(PosDestination.POS)
            },
            onDeleteDraft = { draftOrderId ->
                posViewModel.deleteDraftSale(draftOrderId)
            }
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    when (val current = appDestination) {
        is AppDestination.Main -> {
            BackHandler(enabled = isPosSearchActive && current.destination == PosDestination.POS) {
                isPosSearchActive = false
                posViewModel.setSearchQuery("")
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(280.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "DeCo",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        PosDestination.entries.filter { it != PosDestination.SETTINGS }.forEach { dest ->
                            NavigationDrawerItem(
                                icon = { Icon(dest.icon, contentDescription = dest.title) },
                                label = { Text(dest.title) },
                                selected = current.destination == dest,
                                onClick = {
                                    if (dest != PosDestination.POS) {
                                        isPosSearchActive = false
                                        posViewModel.setSearchQuery("")
                                    }
                                    appDestination = AppDestination.Main(dest)
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") },
                            selected = current.destination == PosDestination.SETTINGS,
                            onClick = {
                                isPosSearchActive = false
                                posViewModel.setSearchQuery("")
                                appDestination = AppDestination.Main(PosDestination.SETTINGS)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isTabletLandscape = maxWidth >= 720.dp
                    val isPosTablet = isTabletLandscape && current.destination == PosDestination.POS

                    val context = LocalContext.current
                    val topBarContent: @Composable () -> Unit = {
                        MainTopAppBar(
                            destination = current.destination,
                            isPosSearchActive = isPosSearchActive,
                            posSearchQuery = posSearchQuery,
                            onPosSearchQueryChange = { posViewModel.setSearchQuery(it) },
                            onPosSearchActiveChange = { isPosSearchActive = it },
                            posFocusRequester = posFocusRequester,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            draftOrdersCount = draftOrders.size,
                            onOpenDraftSales = { showDraftSalesDialog = true },
                            isInventoryGridView = isInventoryGridView,
                            onToggleInventoryGridView = { isInventoryGridView = !isInventoryGridView },
                            onExportCsv = {
                                scope.launch {
                                    analyticsViewModel.exportOrdersCsv(context)
                                }
                            },
                            windowInsets = if (isPosTablet) WindowInsets(0, 0, 0, 0) else TopAppBarDefaults.windowInsets
                        )
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            if (!isPosTablet) {
                                topBarContent()
                            }
                        }
                    ) { innerPadding ->
                        AppScreenContent(
                            destination = current.destination,
                            posViewModel = posViewModel,
                            inventoryViewModel = inventoryViewModel,
                            analyticsViewModel = analyticsViewModel,
                            isInventoryGridView = isInventoryGridView,
                            onNavigateToProducts = { appDestination = AppDestination.ProductList },
                            onNavigateToCategories = { appDestination = AppDestination.CategoryList },
                            onNavigateToAllSellingProducts = { appDestination = AppDestination.AllSellingProducts },
                            onNavigateToAllSales = { appDestination = AppDestination.AllSales },
                            onNavigateToPaymentMethods = { appDestination = AppDestination.PaymentMethods },
                            onNavigateToPrinters = { appDestination = AppDestination.Printers },
                            topBar = if (isPosTablet) topBarContent else ({}),
                            modifier = if (isPosTablet) Modifier.fillMaxSize() else Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
        is AppDestination.ProductList -> {
            ProductListScreen(
                viewModel = inventoryViewModel,
                onBack = { appDestination = AppDestination.Main(PosDestination.INVENTORY) },
                onAddProduct = { appDestination = AppDestination.ProductAdd() },
                onEditProduct = { prod -> appDestination = AppDestination.ProductAdd(prod) }
            )
        }
        is AppDestination.ProductAdd -> {
            val categories by inventoryViewModel.categories.collectAsState()
            ProductAddScreen(
                productWithVariants = current.productToEdit,
                categories = categories,
                onBack = { appDestination = AppDestination.ProductList },
                onSave = { prod, variants ->
                    inventoryViewModel.saveProductWithVariants(prod, variants)
                },
                onDelete = { prod ->
                    inventoryViewModel.deleteProduct(prod)
                }
            )
        }
        is AppDestination.CategoryList -> {
            CategoryListScreen(
                viewModel = inventoryViewModel,
                onBack = { appDestination = AppDestination.Main(PosDestination.INVENTORY) },
                onAddCategory = { appDestination = AppDestination.CategoryAdd }
            )
        }
        is AppDestination.CategoryAdd -> {
            CategoryAddScreen(
                onBack = { appDestination = AppDestination.CategoryList },
                onSaveCategory = { name ->
                    inventoryViewModel.addCategory(name)
                }
            )
        }
        is AppDestination.AllSellingProducts -> {
            AllSellingProductsScreen(
                onBack = { appDestination = AppDestination.Main(PosDestination.ANALYTICS) }
            )
        }
        is AppDestination.AllSales -> {
            AllSalesScreen(
                onBack = { appDestination = AppDestination.Main(PosDestination.ANALYTICS) }
            )
        }
        is AppDestination.Settings -> {
            SettingScreen(
                onBack = { appDestination = AppDestination.Main(PosDestination.POS) },
                onNavigateToPaymentMethods = { appDestination = AppDestination.PaymentMethods },
                onNavigateToPrinters = { appDestination = AppDestination.Printers }
            )
        }
        is AppDestination.PaymentMethods -> {
            PaymentMethodScreen(
                onBack = { appDestination = AppDestination.Main(PosDestination.SETTINGS) }
            )
        }
        is AppDestination.Printers -> {
            PrinterScreen(
                onBack = { appDestination = AppDestination.Main(PosDestination.SETTINGS) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopAppBar(
    destination: PosDestination,
    isPosSearchActive: Boolean,
    posSearchQuery: String,
    onPosSearchQueryChange: (String) -> Unit,
    onPosSearchActiveChange: (Boolean) -> Unit,
    posFocusRequester: FocusRequester,
    onOpenDrawer: () -> Unit,
    draftOrdersCount: Int = 0,
    onOpenDraftSales: () -> Unit = {},
    isInventoryGridView: Boolean,
    onToggleInventoryGridView: () -> Unit,
    onExportCsv: () -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
) {
    TopAppBar(
        windowInsets = windowInsets,
        title = {
            if (destination == PosDestination.POS && isPosSearchActive) {
                OutlinedTextField(
                    value = posSearchQuery,
                    onValueChange = onPosSearchQueryChange,
                    placeholder = { Text("Search products") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(posFocusRequester),
                    trailingIcon = {
                        if (posSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { onPosSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    }
                )
            } else {
                Text(destination.title)
            }
        },
        navigationIcon = {
            if (destination == PosDestination.POS && isPosSearchActive) {
                IconButton(onClick = {
                    onPosSearchActiveChange(false)
                    onPosSearchQueryChange("")
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit search"
                    )
                }
            } else {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open navigation menu"
                    )
                }
            }
        },
        actions = {
            if (destination == PosDestination.POS && !isPosSearchActive) {
                IconButton(onClick = onOpenDraftSales) {
                    BadgedBox(
                        badge = {
                            if (draftOrdersCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                ) {
                                    Text(text = "$draftOrdersCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Draft sales",
                        )
                    }
                }
                IconButton(
                    onClick = { onPosSearchActiveChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Products",
                    )
                }
            }
            if (destination == PosDestination.INVENTORY) {
                IconButton(onClick = onToggleInventoryGridView) {
                    Icon(
                        imageVector = if (isInventoryGridView) Icons.AutoMirrored.Outlined.ViewList else Icons.Outlined.GridView,
                        contentDescription = if (isInventoryGridView) "Switch to List View" else "Switch to Grid View"
                    )
                }
            }
            if (destination == PosDestination.ANALYTICS) {
                var showAnalyticsMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showAnalyticsMenu = true }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More options",
                        )
                    }
                    DropdownMenu(
                        expanded = showAnalyticsMenu,
                        onDismissRequest = { showAnalyticsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export CSV") },
                            onClick = {
                                showAnalyticsMenu = false
                                onExportCsv()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun AppScreenContent(
    destination: PosDestination,
    posViewModel: PosViewModel,
    inventoryViewModel: InventoryViewModel,
    analyticsViewModel: AnalyticsViewModel,
    isInventoryGridView: Boolean,
    onNavigateToProducts: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToAllSellingProducts: () -> Unit = {},
    onNavigateToAllSales: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToPrinters: () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (destination) {
        PosDestination.POS -> PosScreen(
            viewModel = posViewModel,
            topBar = topBar,
            modifier = modifier
        )
        PosDestination.INVENTORY -> InventoryScreen(
            viewModel = inventoryViewModel,
            isGridView = isInventoryGridView,
            onNavigateToProducts = onNavigateToProducts,
            onNavigateToCategories = onNavigateToCategories,
            modifier = modifier
        )
        PosDestination.ANALYTICS -> AnalyticsScreen(
            viewModel = analyticsViewModel,
            onNavigateToAllSellingProducts = onNavigateToAllSellingProducts,
            onNavigateToAllSales = onNavigateToAllSales,
            modifier = modifier
        )
        PosDestination.SETTINGS -> SettingScreen(
            onNavigateToPaymentMethods = onNavigateToPaymentMethods,
            onNavigateToPrinters = onNavigateToPrinters,
            showTopBar = false,
            modifier = modifier
        )
    }
}