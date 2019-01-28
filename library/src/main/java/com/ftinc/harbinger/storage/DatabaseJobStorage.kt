package com.ftinc.harbinger.storage

import android.content.Context
import com.ftinc.harbinger.Database
import com.ftinc.harbinger.JobRequest
import com.ftinc.harbinger.util.PersistableBundleCompat
import com.squareup.sqldelight.android.AndroidSqliteDriver


class DatabaseJobStorage(context: Context) : JobStorage {

    private val database by lazy {
        val driver = AndroidSqliteDriver(Database.Schema, context, DB_NAME)
        Database(driver)
    }


    override fun putJob(request: JobRequest) {
        database.jobQueries.insertJob(request.id, request.tag, request.extras.saveToXml(), request.startTimeInMillis,
            request.endTimeInMillis, if (request.exact) 1 else 0, request.intervalInMillis)
    }

    override fun getJobRequest(id: Int): JobRequest? {
        return database.jobQueries.forJobId(id, jobRequestMapper).executeAsOneOrNull()
    }

    override fun getJobRequests(): List<JobRequest> {
        return database.jobQueries.selectAll(jobRequestMapper).executeAsList()
    }

    override fun deleteJob(id: Int) {
        database.jobQueries.deleteByJobId(id)
    }

    override fun deleteAllJobs() {
        database.jobQueries.deleteAll()
    }

    companion object {
        private const val DB_NAME = "harbinger_jobs.db"

        private val jobRequestMapper = { id: Long, job_id: Int, tag: String, extras: String, start_time: Long, end_time: Long, exact: Int, interval: Long? ->
            JobRequest(job_id, tag, PersistableBundleCompat.fromXml(extras), start_time, end_time, exact == 1, interval)
        }
    }
}