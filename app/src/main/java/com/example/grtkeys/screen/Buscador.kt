package com.example.grtkeys.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grtkeys.R // Asegúrate de que la ruta de tu recurso de imagen sea correcta

@Composable
fun SearchScreen() {
    val searchText = remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD3D3D3)) // Fondo gris
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SearchBar(
            searchText = searchText.value,
            onSearchTextChange = { searchText.value = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchText: TextFieldValue,
    onSearchTextChange: (TextFieldValue) -> Unit
) {
    TextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        leadingIcon = {
            Image(
                painter = painterResource(id = R.drawable.lupa), // Imagen de lupa
                contentDescription = "Search Icon",
                modifier = Modifier.size(24.dp)
            )
        },
        placeholder = {
            Text(text = "Buscar...", fontSize = 16.sp, color = Color.Gray)
        },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.White, // Fondo del campo de texto
            cursorColor = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen()
}
