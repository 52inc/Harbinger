package com.ftinc.harbinger

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.ftinc.harbinger.logging.JobLogger
import com.ftinc.harbinger.logging.LogcatLogger
import com.ftinc.harbinger.storage.DatabaseJobStorage
import com.ftinc.harbinger.storage.JobStorage
import java.lang.IllegalStateException
import java.util.concurrent.Executors
import java.util.concurrent.Future

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
    private lateinit var scheduler: JobScheduler

    internal var jobLogger: JobLogger = LogcatLogger()
    internal val jobCreators = HashMap<String, JobCreator>()
    internal val jobExecutor = Executors.newSingleThreadExecutor()
    internal val jobFutures = ArrayList<Future<Job.Result>>()

    private val jobStorageExecutor = Executors.newSingleThreadExecutor()
    private var storage: JobStorage? = null


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
            scheduler = JobScheduler(context)
            storage = DatabaseJobStorage(context)

            initialized = true
        }
        return this
    }

    /**
     * Set the logger that this library should use
     */
    fun setLogger(logger: JobLogger): Harbinger {
        jobLogger = logger
        return this
    }

    /**
     * Set the storage mechanism for storing/retrieving Jobs. By default it is backed by [DatabaseJobStorage] that
     * uses SQLite to store and retrieve jobs
     * @param storage the [JobStorage] implementation
     */
    fun setStorage(storage: JobStorage): Harbinger {
        this.storage = storage
        return this
    }

    /**
     * Register a job creator by it's [tag], these are used during [schedule] to
     * create jobs when running them
     * @param tag the tag of the Job/JobCreator to register. Must be unique.
     * @param creator the job creator to register
     */
    fun registerJobCreator(tag: String, creator: JobCreator): Harbinger {
        checkInitialization()
        jobCreators[tag] = creator
        return this
    }

    /**
     * Schedule a job request
     * @param request the [JobRequest] to schedule
     * @return the job id of the recently scheduled task
     */
    fun schedule(request: JobRequest): Int {
        checkInitialization()
        val jobId = scheduler.schedule(request)

        // Store Request
        jobStorageExecutor.submit {
            val jobRequest = request.copy(id = jobId)
            storage?.putJob(jobRequest)
        }

        // Return the job id
        return jobId
    }

    /**
     * Unschedule a job by it's Id
     */
    fun unschedule(jobId: Int? = null) {
        if (jobId != null) {
            // Delete stored job
            jobStorageExecutor.submit {
                storage?.getJobRequest(jobId)?.apply {
                    scheduler.unschedule(this)
                }
                storage?.deleteJob(jobId)
            }


        } else {
            jobStorageExecutor.submit {
                storage?.getJobRequests()?.let { requests ->
                    requests.forEach {
                        scheduler.unschedule(it)
                    }
                }
                storage?.deleteAllJobs()
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
     * Reschedule jobs from storage against the [AlarmManager]
     */
    internal fun rescheduleJobs() {
        checkInitialization()
        jobStorageExecutor.submit {
            val requests = storage?.getJobRequests()
            jobLogger.i("Rescheduling Jobs: $requests")
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