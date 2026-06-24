package com.example.saptanawa.repository

import android.net.Uri
import com.example.saptanawa.model.PermissionRequest
import com.example.saptanawa.model.PermissionStatus
import com.example.saptanawa.model.User
import com.example.saptanawa.service.StorageService
import com.example.saptanawa.utils.Constants
import com.example.saptanawa.utils.DateHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PermissionRepository {
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

    suspend fun submitPermission(
        type: String,
        reason: String,
        proofUri: Uri? = null,
        driveLink: String? = null,
        fileName: String? = null,
        mimeType: String? = null
    ): Result<Unit> = runCatching {
        if (reason.isBlank()) throw Exception("Reason is required")
        if (proofUri == null && driveLink.isNullOrBlank()) {
            throw Exception("Proof file or Google Drive link is required")
        }

        val user = getCurrentUser() ?: throw Exception("User profile not found")
        val today = DateHelper.getCurrentDate()
        var uploadedUrl = ""
        
        if (proofUri != null) {
            val extension = fileName?.substringAfterLast(".", "jpg") ?: "jpg"
            val path = "${Constants.PERMISSION_PROOFS_PATH}/$currentUserId/$today/proof.$extension"
            uploadedUrl = storageService.uploadFile(path, proofUri)
        }

        val permission = PermissionRequest(
            id = firestore.collection(Constants.PERMISSIONS_COLLECTION).document().id,
            userId = currentUserId,
            employeeName = user.name,
            employeeNip = user.nip,
            type = type,
            reason = reason,
            date = today,
            proofUrl = uploadedUrl,
            driveLink = driveLink ?: "",
            fileName = fileName ?: "",
            mimeType = mimeType ?: "",
            status = PermissionStatus.PENDING.name
        )

        firestore.collection(Constants.PERMISSIONS_COLLECTION)
            .document(permission.id)
            .set(permission)
            .await()
    }

    suspend fun getMyPermissions(): List<PermissionRequest> {
        return firestore.collection(Constants.PERMISSIONS_COLLECTION)
            .whereEqualTo("userId", currentUserId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(PermissionRequest::class.java)
    }
}
