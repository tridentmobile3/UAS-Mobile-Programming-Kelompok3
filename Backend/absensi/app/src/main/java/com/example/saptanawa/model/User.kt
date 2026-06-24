package com.example.saptanawa.model

data class User(
    val id: String = "",
    val nip: String = "",
    val authEmail: String = "",
    val name: String = "",
    val role: String = Role.KARYAWAN.name,
    val department: String = "",
    val position: String = "",
    val photoProfileUrl: String = "",
    val fcmToken: String = "",
    val status: String = UserStatus.ACTIVE.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class Role {
    HC,
    KARYAWAN
}

enum class UserStatus {
    ACTIVE,
    INACTIVE
}
