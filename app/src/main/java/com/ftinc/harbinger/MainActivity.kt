package com.ftinc.harbinger

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ftinc.harbinger.job.MessageJob
import com.ftinc.harbinger.util.PersistableBundleCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {

    private val id = AtomicInteger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        actionSchedule.setOnClickListener {

            val request = JobRequest.Builder(MessageJob.TAG)
                .setId(JOB_ID)
                .setExact(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5))
                .setPeriodic(TimeUnit.SECONDS.toMillis(30L))
                .setExtras(PersistableBundleCompat().apply {
                    putInt("id", id.getAndIncrement())
                })
                .build()

            val resultingId = Harbinger.schedule(request)

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
