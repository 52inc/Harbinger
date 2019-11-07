package com.ftinc.harbinger.work

import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should not be null`
import org.junit.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

class WorkOrderTest {

    @Test
    fun `test build validation - start time - throws error`() {
        try {
            workOrder(TAG, 0) {}
        } catch (e: Throwable) {
            e `should be instance of` IllegalArgumentException::class
            return
        }
        assert(false) { "Should have thrown error" }
    }

    @Test
    fun `test build validation - start time - wrong offset`() {
        try {
            workOrder(TAG, 0) {
                startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.ofHours(-4))
            }
        } catch (e: Throwable) {
            e `should be instance of` IllegalArgumentException::class
            return
        }
        assert(false) { "Should have thrown error" }
    }

    @Test
    fun `test build validation - end time - wrong offset`() {
        try {
            workOrder(TAG, 0) {
                startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
                endTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.ofHours(-4))
            }
        } catch (e: Throwable) {
            e `should be instance of` IllegalArgumentException::class
            return
        }
        assert(false) { "Should have thrown error" }
    }

    @Test
    fun `test build validation - start time with older end time - throws error`() {
        try {
            workOrder(TAG, 0) {
                startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
                endTime = OffsetDateTime.of(2019, 4, 11, 2, 30, 0, 0, ZoneOffset.UTC)
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
            workOrder(TAG, 0) {
                startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
                days = setOf(
                    DayOfWeek.MONDAY
                )
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
            workOrder(TAG, 0) {
                startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
                days = setOf(
                    DayOfWeek.MONDAY
                )
                interval = Duration.ofDays(4)
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
            workOrder(TAG, 0) {
                startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
                interval = Duration.ofMinutes(10L)
            }
        } catch (e: Throwable) {
            e `should be instance of` IllegalArgumentException::class
            return
        }
        assert(false) { "Should have thrown error" }
    }

    @Test
    fun `test build validation - start time - correct offset`() {
        val order = workOrder(TAG, 0) {
            startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
        }
        order.`should not be null`()
    }

    @Test
    fun `test build validation - end time - correct offset`() {
        val order = workOrder(TAG, 0) {
            startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
            endTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
        }
        order.`should not be null`()
    }

    @Test
    fun `test build validation - non-weekly interval - passes`() {
        val order = workOrder(TAG, 0) {
            startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
            interval = Duration.ofMinutes(16L)
        }
        order.`should not be null`()
    }

    @Test
    fun `test build validation - valid day of week - non-week interval - passes`() {
        val order = workOrder(TAG, 0) {
            startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
            days = setOf(
                DayOfWeek.MONDAY
            )
            interval = Duration.ofDays(14)
        }
        order.`should not be null`()
    }

    @Test
    fun `test build validation - single event - passes`() {
        val order = workOrder(TAG, 0) {
            startTime = OffsetDateTime.of(2019, 4, 11, 5, 30, 0, 0, ZoneOffset.UTC)
        }
        order.`should not be null`()
    }


    companion object {
        private const val TAG = "test_tag"
    }
}
