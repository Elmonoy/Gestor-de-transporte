package com.example.grtkeys.screen



import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.grtkeys.R


@Composable
fun PaginaPrincipal(navController: NavHostController, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_bus),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Capa de color negro diagonal en el lado izquierdo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.4f) // Ajuste para bajar la altura de la capa negra
                    lineTo(0f, size.height)
                    close()
                }
                canvas.drawPath(path, androidx.compose.ui.graphics.Paint().apply {
                    color = Color.Black
                    alpha = 0.7f // Ajusta la opacidad si es necesario
                })
            }
        }
        // Contenido dentro de la capa negra
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ajusta el contenido para que esté dentro de la capa negra
            Text(
                text = "Bienvenidos a nuestra aplicación",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp), // Ajuste de tamaño de fuente
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            Text(
                text = "Selecciona tu rol",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = { /* TODO: Navegar a la vista de conductor */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(text = "Conductor", color = Color.White)
            }
            Button(
                onClick = { navController.navigate("login") }, // Navegar a la pantalla de inicio de sesión
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Pasajero", color = Color.White)
            }
        }
    }
}
