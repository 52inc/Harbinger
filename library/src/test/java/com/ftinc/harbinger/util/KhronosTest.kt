package com.ftinc.harbinger.util

import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

class KhronosTest {

    class MockTimeSpace : Khronos.TimeSpace {
        var dateTime: OffsetDateTime = OffsetDateTime.of(2019, 1, 1, 1, 0, 0, 0, ZoneOffset.UTC)

        override fun now(): OffsetDateTime {
            return dateTime
        }
    }

    private val timeSpace = MockTimeSpace()

    @Before
    fun setUp() {
        Khronos.timeSpace = timeSpace
    }

    @Test
    fun `next day of week when now() less than scheduled time`() {
        // This date is on a SATURDAY
        timeSpace.dateTime = OffsetDateTime.of(2019, 5, 11, 9, 30, 0, 0, ZoneOffset.UTC)
        val scheduledTime = OffsetDateTime.of(2019, 4, 16, 18, 30, 0, 0, ZoneOffset.UTC)

        val result = Khronos.next(scheduledTime, DayOfWeek.SATURDAY)

        result shouldEqual OffsetDateTime.of(2019, 5, 11, 18, 30, 0, 0, ZoneOffset.UTC)
    }

    @Test
    fun `next day of week when now() is past scheduled time`() {
        // This date is on a SATURDAY
        timeSpace.dateTime = OffsetDateTime.of(2019, 5, 11, 20, 30, 0, 0, ZoneOffset.UTC)
        val scheduledTime = OffsetDateTime.of(2019, 4, 16, 18, 30, 0, 0, ZoneOffset.UTC)

        val result = Khronos.next(scheduledTime, DayOfWeek.SATURDAY)

        result shouldEqual OffsetDateTime.of(2019, 5, 18, 18, 30, 0, 0, ZoneOffset.UTC)
    }

    @Test
    fun `next day of week when next day is not now()`() {
        // This date is on a SATURDAY
        timeSpace.dateTime = OffsetDateTime.of(2019, 5, 11, 20, 30, 0, 0, ZoneOffset.UTC)
        val scheduledTime = OffsetDateTime.of(2019, 4, 16, 18, 30, 0, 0, ZoneOffset.UTC)

        val result = Khronos.next(scheduledTime, DayOfWeek.MONDAY)

        result shouldEqual OffsetDateTime.of(2019, 5, 13, 18, 30, 0, 0, ZoneOffset.UTC)
    }

    @Test
    fun `next day of week with greater than minimum interval from previous scheduled time`() {
        // This date is on a SATURDAY
        timeSpace.dateTime = OffsetDateTime.of(2019, 5, 11, 9, 30, 0, 0, ZoneOffset.UTC)
        val scheduledTime = OffsetDateTime.of(2019, 4, 16, 18, 30, 0, 0, ZoneOffset.UTC)
        val previousScheduledTime = OffsetDateTime.of(2019, 5, 4, 18, 30, 0, 0, ZoneOffset.UTC)
        val interval = Duration.ofDays(7)

        val result = Khronos.next(scheduledTime, DayOfWeek.SATURDAY, previousScheduledTime, interval)

        result shouldEqual OffsetDateTime.of(2019, 5, 11, 18, 30, 0, 0, ZoneOffset.UTC)
    }

    @Test
    fun `next day of week with less than minimum interval from previous scheduled time`() {
        // This date is on a SATURDAY
        timeSpace.dateTime = OffsetDateTime.of(2019, 5, 11, 9, 30, 0, 0, ZoneOffset.UTC)
        val scheduledTime = OffsetDateTime.of(2019, 4, 16, 18, 30, 0, 0, ZoneOffset.UTC)
        val previousScheduledTime = OffsetDateTime.of(2019, 5, 4, 18, 30, 0, 0, ZoneOffset.UTC)
        val interval = Duration.ofDays(14)

        val result = Khronos.next(scheduledTime, DayOfWeek.SATURDAY, previousScheduledTime, interval)

        result shouldEqual OffsetDateTime.of(2019, 5, 18, 18, 30, 0, 0, ZoneOffset.UTC)
    }

    @Test
    fun `next scheduled time after now() no interval or end date`() {
        // This date is on a SATURDAY
        timeSpace.dateTime = OffsetDateTime.of(2019, 5, 11, 9, 30, 0, 0, ZoneOffset.UTC)
        val scheduledTime = OffsetDateTime.of(2020, 4, 16, 18, 30, 0, 0, ZoneOffset.UTC)

        val result = Khronos.next(scheduledTime, null, null)

        result.shouldNotBeNull()
        result shouldEqual scheduledTime
    }

    @Test
    fun `next scheduled time before now() no interval or end date`() {
        // This date is on a SATURDAY
        timeSpace.dateTime = OffsetDateTime.of(2019, 5, 11, 9, 30, 0, 0, ZoneOffset.UTC)
        val scheduledTime = OffsetDateTime.of(2019, 4, 16, 18, 30, 0, 0, ZoneOffset.UTC)

        val result = Khronos.next(scheduledTime, null, null)

        result.shouldBeNull()
    }

    @Test
    fun `next scheduled time before now() with interval and no end date`() {
        // This date is on a SATURDAY
        timeSpace.dateTime = OffsetDateTime.of(2019, 5, 11, 9, 30, 0, 0, ZoneOffset.UTC)
        val scheduledTime = OffsetDateTime.of(2019, 5, 1, 18, 30, 0, 0, ZoneOffset.UTC)
        val interval = Duration.ofDays(7)

        val result = Khronos.next(scheduledTime, null, interval)

        result.shouldNotBeNull()
        result shouldEqual OffsetDateTime.of(2019, 5, 15, 18, 30, 0, 0, ZoneOffset.UTC)
    }

    @Test
    fun `next scheduled time before now() with interval and end date before next date`() {
        // This date is on a SATURDAY
        timeSpace.dateTime = OffsetDateTime.of(2019, 5, 11, 9, 30, 0, 0, ZoneOffset.UTC)
        val scheduledTime = OffsetDateTime.of(2019, 5, 1, 18, 30, 0, 0, ZoneOffset.UTC)
        val endDate = OffsetDateTime.of(2019, 5, 14, 18, 30, 0, 0, ZoneOffset.UTC)
        val interval = Duration.ofDays(7)

        val result = Khronos.next(scheduledTime, endDate, interval)

        result.shouldBeNull()
    }

    @Test
    fun `next scheduled time before now() with interval and end date after next date`() {
        // This date is on a SATURDAY
        timeSpace.dateTime = OffsetDateTime.of(2019, 5, 11, 9, 30, 0, 0, ZoneOffset.UTC)
        val scheduledTime = OffsetDateTime.of(2019, 5, 1, 18, 30, 0, 0, ZoneOffset.UTC)
        val endDate = OffsetDateTime.of(2019, 5, 16, 18, 30, 0, 0, ZoneOffset.UTC)
        val interval = Duration.ofDays(7)

        val result = Khronos.next(scheduledTime, endDate, interval)

        result.shouldNotBeNull()
        result shouldEqual OffsetDateTime.of(2019, 5, 15, 18, 30, 0, 0, ZoneOffset.UTC)
    }
}
