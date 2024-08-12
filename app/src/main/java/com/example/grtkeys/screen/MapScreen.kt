package com.example.grtkeys.screen

import android.content.Intent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.grtkeys.MapAbiActivity

@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current

    Button(onClick = {

        // Inicia la actividad MapaUbiActivity al hacer clic en el botón
        context.startActivity(Intent(context, MapAbiActivity::class.java))
    }) {
        Text("Abrir Mapa")
    }
}
