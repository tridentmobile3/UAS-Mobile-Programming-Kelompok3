package com.example.saptanawa.model

data class OvertimeReport(
    val id: String = "",
    val userId: String = "",
    val employeeName: String = "",
    val employeeNip: String = "",

    val date: String = "",

    val startTime: String = "",
    val endTime: String = "",

    val totalHours: Double = 0.0,

    val title: String = "",
    val description: String = "",

    val attachmentUrl: String = "",
    val fileName: String = "",
    val mimeType: String = "",

    val status: String = OvertimeStatus.PENDING.name,

    val reviewedBy: String = "",
    val reviewerName: String = "",
    val reviewNote: String = "",

    val reviewedAt: Long = 0L,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class OvertimeStatus {
    PENDING,
    APPROVED,
    REJECTED
}