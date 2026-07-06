package com.feisal.workingreport

import android.app.Application
import android.content.Context

class WorkingReportApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: WorkingReportApp? = null

        fun getContext(): Context {
            return instance!!.applicationContext
        }
    }
}
