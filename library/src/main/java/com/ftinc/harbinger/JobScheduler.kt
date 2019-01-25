package com.ftinc.harbinger

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import java.util.concurrent.atomic.AtomicInteger


class JobScheduler(val context: Context) {

    private val ids = AtomicInteger()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    fun schedule(jobRequest: JobRequest) {
        val intent = JobReceiver.createIntent(context)
        intent.putExtra(JobRequest.KEY_EXTRAS, jobRequest.extras.saveToXml())

        val id = if (jobRequest.id == -1) {
            ids.getAndIncrement()
        } else {
            jobRequest.id
        }

        val operation = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (jobRequest.isPeriodic) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, jobRequest.startTimeInMillis, jobRequest.intervalInMillis!!, operation)
        } else {
            if (jobRequest.exact) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, jobRequest.startTimeInMillis, operation)
            } else {
                alarmManager.setWindow(AlarmManager.RTC_WAKEUP, jobRequest.startTimeInMillis, jobRequest.endTimeInMillis - jobRequest.startTimeInMillis, operation)
            }
        }
    }
}