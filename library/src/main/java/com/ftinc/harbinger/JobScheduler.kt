package com.ftinc.harbinger

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import java.util.concurrent.atomic.AtomicInteger


class JobScheduler(val context: Context) {

    private val ids = AtomicInteger()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    fun schedule(jobRequest: JobRequest): Int {
        val intent = JobReceiver.createIntent(context)
        intent.putExtra(JobRequest.KEY_EXTRAS, jobRequest.extras.saveToXml())
        intent.putExtra(JobRequest.KEY_TAG, jobRequest.tag)

        val id = if (jobRequest.id == -1) {
            ids.getAndIncrement()
        } else {
            jobRequest.id
        }

        val operation = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (jobRequest.isPeriodic) {
            Harbinger.jobLogger.d("Setting periodic alarm for (${jobRequest.startTimeInMillis}, with interval ${jobRequest.intervalInMillis})")
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, jobRequest.startTimeInMillis, jobRequest.intervalInMillis!!, operation)
        } else {
            if (jobRequest.exact) {
                Harbinger.jobLogger.d("Setting exact alarm for (${jobRequest.startTimeInMillis}")
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, jobRequest.startTimeInMillis, operation)
            } else {
                Harbinger.jobLogger.d("Setting inexact alarm for (${jobRequest.startTimeInMillis} to ${jobRequest.endTimeInMillis})")
                alarmManager.setWindow(AlarmManager.RTC_WAKEUP, jobRequest.startTimeInMillis, jobRequest.endTimeInMillis - jobRequest.startTimeInMillis, operation)
            }
        }

        return id
    }

    fun unschedule(request: JobRequest) {
        val intent = JobReceiver.createIntent(context)
        val operation = PendingIntent.getBroadcast(context, request.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(operation)
    }
}