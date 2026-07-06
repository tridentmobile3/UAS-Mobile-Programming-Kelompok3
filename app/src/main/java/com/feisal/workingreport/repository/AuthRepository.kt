package com.feisal.workingreport.repository

import android.content.Context
import com.feisal.workingreport.WorkingReportApp
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

    companion object {
        private var dummyUser: User? = null
    }

    suspend fun loginWithNip(nip: String, password: String): Result<User> = runCatching {
        if (nip == "1234" && password == "1234") {
            val user = User(
                id = "dummy_hc_id",
                nip = "1234",
                authEmail = "1234@saptawork.app",
                name = "Human Capital",
                role = "HC",
                department = "Human Capital",
                position = "HC Staff",
                status = "ACTIVE"
            )
            dummyUser = user
            return@runCatching user
        } else if (nip == "12345678" && password == "12345678") {
            val user = User(
                id = "dummy_user_id",
                nip = "12345678",
                authEmail = "12345678@saptawork.app",
                name = "User Dummy",
                role = "KARYAWAN",
                department = "IT",
                position = "Staff",
                status = "ACTIVE"
            )
            dummyUser = user
            return@runCatching user
        }

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
        restoreDummyUserIfPossible()
        if (dummyUser != null) return dummyUser
        
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
        restoreDummyUserIfPossible()
        if (dummyUser != null) return dummyUser?.id
        return try { auth?.currentUser?.uid } catch (e: Exception) { null }
    }

    private fun restoreDummyUserIfPossible() {
        if (dummyUser != null) return
        try {
            val context = WorkingReportApp.getContext()
            val pref = context.getSharedPreferences("AppPref", Context.MODE_PRIVATE)
            val isLoggedIn = pref.getBoolean("isLoggedIn", false)
            if (isLoggedIn) {
                val userId = pref.getString("userId", null)
                val role = pref.getString("userRole", null)
                if (userId != null && userId.startsWith("dummy")) {
                    dummyUser = if (role == "HC") {
                        User(
                            id = "dummy_hc_id",
                            nip = "1234",
                            authEmail = "1234@saptawork.app",
                            name = "Human Capital",
                            role = "HC",
                            department = "Human Capital",
                            position = "HC Staff",
                            status = "ACTIVE"
                        )
                    } else {
                        User(
                            id = "dummy_user_id",
                            nip = "12345678",
                            authEmail = "12345678@saptawork.app",
                            name = "User Dummy",
                            role = "KARYAWAN",
                            department = "IT",
                            position = "Staff",
                            status = "ACTIVE"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun logout() {
        dummyUser = null
        try { auth?.signOut() } catch (e: Exception) {}
    }
}
