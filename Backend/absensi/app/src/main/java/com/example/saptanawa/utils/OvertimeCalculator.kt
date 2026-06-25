package com.example.saptanawa.utils

import java.text.SimpleDateFormat
import java.util.Locale

object OvertimeCalculator {

    private const val TIME_FORMAT = "HH:mm"

    fun calculateHours(
        startTime: String,
        endTime: String
    ): Double {

        return try {

            val sdf = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())

            val start = sdf.parse(startTime)
            val end = sdf.parse(endTime)

            if (start == null || end == null) {
                0.0
            } else {

                val diffMillis = end.time - start.time

                diffMillis / (1000.0 * 60 * 60)
            }

        } catch (e: Exception) {
            0.0
        }
    }
}