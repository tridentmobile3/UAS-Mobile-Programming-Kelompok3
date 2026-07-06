package com.feisal.workingreport.model

data class WorkingReport(
    val id: String = "",
    val userId: String = "",
    val employeeName: String = "",
    val employeeNip: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val workLocation: String = "",
    val title: String = "",
    val description: String = "",
    val progress: String = "",
    val obstacle: String = "",
    val nextPlan: String = "",
    val attachmentUrl: String = "",
    val fileName: String = "",
    val mimeType: String = "",
    val status: String = WorkingReportStatus.SUBMITTED.name,
    val revisionNote: String = "",
    val isLocked: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class WorkingReportStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REVISION
}
