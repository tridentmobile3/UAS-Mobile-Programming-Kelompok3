package com.example.saptanawa.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "KARYAWAN", // KARYAWAN, HC, DIREKSI, ADMIN
    val department: String = "",
    val position: String = ""
)
