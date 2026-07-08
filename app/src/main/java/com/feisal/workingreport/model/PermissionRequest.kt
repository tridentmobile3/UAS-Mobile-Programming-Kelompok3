package com.feisal.workingreport.model

data class PermissionRequest(
    val id: String = "",
    val userId: String = "",
    val employeeName: String = "",
    val employeeNip: String = "",
    val type: String = PermissionType.IZIN.name,
    val reason: String = "",
    val date: String = "",
    val proofUrl: String = "",
    val driveLink: String = "",
    val fileName: String = "",
    val mimeType: String = "",
    val status: String = PermissionStatus.PENDING.name,
    val reviewedBy: String = "",
    val reviewerName: String = "",
    val reviewNote: String = "",
    val reviewedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class PermissionType { IZIN, SAKIT, CUTI }
enum class PermissionStatus { PENDING, APPROVED, REJECTED }
