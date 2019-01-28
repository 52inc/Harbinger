package com.ftinc.harbinger.job

import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.ftinc.harbinger.Job
import com.ftinc.harbinger.JobCreator
import com.ftinc.harbinger.R
import com.ftinc.harbinger.util.PersistableBundleCompat
import kotlin.math.sign


class MessageJob : Job() {

    override fun doWork(extras: PersistableBundleCompat): Result {
        val signalId = extras.getInt("id", -1)

        Log.d("MessageJob", "Job Fired($signalId), Show notification.")

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Test Schedule ($signalId)")
            .setContentText("This is a test of the emergency broadcast system")
            .setSmallIcon(R.drawable.ic_messages_icon)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)

        return Result.Success
    }

    class Creator : JobCreator {

        override fun createJob(): Job {
            return MessageJob()
        }
    }

    companion object {
        const val TAG = "com.ftinc.harbinger.job.MESSAGE"
        const val NOTIFICATION_ID = 100
        const val CHANNEL_ID = "harbinger-test-client"
    }
}