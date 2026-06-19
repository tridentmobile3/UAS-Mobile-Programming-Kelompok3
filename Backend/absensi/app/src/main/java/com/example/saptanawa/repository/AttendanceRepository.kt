package com.example.saptanawa.repository

import android.net.Uri
import com.example.saptanawa.model.Attendance
import com.example.saptanawa.model.OfficeLocation
import com.example.saptanawa.model.PermissionRequest
import com.example.saptanawa.service.StorageService
import com.example.saptanawa.utils.Constants
import com.example.saptanawa.utils.DateHelper
import com.example.saptanawa.utils.LocationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AttendanceRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageService = StorageService()

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    private val currentUserName: String
        get() = auth.currentUser?.displayName ?: "Unknown Employee"

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
        photoUri: Uri
    ): Result<Unit> = runCatching {
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
            employeeName = currentUserName,
            date = today,
            status = "HADIR",
            checkInTime = DateHelper.getCurrentTime(),
            checkInLatitude = latitude,
            checkInLongitude = longitude,
            checkInAccuracy = accuracy,
            checkInDistance = distance,
            checkInPhotoUrl = photoUrl
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

    suspend fun submitPermission(
        type: String,
        reason: String,
        proofUri: Uri? = null,
        driveLink: String? = null,
        fileName: String? = null,
        mimeType: String? = null
    ): Result<Unit> = runCatching {
        if (reason.isBlank()) throw Exception("Reason is required")
        if (proofUri == null && driveLink.isNullOrBlank()) {
            throw Exception("Proof file or Google Drive link is required")
        }

        val today = DateHelper.getCurrentDate()
        var uploadedUrl = ""
        
        if (proofUri != null) {
            val extension = fileName?.substringAfterLast(".", "jpg") ?: "jpg"
            val path = "${Constants.PERMISSION_PROOFS_PATH}/$currentUserId/$today/proof.$extension"
            uploadedUrl = storageService.uploadFile(path, proofUri)
        }

        val permission = PermissionRequest(
            id = firestore.collection(Constants.PERMISSIONS_COLLECTION).document().id,
            userId = currentUserId,
            employeeName = currentUserName,
            type = type,
            reason = reason,
            date = today,
            proofUrl = uploadedUrl,
            driveLink = driveLink ?: "",
            fileName = fileName ?: "",
            mimeType = mimeType ?: "",
            status = "PENDING"
        )

        firestore.collection(Constants.PERMISSIONS_COLLECTION)
            .document(permission.id)
            .set(permission)
            .await()
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
