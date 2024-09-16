package com.example.grtkeys.screen


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.grtkeys.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun EditConScreen(
    navController: NavController
) {
    var conductorId by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userPlate by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // LaunchedEffect para cargar los datos cuando conductorId cambie
    LaunchedEffect(conductorId) {
        if (conductorId.isNotEmpty()) {
            isLoading = true
            errorMessage = null
            try {
                withContext(Dispatchers.IO) {
                    val docSnapshot = FirebaseFirestore.getInstance().collection("conductores").document(conductorId).get().await()
                    if (docSnapshot.exists()) {
                        userName = docSnapshot.getString("NOMBRECONDUCTOR") ?: ""
                        userPlate = docSnapshot.getString("PLACAVEHICULO") ?: ""
                    } else {
                        errorMessage = "Conductor no encontrado"
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar los datos"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD3D3D3))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Campo para ingresar el ID del conductor
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.persona),
                contentDescription = "Icono ID",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = conductorId,
                onValueChange = { newId ->
                    conductorId = newId
                },
                textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
        }

        // Mostrar mensaje de error
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Indicador de carga
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Campo para editar el nombre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nomusuario),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            // Campo para editar la placa
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.btnconfig),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = userPlate,
                    onValueChange = { userPlate = it },
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            // Botón para guardar cambios
            Button(
                onClick = {
                    if (conductorId.isNotEmpty()) {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                // Guardar los datos en Firestore
                                FirebaseFirestore.getInstance().collection("conductores").document(conductorId)
                                    .update("nombreConductor", userName, "placaVehiculo", userPlate)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        // Mostrar mensaje de éxito o realizar otra acción
                                        navController.popBackStack() // Volver a la pantalla anterior
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Error al guardar los cambios: ${e.message}"
                                        isLoading = false
                                        e.printStackTrace()
                                    }
                            } catch (e: Exception) {
                                errorMessage = "Error al guardar los cambios"
                                e.printStackTrace()
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = "Por favor, ingrese un ID de conductor"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Guardar Cambios", fontSize = 20.sp, color = Color.Black)
            }
        }
    }
}
