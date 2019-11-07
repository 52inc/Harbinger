package com.ftinc.harbinger.work

import android.content.Context
import com.ftinc.harbinger.Harbinger
import com.ftinc.harbinger.util.support.PersistableBundleCompat
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * The worker interface that runs the work task set out and defined by a [com.ftinc.harbinger.work.WorkOrder]
 */
abstract class Worker {

    /**
     * Called when job is fired off and needs to do it's work
     * @param extras the extra data set in the job request
     */
    abstract suspend fun doWork(extras: PersistableBundleCompat)

    protected lateinit var applicationContext: Context

    /**
     * Set the context to be associated with this job running
     */
    internal fun setContext(context: Context): Worker {
        applicationContext = context.applicationContext
        return this
    }
}
