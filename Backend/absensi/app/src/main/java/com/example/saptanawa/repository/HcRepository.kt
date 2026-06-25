package com.example.saptanawa.repository

import com.example.saptanawa.model.Attendance
import com.example.saptanawa.model.PermissionRequest
import com.example.saptanawa.model.User
import com.example.saptanawa.model.WorkingReport
import com.example.saptanawa.model.OvertimeReport
import com.example.saptanawa.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class HcRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // USER MANAGEMENT

    suspend fun getAllEmployees(): List<User> {
        return firestore
            .collection(Constants.USERS_COLLECTION)
            .whereEqualTo("role", Constants.ROLE_KARYAWAN)
            .get()
            .await()
            .toObjects(User::class.java)
    }

    // ATTENDANCE MONITORING


    suspend fun getAllAttendancesByDate(
        date: String
    ): List<Attendance> {

        return firestore
            .collection(Constants.ATTENDANCES_COLLECTION)
            .whereEqualTo("date", date)
            .get()
            .await()
            .toObjects(Attendance::class.java)
    }

    // PERMISSION APPROVAL


    suspend fun getPendingPermissions(): List<PermissionRequest> {

        return firestore
            .collection(Constants.PERMISSIONS_COLLECTION)
            .whereEqualTo("status", "PENDING")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(PermissionRequest::class.java)
    }

    suspend fun reviewPermission(
        permissionId: String,
        status: String,
        reviewerId: String,
        reviewerName: String,
        note: String
    ): Result<Unit> = runCatching {

        firestore
            .collection(Constants.PERMISSIONS_COLLECTION)
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
            )
            .await()
    }


    // WORKING REPORT REVIEW


    suspend fun getAllWorkingReports(): List<WorkingReport> {

        return firestore
            .collection(Constants.WORKING_REPORTS_COLLECTION)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(WorkingReport::class.java)
    }


    // OVERTIME MANAGEMENT


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