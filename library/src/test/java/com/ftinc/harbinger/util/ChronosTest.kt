package com.ftinc.harbinger.util

import org.junit.Test
import java.util.*
import com.ftinc.harbinger.util.Chronos.next
import com.ftinc.harbinger.util.extensions.*
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.shouldEqualTo
import org.junit.Before
import java.util.Calendar.*
import java.util.concurrent.TimeUnit

class ChronosTest {

    class MockClock : Chronos.Clock {
        var time: Long = 0L
        override fun now(): Long = time
    }


    private val clock = MockClock()


    @Before
    fun setUp() {
        Chronos.systemClock = clock
    }

    @Test
    fun `test next day later in week`() {
        val now = Calendar.getInstance()
        now[DAY_OF_WEEK] = Calendar.TUESDAY

        val nextDay = now.next(Calendar.FRIDAY, 10)

        nextDay[YEAR].shouldEqualTo(now[YEAR])
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR])
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.FRIDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(10)
        nextDay[MINUTE].shouldEqualTo(0)
        nextDay[SECOND].shouldEqualTo(0)
        nextDay[MILLISECOND].shouldEqualTo(0)
    }


    @Test
    fun `test next day next week`() {
        val now = Calendar.getInstance()
        now[DAY_OF_WEEK] = Calendar.THURSDAY
        now[WEEK_OF_YEAR] = 10 // To make sure tests don't fail on the last week of the year lol

        val nextDay = now.next(Calendar.MONDAY, 10)

        nextDay[YEAR].shouldEqualTo(now[YEAR])
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR] + 1)
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.MONDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(10)
        nextDay[MINUTE].shouldEqualTo(0)
        nextDay[SECOND].shouldEqualTo(0)
        nextDay[MILLISECOND].shouldEqualTo(0)
    }


    @Test
    fun `test next day same day later hour`() {
        val now = Calendar.getInstance()
        now[DAY_OF_WEEK] = Calendar.WEDNESDAY
        now[HOUR_OF_DAY] = 12 // To make sure tests don't fail on the last week of the year lol

        val nextDay = now.next(Calendar.WEDNESDAY, 18)

        nextDay[YEAR].shouldEqualTo(now[YEAR])
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR])
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.WEDNESDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(18)
        nextDay[MINUTE].shouldEqualTo(0)
        nextDay[SECOND].shouldEqualTo(0)
        nextDay[MILLISECOND].shouldEqualTo(0)
    }


    @Test
    fun `test next day same day earlier hour`() {
        val now = Calendar.getInstance()
        now[DAY_OF_WEEK] = Calendar.WEDNESDAY
        now[HOUR_OF_DAY] = 18 // To make sure tests don't fail on the last week of the year lol

        val nextDay = now.next(Calendar.WEDNESDAY, 12)

        nextDay[YEAR].shouldEqualTo(now[YEAR])
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR] + 1)
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.WEDNESDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(12)
        nextDay[MINUTE].shouldEqualTo(0)
        nextDay[SECOND].shouldEqualTo(0)
        nextDay[MILLISECOND].shouldEqualTo(0)
    }

    @Test
    fun `test next time later in week`() {
        val now = Calendar.getInstance()
        now.dayOfWeek = Calendar.TUESDAY
        val target = Calendar.getInstance()
        target.dayOfWeek = Calendar.TUESDAY
        target.add(Calendar.HOUR, 1)
        target.add(Calendar.MINUTE, 1)
        target.add(Calendar.SECOND, 1)

        val nextDay = now.next(target.timeInMillis, Calendar.FRIDAY)

        nextDay[YEAR].shouldEqualTo(now.year)
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR])
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.FRIDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(target.hourOfDay)
        nextDay[MINUTE].shouldEqualTo(target.minute)
        nextDay[SECOND].shouldEqualTo(target.second)
        nextDay[MILLISECOND].shouldEqualTo(target.millisecond)
    }

    @Test
    fun `test next time next week`() {
        val now = Calendar.getInstance()
        now.dayOfWeek = Calendar.THURSDAY
        now.weekOfYear = 10
        val target = Calendar.getInstance()
        now.weekOfYear = 10
        target.add(Calendar.HOUR, 1)
        target.add(Calendar.MINUTE, 1)
        target.add(Calendar.SECOND, 1)

        val nextDay = now.next(target.timeInMillis, Calendar.MONDAY)

        nextDay[YEAR].shouldEqualTo(now.year)
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now.weekOfYear + 1)
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.MONDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(target.hourOfDay)
        nextDay[MINUTE].shouldEqualTo(target.minute)
        nextDay[SECOND].shouldEqualTo(target.second)
        nextDay[MILLISECOND].shouldEqualTo(target.millisecond)
    }

    @Test
    fun `test next time same day later hour`() {
        val now = Calendar.getInstance()
        now.dayOfWeek = Calendar.WEDNESDAY
        now.hourOfDay = 12
        val target = Calendar.getInstance()
        target.hourOfDay = 18

        val nextDay = now.next(target.timeInMillis, Calendar.WEDNESDAY)

        nextDay[YEAR].shouldEqualTo(now.year)
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR])
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.WEDNESDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(18)
        nextDay[MINUTE].shouldEqualTo(target.minute)
        nextDay[SECOND].shouldEqualTo(target.second)
        nextDay[MILLISECOND].shouldEqualTo(target.millisecond)
    }

    @Test
    fun `test next time same day later minute`() {
        val now = Calendar.getInstance()
        now.dayOfWeek = Calendar.WEDNESDAY
        now.hourOfDay = 18
        now.minute = 20
        val target = Calendar.getInstance()
        target.hourOfDay = 18
        target.minute = 40

        val nextDay = now.next(target.timeInMillis, Calendar.WEDNESDAY)

        nextDay[YEAR].shouldEqualTo(now.year)
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR])
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.WEDNESDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(18)
        nextDay[MINUTE].shouldEqualTo(40)
        nextDay[SECOND].shouldEqualTo(target.second)
        nextDay[MILLISECOND].shouldEqualTo(target.millisecond)
    }

    @Test
    fun `test next time same day later second`() {
        val now = Calendar.getInstance()
        now.dayOfWeek = Calendar.WEDNESDAY
        now.hourOfDay = 18
        now.minute = 40
        now.second = 20
        val target = Calendar.getInstance()
        target.hourOfDay = 18
        target.minute = 40
        target.second = 40

        val nextDay = now.next(target.timeInMillis, Calendar.WEDNESDAY)

        nextDay[YEAR].shouldEqualTo(now.year)
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR])
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.WEDNESDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(18)
        nextDay[MINUTE].shouldEqualTo(40)
        nextDay[SECOND].shouldEqualTo(40)
        nextDay[MILLISECOND].shouldEqualTo(target.millisecond)
    }

    @Test
    fun `test next time same day later millisecond`() {
        val now = Calendar.getInstance()
        now.dayOfWeek = Calendar.WEDNESDAY
        now.hourOfDay = 18
        now.minute = 40
        now.second = 40
        now.millisecond = 20
        val target = Calendar.getInstance()
        target.hourOfDay = 18
        target.minute = 40
        target.second = 40
        target.millisecond = 40

        val nextDay = now.next(target.timeInMillis, Calendar.WEDNESDAY)

        nextDay[YEAR].shouldEqualTo(now.year)
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR])
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.WEDNESDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(18)
        nextDay[MINUTE].shouldEqualTo(40)
        nextDay[SECOND].shouldEqualTo(40)
        nextDay[MILLISECOND].shouldEqualTo(40)
    }

    @Test
    fun `test next time same day earlier hour`() {
        val now = Calendar.getInstance()
        now.dayOfWeek = Calendar.WEDNESDAY
        now.hourOfDay = 18
        val target = Calendar.getInstance()
        target.hourOfDay = 12

        val nextDay = now.next(target.timeInMillis, Calendar.WEDNESDAY)

        nextDay[YEAR].shouldEqualTo(now.year)
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR] + 1)
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.WEDNESDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(12)
        nextDay[MINUTE].shouldEqualTo(target.minute)
        nextDay[SECOND].shouldEqualTo(target.second)
        nextDay[MILLISECOND].shouldEqualTo(target.millisecond)
    }

    @Test
    fun `test next time same day earlier minute`() {
        val now = Calendar.getInstance()
        now.dayOfWeek = Calendar.WEDNESDAY
        now.hourOfDay = 12
        now.minute = 30
        val target = Calendar.getInstance()
        target.hourOfDay = 12
        target.minute = 25

        val nextDay = now.next(target.timeInMillis, Calendar.WEDNESDAY)

        nextDay[YEAR].shouldEqualTo(now.year)
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR] + 1)
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.WEDNESDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(12)
        nextDay[MINUTE].shouldEqualTo(target.minute)
        nextDay[SECOND].shouldEqualTo(target.second)
        nextDay[MILLISECOND].shouldEqualTo(target.millisecond)
    }

    @Test
    fun `test next time same day earlier second`() {
        val now = Calendar.getInstance()
        now.dayOfWeek = Calendar.WEDNESDAY
        now.hourOfDay = 12
        now.minute = 30
        now.second = 30
        val target = Calendar.getInstance()
        target.hourOfDay = 12
        target.minute = 30
        target.second = 25

        val nextDay = now.next(target.timeInMillis, Calendar.WEDNESDAY)

        nextDay[YEAR].shouldEqualTo(now.year)
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR] + 1)
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.WEDNESDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(12)
        nextDay[MINUTE].shouldEqualTo(target.minute)
        nextDay[SECOND].shouldEqualTo(target.second)
        nextDay[MILLISECOND].shouldEqualTo(target.millisecond)
    }

    @Test
    fun `test next time same day earlier millisecond`() {
        val now = Calendar.getInstance()
        now.dayOfWeek = Calendar.WEDNESDAY
        now.hourOfDay = 12
        now.minute = 30
        now.second = 30
        now.millisecond = 30
        val target = Calendar.getInstance()
        target.hourOfDay = 12
        target.minute = 30
        target.second = 30
        target.millisecond = 25

        val nextDay = now.next(target.timeInMillis, Calendar.WEDNESDAY)

        nextDay[YEAR].shouldEqualTo(now.year)
        nextDay[WEEK_OF_YEAR].shouldEqualTo(now[WEEK_OF_YEAR] + 1)
        nextDay[DAY_OF_WEEK].shouldEqualTo(Calendar.WEDNESDAY)
        nextDay[HOUR_OF_DAY].shouldEqualTo(12)
        nextDay[MINUTE].shouldEqualTo(target.minute)
        nextDay[SECOND].shouldEqualTo(target.second)
        nextDay[MILLISECOND].shouldEqualTo(target.millisecond)
    }

    @Test
    fun `test next interval time in future`() {
        clock.time = 1000L
        val startTime = 2000L

        val result = Chronos.next(startTime)

        result.`should not be null`()
        result.timeInMillis.`should be equal to`(startTime)
    }

    @Test
    fun `test next interval time in past without interval`() {
        clock.time = 2000L
        val startTime = 1000L

        val result = Chronos.next(startTime)

        result.`should be null`()
    }

    @Test
    fun `test next interval time in past with interval`() {
        clock.time = 2000L
        val startTime = 1000L
        val interval = 600L

        val result = Chronos.next(startTime, intervalInMillis = interval)

        result.`should not be null`()
        result.timeInMillis.`should be equal to`(2200L)
    }

    @Test
    fun `test next interval time in past with interval and end date`() {
        clock.time = 2000L
        val startTime = 1000L
        val endTime = 2300L
        val interval = 600L

        val result = Chronos.next(startTime, endTime, interval)

        result.`should not be null`()
        result.timeInMillis.`should be equal to`(2200L)
    }

    @Test
    fun `test next interval time in past with interval and end date that can't be rescheduled`() {
        clock.time = 2000L
        val startTime = 1000L
        val endTime = 2100L
        val interval = 600L

        val result = Chronos.next(startTime, endTime, interval)

        result.`should be null`()
    }
}