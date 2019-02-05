package com.ftinc.harbinger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ftinc.harbinger.scheduler.Scheduler
import com.ftinc.harbinger.storage.WorkStorage
import com.ftinc.harbinger.util.extensions.days
import com.ftinc.harbinger.work.workOrder
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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

        val order = workOrder(TAG) {
            startTimeInMillis = 1000L
            day = Calendar.MONDAY
            intervalInMillis = 14.days()
        }

        val result = Harbinger.schedule(order)

        result `should be equal to` WorkScheduler.IDS_OFFSET
        verify(scheduler).repeating(any(), any(), any())
        verifyBlocking(storage) { insert(order.copy(id = result)) }
    }

    companion object {
        private const val TAG = "TestTag"
    }
}