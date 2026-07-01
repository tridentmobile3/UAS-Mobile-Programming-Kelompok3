package com.feisal.workingreport.repository

import com.feisal.workingreport.model.Attendance
import com.feisal.workingreport.model.PermissionRequest
import com.feisal.workingreport.model.User
import com.feisal.workingreport.model.WorkingReport
import com.feisal.workingreport.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class HcRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // --- User Management ---
    suspend fun getAllEmployees(): List<User> {
        return firestore.collection(Constants.USERS_COLLECTION)
            .whereEqualTo("role", Constants.ROLE_KARYAWAN)
            .get()
            .await()
            .toObjects(User::class.java)
    }

    // --- Attendance Monitoring ---
    suspend fun getAllAttendancesByDate(date: String): List<Attendance> {
        return firestore.collection(Constants.ATTENDANCES_COLLECTION)
            .whereEqualTo("date", date)
            .get()
            .await()
            .toObjects(Attendance::class.java)
    }

    // --- Permission Approval ---
    suspend fun getPendingPermissions(): List<PermissionRequest> {
        return firestore.collection(Constants.PERMISSIONS_COLLECTION)
            .whereEqualTo("status", "PENDING")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(PermissionRequest::class.java)
    }

    suspend fun reviewPermission(
        permissionId: String,
        status: String, // APPROVED / REJECTED
        reviewerId: String,
        reviewerName: String,
        note: String
    ): Result<Unit> = runCatching {
        firestore.collection(Constants.PERMISSIONS_COLLECTION)
            .document(permissionId)
            .update(
                mapOf(
                    "status" to status,
                    "reviewedBy" to reviewerId,
                    "reviewerName" to reviewerName,
                    "reviewNote" to note,
                    "reviewedAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
    }

    // --- Working Report Review ---
    suspend fun getAllWorkingReports(): List<WorkingReport> {
        return firestore.collection(Constants.WORKING_REPORTS_COLLECTION)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(WorkingReport::class.java)
    }
}
