package com.example.saptanawa.repository

import com.example.saptanawa.model.Attendance
import com.example.saptanawa.model.DashboardSummary
import com.example.saptanawa.model.PermissionRequest
import com.example.saptanawa.model.OvertimeReport
import com.example.saptanawa.model.User
import com.example.saptanawa.model.WorkingReport
import com.example.saptanawa.utils.Constants
import com.example.saptanawa.utils.DateHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DashboardRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getDashboardSummary(): DashboardSummary {

        val today = DateHelper.getCurrentDate()

        val employees = firestore
            .collection(Constants.USERS_COLLECTION)
            .whereEqualTo("role", Constants.ROLE_KARYAWAN)
            .get()
            .await()
            .toObjects(User::class.java)

        val attendances = firestore
            .collection(Constants.ATTENDANCES_COLLECTION)
            .whereEqualTo("date", today)
            .get()
            .await()
            .toObjects(Attendance::class.java)

        val permissions = firestore
            .collection(Constants.PERMISSIONS_COLLECTION)
            .get()
            .await()
            .toObjects(PermissionRequest::class.java)

        val reports = firestore
            .collection(Constants.WORKING_REPORTS_COLLECTION)
            .whereEqualTo("date", today)
            .get()
            .await()
            .toObjects(WorkingReport::class.java)

        val overtimes = firestore
            .collection(Constants.OVERTIMES_COLLECTION)
            .get()
            .await()
            .toObjects(OvertimeReport::class.java)

        return DashboardSummary(
            totalEmployees = employees.size,

            presentToday = attendances.size,

            lateToday = attendances.count {
                it.status == "TERLAMBAT"
            },

            permissionToday = permissions.count {
                it.date == today
            },

            workingReportsToday = reports.size,

            pendingPermissions = permissions.count {
                it.status == "PENDING"
            },

            pendingOvertimes = overtimes.count {
                it.status == "PENDING"
            }
        )
    }
}