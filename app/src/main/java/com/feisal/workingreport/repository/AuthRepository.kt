package com.feisal.workingreport.repository

import com.feisal.workingreport.model.User
import com.feisal.workingreport.model.UserStatus
import com.feisal.workingreport.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth by lazy { try { FirebaseAuth.getInstance() } catch (e: Exception) { null } }
    private val firestore by lazy { try { FirebaseFirestore.getInstance() } catch (e: Exception) { null } }

    private fun nipToAuthEmail(nip: String): String {
        return "${nip.trim()}@saptawork.app"
    }

    suspend fun loginWithNip(nip: String, password: String): Result<User> = runCatching {
        val firebaseAuth = auth ?: throw Exception("Firebase Auth not initialized")
        val firebaseFirestore = firestore ?: throw Exception("Firestore not initialized")

        val cleanNip = nip.trim()
        if (cleanNip.isBlank()) throw IllegalArgumentException("NIP wajib diisi")
        if (password.isBlank()) throw IllegalArgumentException("Password wajib diisi")

        val authEmail = nipToAuthEmail(cleanNip)
        val authResult = firebaseAuth.signInWithEmailAndPassword(authEmail, password).await()
        val uid = authResult.user?.uid ?: throw IllegalStateException("Login gagal: UID tidak ditemukan")

        val snapshot = firebaseFirestore.collection(Constants.USERS_COLLECTION).document(uid).get().await()
        val user = snapshot.toObject(User::class.java) ?: throw IllegalStateException("Data user tidak ditemukan di Firestore")

        if (user.nip != cleanNip) {
            firebaseAuth.signOut()
            throw IllegalStateException("NIP tidak cocok dengan akun")
        }

        if (user.status != UserStatus.ACTIVE.name) {
            firebaseAuth.signOut()
            throw IllegalStateException("Akun tidak aktif")
        }

        user.copy(id = uid)
    }

    suspend fun getCurrentUserProfile(): User? {
        return try {
            val firebaseAuth = auth ?: return null
            val firebaseFirestore = firestore ?: return null
            val uid = firebaseAuth.currentUser?.uid ?: return null
            
            firebaseFirestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .await()
                .toObject(User::class.java)
                ?.copy(id = uid)
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserId(): String? {
        return try { auth?.currentUser?.uid } catch (e: Exception) { null }
    }

    fun logout() {
        try { auth?.signOut() } catch (e: Exception) {}
    }
}
