package com.feisal.workingreport.utils

import android.location.Location

object LocationHelper {

    fun calculateDistanceMeter(
        userLat: Double,
        userLng: Double,
        officeLat: Double,
        officeLng: Double
    ): Float {
        val result = FloatArray(1)

        Location.distanceBetween(
            userLat,
            userLng,
            officeLat,
            officeLng,
            result
        )

        return result[0]
    }

    fun isInsideRadius(distanceMeter: Float, radiusMeter: Int): Boolean {
        return distanceMeter <= radiusMeter
    }
}
