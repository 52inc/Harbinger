package com.ftinc.harbinger.job

import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.ftinc.harbinger.work.Worker
import com.ftinc.harbinger.work.WorkCreator
import com.ftinc.harbinger.R
import com.ftinc.harbinger.util.support.PersistableBundleCompat


class MessageWorker : Worker() {

    override suspend fun doWork(extras: PersistableBundleCompat) {
        val signalId = extras.getInt("id", -1)

        Log.d("MessageJob", "Job Fired($signalId, thread: ${Thread.currentThread().name}), Show notification.")

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
    }

    class Creator : WorkCreator {

        override fun createWorker(): Worker {
            return MessageWorker()
        }
    }

    companion object {
        const val TAG = "com.ftinc.harbinger.job.MESSAGE"
        const val NOTIFICATION_ID = 100
        const val CHANNEL_ID = "harbinger-test-client"
    }
}