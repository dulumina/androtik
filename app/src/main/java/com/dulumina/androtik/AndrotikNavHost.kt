package com.dulumina.androtik

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dulumina.androtik.ui.login.LoginScreen
import com.dulumina.androtik.ui.login.LoginViewModel

@Composable
fun AndrotikNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as AndrotikApp

    NavHost(navController = navController, startDestination = "login") {
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
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            // TODO: Dashboard screen
        }
        composable("interfaces") {
            // TODO: Interface list screen
        }
        composable("profile") {
            // TODO: Profile management screen
        }
    }
}
