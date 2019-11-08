package com.ftinc.harbinger

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.SparseArray
import com.ftinc.harbinger.scheduler.AlarmManagerScheduler
import com.ftinc.harbinger.scheduler.Scheduler
import com.ftinc.harbinger.util.Khronos
import com.ftinc.harbinger.util.extensions.getDayOfWeekExtra
import com.ftinc.harbinger.util.extensions.getOffsetDateTimeExtra
import com.ftinc.harbinger.util.extensions.isoTime
import com.ftinc.harbinger.work.WorkOrder

class WorkScheduler(
    val context: Context,
    var scheduler: Scheduler = AlarmManagerScheduler(context)
) {

    private val registeredIds = SparseArray<WorkOrder>()

    /**
     * Schedule a [WorkOrder] to be run at it's defined day/date
     */
    fun schedule(order: WorkOrder): Int {
        if (order.daysOfWeek.isNotEmpty()) {

            // Now schedule an alarm for each day of the week that this order has been scheduled for
            order.daysOfWeek.forEach { dayOfWeek ->
                // Preparing the pending intent operation and find the next date that this order should be scheduled by
                val nextDateTime = Khronos.next(order.startTime, dayOfWeek)

                // Create intent and pending intent ID
                val pendingIntentId = order.id + dayOfWeek.ordinal
                val intent = createIntent(context) {
                    putExtra(WorkOrder.KEY_ID, order.id)
                    putExtra(WorkOrder.KEY_DAY, dayOfWeek.value)
                    putExtra(WorkOrder.KEY_TIME, nextDateTime.isoTime())
                }

                // Check against already registered work orders to ensure we don't end up with a collision
                val existingWorkOrder = registeredIds.get(pendingIntentId)
                check(!(existingWorkOrder != null && existingWorkOrder.id != order.id)) {
                    "Trying to schedule a work order for Id(${order.id}) where $existingWorkOrder has already be scheduled for"
                }

                val operation = PendingIntent.getBroadcast(context, pendingIntentId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                scheduler.exact(nextDateTime.toInstant().toEpochMilli(), operation)

                // Register this work order so we can ensure that no duplicates get scheduled
                registeredIds.put(pendingIntentId, order)
            }
        } else {
            val nextDateTime = Khronos.next(order.startTime, order.endTime, order.interval)
            if (nextDateTime != null) {
                val intent = createIntent(context) {
                    putExtra(WorkOrder.KEY_ID, order.id)
                    putExtra(WorkOrder.KEY_TIME, nextDateTime.isoTime())
                }

                val existingWorkOrder = registeredIds.get(order.id)
                check(!(existingWorkOrder != null && existingWorkOrder.id != order.id)) {
                    "Trying to schedule a work order for Id(${order.id}) where $existingWorkOrder has already be scheduled for"
                }

                val operation = PendingIntent.getBroadcast(context, order.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                scheduler.exact(nextDateTime.toInstant().toEpochMilli(), operation)

                // Register this work order so we can ensure that no duplicates get scheduled
                registeredIds.put(order.id, order)
            } else {
                return WorkOrder.DEAD_ID
            }
        }

        return order.id
    }

    /**
     * Reschedule a [WorkOrder]
     *
     * @param order the WorkOrder to schedule
     * @param previousIntent the intent data from the previous alert that is triggering this re-schedule
     */
    fun reschedule(order: WorkOrder, previousIntent: Intent): Int {
        if (order.daysOfWeek.isNotEmpty() && order.interval != null) {
            val scheduledDay = previousIntent.getDayOfWeekExtra(WorkOrder.KEY_DAY)
            val previousScheduledDateTime = previousIntent.getOffsetDateTimeExtra(WorkOrder.KEY_TIME)

            // Create our intent to schedule
            val intent = createIntent(context) {
                putExtra(WorkOrder.KEY_ID, order.id)
                if (scheduledDay != null) {
                    putExtra(WorkOrder.KEY_DAY, scheduledDay.value)
                }
            }

            // Now we need to calculate the next available date
            val nextDateTime = if (scheduledDay != null) {
                if (previousScheduledDateTime != null) {
                    // We need to make sure we schedule appropriately to the work order's defined interval
                    // We know that the work order's defined interval is a multple of 1 week (i.e. 7 days)
                    // So we can have Khronos create the next date and verify minimum time
                    Khronos.next(order.startTime, scheduledDay, previousScheduledDateTime, order.interval)
                } else {
                    Khronos.next(order.startTime, scheduledDay)
                }
            } else {
                Khronos.next(order.startTime, order.endTime, order.interval)
            }

            // Schedule if our next date is valid
            if (nextDateTime != null) {
                Harbinger.logger.d(
                    "Re-scheduling exact weekly alarm for (${nextDateTime.isoTime()})"
                )

                // Be sure that we store the scheduled time in the intent
                intent.putExtra(WorkOrder.KEY_TIME, nextDateTime.isoTime())

                val operation = PendingIntent.getBroadcast(
                    context,
                    order.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                scheduler.exact(nextDateTime.toInstant().toEpochMilli(), operation)

                return order.id
            } else {
                return WorkOrder.DEAD_ID
            }
        } else {
            return WorkOrder.DEAD_ID
        }
    }

    fun unschedule(request: WorkOrder) {
        val intent = WorkReceiver.createIntent(context)
        val operation = PendingIntent.getBroadcast(context, request.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        scheduler.cancel(operation)
    }

    private fun createIntent(context: Context, extras: Intent.() -> Unit): Intent {
        return WorkReceiver.createIntent(context).apply {
            extras()
        }
    }

    companion object {
        const val IDS_OFFSET = 1000000
    }
}
