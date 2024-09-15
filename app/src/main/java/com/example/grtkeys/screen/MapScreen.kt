package com.example.grtkeys

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.navigation.NavHostController
import androidx.compose.ui.res.painterResource
import com.google.android.gms.maps.model.MapStyleOptions


private val darkModeStyle = """
    [
  {
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "featureType": "administrative.locality",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#263c3f"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#6b9a76"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#38414e"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#212a37"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#9ca5b3"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#1f2835"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#f3d19c"
      }
    ]
  },
  {
    "featureType": "transit",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#2f3948"
      }
    ]
  },
  {
    "featureType": "transit.station",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#515c6d"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  }
]
"""

@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    var start by remember { mutableStateOf<LatLng?>(null) }
    var end by remember { mutableStateOf<LatLng?>(null) }
    var polylinePoints by remember { mutableStateOf(emptyList<LatLng>()) } // Polilínea para la ruta calculada
    var searchedPolylinePoints by remember { mutableStateOf(emptyList<LatLng>()) } // Polilínea para la ruta buscada
    var durationText by remember { mutableStateOf("") } // Tiempo estimado para la ruta calculada
    var searchedDurationText by remember { mutableStateOf("") } // Tiempo estimado para la ruta buscada
    var location by remember { mutableStateOf<LatLng?>(null) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var searchResults by remember { mutableStateOf(emptyList<SearchResult>()) }
    var selectedRoute by remember { mutableStateOf<SearchResult?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(6.2442, -75.5812), 12f)
    }

    val locationPermissionGranted = remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted.value = isGranted
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                loc?.let {
                    location = LatLng(it.latitude, it.longitude)
                    start = location
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted.value = true
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                loc?.let {
                    location = LatLng(it.latitude, it.longitude)
                    start = location
                }
            }
        } else {
            launcher.launch(permission)
        }
    }

    // Función para buscar rutas en Firestore
    fun searchRoutes(query: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("rutas")
            .whereGreaterThanOrEqualTo("nombreRuta", query)
            .whereLessThanOrEqualTo("nombreRuta", query + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                val results = documents.map { doc ->
                    val origenStr = doc.getString("origen") ?: ""
                    val destinoStr = doc.getString("destino") ?: ""

                    val (origenLat, origenLng) = origenStr.split(", ").map { it.toDouble() }
                    val (destinoLat, destinoLng) = destinoStr.split(", ").map { it.toDouble() }

                    SearchResult(
                        name = doc.getString("nombreRuta") ?: "",
                        startLatLng = LatLng(origenLat, origenLng),
                        endLatLng = LatLng(destinoLat, destinoLng),
                        id = doc.id // Asignar ID del documento
                    )
                }
                searchResults = results
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                searchResults = emptyList()
            }
    }

    fun showRouteOnMap(route: SearchResult) {
        CoroutineScope(Dispatchers.IO).launch {
            createRoute(route.startLatLng, route.endLatLng) { polyline, duration ->
                // Actualiza la UI en el hilo principal
                CoroutineScope(Dispatchers.Main).launch {
                    searchedPolylinePoints = polyline // Asignar polilínea de búsqueda
                    searchedDurationText = duration // Asignar tiempo estimado de la ruta buscada
                    Log.d("MapScreen", "Searched polyline points: $searchedPolylinePoints")
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(route.startLatLng, 15f))
                    // Vaciar los resultados de búsqueda para que solo se muestre la ruta seleccionada
                    searchResults = emptyList()
                }
            }
        }
    }

    fun calculateRoute(start: LatLng, end: LatLng) {
        CoroutineScope(Dispatchers.IO).launch {
            createRoute(start, end) { polyline, duration ->
                // Actualiza la UI en el hilo principal
                CoroutineScope(Dispatchers.Main).launch {
                    polylinePoints = polyline // Asignar polilínea calculada
                    durationText = duration // Asignar tiempo estimado de la ruta calculada
                    Log.d("MapScreen", "Calculated polyline points: $polylinePoints")
                }
            }
        }
    }

    if (locationPermissionGranted.value) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra superior con buscador y botón de configuración
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Campo de búsqueda
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { newValue ->
                        searchQuery = newValue
                        searchRoutes(newValue.text) // Buscar mientras se escribe
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                        .padding(8.dp),
                    textStyle = TextStyle(fontSize = 18.sp)
                )

                // Botón de configuración
                IconButton(
                    onClick = {
                        navController.navigate("settingsScreen")
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.btnconfig), // Asegúrate de tener esta imagen en res/drawable
                        contentDescription = "Configuración",
                        tint = Color.Gray
                    )
                }
            }

            // Mostrar resultados de búsqueda
            if (searchResults.isNotEmpty()) {
                LazyColumn {
                    items(searchResults) { result ->
                        Text(
                            text = result.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    selectedRoute = result
                                    showRouteOnMap(result)
                                },
                            style = TextStyle(fontSize = 16.sp)
                        )
                    }
                }
            }

            // GoogleMap y configuración
            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapStyleOptions = MapStyleOptions(darkModeStyle) // Aplica el estilo oscuro
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = true
                    ),
                    onMapClick = { latLng ->
                        if (end == null) {
                            end = latLng
                            calculateRoute(start!!, end!!)
                        }
                    }
                ) {
                    start?.let {
                        Marker(state = MarkerState(position = it), title = "Punto de Origen")
                    }
                    end?.let {
                        Marker(state = MarkerState(position = it), title = "Punto de Destino")
                    }
                    if (polylinePoints.isNotEmpty()) {
                        Polyline(points = polylinePoints, color = Color.Blue)
                    }
                    if (searchedPolylinePoints.isNotEmpty()) {
                        Polyline(points = searchedPolylinePoints, color = Color.Red)
                    }
                    selectedRoute?.let { route ->
                        Marker(state = MarkerState(position = route.startLatLng), title = "Inicio: ${route.name}", snippet = "Destino: ${route.endLatLng}")
                        Marker(state = MarkerState(position = route.endLatLng), title = "Fin: ${route.name}")
                    }
                }
            }

            // Botón para calcular la ruta
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        end = null
                        polylinePoints = emptyList() // Limpiar solo la polilínea calculada
                        durationText = "" // Limpiar tiempo de ruta calculada
                        Toast.makeText(context, "Selecciona el punto de destino", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .wrapContentSize()
                ) {
                    Text(text = "Calcular Ruta")
                }

                // Mostrar el tiempo estimado de la ruta calculada
                if (durationText.isNotEmpty()) {
                    Text(
                        text = "Duración estimada de la ruta calculada: $durationText",
                        modifier = Modifier.padding(bottom = 4.dp),
                        style = TextStyle(fontSize = 16.sp, color = Color.Blue)
                    )
                }

                // Mostrar el tiempo estimado de la ruta buscada
                if (searchedDurationText.isNotEmpty()) {
                    Text(
                        text = "Duración estimada de la ruta buscada: $searchedDurationText",
                        modifier = Modifier.padding(bottom = 4.dp),
                        style = TextStyle(fontSize = 16.sp, color = Color.Red)
                    )
                }
            }
        }
    } else {
        // Mensaje para solicitar permisos
        Text(text = "Permiso de ubicación necesario")
    }
}


private fun createRoute(start: LatLng, end: LatLng, onRouteReady: (List<LatLng>, String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val call = getRetrofit().create(ApiService::class.java)
            .getRoute(
                "5b3ce3597851110001cf6248abe81b05d8314a5ba17589eb9f295695",
                "${start.longitude},${start.latitude}",
                "${end.longitude},${end.latitude}"
            )
        if (call.isSuccessful) {
            val body = call.body()
            val polylineOptions = body?.features?.first()?.geometry?.coordinates?.map {
                LatLng(it[1], it[0])
            } ?: emptyList()

            val duration = body?.features?.first()?.properties?.segments?.first()?.duration ?: 0.0
            val durationText = formatDuration(duration)

            onRouteReady(polylineOptions, durationText)
        } else {
            Log.i("MapScreen", "Error al obtener la ruta: ${call.errorBody()?.string()}")
        }
    }
}

private fun formatDuration(duration: Double): String {
    val minutes = (duration / 60).toInt()
    val seconds = (duration % 60).toInt()
    return "${minutes}m ${seconds}s"
}

private fun getRetrofit(): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://api.openrouteservice.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

data class SearchResult(
    val name: String,
    val startLatLng: LatLng,
    val endLatLng: LatLng,
    val id: String // Agregar ID del documento
)