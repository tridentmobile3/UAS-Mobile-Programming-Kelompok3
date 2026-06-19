package com.example.saptanawa.model

data class PermissionRequest(
    val id: String = "",
    val userId: String = "",
    val employeeName: String = "",
    val type: String = "", // IZIN / SAKIT / CUTI
    val reason: String = "",
    val date: String = "",
    val proofUrl: String = "",
    val driveLink: String = "",
    val fileName: String = "",
    val mimeType: String = "",
    val status: String = "PENDING", // PENDING / APPROVED / REJECTED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
