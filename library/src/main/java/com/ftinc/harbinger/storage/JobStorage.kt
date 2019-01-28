package com.ftinc.harbinger.storage

import com.ftinc.harbinger.JobRequest


interface JobStorage {

    fun putJob(request: JobRequest)
    fun getJobRequest(id: Int): JobRequest?
    fun getJobRequests(): List<JobRequest>
    fun deleteJob(id: Int)
    fun deleteAllJobs()
}