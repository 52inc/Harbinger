package com.ftinc.harbinger

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.facebook.stetho.Stetho
import com.ftinc.harbinger.job.MessageWorker


class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)

        createChannel(this, MessageWorker.CHANNEL_ID) {
            name = "Harbinger Test"
            description = "Testing Notification dispatching"
        }

        Harbinger.create(this)
            .registerCreator(MessageWorker.TAG, MessageWorker.Creator())
    }

    @SuppressLint("WrongConstant")
    fun createChannel(context: Context, id: String, builder: NotificationChannelBuilder.() -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val b = NotificationChannelBuilder()
            b.builder()
            val channel = NotificationChannel(id, b.name, b.importance)
            channel.description = b.description
            b.enableVibration?.let { channel.enableVibration(it) }
            b.group?.let { channel.group = it }
            b.lightColor?.let {
                channel.lightColor = it
                channel.enableLights(true)
            }
            b.showBadge?.let { channel.setShowBadge(it) }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}