package com.ftinc.harbinger.scheduler

import android.app.PendingIntent


interface Scheduler {

    fun exact(timeInMillis: Long, operation: PendingIntent)
    fun repeating(timeInMillis: Long, intervalInMillis: Long, operation: PendingIntent)
    fun cancel(operation: PendingIntent)
}