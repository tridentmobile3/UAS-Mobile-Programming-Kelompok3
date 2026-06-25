package com.example.saptanawa.repository

import com.example.saptanawa.model.OvertimeReport
import com.example.saptanawa.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OvertimeRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun submitOvertime(
        overtime: OvertimeReport
    ): Result<Unit> = runCatching {

        firestore
            .collection(Constants.OVERTIMES_COLLECTION)
            .document(overtime.id)
            .set(overtime)
            .await()
    }

    suspend fun getMyOvertimes(
        userId: String
    ): List<OvertimeReport> {

        return firestore
            .collection(Constants.OVERTIMES_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(OvertimeReport::class.java)
    }

    suspend fun getPendingOvertimes(): List<OvertimeReport> {

        return firestore
            .collection(Constants.OVERTIMES_COLLECTION)
            .whereEqualTo("status", "PENDING")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(OvertimeReport::class.java)
    }

    suspend fun approveOvertime(
        overtimeId: String,
        reviewerId: String,
        reviewerName: String,
        note: String
    ): Result<Unit> = runCatching {

        firestore
            .collection(Constants.OVERTIMES_COLLECTION)
            .document(overtimeId)
            .update(
                mapOf(
                    "status" to "APPROVED",
                    "reviewedBy" to reviewerId,
                    "reviewerName" to reviewerName,
                    "reviewNote" to note,
                    "reviewedAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    suspend fun rejectOvertime(
        overtimeId: String,
        reviewerId: String,
        reviewerName: String,
        note: String
    ): Result<Unit> = runCatching {

        firestore
            .collection(Constants.OVERTIMES_COLLECTION)
            .document(overtimeId)
            .update(
                mapOf(
                    "status" to "REJECTED",
                    "reviewedBy" to reviewerId,
                    "reviewerName" to reviewerName,
                    "reviewNote" to note,
                    "reviewedAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }
}