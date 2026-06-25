package com.example.saptanawa.repository

import com.example.saptanawa.model.Notification
import com.example.saptanawa.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun createNotification(
        notification: Notification
    ): Result<Unit> = runCatching {

        firestore
            .collection(Constants.NOTIFICATIONS_COLLECTION)
            .document(notification.id)
            .set(notification)
            .await()
    }

    suspend fun getMyNotifications(
        userId: String
    ): List<Notification> {

        return firestore
            .collection(Constants.NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Notification::class.java)
    }

    suspend fun markAsRead(
        notificationId: String
    ): Result<Unit> = runCatching {

        firestore
            .collection(Constants.NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .update(
                mapOf(
                    "isRead" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    suspend fun getUnreadCount(
        userId: String
    ): Int {

        return firestore
            .collection(Constants.NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .await()
            .size()
    }
}