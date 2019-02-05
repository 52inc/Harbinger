package com.ftinc.harbinger

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.ftinc.harbinger.scheduler.AlarmManagerScheduler
import com.ftinc.harbinger.scheduler.Scheduler
import com.ftinc.harbinger.util.Chronos
import com.ftinc.harbinger.util.extensions.iso8601
import com.ftinc.harbinger.work.WorkOrder
import java.util.concurrent.atomic.AtomicInteger


class WorkScheduler(
    val context: Context,
    var scheduler: Scheduler = AlarmManagerScheduler(context)
) {

    private val ids = AtomicInteger(IDS_OFFSET)


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

        val operationTime = (if (order.day != null) {
            // Weekly Recurring timer
            Chronos.next(order.startTimeInMillis, order.day)
        } else {
            Chronos.next(order.startTimeInMillis, order.endTimeInMillis, order.intervalInMillis)
        }) ?: return WorkOrder.DEAD_ID

        intent.putExtra(WorkOrder.KEY_ID, id)
        intent.putExtra(WorkOrder.KEY_TIME, operationTime.timeInMillis)

        val operation = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (order.exact) {
            Harbinger.logger.d("Scheduling exact weekly alarm for (${operationTime.iso8601()})")
            scheduler.exact(operationTime.timeInMillis, operation)
        } else if (order.intervalInMillis != null) {
            Harbinger.logger.d("Scheduling periodic weekly alarm for (${operationTime.iso8601()}, with interval ${order.intervalInMillis})")
            scheduler.repeating(operationTime.timeInMillis, order.intervalInMillis, operation)
        } else {
            // We were unable to schedule this work order, return dead.
            return WorkOrder.DEAD_ID
        }

        return id
    }

    /**
     * Reschedule a [WorkOrder], only if exact, by it's defined interval
     * @param order the WorkOrder to schedule
     * @param lastScheduledTime the last scheduled time of this work order so to accurately reschedule by it's interval
     */
    fun reschedule(order: WorkOrder, lastScheduledTime: Long) {
        if (order.exact && order.day != null && order.intervalInMillis != null) {
            val intent = WorkReceiver.createIntent(context)
            val operationTime = lastScheduledTime + order.intervalInMillis

            intent.putExtra(WorkOrder.KEY_ID, order.id)
            intent.putExtra(WorkOrder.KEY_TIME, operationTime)

            Harbinger.logger.d("Re-scheduling exact weekly alarm for (${operationTime.iso8601()})")
            val operation = PendingIntent.getBroadcast(context, order.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            scheduler.exact(operationTime, operation)
        } else if (order.exact && order.day == null) {
            // This isn't a weekly repeating timer, it's a date-set variable-repeating timer w/ possible end date
            schedule(order)
        }
    }

    fun unschedule(request: WorkOrder) {
        val intent = WorkReceiver.createIntent(context)
        val operation = PendingIntent.getBroadcast(context, request.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        scheduler.cancel(operation)
    }

    companion object {
        const val IDS_OFFSET = 1000000
    }
}