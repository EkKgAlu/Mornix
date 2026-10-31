package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.MornixTopBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MarketsScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.MornixTheme
import com.example.ui.viewmodel.MornixViewModel

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: MornixViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MornixTheme {
                MornixApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MornixApp(viewModel: MornixViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isPlayingTts by viewModel.isPlayingTts.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()

    // Request location permission so device current location is active by default
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.refreshDeviceLocation()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val availableCities = listOf(
        "Current Location", "Kathmandu", "Pokhara", "Biratnagar", "Itahari",
        "Dharan", "Chitwan", "Butwal", "Nepalgunj"
    )

    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        NavItem("News", Icons.Filled.Newspaper, Icons.Outlined.Newspaper, "nav_news"),
        NavItem("Markets", Icons.Filled.ShowChart, Icons.Outlined.ShowChart, "nav_markets"),
        NavItem("Weather", Icons.Filled.WbSunny, Icons.Outlined.WbSunny, "nav_weather"),
        NavItem("Ask AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_chat"),
        NavItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
    )

    // Handle back button: return to Home tab if currently on a sub-screen
    BackHandler(enabled = selectedTab != 0) {
        viewModel.selectTab(0)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        if (isTablet) {
            // Tablet / Foldable Adaptive Layout: NavigationRail on side
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxHeight()
                        .testTag("tablet_navigation_rail")
                ) {
                    navItems.forEachIndexed { index, item ->
                        NavigationRailItem(
                            selected = (selectedTab == index),
                            onClick = { viewModel.selectTab(index) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(item.title) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }

                Scaffold(
                    topBar = {
                        MornixTopBar(
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.refreshAll() }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(modifier = Modifier.widthIn(max = 1100.dp)) {
                            ScreenContent(selectedTab = selectedTab, viewModel = viewModel)
                        }
                    }
                }
            }
        } else {
            // Phone Compact Layout: NavigationBar at bottom
            Scaffold(
                topBar = {
                    MornixTopBar(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refreshAll() }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = (selectedTab == index),
                                onClick = { viewModel.selectTab(index) },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = { Text(item.title) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ScreenContent(selectedTab = selectedTab, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ScreenContent(selectedTab: Int, viewModel: MornixViewModel) {
    when (selectedTab) {
        0 -> HomeScreen(viewModel = viewModel)
        1 -> NewsScreen(viewModel = viewModel)
        2 -> MarketsScreen(viewModel = viewModel)
        3 -> WeatherScreen(viewModel = viewModel)
        4 -> ChatScreen(viewModel = viewModel)
        5 -> SettingsScreen(viewModel = viewModel)
    }
}
