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
        val user = getCurrentUser() ?: throw Exception("User profile not found")
        val today = DateHelper.getCurrentDate()
        val docId = "${currentUserId}_$today"

        val existing = firestore
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
            val path = "${Constants.WORKING_REPORT_FILES_PATH}/$currentUserId/$today/attachment.$extension"
            attachmentUrl = storageService.uploadFile(path, attachmentUri)
        }

        val report = WorkingReport(
            id = docId,
            userId = currentUserId,
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

        firestore.collection(Constants.WORKING_REPORTS_COLLECTION)
            .document(docId)
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
