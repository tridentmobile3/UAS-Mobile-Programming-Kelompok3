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
        val cleanNip = nip.trim()
        return if (cleanNip.contains("@")) {
            cleanNip
        } else {
            "$cleanNip@saptawork.app"
        }
    }

    suspend fun loginWithNip(nip: String, password: String): Result<User> {
        return try {
            val firebaseAuth = auth ?: throw Exception("Server sedang bermasalah.")
            val firebaseFirestore = firestore ?: throw Exception("Server sedang bermasalah.")

            val cleanNip = nip.trim()
            if (cleanNip.isBlank()) throw Exception("NIP wajib diisi.")
            if (password.isBlank()) throw Exception("Password wajib diisi.")

            // CEK DULU APAKAH NIP ADA
            val userQuery = firebaseFirestore
                .collection(Constants.USERS_COLLECTION)
                .whereEqualTo("nip", cleanNip)
                .limit(1)
                .get()
                .await()

            if (userQuery.isEmpty) {
                throw Exception("Akun tidak ditemukan.")
            }

            val authEmail = nipToAuthEmail(cleanNip)

            // LOGIN FIREBASE
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(authEmail, password)
                .await()

            val uid = authResult.user?.uid ?: throw Exception("Login gagal.")

            val snapshot = firebaseFirestore
                .collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            val user = snapshot.toObject(User::class.java)
                ?: throw Exception("Data akun tidak ditemukan.")

            if (user.status != UserStatus.ACTIVE.name) {
                firebaseAuth.signOut()
                throw Exception("Akun tidak aktif.")
            }

            Result.success(user.copy(id = uid))

        } catch (e: Exception) {
            val message = when {
                e.message == "Akun tidak ditemukan." -> "Akun tidak ditemukan."
                e.message == "NIP wajib diisi." -> "NIP wajib diisi."
                e.message == "Password wajib diisi." -> "Password wajib diisi."
                e.message == "Akun tidak aktif." -> "Akun tidak aktif."
                e.message?.contains("INVALID_LOGIN_CREDENTIALS", true) == true -> "NIP atau Password salah."
                e.message?.contains("password", true) == true -> "NIP atau Password salah."
                e.message?.contains("credential", true) == true -> "NIP atau Password salah."
                e.message?.contains("network", true) == true -> "Periksa koneksi internet."
                else -> "Login gagal. Silakan coba lagi."
            }
            Result.failure(Exception(message))
        }
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
