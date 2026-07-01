package com.feisal.workingreport.service

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageService {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadFile(path: String, uri: Uri): String {
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
