package com.ftinc.harbinger.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build


class AlarmManagerScheduler(context: Context) : Scheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun exact(timeInMillis: Long, operation: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, operation)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, operation)
        }
    }

    override fun cancel(operation: PendingIntent) {
        alarmManager.cancel(operation)
    }
}
