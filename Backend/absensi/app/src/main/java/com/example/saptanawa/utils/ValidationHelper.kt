package com.example.saptanawa.utils

object ValidationHelper {

    fun isValidNip(nip: String): Boolean {
        return nip.isNotBlank() && nip.length >= 5
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidWorkingReport(
        title: String,
        description: String
    ): Boolean {

        return title.isNotBlank()
                && description.isNotBlank()
    }

    fun isValidPermission(
        reason: String
    ): Boolean {

        return reason.isNotBlank()
    }

    fun isValidOvertime(
        title: String,
        description: String
    ): Boolean {

        return title.isNotBlank()
                && description.isNotBlank()
    }
}