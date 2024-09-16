package com.example.grtkeys.conductor

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import com.google.android.gms.maps.CameraUpdateFactory
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
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.grtkeys.R


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
    var routeName by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var vehiclePlate by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

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
                .background(Color(0xFF242f3e)) // Color negro
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.weight(1f),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapStyleOptions = MapStyleOptions(darkModeStyle) // Aplicar el estilo oscuro
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
                            color = Color.Green // Configura el color de la línea a verde
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
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
                        modifier = Modifier.wrapContentSize(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF746855) //0xFF746855 Color de fondo para el botón
                        )
                    ) {
                        Text(text = "Crear Rutas", color = Color.White)
                    }

                    if (showSaveButton) {
                        Button(
                            onClick = { showDialog = true },
                            modifier = Modifier.wrapContentSize(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red // Color de fondo rojo para el botón
                            )
                        ) {
                            Text("Guardar Ruta", color = Color.White)
                        }
                    }
                }
            }

            // Imagen pequeña en la esquina superior derecha
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