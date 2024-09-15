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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SearchRoutesScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var searchResults by remember { mutableStateOf(emptyList<String>()) }

    // Función para buscar rutas en Firestore
    fun searchRoutes(query: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("rutas")
            .whereGreaterThanOrEqualTo("nombreRuta", query)
            .whereLessThanOrEqualTo("nombreRuta", query + '\uf8ff') // Para coincidencias similares
            .get()
            .addOnSuccessListener { documents ->
                val routeNames = documents.map { it.getString("nombreRuta") ?: "" }
                searchResults = routeNames
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                searchResults = emptyList() // En caso de error
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Campo de búsqueda
        BasicTextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = newValue
                searchRoutes(newValue.text) // Buscar mientras se escribe
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
                .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                .padding(8.dp),
            textStyle = TextStyle(fontSize = 18.sp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar resultados de búsqueda
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
    }
}

