package com.example.grtkeys.screen



import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@SuppressLint("StaticFieldLeak")
private val db = FirebaseFirestore.getInstance()

// Función suspendida para agregar datos al documento
suspend fun addUser(user: Map<String, Any>, conductorId: String) {
    try {
        // Guarda los datos junto con el conductorId en Firestore
        db.collection("conductores").document(conductorId).set(user).await()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Función suspendida para obtener el siguiente conductorId autoincremental
suspend fun getNextConductorId(): String {
    val counterDocRef = db.collection("metadata").document("conductorCounter")
    val counterDoc = counterDocRef.get().await()

    val currentCount = counterDoc.getLong("currentCount") ?: 0L
    val nextCount = currentCount + 1

    // Actualiza el contador en la base de datos
    counterDocRef.set(mapOf("currentCount" to nextCount)).await()

    return "conductor$nextCount"
}

@Composable
fun AddUserView() {
    var identificacion by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") } // Añadir el campo para la placa del vehículo
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = identificacion,
            onValueChange = { identificacion = it },
            label = { Text("IDENTIFICACION") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contraseña,
            onValueChange = { contraseña = it },
            label = { Text("CONTRASEÑA") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo adicional para la placa del vehículo
        OutlinedTextField(
            value = placa,
            onValueChange = { placa = it },
            label = { Text("PLACA DEL VEHÍCULO") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    // Obtener el conductorIdhdhsaodjoifjohfodhfjhfj
                    val conductorId = getNextConductorId()

                    // Crear el mapa de datos del usuario con el conductorId y la placa incluidos
                    val userMap = mapOf(
                        "IDENTIFICACION" to identificacion,
                        "CONTRASEÑA" to contraseña,
                        "PLACAVEHICULO" to placa, // Agregar la placa al mapa de datos
                        "conductorId" to conductorId
                    )

                    // Agregar el usuario con el conductorId
                    addUser(userMap, conductorId)

                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add User")
        }

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewAddUserView() {
    AddUserView()
}