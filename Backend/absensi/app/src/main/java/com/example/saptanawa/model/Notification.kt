package com.example.saptanawa.model

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)