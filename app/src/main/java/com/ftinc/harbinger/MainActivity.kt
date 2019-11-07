package com.ftinc.harbinger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ftinc.harbinger.job.MessageWorker
import com.ftinc.harbinger.work.workOrder
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val clickId = AtomicInteger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        actionSchedule.setOnClickListener {

            val order = workOrder(MessageWorker.TAG, JOB_ID) {
                days = setOf(
                    LocalDateTime.now().dayOfWeek
                )
                startTime = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(10)
                interval = Duration.ofMinutes(15L)

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
