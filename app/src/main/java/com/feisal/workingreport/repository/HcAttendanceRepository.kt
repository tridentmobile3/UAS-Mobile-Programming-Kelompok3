package com.feisal.workingreport.repository

import com.feisal.workingreport.model.Attendance
import com.feisal.workingreport.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class HcAttendanceRepository {
    private val firestore by lazy { try { FirebaseFirestore.getInstance() } catch (e: Exception) { null } }

    suspend fun getAllAttendances(): List<Attendance> {
        val db = firestore ?: return emptyList()
        return try {
            db.collection(Constants.ATTENDANCES_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Attendance::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getEmployeeAttendanceHistory(userId: String): List<Attendance> {
        val db = firestore ?: return emptyList()
        return try {
            db.collection(Constants.ATTENDANCES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .toObjects(Attendance::class.java)
                .sortedByDescending { it.createdAt } // Client-side sort to avoid index issues
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
