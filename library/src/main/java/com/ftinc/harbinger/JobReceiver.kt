package com.ftinc.harbinger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class JobReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

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