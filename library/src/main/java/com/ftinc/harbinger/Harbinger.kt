package com.ftinc.harbinger

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import com.ftinc.harbinger.logging.WorkLogger
import com.ftinc.harbinger.logging.LogcatLogger
import com.ftinc.harbinger.storage.DatabaseWorkStorage
import com.ftinc.harbinger.storage.WorkStorage
import com.ftinc.harbinger.work.OrderEvent
import com.ftinc.harbinger.work.WorkCreator
import com.ftinc.harbinger.work.WorkOrder
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.*
import java.lang.IllegalStateException
import java.util.concurrent.Executors

/**
 * > A person or thing that announces or signals the approach of another.
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

    private val storageScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    private var storage: WorkStorage? = null

    /**
     * Initialize Harbinger with the android context
     * @param context the Android context to initialize internal components with, should be the Application context
     */
    fun create(context: Context, workScheduler: WorkScheduler = WorkScheduler(context.applicationContext)): Harbinger {
        if (!initialized) {
            AndroidThreeTen.init(context)

            // Check if null, can happen during testing
            applicationContext = if (context.applicationContext != null) {
                context.applicationContext
            } else {
                context
            }

            // Load system services
            powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager

            // Create scheduler and storage
            scheduler = workScheduler
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
     * Schedule a work order
     * @param request the [WorkOrder] to schedule
     * @return the job id of the recently scheduled task
     */
    fun schedule(request: WorkOrder): Int {
        checkInitialization()
        val workId = scheduler.schedule(request)
        if (workId == WorkOrder.DEAD_ID) {
            // Work order is dead, delete if possible
            if (request.id != WorkOrder.NO_ID) {
                storageScope.launch {
                    storage?.delete(request.id)
                }
            }
        } else {
            storageScope.launch {
                val order = request.copy(id = workId)
                storage?.insert(order)
                logger.d("Inserted WorkOrder($order)")
            }
        }

        // Return the job id
        return workId
    }

    /**
     * Schedule a list of work orders
     */
    fun schedule(orders: List<WorkOrder>): List<Int> {
        checkInitialization()

        val orderIds = ArrayList<Int>()
        val ordersToDelete = ArrayList<Int>()
        val ordersToSave = ArrayList<WorkOrder>()

        orders.forEach { order ->
            val workId = scheduler.schedule(order)
            if (workId == WorkOrder.DEAD_ID) {
                if (order.id != WorkOrder.NO_ID) {
                    ordersToDelete += order.id
                }
            } else {
                ordersToSave += order.copy(id = workId)
            }

            orderIds += workId
        }

        if (ordersToDelete.isNotEmpty()) {
            storageScope.launch {
                storage?.delete(ordersToDelete)
            }
        }

        if (ordersToSave.isNotEmpty()) {
            storageScope.launch {
                storage?.insert(ordersToSave)
            }
        }

        return orderIds
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
    internal fun reschedule(order: WorkOrder, intent: Intent) {
        val resultId = scheduler.reschedule(order, intent)
        if (resultId == WorkOrder.DEAD_ID) {
            logger.d("Rescheduling failed with a dead_id for Order(${order.id})")
            if (order.id != WorkOrder.NO_ID) {
                storageScope.launch {
                    storage?.delete(order.id)
                }
            }
        }
    }

    /**
     * Reschedule jobs from storage against the [AlarmManager]
     */
    internal fun rescheduleJobs() {
        checkInitialization()
        storageScope.launch {
            val orders = storage?.getAll()
            val ordersToDelete = ArrayList<Int>()
            logger.i("Rescheduling Jobs: $orders")

            orders?.forEach { order ->
                val id = scheduler.schedule(order)
                if (id == WorkOrder.DEAD_ID) {
                    ordersToDelete += id
                }
            }

            if (ordersToDelete.isNotEmpty()) {
                storage?.delete(ordersToDelete)
            }
        }
    }

    /**
     * Insert a [WorkOrder] event for record keeping and re-scheduling purposes
     * @param event the order event to insert
     */
    internal fun insertEvent(event: OrderEvent) {
        storageScope.launch {
            storage?.insert(event)
        }
    }

    /**
     * Check to see if [Harbinger] has been initialized, if not throw error
     */
    private fun checkInitialization() {
        check(initialized) { "You must initialize Harbinger first via `create()`" }
    }
}
