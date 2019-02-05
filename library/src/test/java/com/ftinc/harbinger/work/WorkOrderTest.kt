package com.ftinc.harbinger.work

import com.ftinc.harbinger.util.extensions.days
import com.ftinc.harbinger.util.extensions.minutes
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should not be null`
import org.junit.Assert.*
import org.junit.Test
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.TimeUnit

class WorkOrderTest {

    @Test
    fun `test build validation - start time - throws error`() {
        try {
            workOrder(TAG) {}
        } catch (e: Throwable) {
            e `should be instance of` IllegalArgumentException::class
            return
        }
        assert(false) { "Should have thrown error" }
    }

    @Test
    fun `test build validation - start time with older end time - throws error`() {
        try {
            workOrder(TAG) {
                startTimeInMillis = 1000L
                endTimeInMillis = 500L
            }
        } catch (e: Throwable) {
            e `should be instance of` IllegalArgumentException::class
            return
        }
        assert(false) { "Should have thrown error" }
    }

    @Test
    fun `test build validation - day of week - throws error`() {
        try {
            workOrder(TAG) {
                startTimeInMillis = 1000L
                day = 500
            }
        } catch (e: Throwable) {
            e `should be instance of` IllegalArgumentException::class
            return
        }
        assert(false) { "Should have thrown error" }
    }

    @Test
    fun `test build validation - valid day of week - no interval - throws error`() {
        try {
            workOrder(TAG) {
                startTimeInMillis = 1000L
                day = Calendar.MONDAY
            }
        } catch (e: Throwable) {
            e `should be instance of` IllegalArgumentException::class
            return
        }
        assert(false) { "Should have thrown error" }
    }

    @Test
    fun `test build validation - valid day of week - non-week interval - throws error`() {
        try {
            workOrder(TAG) {
                startTimeInMillis = 1000L
                day = Calendar.MONDAY
                intervalInMillis = 4.days()
            }
        } catch (e: Throwable) {
            e `should be instance of` IllegalArgumentException::class
            return
        }
        assert(false) { "Should have thrown error" }
    }

    @Test
    fun `test build validation - non-weekly interval - throws error`() {
        try {
            workOrder(TAG) {
                startTimeInMillis = 1000L
                intervalInMillis = 5000L
            }
        } catch (e: Throwable) {
            e `should be instance of` IllegalArgumentException::class
            return
        }
        assert(false) { "Should have thrown error" }
    }

    @Test
    fun `test build validation - non-weekly interval - passes`() {
        val order = workOrder(TAG) {
            startTimeInMillis = 1000L
            intervalInMillis = 16.minutes()
        }
        order.`should not be null`()
    }

    @Test
    fun `test build validation - valid day of week - non-week interval - passes`() {
        val order = workOrder(TAG) {
            startTimeInMillis = 1000L
            day = Calendar.MONDAY
            intervalInMillis = 14.days()
        }
        order.`should not be null`()
        order.exact.`should be false`()
    }

    @Test
    fun `test build validation - single event - passes`() {
        val order = workOrder(TAG) {
            startTimeInMillis = 1000L
        }
        order.`should not be null`()
        order.exact.`should be true`()
    }


    companion object {
        private const val TAG = "test_tag"
    }
}