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
            lemburCollection.document(id).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}