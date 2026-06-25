package com.example.saptanawa.repository

import com.example.saptanawa.model.OfficeLocation
import com.example.saptanawa.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OfficeLocationRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getActiveOfficeLocation(): OfficeLocation? {

        return firestore
            .collection(Constants.OFFICE_LOCATIONS_COLLECTION)
            .whereEqualTo("active", true)
            .get()
            .await()
            .toObjects(OfficeLocation::class.java)
            .firstOrNull()
    }

    suspend fun saveOfficeLocation(
        officeLocation: OfficeLocation
    ): Result<Unit> = runCatching {

        firestore
            .collection(Constants.OFFICE_LOCATIONS_COLLECTION)
            .document(officeLocation.id)
            .set(officeLocation)
            .await()
    }

    suspend fun updateOfficeLocation(
        officeLocation: OfficeLocation
    ): Result<Unit> = runCatching {

        firestore
            .collection(Constants.OFFICE_LOCATIONS_COLLECTION)
            .document(officeLocation.id)
            .set(officeLocation)
            .await()
    }

    suspend fun deactivateOfficeLocation(
        officeId: String
    ): Result<Unit> = runCatching {

        firestore
            .collection(Constants.OFFICE_LOCATIONS_COLLECTION)
            .document(officeId)
            .update(
                mapOf(
                    "active" to false
                )
            )
            .await()
    }

    suspend fun getAllOfficeLocations(): List<OfficeLocation> {

        return firestore
            .collection(Constants.OFFICE_LOCATIONS_COLLECTION)
            .get()
            .await()
            .toObjects(OfficeLocation::class.java)
    }
}