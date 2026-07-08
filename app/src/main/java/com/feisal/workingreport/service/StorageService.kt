package com.feisal.workingreport.service

import android.net.Uri

/**
 * StorageService has been disabled to remove Firebase Storage dependency.
 * It now returns local URIs instead of uploading to Cloud Storage.
 */
class StorageService {
    suspend fun uploadFile(
        path: String,
        uri: Uri,
        contentType: String? = null
    ): String {
        // Return local URI as string instead of uploading to Firebase Storage
        return uri.toString()
    }
}
