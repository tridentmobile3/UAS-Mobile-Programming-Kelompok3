package com.feisal.workingreport.repository

import android.net.Uri
import com.feisal.workingreport.model.PermissionRequest
import com.feisal.workingreport.model.PermissionStatus
import com.feisal.workingreport.model.User
import com.feisal.workingreport.service.StorageService
import com.feisal.workingreport.utils.Constants
import com.feisal.workingreport.utils.DateHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PermissionRepository {
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

    suspend fun submitPermission(
        type: String,
        reason: String,
        proofUri: Uri? = null,
        driveLink: String? = null,
        fileName: String? = null,
        mimeType: String? = null
    ): Result<Unit> = runCatching {
        val uid = currentUserId ?: throw Exception("User not logged in")
        val firebaseFirestore = firestore ?: throw Exception("Firestore not initialized")
        val user = getCurrentUser() ?: throw Exception("User profile not found")
        val today = DateHelper.getCurrentDate()
        var uploadedUrl = ""
        
        if (proofUri != null) {
            val extension = fileName?.substringAfterLast(".", "jpg") ?: "jpg"
            val path = "${Constants.PERMISSION_PROOFS_PATH}/$uid/$today/proof.$extension"
            uploadedUrl = storageService.uploadFile(path, proofUri)
        }

        val permission = PermissionRequest(
            id = firebaseFirestore.collection(Constants.PERMISSIONS_COLLECTION).document().id,
            userId = uid,
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

        firebaseFirestore.collection(Constants.PERMISSIONS_COLLECTION)
            .document(permission.id)
            .set(permission)
            .await()
    }

    suspend fun getMyPermissions(): List<PermissionRequest> {
        val uid = currentUserId ?: return emptyList()
        val firebaseFirestore = firestore ?: return emptyList()
        return try {
            firebaseFirestore.collection(Constants.PERMISSIONS_COLLECTION)
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(PermissionRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllPermissions(): List<PermissionRequest> {
        val firebaseFirestore = firestore ?: return emptyList()
        return try {
            firebaseFirestore.collection(Constants.PERMISSIONS_COLLECTION)
                .get()
                .await()
                .toObjects(PermissionRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPendingPermissions(): List<PermissionRequest> {
        return getAllPermissions().filter { it.status == "PENDING" || it.status == "SUBMITTED" }
    }
}
