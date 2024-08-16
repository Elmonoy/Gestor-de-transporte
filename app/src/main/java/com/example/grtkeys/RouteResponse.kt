package com.example.grtkeys

import com.google.gson.annotations.SerializedName
data class RouteResponse(
    val features: List<Feature>
)

data class Feature(
    val geometry: Geometry,
    val properties: Properties
)

data class Geometry(
    val coordinates: List<List<Double>>
)

data class Properties(
    val segments: List<Segment>
)

data class Segment(
    val duration: Double // Duración en segundos
)
