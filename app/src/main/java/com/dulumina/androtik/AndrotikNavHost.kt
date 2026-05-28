package com.dulumina.androtik

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dulumina.androtik.ui.dashboard.DashboardScreen
import com.dulumina.androtik.ui.dashboard.DashboardViewModel
import com.dulumina.androtik.ui.interfaces.InterfacesScreen
import com.dulumina.androtik.ui.interfaces.InterfacesViewModel
import com.dulumina.androtik.ui.ipaddress.IpAddressScreen
import com.dulumina.androtik.ui.ipaddress.IpAddressViewModel
import com.dulumina.androtik.ui.iproutes.IpRoutesScreen
import com.dulumina.androtik.ui.iproutes.IpRoutesViewModel
import com.dulumina.androtik.ui.login.LoginScreen
import com.dulumina.androtik.ui.login.LoginViewModel

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("home", "Dashboard", Icons.Default.Home),
    BottomNavItem("interfaces", "Interfaces", Icons.Default.Wifi),
    BottomNavItem("settings", "Settings", Icons.Default.Settings),
)

@Composable
fun AndrotikNavHost() {
    val rootNavController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as AndrotikApp

    NavHost(navController = rootNavController, startDestination = "login") {
        composable("login") {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(
                    app.container.routerRepository,
                    app.container.sessionManager
                )
            )
            LoginScreen(
                viewModel = loginViewModel,
                onConnected = {
                    rootNavController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScaffold(app = app, onLogout = {
                rootNavController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
    }
}

@Composable
private fun MainScaffold(app: AndrotikApp, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(
                        app.container.sessionManager,
                        app.container.routerRepository
                    )
                )
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToIpAddresses = {
                        navController.navigate("ip_addresses")
                    },
                    onNavigateToInterfaces = {
                        navController.navigate("interfaces") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToIpRoutes = {
                        navController.navigate("ip_routes")
                    },
                    onNavigateToDhcp = {
                        navController.navigate("dhcp")
                    },
                    onNavigateToFirewall = {
                        navController.navigate("firewall")
                    },
                    onLogout = onLogout
                )
            }
            composable("interfaces") {
                val interfacesViewModel: InterfacesViewModel = viewModel(
                    factory = InterfacesViewModel.Factory(
                        app.container.sessionManager
                    )
                )
                InterfacesScreen(viewModel = interfacesViewModel)
            }
            composable("ip_addresses") {
                val viewModel: IpAddressViewModel = viewModel(
                    factory = IpAddressViewModel.Factory(app.container.sessionManager)
                )
                IpAddressScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("ip_routes") {
                val viewModel: IpRoutesViewModel = viewModel(
                    factory = IpRoutesViewModel.Factory(app.container.sessionManager)
                )
                IpRoutesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("dhcp") {
                // TODO: DHCP screen
            }
            composable("firewall") {
                // TODO: Firewall screen
            }
            composable("settings") {
                // TODO: Settings screen
            }
        }
    }
}
