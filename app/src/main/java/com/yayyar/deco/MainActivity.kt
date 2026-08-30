package com.yayyar.deco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yayyar.deco.core.database.model.ProductWithVariants
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
import com.yayyar.deco.feature.shift.ShiftScreen
import com.yayyar.deco.feature.shift.ShiftViewModel
import com.yayyar.deco.ui.theme.DecoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

enum class PosDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    POS("pos", "POS", Icons.Default.PointOfSale),
    INVENTORY("inventory", "Inventory", Icons.Default.Checkroom),
    SHIFT("shift", "Shift", Icons.Default.AccountBalanceWallet),
    ANALYTICS("analytics", "Analytics", Icons.Default.Analytics)
}

sealed interface AppDestination {
    data class Main(val destination: PosDestination = PosDestination.POS) : AppDestination
    data object ProductList : AppDestination
    data class ProductAdd(val productToEdit: ProductWithVariants? = null) : AppDestination
    data object CategoryList : AppDestination
    data object CategoryAdd : AppDestination
}

@AndroidEntryPoint(ComponentActivity::class)
class MainActivity : Hilt_MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DecoTheme {
                DecoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecoApp() {
    var appDestination by remember { mutableStateOf<AppDestination>(AppDestination.Main(PosDestination.POS)) }

    val posViewModel: PosViewModel = hiltViewModel()
    val inventoryViewModel: InventoryViewModel = hiltViewModel()
    val shiftViewModel: ShiftViewModel = hiltViewModel()
    val analyticsViewModel: AnalyticsViewModel = hiltViewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    when (val current = appDestination) {
        is AppDestination.Main -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(280.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Deco POS",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        PosDestination.values().forEach { dest ->
                            NavigationDrawerItem(
                                icon = { Icon(dest.icon, contentDescription = dest.title) },
                                label = { Text(dest.title) },
                                selected = current.destination == dest,
                                onClick = {
                                    appDestination = AppDestination.Main(dest)
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(current.destination.title) },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Open navigation menu"
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    AppScreenContent(
                        destination = current.destination,
                        posViewModel = posViewModel,
                        inventoryViewModel = inventoryViewModel,
                        shiftViewModel = shiftViewModel,
                        analyticsViewModel = analyticsViewModel,
                        onNavigateToProducts = { appDestination = AppDestination.ProductList },
                        onNavigateToCategories = { appDestination = AppDestination.CategoryList },
                        modifier = Modifier.padding(innerPadding)
                    )
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
    }
}

@Composable
private fun AppScreenContent(
    destination: PosDestination,
    posViewModel: PosViewModel,
    inventoryViewModel: InventoryViewModel,
    shiftViewModel: ShiftViewModel,
    analyticsViewModel: AnalyticsViewModel,
    onNavigateToProducts: () -> Unit,
    onNavigateToCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (destination) {
        PosDestination.POS -> PosScreen(viewModel = posViewModel, modifier = modifier)
        PosDestination.INVENTORY -> InventoryScreen(
            viewModel = inventoryViewModel,
            onNavigateToProducts = onNavigateToProducts,
            onNavigateToCategories = onNavigateToCategories,
            modifier = modifier
        )
        PosDestination.SHIFT -> ShiftScreen(viewModel = shiftViewModel, modifier = modifier)
        PosDestination.ANALYTICS -> AnalyticsScreen(viewModel = analyticsViewModel, modifier = modifier)
    }
}