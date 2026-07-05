package com.feisal.workingreport.repository

import com.feisal.workingreport.model.PermissionRequest
import com.feisal.workingreport.model.PermissionStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PermissionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getMyPermissions(userId: String): List<PermissionRequest> {
        return try {
            firestore.collection("permissions")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .toObjects(PermissionRequest::class.java)
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Perbaikan utama: Menggunakan ByteArray untuk menjamin file terupload sempurna
    suspend fun submitPermission(
        userId: String,
        employeeName: String,
        employeeNip: String,
        type: String,
        reason: String,
        date: String,
        fileBytes: ByteArray?, // Diubah dari Uri? ke ByteArray?
        driveLink: String,
        extension: String // Tambahkan parameter ekstensi berkas (misal: "jpg", "pdf")
    ): Result<Boolean> {
        return try {
            val requestId = UUID.randomUUID().toString()
            var finalProofUrl = ""
            var finalFileName = ""

            if (fileBytes != null) {
                finalFileName = "proof_${System.currentTimeMillis()}.$extension"

                val storageRef = storage.reference.child("proofs").child(userId).child(requestId).child(finalFileName)

                // Gunakan putBytes() untuk mengunggah array byte data mentah langsung ke Storage
                storageRef.putBytes(fileBytes).await()

                // Ambil download URL-nya
                finalProofUrl = storageRef.downloadUrl.await().toString()
            }

            val request = PermissionRequest(
                id = requestId,
                userId = userId,
                employeeName = employeeName,
                employeeNip = employeeNip,
                type = type.uppercase(),
                reason = reason,
                date = date,
                proofUrl = finalProofUrl, // Ini akan menyimpan tautan unduhan dari file/foto
                driveLink = driveLink,
                fileName = finalFileName,
                mimeType = if (fileBytes != null) "application/octet-stream" else "",
                status = PermissionStatus.PENDING.name
            )

            firestore.collection("permissions").document(requestId).set(request).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}