package com.feisal.workingreport.repository

import com.feisal.workingreport.model.User
import com.feisal.workingreport.model.UserStatus
import com.feisal.workingreport.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun nipToAuthEmail(nip: String): String {
        return "${nip.trim()}@saptawork.app"
    }

    suspend fun loginWithNip(nip: String, password: String): Result<User> = runCatching {
        val cleanNip = nip.trim()

        if (cleanNip.isBlank()) {
            throw IllegalArgumentException("NIP wajib diisi")
        }

        if (password.isBlank()) {
            throw IllegalArgumentException("Password wajib diisi")
        }

        val authEmail = nipToAuthEmail(cleanNip)

        val authResult = auth
            .signInWithEmailAndPassword(authEmail, password)
            .await()

        val uid = authResult.user?.uid
            ?: throw IllegalStateException("Login gagal: UID tidak ditemukan")

        val snapshot = firestore
            .collection(Constants.USERS_COLLECTION)
            .document(uid)
            .get()
            .await()

        val user = snapshot.toObject(User::class.java)
            ?: throw IllegalStateException("Data user tidak ditemukan di Firestore")

        if (user.nip != cleanNip) {
            auth.signOut()
            throw IllegalStateException("NIP tidak cocok dengan akun")
        }

        if (user.status != UserStatus.ACTIVE.name) {
            auth.signOut()
            throw IllegalStateException("Akun tidak aktif")
        }

        user.copy(id = uid)
    }

    suspend fun getCurrentUserProfile(): User? {
        val uid = auth.currentUser?.uid ?: return null

        return firestore
            .collection(Constants.USERS_COLLECTION)
            .document(uid)
            .get()
            .await()
            .toObject(User::class.java)
            ?.copy(id = uid)
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun logout() {
        auth.signOut()
    }
}
