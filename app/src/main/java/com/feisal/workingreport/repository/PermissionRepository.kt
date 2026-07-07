package com.feisal.workingreport.repository

import com.feisal.workingreport.model.PermissionRequest
import com.feisal.workingreport.model.PermissionStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PermissionRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getMyPermissions(userId: String): List<PermissionRequest> {
        return try {
            firestore.collection("permissions")
                .whereEqualTo("userId", userId) // Filter berdasarkan ID user yang login
                .get()
                .await()
                .toObjects(PermissionRequest::class.java)
                .sortedByDescending { it.createdAt } // Urutkan dari pengajuan terbaru
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Jika gagal/error, kembalikan list kosong agar aplikasi tidak crash
        }
    }

    suspend fun submitPermission(
        userId: String,
        employeeName: String,
        employeeNip: String,
        type: String,
        reason: String,
        date: String,
        fileNameText: String,
        driveLink: String,
        uniqueId: String? = null // 1. Tambahkan parameter opsional unik ID agar sinkron dengan Activity
    ): Result<Boolean> {
        return try {
            // 2. Jika uniqueId dikirim dari Activity, pakai itu. Jika tidak, buat otomatis berdasarkan tanggal

            val requestId = uniqueId ?: "${userId}_${date.replace("/", "-")}"

            val request = PermissionRequest(
                id = requestId,
                userId = userId,
                employeeName = employeeName,
                employeeNip = employeeNip,
                type = type.uppercase(),
                reason = reason,
                date = date,
                proofUrl = "", // Dikosongkan karena tidak diunggah ke Storage
                driveLink = driveLink,
                fileName = fileNameText, // Menyimpan teks nama file di sini
                mimeType = "text/plain",
                status = PermissionStatus.PENDING.name,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // 3. Simpan ke Firestore menggunakan custom document ID untuk menghindari duplikasi data
            firestore.collection("permissions").document(requestId).set(request).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}