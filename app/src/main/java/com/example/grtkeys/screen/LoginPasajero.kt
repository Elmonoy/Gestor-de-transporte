package com.example.grtkeys.screen

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.grtkeys.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPasajero(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    var verificationMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Cerrar sesión automáticamente cuando la pantalla se carga
    LaunchedEffect(Unit) {
        firebaseAuth.signOut() // Cierra la sesión al iniciar la pantalla
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_buslp),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.4f)
                    lineTo(0f, size.height)
                    close()
                }
                canvas.drawPath(path, androidx.compose.ui.graphics.Paint().apply {
                    color = Color.Black
                    alpha = 0.7f
                })
            }
        }

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
                textAlign = TextAlign.Center
            )

            Text(
                text = "CORREO ELECTRÓNICO",
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
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

            Button(
                onClick = {
                    if (email.isNotEmpty()) {
                        firebaseAuth.signInWithEmailAndPassword(email, "passwordTemporal")
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    firebaseAuth.currentUser?.reload()?.addOnCompleteListener { reloadTask ->
                                        if (reloadTask.isSuccessful) {
                                            val currentUser = firebaseAuth.currentUser
                                            if (currentUser != null && !currentUser.isEmailVerified) {
                                                // Enviar el correo de verificación si no está verificado
                                                currentUser.sendEmailVerification()
                                                    .addOnCompleteListener { verificationTask ->
                                                        if (verificationTask.isSuccessful) {
                                                            verificationMessage = "Correo de verificación enviado a $email."
                                                        } else {
                                                            verificationMessage = "Error al enviar el correo: ${verificationTask.exception?.message}"
                                                        }
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(verificationMessage ?: "", duration = SnackbarDuration.Short)
                                                        }
                                                    }
                                            } else if (currentUser != null && currentUser.isEmailVerified) {
                                                verificationMessage = "El correo ya está verificado. Por favor, inicia sesión."
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(verificationMessage ?: "", duration = SnackbarDuration.Short)
                                                }
                                            }
                                        } else {
                                            verificationMessage = "Error al recargar el estado del usuario."
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(verificationMessage ?: "", duration = SnackbarDuration.Short)
                                            }
                                        }
                                    }
                                } else {
                                    verificationMessage = "Error de autenticación: ${authTask.exception?.message}"
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(verificationMessage ?: "", duration = SnackbarDuration.Short)
                                    }
                                }
                            }
                    } else {
                        Toast.makeText(context, "Por favor ingresa un correo electrónico.", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .padding(10.dp)
                    .size(250.dp, 50.dp)
            ) {
                Text(
                    text = "Enviar Código",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                )
            }

            Button(
                onClick = {
                    if (email.isNotEmpty()) {
                        firebaseAuth.signInWithEmailAndPassword(email, "passwordTemporal")
                            .addOnCompleteListener { signInTask ->
                                if (signInTask.isSuccessful) {
                                    firebaseAuth.currentUser?.let { currentUser ->
                                        currentUser.reload().addOnCompleteListener { reloadTask ->
                                            if (reloadTask.isSuccessful) {
                                                if (currentUser.isEmailVerified) {
                                                    // Navegar al mapa solo si el correo ya está verificado
                                                    navController.navigate("LoginPasajeroMap")
                                                } else {
                                                    // Solicitar que verifique el correo si no está verificado
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "Por favor, verifica tu correo antes de continuar.",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Error al recargar el estado del usuario.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                } else {
                                    val exception = signInTask.exception
                                    if (exception is FirebaseAuthInvalidUserException) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "El correo no está registrado. Por favor, regístrate.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Contraseña incorrecta. Inténtalo de nuevo.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Error al iniciar sesión: ${exception?.message}",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            }
                    } else {
                        Toast.makeText(context, "Por favor ingresa un correo electrónico.", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .padding(10.dp)
                    .size(250.dp, 50.dp)
            ) {
                Text(
                    text = "Iniciar Sesión",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                )
            }
        }

        // Snackbar Host para mostrar mensajes flotantes
        SnackbarHost(hostState = snackbarHostState)
    }
}
