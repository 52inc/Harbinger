package com.ftinc.harbinger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ftinc.harbinger.scheduler.Scheduler
import com.ftinc.harbinger.storage.WorkStorage
import com.ftinc.harbinger.work.workOrder
import com.nhaarman.mockitokotlin2.verifyBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import java.util.*

@RunWith(AndroidJUnit4::class)
class HarbingerTest {

    private lateinit var context: Context
    private lateinit var storage: WorkStorage
    private lateinit var scheduler: Scheduler

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storage = mock(WorkStorage::class)
        scheduler = mock(Scheduler::class)
    }

    @Test
    fun `test initializing harbinger`() {
        Harbinger.create(context)
        Harbinger.initialized.`should be true`()
    }

    @Test
    fun `test schedule work order`() {
        Harbinger.create(context, WorkScheduler(context, scheduler))
            .setStorage(storage)

        val order = workOrder(TAG, 0) {
            startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
            days = setOf(
                DayOfWeek.MONDAY
            )
            interval = Duration.ofDays(14)
        }

        val result = Harbinger.schedule(order)

        result `should be equal to` WorkScheduler.IDS_OFFSET
        verifyBlocking(storage) { insert(order.copy(id = result)) }
    }

    companion object {
        private const val TAG = "TestTag"
    }
}
