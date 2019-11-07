package com.ftinc.harbinger.util

import androidx.annotation.VisibleForTesting
import org.threeten.bp.*
import org.threeten.bp.temporal.TemporalAdjusters

/**
 * Helper function to find next available dates using the ThreeTenBP library
 */
object Khronos {

    interface TimeSpace {
        fun now(): OffsetDateTime

        companion object {
            val systemDefault = object : TimeSpace {
                override fun now(): OffsetDateTime {
                    return OffsetDateTime.now(ZoneOffset.UTC)
                }
            }
        }
    }

    @VisibleForTesting
    internal var timeSpace: TimeSpace = TimeSpace.systemDefault

    /**
     * Find the next [OffsetDateTime] for a provided [time] on the next or same [dayOfWeek]
     * If the scheduled [time] is before the current time and the current day of the week equals [dayOfWeek]
     * then return the NEXT [OffsetDateTime] for the next occurrence of the [dayOfWeek]
     */
    fun next(time: OffsetDateTime, dayOfWeek: DayOfWeek): OffsetDateTime {
        val moment = timeSpace.now()
        val adjustedMoment = moment.with(TemporalAdjusters.nextOrSame(dayOfWeek))

        // If Date is same
        // If current Time is before scheduled
        return if (adjustedMoment.toOffsetTime().isBefore(time.toOffsetTime()) &&
            moment.toLocalDate().isEqual(adjustedMoment.toLocalDate())) {
            // if the current time is before the intended schedule time and today is the same
            // date as the adjusted time (to the day of week) then schedule to the adjusted moment
            time.with(moment.toLocalDate())

        // If Date is not the same
        // OR time is after scheduled
        } else {
            // Otherwise adjust the target datetime to today's local date, then adjust it to the next
            // day of the week
            time.with(moment.toLocalDate())
                .with(TemporalAdjusters.next(dayOfWeek))
        }
    }

    /**
     * Similar to [next] but accounts for the [previousScheduledTime] and minimum [interval]
     * duration between schedules
     */
    fun next(
        time: OffsetDateTime,
        dayOfWeek: DayOfWeek,
        previousScheduledTime: OffsetDateTime,
        interval: Duration
    ): OffsetDateTime {
        val moment = timeSpace.now()

        // Check if enough time has elapsed since the interval of the previous scheduled time
        val elapsedSincePrevious = Duration.between(previousScheduledTime, moment)
        return if (elapsedSincePrevious < interval) {
            // Now compute the earliest possible date that we could begin scheduling the next event
            val minimumScheduleTime = previousScheduledTime.plus(interval)
            minimumScheduleTime
                .with(TemporalAdjusters.nextOrSame(dayOfWeek))
                .with(time.toOffsetTime())
        } else {
            // Enough time HAS passed since the previous scheduled date and we should schedule for the
            // next available date for the given input
            next(time, dayOfWeek)
        }
    }

    fun next(time: OffsetDateTime, endTime: OffsetDateTime?, interval: Duration?): OffsetDateTime? {
        val moment = timeSpace.now()

        return if (time.isAfter(moment)) {
            // The target time is in the future, so just return itself
            time
        } else if (interval != null) {
            var nextDateTime = time
            while (nextDateTime.isBefore(moment)) {
                nextDateTime = nextDateTime.plus(interval)
            }

            if (endTime == null) {
                nextDateTime
            } else {
                if (nextDateTime.isBefore(endTime)) {
                    nextDateTime
                } else {
                    null
                }
            }
        } else {
            null
        }
    }
}
