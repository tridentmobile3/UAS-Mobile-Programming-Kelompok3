package com.example.saptanawa.model

data class DashboardSummary(
    val totalEmployees: Int = 0,
    val presentToday: Int = 0,
    val lateToday: Int = 0,
    val permissionToday: Int = 0,
    val workingReportsToday: Int = 0,
    val pendingPermissions: Int = 0,
    val pendingOvertimes: Int = 0
)