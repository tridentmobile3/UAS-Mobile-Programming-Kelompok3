package com.example.saptanawa.model

data class Attendance(
    val id: String = "",
    val userId: String = "",
    val employeeName: String = "",
    val date: String = "",
    val status: String = "HADIR",

    val checkInTime: String = "",
    val checkInLatitude: Double = 0.0,
    val checkInLongitude: Double = 0.0,
    val checkInAccuracy: Float = 0f,
    val checkInDistance: Float = 0f,
    val checkInPhotoUrl: String = "",

    val checkOutTime: String = "",
    val checkOutLatitude: Double = 0.0,
    val checkOutLongitude: Double = 0.0,
    val checkOutAccuracy: Float = 0f,
    val checkOutDistance: Float = 0f,
    val checkOutPhotoUrl: String = "",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
