package com.example.saptanawa.repository

import com.example.saptanawa.model.Attendance
import com.example.saptanawa.utils.Constants
import com.example.saptanawa.utils.DateHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AnalyticsRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getAttendanceStatistics(): Map<String, Int> {

        val today = DateHelper.getCurrentDate()

        val attendances = firestore
            .collection(Constants.ATTENDANCES_COLLECTION)
            .whereEqualTo("date", today)
            .get()
            .await()
            .toObjects(Attendance::class.java)

        return mapOf(
            "HADIR" to attendances.count {
                it.status == "HADIR"
            },

            "TERLAMBAT" to attendances.count {
                it.status == "TERLAMBAT"
            },

            "IZIN" to attendances.count {
                it.status == "IZIN"
            },

            "SAKIT" to attendances.count {
                it.status == "SAKIT"
            },

            "CUTI" to attendances.count {
                it.status == "CUTI"
            },

            "ALPHA" to attendances.count {
                it.status == "ALPHA"
            }
        )
    }
}