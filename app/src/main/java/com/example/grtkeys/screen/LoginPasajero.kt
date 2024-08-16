package com.example.grtkeys.screen


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.grtkeys.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun LoginPasajero(navController: NavHostController) {
    // Variables de estado para los campos de texto
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_buslp),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Capa de color negro diagonal en el lado izquierdo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                val path = androidx.compose.ui.graphics.Path().apply {
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
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Inicio de Sesión",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center,

                )

            Text(
                text = "NOMBRE USUARIO",
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 36.sp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                textAlign = TextAlign.Start,

                )
            TextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Gray,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )

            Text(
                text = "CORREO ELECTRONICO",
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start,

                )
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Gray,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )

            Text(
                text = "CODIGO DE VERIFICACION",
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp),
                textAlign = TextAlign.Start,

                )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                TextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(20.dp)),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Gray,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { /* Acción para enviar el código */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White // Cambia el color del texto a blanco para contraste
                    ),
                    modifier = Modifier
                        .padding(10.dp) // Ajusta el padding alrededor del botón
                        .size(150.dp, 50.dp) // Ajusta el tamaño del botón
                ) {
                    Text(
                        text = "Enviar Código",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold, // Configura el texto en negrita
                            fontSize = 16.sp // Tamaño de fuente para el texto
                        )
                    )
                }
            }
            Button(
                onClick = { navController.navigate("LoginPasajeroMap")  },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White // Cambia el color del texto a blanco para contraste
                ),
                modifier = Modifier
                    .padding(10.dp) // Ajusta el padding alrededor del botón
                    .size(250.dp, 50.dp) // Ajusta el tamaño del botón
            ) {
                Text(
                    text = "Iniciar Sesión",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold, // Configura el texto en negrita
                        fontSize = 25.sp // Tamaño de fuente para el texto
                    )
                )
            }
        }
    }
}

