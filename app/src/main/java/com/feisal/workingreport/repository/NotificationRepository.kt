package com.feisal.workingreport.repository

import com.feisal.workingreport.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("notifications")

    suspend fun createNotification(userId: String, title: String, message: String, type: String): Result<Unit> = runCatching {
        val id = UUID.randomUUID().toString()
        val notification = Notification(
            id = id,
            userId = userId,
            title = title,
            message = message,
            type = type,
            read = false,
            createdAt = System.currentTimeMillis()
        )
        collection.document(id).set(notification).await()
    }

    suspend fun getMyNotifications(userId: String): List<Notification> {
        return try {
            collection.whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Notification::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback for missing index
            collection.whereEqualTo("userId", userId)
                .get()
                .await()
                .toObjects(Notification::class.java)
                .sortedByDescending { it.createdAt }
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> = runCatching {
        collection.document(notificationId).update("read", true).await()
    }

    suspend fun markAllAsRead(userId: String): Result<Unit> = runCatching {
        val unread = collection.whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .get()
            .await()
        
        if (unread.isEmpty) return@runCatching

        val batch = firestore.batch()
        unread.documents.forEach { doc ->
            batch.update(doc.reference, "read", true)
        }
        batch.commit().await()
    }

    suspend fun getUnreadCount(userId: String): Int {
        return try {
            val snapshot = collection.whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun notifyAllHC(title: String, message: String, type: String) {
        try {
            val hcUsers = firestore.collection("users")
                .whereEqualTo("role", "HC")
                .get()
                .await()
            
            hcUsers.documents.forEach { doc ->
                val hcId = doc.id
                createNotification(hcId, title, message, type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
