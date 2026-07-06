package com.feisal.workingreport.service

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await

class StorageService {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadFile(
        path: String,
        uri: Uri,
        contentType: String? = null
    ): String {
        val ref = storage.reference.child(path)

        val metadata = StorageMetadata.Builder()
            .setContentType(contentType ?: guessContentType(path))
            .build()

        val uploadTask = ref.putFile(uri, metadata).await()

        return uploadTask.storage.downloadUrl.await().toString()
    }

    private fun guessContentType(path: String): String {
        val lower = path.lowercase()
        return when {
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".pdf") -> "application/pdf"
            lower.endsWith(".doc") -> "application/msword"
            lower.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "application/octet-stream"
        }
    }
}
