package com.example.grtkeys.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.grtkeys.MapAbiActivity
import com.example.grtkeys.screen.LoginScreen
import com.example.grtkeys.screen.NextScreen
import com.example.grtkeys.screen.PaginaPrincipal

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "pagina_principal", modifier = modifier) {
        composable("pagina_principal") {
            PaginaPrincipal(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("nextScreen") {
            NextScreen(navController = navController)
        }
        composable("MapLogin") {
            MapAbiActivity()
        }
    }
}
