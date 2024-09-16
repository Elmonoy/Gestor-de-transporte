package com.example.grtkeys.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.grtkeys.R // Asegúrate de que la ruta de tu recurso de imagen sea correcta

@Composable
fun SettingsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD3D3D3)) // Fondo gris más claro
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ruedaconfig), // Imagen de tuerca
                contentDescription = "Settings Icon",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CONFIGURACIÓN", // Título grande
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black, // Color del texto del título
            )
        }

        LongButtonWithIcon(
            text = "Editar Datos",
            iconResId = R.drawable.persona, // Imagen para "Editar Datos"
            onClick = {
                // Navega a la vista de EditUserScreen
                navController.navigate("edit_con")
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        LongButtonWithIcon(
            text = "Cerrar Sesión",
            iconResId = R.drawable.salida, // Imagen para "Cerrar Sesión"
            onClick = {
                navController.navigate("pagina_principal") {
                    popUpTo("pagina_principal") { inclusive = true } // Elimina la pila de navegación
                }
            },
            buttonColor = Color.Red,
            textColor = Color.White
        )
    }
}

@Composable
fun LongButtonWithIcon(text: String, iconResId: Int, onClick: () -> Unit, buttonColor: Color = Color.Gray, textColor: Color = Color.Black) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor) // Tamaño de fuente más grande
        }
    }
}