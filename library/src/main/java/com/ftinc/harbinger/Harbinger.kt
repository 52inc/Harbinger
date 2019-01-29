package com.ftinc.harbinger

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.ftinc.harbinger.logging.WorkLogger
import com.ftinc.harbinger.logging.LogcatLogger
import com.ftinc.harbinger.storage.DatabaseWorkStorage
import com.ftinc.harbinger.storage.WorkStorage
import com.ftinc.harbinger.work.WorkCreator
import com.ftinc.harbinger.work.WorkOrder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce
import java.lang.IllegalStateException
import java.util.concurrent.Executors

/**
 * > a person or thing that announces or signals the approach of another.
 *
 * Schedule alarms and notifications through this single source to keep track and ensure that no id's get
 * contaminated or duplicated
 */
@SuppressLint("StaticFieldLeak")
object Harbinger {

    /**
     * Flag indicating whether or not [Harbinger] has been initialized
     */
    var initialized = false

    private lateinit var applicationContext: Context
    private lateinit var powerManager: PowerManager
    private lateinit var scheduler: WorkScheduler

    internal var logger: WorkLogger = LogcatLogger()
    internal val workCreators = HashMap<String, WorkCreator>()
    internal val workExecutor = Executors.newSingleThreadExecutor()
    internal val workJobs = ArrayList<Job>()

    private val storageScope = CoroutineScope(Dispatchers.IO)
    private var storage: WorkStorage? = null


    /**
     * Initialize Harbinger with the android context
     * @param context the Android context to initialize internal components with, should be the Application context
     */
    fun create(context: Context): Harbinger {
        if (!initialized) {

            // Check if null, can happen during testing
            applicationContext = if (context.applicationContext != null) {
                context.applicationContext
            } else {
                context
            }

            // Load system services
            powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager

            // Create scheduler and storage
            scheduler = WorkScheduler(context)
            storage = DatabaseWorkStorage(context)

            initialized = true
        }
        return this
    }

    /**
     * Set the logger that this library should use
     */
    fun setLogger(logger: WorkLogger): Harbinger {
        this.logger = logger
        return this
    }

    /**
     * Set the storage mechanism for storing/retrieving Jobs. By default it is backed by [DatabaseWorkStorage] that
     * uses SQLite to store and retrieve jobs
     * @param storage the [WorkStorage] implementation
     */
    fun setStorage(storage: WorkStorage): Harbinger {
        this.storage = storage
        return this
    }

    /**
     * Register a job creator by it's [tag], these are used during [schedule] to
     * create jobs when running them
     * @param tag the tag of the Job/JobCreator to register. Must be unique.
     * @param creator the job creator to register
     */
    fun registerCreator(tag: String, creator: WorkCreator): Harbinger {
        checkInitialization()
        workCreators[tag] = creator
        return this
    }

    /**
     * Schedule a job request
     * @param request the [WorkOrder] to schedule
     * @return the job id of the recently scheduled task
     */
    fun schedule(request: WorkOrder): Int {
        checkInitialization()
        val workId = scheduler.schedule(request)

        // Store Request
        storageScope.launch {
            val order = request.copy(id = workId)
            storage?.insert(order)
            logger.d("Inserted WorkOrder($order)")
        }

        // Return the job id
        return workId
    }

    /**
     * Unschedule a job by it's Id
     */
    fun unschedule(jobId: Int? = null) {
        if (jobId != null) {
            // Delete stored job
            storageScope.launch {
                storage?.find(jobId)?.apply {
                    scheduler.unschedule(this)
                    logger.d("Unscheduled WorkOrder($jobId)")
                }
                storage?.delete(jobId)
                logger.d("Deleted WorkOrder($jobId)")
            }
        } else {
            storageScope.launch {
                storage?.getAll()?.let { requests ->
                    requests.forEach {
                        scheduler.unschedule(it)
                        logger.d("Unscheduling WorkOrder(${it.id})")
                    }
                }
                storage?.deleteAll()
                logger.d("Deleted all WorkOrders")
            }
        }
    }

    /**
     * Return whether or not this application is ignorning battery optimizations on Android M or higher
     * @return true if ignoring battery optimizations on Android M or higher, true if on lower android device
     */
    fun isIgnoringBatteryOptimizations(): Boolean {
        checkInitialization()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(applicationContext.packageName)
        } else {
            true
        }
    }

    /**
     * Get a [WorkOrder] by it's id
     */
    internal suspend fun getWorkOrder(id: Int): WorkOrder? {
        return storage?.find(id)
    }

    /**
     * Reschedule a work order for the next available date
     */
    internal fun reschedule(order: WorkOrder, lastScheduledTime: Long) {
        scheduler.reschedule(order, lastScheduledTime)
    }

    /**
     * Reschedule jobs from storage against the [AlarmManager]
     */
    internal fun rescheduleJobs() {
        checkInitialization()
        storageScope.launch {
            val requests = storage?.getAll()
            logger.i("Rescheduling Jobs: $requests")
            requests?.forEach { request ->
                scheduler.schedule(request)
            }
        }
    }

    /**
     * Check to see if [Harbinger] has been initialized, if not throw error
     */
    private fun checkInitialization() {
        if (!initialized) throw IllegalStateException("You must initialize Harbinger first via `create()`")
    }
}