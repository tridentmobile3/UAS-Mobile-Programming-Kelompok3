package com.example.saptanawa.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateHelper {
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val TIME_FORMAT = "HH:mm:ss"

    fun getCurrentDate(): String {
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
    }

    fun getCurrentTime(): String {
        return SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).format(Date())
    }
}
