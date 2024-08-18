import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.grtkeys.MapScreen
import com.example.grtkeys.conductor.LoginConductor
import com.example.grtkeys.conductor.MapScreenCon
import com.example.grtkeys.screen.EditUserScreen
import com.example.grtkeys.screen.LoginPasajero
import com.example.grtkeys.screen.LoginScreen

import com.example.grtkeys.screen.NextScreen
import com.example.grtkeys.screen.PaginaPrincipal
import com.example.grtkeys.screen.SettingsScreen

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
        composable("mapScreen") {
            MapScreen(navController = navController)
        }
        composable("LoginConductor") {
            LoginConductor(navController = navController)
        }
        composable("LoginPasajero") {
            LoginPasajero(navController = navController)
        }
        composable("LoginPasajeroMap") {
            MapScreen(navController = navController)
        }
        composable("LoginConductorMap") {
            MapScreenCon()
        }
        composable("settingsScreen") {
            SettingsScreen(navController = navController)
        }
        composable("edit_user") { backStackEntry ->
            EditUserScreen(
                currentUserName = "Nombre Actual", // Estos valores pueden ser dinámicos
                currentUserEmail = "email@example.com",
                navController = navController, // Asegúrate de pasar el navController aquí
                onSave = { newName, newEmail ->
                    // Lógica para manejar el guardado
                }
            )
        }
    }
}
