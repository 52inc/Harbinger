package com.ftinc.harbinger

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.ftinc.harbinger.util.Chronos
import com.ftinc.harbinger.util.extensions.iso8601
import com.ftinc.harbinger.work.WorkOrder
import java.util.concurrent.atomic.AtomicInteger


class WorkScheduler(val context: Context) {

    private val ids = AtomicInteger(1000000)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    /**
     * Schedule a [WorkOrder] to be run at it's defined day/date
     */
    fun schedule(order: WorkOrder): Int {
        val intent = WorkReceiver.createIntent(context)

        val id = if (order.id == WorkOrder.NO_ID) {
            ids.getAndIncrement()
        } else {
            order.id
        }
        val operationTime = Chronos.next(order.startTimeInMillis, order.day)

        intent.putExtra(WorkOrder.KEY_ID, id)
        intent.putExtra(WorkOrder.KEY_TIME, operationTime.timeInMillis)

        val operation = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (order.exact) {
            Harbinger.logger.d("Scheduling exact weekly alarm for (${operationTime.iso8601()})")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, operationTime.timeInMillis, operation)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, operationTime.timeInMillis, operation)
            }
        } else {
            Harbinger.logger.d("Scheduling periodic weekly alarm for (${operationTime.iso8601()}, with interval ${order.intervalInMillis})")
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, operationTime.timeInMillis, order.intervalInMillis, operation)
        }

        return id
    }

    /**
     * Reschedule a [WorkOrder], only if exact, by it's defined interval
     * @param order the WorkOrder to schedule
     * @param lastScheduledTime the last scheduled time of this work order so to accurately reschedule by it's interval
     */
    fun reschedule(order: WorkOrder, lastScheduledTime: Long) {
        if (order.exact) {
            val intent = WorkReceiver.createIntent(context)
            val operationTime = lastScheduledTime + order.intervalInMillis

            intent.putExtra(WorkOrder.KEY_ID, order.id)
            intent.putExtra(WorkOrder.KEY_TIME, operationTime)

            Harbinger.logger.d("Re-scheduling exact weekly alarm for (${operationTime.iso8601()})")
            val operation = PendingIntent.getBroadcast(context, order.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, operationTime, operation)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, operationTime, operation)
            }
        }
    }

    fun unschedule(request: WorkOrder) {
        val intent = WorkReceiver.createIntent(context)
        val operation = PendingIntent.getBroadcast(context, request.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(operation)
    }
}