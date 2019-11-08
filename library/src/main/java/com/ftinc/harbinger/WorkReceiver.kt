package com.ftinc.harbinger

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.ftinc.harbinger.util.CoroutineBroadcastReceiver
import com.ftinc.harbinger.util.extensions.getDayOfWeekExtra
import com.ftinc.harbinger.util.extensions.getOffsetDateTimeExtra
import com.ftinc.harbinger.work.EventType
import com.ftinc.harbinger.work.OrderEvent
import com.ftinc.harbinger.work.WorkOrder
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter


/**
 * The receiver that is called due to scheduled [WorkOrder]s. It creates the defined [com.ftinc.harbinger.work.Worker]
 * and starts it as defined by the [WorkOrder] tag.
 */
class WorkReceiver : CoroutineBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val deliveredTime = OffsetDateTime.now(ZoneOffset.UTC)
        val scheduledDay = intent.getDayOfWeekExtra(WorkOrder.KEY_DAY)
        val scheduledTime = intent.getOffsetDateTimeExtra(WorkOrder.KEY_TIME)
            ?: OffsetDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

        val id = intent.getIntExtra(WorkOrder.KEY_ID, -1)
        if (id != WorkOrder.NO_ID) {

            // Aquire a partial wake lock to ensure our job completes
            val wakeLockTag = "${context.packageName}.WORK_WAKE_LOCK"
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag).apply {
                acquire(2 * 60 * 1000L /* 2 minutes */)
            }

            // Get the worker
            val job = launch {
                val workOrder = Harbinger.getWorkOrder(id)
                if (workOrder != null) {
                    Harbinger.logger.i("Work Order Received ($workOrder)")

                    // Get job creator for tag
                    val creator = Harbinger.workCreators[workOrder.tag]
                    if (creator != null) {
                        val worker = creator.createWorker()
                        worker.setContext(context)

                        // Start the worker and add it's resulting job to the list to track
                        worker.doWork(workOrder.extras)

                        // If work order is 'exact' reschedule order
                        Harbinger.reschedule(workOrder, intent)

                        // Insert recorded event
                        Harbinger.insertEvent(OrderEvent(
                            id,
                            scheduledTime,
                            deliveredTime,
                            scheduledDay,
                            EventType.SUCCESS
                        ))
                    } else {
                        Harbinger.insertEvent(OrderEvent(
                            id,
                            scheduledTime,
                            deliveredTime,
                            scheduledDay,
                            EventType.NO_CREATOR,
                            "There is no creator registered for Tag(${workOrder.tag})"
                        ))
                    }
                } else {
                    Harbinger.logger.e("Unable to find WorkOrder for id ($id)")
                    Harbinger.insertEvent(OrderEvent(
                        id,
                        scheduledTime,
                        deliveredTime,
                        scheduledDay,
                        EventType.MISSING_WORK_ORDER,
                        "Unable to find `WorkOrder` for the given ID"
                    ))
                }
            }

            job.invokeOnCompletion {
                Harbinger.logger.d("Job completed, releasing wakelock")
                wakeLock?.release()
            }
        } else {
            Harbinger.logger.e("Cannot execute WorkOrder for id($id)")
            Harbinger.insertEvent(OrderEvent(
                id,
                scheduledTime,
                deliveredTime,
                scheduledDay,
                EventType.INVALID_ID,
                "The id provided was invalid, unable to execute"
            ))
        }
    }

    companion object {

        /**
         * Create intent to call this receiver
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, WorkReceiver::class.java)
        }
    }
}
