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
    private val firestore by lazy { try { FirebaseFirestore.getInstance() } catch (e: Exception) { null } }
    private val auth by lazy { try { FirebaseAuth.getInstance() } catch (e: Exception) { null } }
    private val storageService = StorageService()

    private val currentUserId: String?
        get() = try { 
            val uid = auth?.currentUser?.uid 
            if (uid == null) {
                // Dummy session for offline mode
                "dummy_user_id"
            } else {
                uid
            }
        } catch (e: Exception) { 
            "dummy_user_id" 
        }

    suspend fun getCurrentUser(): User? {
        val uid = currentUserId ?: return null
        if (uid == "dummy_user_id") {
            return User(
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
        val firebaseFirestore = firestore ?: return null
        return try {
            firebaseFirestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getOfficeLocation(locationId: String = "padepokan79_main"): OfficeLocation? {
        val uid = currentUserId
        if (uid == "dummy_user_id") {
            return OfficeLocation(
                id = "padepokan79_main",
                name = "Padepokan 79",
                latitude = -6.9174639,
                longitude = 107.6191228,
                radiusMeter = 1000,
                active = true
            )
        }
        val firebaseFirestore = firestore ?: return null
        return try {
            firebaseFirestore.collection(Constants.OFFICE_LOCATIONS_COLLECTION)
                .document(locationId)
                .get()
                .await()
                .toObject(OfficeLocation::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkIn(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        photoUri: Uri,
        faceVerified: Boolean
    ): Result<Unit> = runCatching {
        val uid = currentUserId ?: throw Exception("User not logged in")
        val firebaseFirestore = firestore ?: throw Exception("Firestore not initialized")
        
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
        val docId = "${uid}_$today"

        if (uid == "dummy_user_id") {
            // Success dummy in offline mode
            return@runCatching
        }

        val existing = firebaseFirestore.collection(Constants.ATTENDANCES_COLLECTION).document(docId).get().await()

        if (existing.exists()) {
            throw Exception("Already checked in today")
        }

        val photoUrl = storageService.uploadFile(
            "${Constants.ATTENDANCE_PHOTOS_PATH}/$uid/$today/check_in.jpg",
            photoUri
        )

        val attendance = Attendance(
            id = docId,
            userId = uid,
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

        firebaseFirestore.collection(Constants.ATTENDANCES_COLLECTION)
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
        val uid = currentUserId ?: throw Exception("User not logged in")
        val firebaseFirestore = firestore ?: throw Exception("Firestore not initialized")
        val today = DateHelper.getCurrentDate()
        val docId = "${uid}_$today"

        if (uid == "dummy_user_id") {
            // Success dummy in offline mode
            return@runCatching
        }

        val docRef = firebaseFirestore.collection(Constants.ATTENDANCES_COLLECTION).document(docId)
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
            "${Constants.ATTENDANCE_PHOTOS_PATH}/$uid/$today/check_out.jpg",
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
        val uid = currentUserId ?: return null
        val firebaseFirestore = firestore ?: return null
        val today = DateHelper.getCurrentDate()
        val docId = "${uid}_$today"
        return try {
            firebaseFirestore.collection(Constants.ATTENDANCES_COLLECTION)
                .document(docId)
                .get()
                .await()
                .toObject(Attendance::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAttendanceHistory() : List<Attendance> {
        val uid = currentUserId ?: return emptyList()
        val firebaseFirestore = firestore ?: return emptyList()
        return try {
            firebaseFirestore.collection(Constants.ATTENDANCES_COLLECTION)
                .whereEqualTo("userId", uid)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Attendance::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
