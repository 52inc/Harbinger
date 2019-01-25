package com.ftinc.harbinger

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import java.lang.IllegalStateException

/**
 * > a person or thing that announces or signals the approach of another.
 *
 * Schedule alarms and notifications through this single source to keep track and ensure that no id's get
 * contaminated or duplicated
 */
@SuppressLint("StaticFieldLeak")
object Harbinger {

    var initialized = false
    private lateinit var applicationContext: Context
    private lateinit var alarmManager: AlarmManager
    private lateinit var powerManager: PowerManager


    fun create(context: Context): Harbinger {
        if (!initialized) {
            applicationContext = context

            // Do stuff
            powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            initialized = true
        }
        return this
    }


    fun isIgnoringBatteryOptimizations(): Boolean {
        checkInitialization("You must initialize Harbinger first via `create()`")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(applicationContext.packageName)
        } else {
            true
        }
    }


    private fun checkInitialization(errorMessage: String) {
        if (!initialized) throw IllegalStateException(errorMessage)
    }
}