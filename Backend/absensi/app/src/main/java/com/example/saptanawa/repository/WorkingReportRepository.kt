package com.example.saptanawa.repository

import android.net.Uri
import com.example.saptanawa.model.User
import com.example.saptanawa.model.WorkingReport
import com.example.saptanawa.model.WorkingReportStatus
import com.example.saptanawa.service.StorageService
import com.example.saptanawa.utils.Constants
import com.example.saptanawa.utils.DateHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorkingReportRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageService = StorageService()

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    suspend fun getCurrentUser(): User? {
        return firestore.collection(Constants.USERS_COLLECTION)
            .document(currentUserId)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun submitReport(
        title: String,
        description: String,
        progress: String,
        obstacle: String,
        nextPlan: String,
        attachmentUri: Uri? = null,
        fileName: String? = null,
        mimeType: String? = null
    ): Result<Unit> = runCatching {
        val user = getCurrentUser() ?: throw Exception("User profile not found")
        val today = DateHelper.getCurrentDate()
        var attachmentUrl = ""

        if (attachmentUri != null) {
            val extension = fileName?.substringAfterLast(".", "file") ?: "file"
            val path = "${Constants.WORKING_REPORT_FILES_PATH}/$currentUserId/$today/attachment.$extension"
            attachmentUrl = storageService.uploadFile(path, attachmentUri)
        }

        val report = WorkingReport(
            id = firestore.collection(Constants.WORKING_REPORTS_COLLECTION).document().id,
            userId = currentUserId,
            employeeName = user.name,
            employeeNip = user.nip,
            date = today,
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

        firestore.collection(Constants.WORKING_REPORTS_COLLECTION)
            .document(report.id)
            .set(report)
            .await()
    }

    suspend fun getMyReports(): List<WorkingReport> {
        return firestore.collection(Constants.WORKING_REPORTS_COLLECTION)
            .whereEqualTo("userId", currentUserId)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(WorkingReport::class.java)
    }
}
