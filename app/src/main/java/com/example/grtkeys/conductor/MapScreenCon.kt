package com.example.grtkeys.conductor

import com.example.grtkeys.ApiService



import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
@Composable
fun MapScreenCon() {
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    var start by remember { mutableStateOf<LatLng?>(null) }
    var end by remember { mutableStateOf<LatLng?>(null) }
    var polylinePoints by remember { mutableStateOf(emptyList<LatLng>()) }
    var durationText by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<LatLng?>(null) }

    val locationPermissionGranted = remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted.value = isGranted
        if (isGranted) {
            // Obtener ubicación actual al conceder permisos
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
            // Obtener ubicación actual si ya se tienen permisos
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                loc?.let {
                    location = LatLng(it.latitude, it.longitude)
                }
            }
        } else {
            launcher.launch(permission)
        }
    }

    // Coordenadas de Medellín
    val medellinLatLng = LatLng(6.2442, -75.5812)
    val initialCameraPosition = CameraPosition.fromLatLngZoom(medellinLatLng, 12f)

    val cameraPositionState = rememberCameraPositionState {
        position = initialCameraPosition
    }

    if (locationPermissionGranted.value) {
        Column(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.weight(1f),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true, // Habilitar mi ubicación
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true // Mostrar botón de mi ubicación
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
                    Polyline(points = polylinePoints)

                }
            }

            Button(
                onClick = {
                    start = null
                    end = null
                    polylinePoints = emptyList()
                    durationText = ""
                    Toast.makeText(context, "Selecciona punto de origen y final", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentSize()
            ) {
                Text(text = "Crear Rutas")
            }

            // Mostrar el tiempo estimado de viaje
            if (durationText.isNotEmpty()) {
                Text(
                    text = "Tiempo estimado: $durationText",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        LaunchedEffect(location) {
            location?.let {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }
        }
    } else {
        // Mostrar un mensaje si no se conceden permisos
        Toast.makeText(context, "Permisos de ubicación no concedidos", Toast.LENGTH_SHORT).show()
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
            Log.i("MapScreen", "Error al obtener la ruta")
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
