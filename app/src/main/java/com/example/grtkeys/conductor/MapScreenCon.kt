package com.example.grtkeys.conductor
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.grtkeys.ApiService
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment

import androidx.navigation.NavController
import com.example.grtkeys.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds


@SuppressLint("StaticFieldLeak")
private val db = FirebaseFirestore.getInstance()

suspend fun getNextRouteId(): String {
    val counterDocRef = db.collection("metadata").document("rutasCounter")
    val counterDoc = counterDocRef.get().await()

    val currentCount = counterDoc.getLong("currentCount") ?: 0L
    val nextCount = currentCount + 1

    // Actualiza el contador en la base de datos
    counterDocRef.set(mapOf("currentCount" to nextCount)).await()

    return "ruta$nextCount"
}

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
fun MapScreenCon(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val firestore = FirebaseFirestore.getInstance()

    var start by remember { mutableStateOf<LatLng?>(null) }
    var end by remember { mutableStateOf<LatLng?>(null) }
    var polylinePoints by remember { mutableStateOf(emptyList<LatLng>()) }
    var durationText by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<LatLng?>(null) }
    var showSaveButton by remember { mutableStateOf(false) }
    var showFinishButton by remember { mutableStateOf(false) } // Nueva variable para mostrar el botón "Finalizar Ruta"
    var routeName by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var vehiclePlate by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    var showRoutesDialog by remember { mutableStateOf(false) }
    var routesList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var routeToEdit by remember { mutableStateOf<Map<String, Any>?>(null) }
    var routeOriginalName by remember { mutableStateOf("") }

    val locationPermissionGranted = remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted.value = isGranted
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                loc?.let {
                    location = LatLng(it.latitude, it.longitude)
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
                }
            }
        } else {
            launcher.launch(permission)
        }
    }

    val medellinLatLng = LatLng(6.2442, -75.5812)
    val initialCameraPosition = CameraPosition.fromLatLngZoom(medellinLatLng, 12f)
    val cameraPositionState = rememberCameraPositionState {
        position = initialCameraPosition
    }

    if (locationPermissionGranted.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF242f3e))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.weight(1f),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapStyleOptions = MapStyleOptions(darkModeStyle)
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = true
                    ),
                    onMapClick = { latLng ->
                        if (start == null) {
                            start = latLng
                            Toast.makeText(context, "Selecciona el punto final", Toast.LENGTH_SHORT).show()
                        } else if (end == null) {
                            end = latLng
                            createRoute(start!!, end!!) { route, duration ->
                                polylinePoints = route
                                durationText = duration
                                showSaveButton = true
                            }
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
                        Polyline(
                            points = polylinePoints,
                            color = Color.Green
                        )
                    }
                }

                // Botones para crear ruta y ver lista de rutas
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            start = null
                            end = null
                            polylinePoints = emptyList()
                            durationText = ""
                            showSaveButton = false
                            Toast.makeText(context, "Selecciona punto de origen y final", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF746855)
                        )
                    ) {
                        Text(text = "Crear Rutas", color = Color.White)
                    }

                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                routesList = fetchRoutesFromFirestore(firestore)
                                withContext(Dispatchers.Main) {
                                    showRoutesDialog = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF746855)
                        )
                    ) {
                        Text(text = "Lista de Rutas", color = Color.White)
                    }

                    if (showSaveButton) {
                        Button(
                            onClick = { showDialog = true },
                            modifier = Modifier.weight(1f).padding(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF746855)
                            )
                        ) {
                            Text("Guardar Ruta", color = Color.White)
                        }
                    }
                }
                // Dialogo para guardar la ruta
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(text = "Guardar Ruta") },
                        text = {
                            Column {
                                TextField(
                                    value = routeName,
                                    onValueChange = { routeName = it },
                                    label = { Text("Nombre de la Ruta") }
                                )
                                TextField(
                                    value = driverName,
                                    onValueChange = { driverName = it },
                                    label = { Text("Nombre del Conductor") }
                                )
                                TextField(
                                    value = vehiclePlate,
                                    onValueChange = { vehiclePlate = it },
                                    label = { Text("Placa del Vehículo") }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        // Guardar la ruta en Firestore
                                        saveRouteToFirestore(
                                            firestore,
                                            routeName,
                                            driverName,
                                            vehiclePlate,
                                            start,
                                            end
                                        )
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Ruta guardada exitosamente 🚗💨", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    showDialog = false
                                }
                            ) {
                                Text("Guardar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }


                // Mostrar el botón "Finalizar Ruta" solo cuando se está recorriendo una ruta
                if (showFinishButton) {
                    Button(
                        onClick = {
                            start = null
                            end = null
                            polylinePoints = emptyList()
                            durationText = ""
                            showSaveButton = false
                            showFinishButton = false // Ocultar el botón "Finalizar Ruta" después de finalizar
                            Toast.makeText(context, "Ruta finalizada", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Finalizar Ruta", color = Color.White)
                    }
                }

                // Diálogo de rutas guardadas
                if (showRoutesDialog) {
                    AlertDialog(
                        onDismissRequest = { showRoutesDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showRoutesDialog = false }) {
                                Text("Cerrar")
                            }
                        },
                        title = { Text("Rutas Guardadas") },
                        text = {
                            // Envolver la lista de rutas en un LazyColumn para permitir el desplazamiento
                            LazyColumn(
                                modifier = Modifier.fillMaxHeight(0.6f) // Controla la altura máxima del diálogo
                            ) {
                                items(routesList) { route ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp) // Espaciado entre rutas
                                    ) {
                                        Text(route["nombreRuta"] as String, modifier = Modifier.padding(bottom = 4.dp)) // Nombre de la ruta

                                        // Fila para botones "Recorrer" y "Eliminar"
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween // Asegura que los botones estén separados
                                        ) {
                                            Button(
                                                onClick = {
                                                    val origen = route["origen"] as String
                                                    val destino = route["destino"] as String
                                                    val origenCoords = origen.split(", ").map { it.toDouble() }
                                                    val destinoCoords = destino.split(", ").map { it.toDouble() }
                                                    start = LatLng(origenCoords[0], origenCoords[1])
                                                    end = LatLng(destinoCoords[0], destinoCoords[1])

                                                    // Llamada para crear la ruta
                                                    createRoute(start!!, end!!) { route, duration ->
                                                        polylinePoints = route
                                                        durationText = duration
                                                        showSaveButton = true
                                                        showFinishButton = true // Mostrar el botón "Finalizar Ruta"

                                                        // Actualizar la posición de la cámara (centrar y hacer zoom)
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            val bounds = LatLngBounds.Builder()
                                                            bounds.include(start!!)  // Añadir el punto inicial
                                                            bounds.include(end!!)    // Añadir el punto final

                                                            // Puedes incluir los puntos intermedios si quieres ajustar a toda la ruta
                                                            route.forEach { point ->
                                                                bounds.include(point)
                                                            }

                                                            cameraPositionState.animate(
                                                                update = CameraUpdateFactory.newLatLngBounds(bounds.build(), 100),
                                                                durationMs = 1000  // Duración del zoom animado
                                                            )
                                                        }
                                                    }
                                                    showRoutesDialog = false
                                                },
                                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242f3e))
                                            ) {
                                                Text("Recorrer", color = Color.White)
                                            }

                                            Button(
                                                onClick = {
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        deleteRouteFromFirestore(firestore, route["nombreRuta"] as String)
                                                        routesList = fetchRoutesFromFirestore(firestore) // Actualizar la lista de rutas
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).padding(start = 8.dp), // Espacio entre los botones
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF746855))
                                            ) {
                                                Text("Eliminar", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }


            }
        }
    } else {
        Text("Por favor, permite el acceso a la ubicación.")
    }

    // Botón de configuración en la esquina superior derecha
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Mapa o el contenido principal aquí

        // Botón de configuración en la esquina superior derecha
        IconButton(
            onClick = {
                navController.navigate("settingsScreen")
            },
            modifier = Modifier
                .align(Alignment.TopStart) // Alinea el botón en la esquina superior derecha
                .padding(16.dp) // Padding opcional para separarlo un poco de los bordes
        ) {
            Icon(
                painter = painterResource(id = R.drawable.btnconfig),
                contentDescription = "Configuración",
                tint = Color.Gray
            )
        }
    }

}

// Función para eliminar una ruta de Firestore
private suspend fun deleteRouteFromFirestore(
    firestore: FirebaseFirestore,
    routeName: String
) {
    try {
        firestore.collection("rutas").document(routeName).delete().await()
        Log.d("Firebase", "Ruta eliminada exitosamente")
    } catch (e: Exception) {
        Log.e("Firebase", "Error al eliminar la ruta: ${e.message}", e)
    }
}


// Función para obtener las rutas guardadas en Firebase
suspend fun fetchRoutesFromFirestore(firestore: FirebaseFirestore): List<Map<String, Any>> {
    return try {
        val routesSnapshot = firestore.collection("rutas").get().await()
        routesSnapshot.documents.mapNotNull { document ->
            document.data
        }
    } catch (e: Exception) {
        Log.e("Firebase", "Error al obtener las rutas: ${e.message}", e)
        emptyList()
    }
}


private suspend fun saveRouteToFirestore(
    firestore: FirebaseFirestore,
    routeName: String,
    driverName: String,
    vehiclePlate: String,
    start: LatLng?,
    end: LatLng?
) {
    val routesCollection = firestore.collection("rutas")

    try {
        // Crear los datos de la ruta
        val routeData = hashMapOf(
            "nombreRuta" to routeName,
            "nombreConductor" to driverName,
            "placaVehiculo" to vehiclePlate,
            "origen" to "${start?.latitude}, ${start?.longitude}",
            "destino" to "${end?.latitude}, ${end?.longitude}"
        )

        // Guardar la ruta en Firestore usando el nombre de la ruta como ID
        routesCollection.document(routeName)
            .set(routeData)
            .addOnSuccessListener {
                Log.d("Firebase", "Ruta guardada exitosamente")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al guardar la ruta", e)
            }
    } catch (e: Exception) {
        Log.e("Firebase", "Error al guardar la ruta: ${e.message}", e)
    }
}

private fun createRoute(
    start: LatLng,
    end: LatLng,
    onRouteReady: (List<LatLng>, String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val call = getRetrofit().create(ApiService::class.java)
                .getRoute(
                    "5b3ce3597851110001cf6248abe81b05d8314a5ba17589eb9f295695",
                    "${start.longitude},${start.latitude}",
                    "${end.longitude},${end.latitude}"
                )

            if (call.isSuccessful) {
                val body = call.body()
                val polylineOptions = body?.features?.firstOrNull()?.geometry?.coordinates?.map {
                    // Asegúrate de invertir latitud y longitud correctamente
                    LatLng(it[1], it[0])
                } ?: emptyList()

                // Obtener la duración del primer segmento de la ruta
                val duration = body?.features?.firstOrNull()?.properties?.segments?.firstOrNull()?.duration ?: 0.0
                val durationText = formatDuration(duration)

                // Pasar la ruta y la duración formateada
                onRouteReady(polylineOptions, durationText)
            } else {
                Log.e("MapScreen", "Error en la solicitud de la ruta: ${call.code()}")
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "Error al obtener la ruta: ${e.message}", e)
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