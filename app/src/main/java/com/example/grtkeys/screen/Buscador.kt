package com.example.grtkeys.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.TextStyle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SearchRoutesScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var allRoutes by remember { mutableStateOf(emptyList<String>()) }
    var searchResults by remember { mutableStateOf(emptyList<String>()) }

    // Función para traer todas las rutas al iniciar la pantalla
    fun getAllRoutes() {
        val db = FirebaseFirestore.getInstance()
        db.collection("rutas")
            .get()
            .addOnSuccessListener { documents ->
                val routeNames = documents.map { it.getString("nombreRuta") ?: "" }
                allRoutes = routeNames
                searchResults = routeNames // Al principio muestra todas
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error obteniendo documentos: ", exception)
                allRoutes = emptyList() // En caso de error
            }
    }


    // Función para buscar dentro de las palabras localmente
    fun searchRoutes(query: String) {
        searchResults = if (query.isEmpty()) {
            allRoutes // Si no hay búsqueda, muestra todas
        } else {
            allRoutes.filter { it.contains(query, ignoreCase = true) }
        }.take(4) // Limitar a las primeras 4 coincidencias
    }

    // Cargar todas las rutas cuando se inicia la pantalla
    LaunchedEffect(Unit) {
        getAllRoutes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Campo de búsqueda con lupa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de lupa
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Campo de búsqueda
            BasicTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    searchQuery = newValue
                    searchRoutes(newValue.text) // Filtrar mientras se escribe
                },
                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = TextStyle(fontSize = 18.sp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar resultados de búsqueda o mensaje si no hay resultados
        if (searchResults.isNotEmpty()) {
            LazyColumn {
                items(searchResults) { routeName ->
                    Text(
                        text = routeName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        style = TextStyle(fontSize = 16.sp)
                    )
                }
            }
        } else if (searchQuery.text.isNotEmpty()) {
            // Mostrar el mensaje si no hay resultados y se ha ingresado texto en el campo de búsqueda
            Text(
                text = "No hay rutas asignadas",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                style = TextStyle(fontSize = 16.sp, color = Color.Gray)
            )
        }
    }
}

@Preview
@Composable
fun PreviewSearchRoutesScreen() {
    SearchRoutesScreen(navController = NavHostController(LocalContext.current))
}
