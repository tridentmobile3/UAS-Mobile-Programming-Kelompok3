package com.feisal.workingreport.repository

import android.net.Uri
import com.feisal.workingreport.model.User
import com.feisal.workingreport.model.WorkingReport
import com.feisal.workingreport.model.WorkingReportStatus
import com.feisal.workingreport.service.StorageService
import com.feisal.workingreport.utils.Constants
import com.feisal.workingreport.utils.DateHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorkingReportRepository {
    private val firestore by lazy { try { FirebaseFirestore.getInstance() } catch (e: Exception) { null } }
    private val auth by lazy { try { FirebaseAuth.getInstance() } catch (e: Exception) { null } }
    private val storageService = StorageService()

    private val currentUserId: String?
        get() = try { auth?.currentUser?.uid } catch (e: Exception) { null }

    suspend fun getCurrentUser(): User? {
        val uid = currentUserId ?: return null
        val firebaseFirestore = firestore ?: return null
        return try {
            firebaseFirestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun submitReport(
        startTime: String,
        endTime: String,
        workLocation: String,
        title: String,
        description: String,
        progress: String,
        obstacle: String,
        nextPlan: String,
        attachmentUri: Uri? = null,
        fileName: String? = null,
        mimeType: String? = null
    ): Result<Unit> = runCatching {
        val uid = currentUserId ?: throw Exception("User not logged in")
        val firebaseFirestore = firestore ?: throw Exception("Firestore not initialized")
        val user = getCurrentUser() ?: throw Exception("User profile not found")
        val today = DateHelper.getCurrentDate()
        val docId = "${uid}_$today"

        val existing = firebaseFirestore
            .collection(Constants.WORKING_REPORTS_COLLECTION)
            .document(docId)
            .get()
            .await()

        if (existing.exists()) {
            throw Exception("Working report hari ini sudah dikirim")
        }

        var attachmentUrl = ""
        if (attachmentUri != null) {
            val extension = fileName?.substringAfterLast(".", "file") ?: "file"
            val path = "${Constants.WORKING_REPORT_FILES_PATH}/$uid/$today/attachment.$extension"
            attachmentUrl = storageService.uploadFile(path, attachmentUri)
        }

        val report = WorkingReport(
            id = docId,
            userId = uid,
            employeeName = user.name,
            employeeNip = user.nip,
            date = today,
            startTime = startTime,
            endTime = endTime,
            workLocation = workLocation,
            title = title,
            description = description,
            progress = progress,
            obstacle = obstacle,
            nextPlan = nextPlan,
            attachmentUrl = attachmentUrl,
            fileName = fileName ?: "",
            mimeType = mimeType ?: "",
            status = WorkingReportStatus.SUBMITTED.name,
            isLocked = true
        )

        firebaseFirestore.collection(Constants.WORKING_REPORTS_COLLECTION)
            .document(docId)
            .set(report)
            .await()
    }

    suspend fun getMyReports(): List<WorkingReport> {
        val uid = currentUserId ?: return emptyList()
        val firebaseFirestore = firestore ?: return emptyList()
        return try {
            firebaseFirestore.collection(Constants.WORKING_REPORTS_COLLECTION)
                .whereEqualTo("userId", uid)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(WorkingReport::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllReports(): List<WorkingReport> {
        val firebaseFirestore = firestore ?: return emptyList()
        return try {
            firebaseFirestore.collection(Constants.WORKING_REPORTS_COLLECTION)
                .get()
                .await()
                .toObjects(WorkingReport::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPendingReports(): List<WorkingReport> {
        return getAllReports().filter { it.status == "PENDING" || it.status == "SUBMITTED" }
    }
}
