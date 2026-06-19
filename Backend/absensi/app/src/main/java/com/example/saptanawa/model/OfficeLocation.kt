package com.example.saptanawa.model

data class OfficeLocation(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeter: Int = 100,
    val active: Boolean = true
)
