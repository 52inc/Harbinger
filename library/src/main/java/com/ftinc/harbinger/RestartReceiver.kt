package com.ftinc.harbinger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class RestartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            if (it.action == Intent.ACTION_MY_PACKAGE_REPLACED || it.action == Intent.ACTION_BOOT_COMPLETED) {
                // Restart Jobs

            }
        }
    }
}