package com.feisal.workingreport.model

data class Attendance(
    val id: String = "",
    val userId: String = "",
    val employeeName: String = "",
    val employeeNip: String = "",
    val date: String = "",
    val status: String = AttendanceStatus.HADIR.name,

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

    val faceVerified: Boolean = false,
    val isLocked: Boolean = true,
    val source: String = "ANDROID_APP",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class AttendanceStatus {
    HADIR,
    TERLAMBAT,
    IZIN,
    SAKIT,
    CUTI,
    ALPHA
}
