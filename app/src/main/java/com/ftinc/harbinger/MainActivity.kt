package com.ftinc.harbinger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ftinc.harbinger.job.MessageWorker
import com.ftinc.harbinger.util.extensions.dayOfWeek
import com.ftinc.harbinger.util.extensions.seconds
import com.ftinc.harbinger.work.workOrder
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {

    private val clickId = AtomicInteger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        actionSchedule.setOnClickListener {

            val order = workOrder(MessageWorker.TAG) {
                id = JOB_ID
                exact = true
                day = Calendar.getInstance().dayOfWeek
                startTimeInMillis = System.currentTimeMillis() + 10.seconds()
                intervalInMillis = 30.seconds()

                extras {
                    "id" to clickId.getAndIncrement()
                }
            }

            val resultingId = Harbinger.schedule(order)

            Log.i("Harbinger", "Scheduled Job($resultingId)")
        }

        actionUnschedule.setOnClickListener {
            Harbinger.unschedule(JOB_ID)
        }
    }

    companion object {
        const val JOB_ID = 5
    }
}
