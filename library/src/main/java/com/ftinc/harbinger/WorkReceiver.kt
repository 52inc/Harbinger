package com.ftinc.harbinger

import android.content.Context
import android.content.Intent
import com.ftinc.harbinger.util.CoroutineBroadcastReceiver
import com.ftinc.harbinger.work.WorkOrder
import kotlinx.coroutines.launch


/**
 * The receiver that is called due to scheduled [WorkOrder]s. It creates the defined [com.ftinc.harbinger.work.Worker]
 * and starts it as defined by the [WorkOrder] tag.
 */
class WorkReceiver : CoroutineBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(WorkOrder.KEY_ID, -1)
        val time = intent.getLongExtra(WorkOrder.KEY_TIME, -1L)
        if (id != WorkOrder.NO_ID && time != -1L) {
            // Get the worker
            launch {
                val workOrder = Harbinger.getWorkOrder(id)
                if (workOrder != null) {
                    Harbinger.logger.i("Work Order Received ($workOrder)")

                    // Get job creator for tag
                    val creator = Harbinger.workCreators[workOrder.tag]
                    creator?.let {
                        val worker = it.createWorker()
                        worker.setContext(context)

                        // Start the worker and add it's resulting job to the list to track
                        Harbinger.workJobs += worker.startWork(workOrder.extras)

                        // If work order is 'exact' reschedule order
                        if (workOrder.exact) {
                            Harbinger.reschedule(workOrder, time)
                        }
                    }
                } else {
                    Harbinger.logger.e("Unable to find WorkOrder for id ($id)")
                }
            }
        } else {
            Harbinger.logger.e("Cannot execute WorkOrder for id($id)")
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