package com.dulumina.androtik

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AndrotikNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            // TODO: Login screen
        }
        composable("home") {
            // TODO: Home/Dashboard screen
        }
        composable("interfaces") {
            // TODO: Interface list screen
        }
        composable("profile") {
            // TODO: Profile management screen
        }
    }
}
