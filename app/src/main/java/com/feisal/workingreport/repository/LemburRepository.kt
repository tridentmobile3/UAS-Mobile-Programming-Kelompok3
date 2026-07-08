package com.feisal.workingreport.repository

import com.feisal.workingreport.model.Lembur
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LemburRepository {
    private val db = FirebaseFirestore.getInstance()
    private val lemburCollection = db.collection("lembur")

    // 1. Menyimpan data pengajuan lembur baru (DIPANGGIL DARI LEMBURACTIVITY)
    suspend fun submitLembur(lembur: Lembur): Result<Unit> {
        return try {
            lemburCollection.add(lembur).await()
            
            // Notify HC
            NotificationRepository().notifyAllHC(
                title = "Pengajuan Lembur",
                message = "${lembur.namaKaryawan} mengajukan lembur.",
                type = "OVERTIME_PENDING"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 2. Mengambil riwayat milik user tertentu (DIPANGGIL DARI LEMBURACTIVITY)
    suspend fun getMyLemburHistory(userId: String): List<Lembur> {
        return try {
            val snapshot = lemburCollection.whereEqualTo("userId", userId).get().await()
            snapshot.documents.mapNotNull { doc ->
                val lembur = doc.toObject(Lembur::class.java)
                lembur?.apply { id = doc.id }
            }.sortedByDescending { it.tanggal } // Urutkan dari tanggal terbaru
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 3. Mengambil semua data pengajuan lembur (DIPANGGIL DARI APPROVALLEMBURACTIVITY)
    suspend fun getAllLembur(): List<Lembur> {
        return try {
            val snapshot = lemburCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                val lembur = doc.toObject(Lembur::class.java)
                lembur?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 4. Mengupdate status lembur APPROVED/REJECTED (DIPANGGIL DARI APPROVALLEMBURACTIVITY)
    suspend fun updateStatusLembur(id: String, newStatus: String): Result<Unit> {
        return try {
            val snapshot = lemburCollection.document(id).get().await()
            val userId = snapshot.getString("userId") ?: ""

            lemburCollection.document(id).update("status", newStatus).await()
            
            if (userId.isNotEmpty()) {
                val title = if (newStatus == "APPROVED") "Lembur Disetujui" else "Lembur Ditolak"
                val message = if (newStatus == "APPROVED") "Pengajuan lembur Anda disetujui." else "Pengajuan lembur Anda ditolak."
                val type = if (newStatus == "APPROVED") "OVERTIME_APPROVED" else "OVERTIME_REJECTED"
                
                NotificationRepository().createNotification(userId, title, message, type)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
