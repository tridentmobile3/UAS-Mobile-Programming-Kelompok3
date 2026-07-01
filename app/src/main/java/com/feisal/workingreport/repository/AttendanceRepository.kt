package com.feisal.workingreport.repository

import android.net.Uri
import com.feisal.workingreport.model.Attendance
import com.feisal.workingreport.model.AttendanceStatus
import com.feisal.workingreport.model.OfficeLocation
import com.feisal.workingreport.model.User
import com.feisal.workingreport.service.StorageService
import com.feisal.workingreport.utils.Constants
import com.feisal.workingreport.utils.DateHelper
import com.feisal.workingreport.utils.LocationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AttendanceRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageService = StorageService()

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    suspend fun getCurrentUser(): User? {
        return firestore.collection(Constants.USERS_COLLECTION)
            .document(currentUserId)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun getOfficeLocation(locationId: String = "padepokan79_main"): OfficeLocation? {
        return firestore.collection(Constants.OFFICE_LOCATIONS_COLLECTION)
            .document(locationId)
            .get()
            .await()
            .toObject(OfficeLocation::class.java)
    }

    suspend fun checkIn(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        photoUri: Uri,
        faceVerified: Boolean
    ): Result<Unit> = runCatching {
        if (!faceVerified) {
            throw Exception("Verifikasi wajah gagal")
        }

        val user = getCurrentUser() ?: throw Exception("User profile not found")
        val office = getOfficeLocation() ?: throw Exception("Office location not found")
        val distance = LocationHelper.calculateDistanceMeter(
            latitude, longitude, office.latitude, office.longitude
        )

        if (!LocationHelper.isInsideRadius(distance, office.radiusMeter)) {
            throw Exception("Outside office radius ($distance meters)")
        }

        val today = DateHelper.getCurrentDate()
        val docId = "${currentUserId}_$today"
        val existing = firestore.collection(Constants.ATTENDANCES_COLLECTION).document(docId).get().await()

        if (existing.exists()) {
            throw Exception("Already checked in today")
        }

        val photoUrl = storageService.uploadFile(
            "${Constants.ATTENDANCE_PHOTOS_PATH}/$currentUserId/$today/check_in.jpg",
            photoUri
        )

        val attendance = Attendance(
            id = docId,
            userId = currentUserId,
            employeeName = user.name,
            employeeNip = user.nip,
            date = today,
            status = AttendanceStatus.HADIR.name,
            checkInTime = DateHelper.getCurrentTime(),
            checkInLatitude = latitude,
            checkInLongitude = longitude,
            checkInAccuracy = accuracy,
            checkInDistance = distance,
            checkInPhotoUrl = photoUrl,
            faceVerified = faceVerified,
            isLocked = true
        )

        firestore.collection(Constants.ATTENDANCES_COLLECTION)
            .document(docId)
            .set(attendance)
            .await()
    }

    suspend fun checkOut(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        photoUri: Uri
    ): Result<Unit> = runCatching {
        val today = DateHelper.getCurrentDate()
        val docId = "${currentUserId}_$today"
        val docRef = firestore.collection(Constants.ATTENDANCES_COLLECTION).document(docId)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            throw Exception("No check-in record found for today")
        }

        val attendance = snapshot.toObject(Attendance::class.java)!!
        if (attendance.checkOutTime.isNotEmpty()) {
            throw Exception("Already checked out today")
        }

        val office = getOfficeLocation() ?: throw Exception("Office location not found")
        val distance = LocationHelper.calculateDistanceMeter(
            latitude, longitude, office.latitude, office.longitude
        )

        if (!LocationHelper.isInsideRadius(distance, office.radiusMeter)) {
            throw Exception("Outside office radius ($distance meters)")
        }

        val photoUrl = storageService.uploadFile(
            "${Constants.ATTENDANCE_PHOTOS_PATH}/$currentUserId/$today/check_out.jpg",
            photoUri
        )

        docRef.update(
            mapOf(
                "checkOutTime" to DateHelper.getCurrentTime(),
                "checkOutLatitude" to latitude,
                "checkOutLongitude" to longitude,
                "checkOutAccuracy" to accuracy,
                "checkOutDistance" to distance,
                "checkOutPhotoUrl" to photoUrl,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun getTodayAttendance(): Attendance? {
        val today = DateHelper.getCurrentDate()
        val docId = "${currentUserId}_$today"
        return firestore.collection(Constants.ATTENDANCES_COLLECTION)
            .document(docId)
            .get()
            .await()
            .toObject(Attendance::class.java)
    }

    suspend fun getAttendanceHistory() : List<Attendance> {
        return firestore.collection(Constants.ATTENDANCES_COLLECTION)
            .whereEqualTo("userId", currentUserId)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Attendance::class.java)
    }
}
