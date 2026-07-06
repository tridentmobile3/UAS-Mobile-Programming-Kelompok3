package com.feisal.workingreport.repository

import com.feisal.workingreport.utils.Constants
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val firestore by lazy { try { FirebaseFirestore.getInstance() } catch (e: Exception) { null } }
    private val auth by lazy { try { FirebaseAuth.getInstance() } catch (e: Exception) { null } }

    suspend fun updateProfileName(newName: String): Result<Unit> = runCatching {
        val firebaseAuth = auth ?: throw Exception("Auth not initialized")
        val firebaseFirestore = firestore ?: throw Exception("Firestore not initialized")
        val uid = firebaseAuth.currentUser?.uid ?: throw Exception("User not logged in")

        firebaseFirestore.collection(Constants.USERS_COLLECTION)
            .document(uid)
            .update("name", newName)
            .await()
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> = runCatching {
        val firebaseAuth = auth ?: throw Exception("Auth not initialized")
        val user = firebaseAuth.currentUser ?: throw Exception("User not logged in")
        val email = user.email ?: throw Exception("Email not found")

        val credential = EmailAuthProvider.getCredential(email, oldPassword)
        
        // Re-authenticate user first
        user.reauthenticate(credential).await()
        
        // If successful, update password
        user.updatePassword(newPassword).await()
    }
}
