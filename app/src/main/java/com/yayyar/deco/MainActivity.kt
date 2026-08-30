package com.yayyar.deco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yayyar.deco.feature.analytics.AnalyticsScreen
import com.yayyar.deco.feature.analytics.AnalyticsViewModel
import com.yayyar.deco.feature.inventory.InventoryScreen
import com.yayyar.deco.feature.inventory.InventoryViewModel
import com.yayyar.deco.feature.pos.PosScreen
import com.yayyar.deco.feature.pos.PosViewModel
import com.yayyar.deco.feature.shift.ShiftScreen
import com.yayyar.deco.feature.shift.ShiftViewModel
import com.yayyar.deco.ui.theme.DecoTheme
import dagger.hilt.android.AndroidEntryPoint

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

@Composable
fun DecoApp() {
    var currentDestination by remember { mutableStateOf(PosDestination.POS) }

    val posViewModel: PosViewModel = hiltViewModel()
    val inventoryViewModel: InventoryViewModel = hiltViewModel()
    val shiftViewModel: ShiftViewModel = hiltViewModel()
    val analyticsViewModel: AnalyticsViewModel = hiltViewModel()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTabletLandscape = maxWidth >= 720.dp

        if (isTabletLandscape) {
            // Tablet Layout with Navigation Rail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(modifier = Modifier.fillMaxHeight()) {
                    PosDestination.values().forEach { destination ->
                        NavigationRailItem(
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            icon = { Icon(destination.icon, contentDescription = destination.title) },
                            label = { Text(destination.title) }
                        )
                    }
                }

                Scaffold(modifier = Modifier.weight(1f)) { innerPadding ->
                    AppScreenContent(
                        destination = currentDestination,
                        posViewModel = posViewModel,
                        inventoryViewModel = inventoryViewModel,
                        shiftViewModel = shiftViewModel,
                        analyticsViewModel = analyticsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        } else {
            // Mobile Layout with Bottom Navigation Bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar {
                        PosDestination.values().forEach { destination ->
                            NavigationBarItem(
                                selected = currentDestination == destination,
                                onClick = { currentDestination = destination },
                                icon = { Icon(destination.icon, contentDescription = destination.title) },
                                label = { Text(destination.title) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                AppScreenContent(
                    destination = currentDestination,
                    posViewModel = posViewModel,
                    inventoryViewModel = inventoryViewModel,
                    shiftViewModel = shiftViewModel,
                    analyticsViewModel = analyticsViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
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
    modifier: Modifier = Modifier
) {
    when (destination) {
        PosDestination.POS -> PosScreen(viewModel = posViewModel, modifier = modifier)
        PosDestination.INVENTORY -> InventoryScreen(viewModel = inventoryViewModel, modifier = modifier)
        PosDestination.SHIFT -> ShiftScreen(viewModel = shiftViewModel, modifier = modifier)
        PosDestination.ANALYTICS -> AnalyticsScreen(viewModel = analyticsViewModel, modifier = modifier)
    }
}