package com.ftinc.harbinger

import android.content.Context
import com.ftinc.harbinger.util.PersistableBundleCompat


abstract class Job {

    sealed class Result {
        object Success : Result()
        class Failure(data: PersistableBundleCompat)
        object Reschedule : Result()
    }

    /**
     * Called when job is fired off and needs to do it's work
     * @param extras the extra data set in the job request
     */
    abstract fun doWork(extras: PersistableBundleCompat): Result


    /**
     * The application context
     */
    protected lateinit var applicationContext: Context

    /**
     * Set the context to be associated with this job running
     */
    internal fun setContext(context: Context): Job {
        applicationContext = context.applicationContext
        return this
    }
}