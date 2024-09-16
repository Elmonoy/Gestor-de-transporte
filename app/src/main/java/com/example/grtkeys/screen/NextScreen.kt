package com.example.grtkeys.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.grtkeys.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_pasajero),
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
                    lineTo(size.width, size.height * 0.4f)
                    lineTo(0f, size.height)
                    close()
                }
                canvas.drawPath(path, androidx.compose.ui.graphics.Paint().apply {
                    color = Color.Yellow

                    alpha = 0.7f
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
            Text(
                text = "Para registrarse escribe un nombre de usuario",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp),
                color = Color.White,
                modifier = Modifier.padding(bottom = 15.dp)
            )
            Text(
                text = "Ingresa tu nombre de usuario",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 16.sp),
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Gray,
                    unfocusedLabelColor = Color.White,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )

            Button(
                onClick = { navController.navigate("mapScreen") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Continuar", color = Color.White)
            }
        }
    }
}
