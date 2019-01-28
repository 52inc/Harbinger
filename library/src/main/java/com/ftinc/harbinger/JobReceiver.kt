package com.ftinc.harbinger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ftinc.harbinger.util.PersistableBundleCompat
import java.util.concurrent.Callable


class JobReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val tag = intent.getStringExtra(JobRequest.KEY_TAG)
        val extrasXml = intent.getStringExtra(JobRequest.KEY_EXTRAS)
        val extras = PersistableBundleCompat.fromXml(extrasXml)

        // Get job creator for tag
        val creator = Harbinger.jobCreators[tag]
        creator?.let {
            val job = it.createJob()
            job.setContext(context)

            Harbinger.jobLogger.i("Job Received ($tag, $extrasXml, $job)")
            val future = Harbinger.jobExecutor.submit(JobCallable(extras, job))
            Harbinger.jobFutures.add(future) // ? why
        }
    }

    class JobCallable(val extras: PersistableBundleCompat, val job: Job) : Callable<Job.Result> {

        override fun call(): Job.Result {
            return job.doWork(extras)
        }
    }

    companion object {

        /**
         * Create intent to call this receiver
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, JobReceiver::class.java)
        }
    }
}