package com.ftinc.harbinger.util

import com.ftinc.harbinger.util.extensions.*
import com.ftinc.harbinger.work.WorkOrder
import java.util.*
import java.util.Calendar.*


object Chronos {

    /**
     * @see [Calendar.next]
     */
    fun next(day: Int, hour: Int): Calendar {
        val now = Calendar.getInstance()
        return now.next(day, hour)
    }

    /**
     * Get a Calendar setup to the next [day] at the given [hour].
     * @param day The next [Calendar.DAY_OF_WEEK] field that you want to get
     * @param hour The hour in the next day that you want.
     */
    fun Calendar.next(day: Int, hour: Int): Calendar {
        // Make a copy as to not distort the original object
        val now = this.clone() as Calendar

        // Setup Date
        val dow = now.dayOfWeek
        when {
            dow < day -> now.dayOfWeek = day
            dow > day -> now.add(DAY_OF_WEEK, (day - dow) + 7)
            dow == day -> if (now.hourOfDay >= hour) {
                 now.add(DAY_OF_WEEK, 7)
            }
        }

        // Setup Time
        now.hourOfDay = hour
        now.minute = 0
        now.second = 0
        now.millisecond = 0

        return now
    }

    /**
     * @see [Calendar.next]
     */
    fun next(timeInMillis: Long, day: Int): Calendar {
        val now = Calendar.getInstance()
        return now.next(timeInMillis, day)
    }

    /**
     * Get a [Calendar] setup to the next [day] per the given target [timeInMillis]
     * @param timeInMillis the target date/time of the next [day] you want
     * @param day the next [Calendar.DAY_OF_WEEK] field that you want to get
     */
    fun Calendar.next(timeInMillis: Long, day: Int): Calendar {
        val target = getInstance()
        target.timeInMillis = timeInMillis
        val now = this.clone() as Calendar

        val dow = now.dayOfWeek
        when {
            dow < day -> now.dayOfWeek = day
            dow > day -> now.add(DAY_OF_WEEK, (day - dow) + 7)
            dow == day -> {
                when {
                    now.hourOfDay > target.hourOfDay -> now.add(DAY_OF_WEEK, 7)
                    now.hourOfDay == target.hourOfDay && now.minute > target.minute -> now.add(DAY_OF_WEEK, 7)
                    now.hourOfDay == target.hourOfDay && now.minute == target.minute && now.second > target.second -> now.add(DAY_OF_WEEK, 7)
                    now.hourOfDay == target.hourOfDay && now.minute == target.minute && now.second == target.second && now.millisecond > target.millisecond -> now.add(DAY_OF_WEEK, 7)
                }
            }
        }

        now.hourOfDay = target.hourOfDay
        now.minute = target.minute
        now.second = target.second
        now.millisecond = target.millisecond

        return now
    }

    fun next(startTimeInMillis: Long, endTimeInMillis: Long? = null, intervalInMillis: Long? = null): Calendar? {
        val now = Calendar.getInstance()
        return now.next(startTimeInMillis, endTimeInMillis, intervalInMillis)
    }

    /**
     * Get a [Calendar] setup to the next start time given an end time and interval optionally.
     * @param startTimeInMillis the time/date that the next schedule should execute
     * @param endTimeInMillis the end date of the schedule that can't be scheduled past, null if it repeats infinitely
     * @param intervalInMillis the interval between schedules, null if it's just a one-shot
     * @return the [Calendar] set to the next (if possible) schedule time. null if next possible time isn't valid
     */
    fun Calendar.next(startTimeInMillis: Long, endTimeInMillis: Long? = null, intervalInMillis: Long? = null): Calendar? {
        val now = this.clone() as Calendar
        if (startTimeInMillis > System.currentTimeMillis()) {
            // The target datetime is still in the future, schedule for that
            now.timeInMillis = startTimeInMillis
        } else if (intervalInMillis != null) {

            // Calculate the next valid work time based on interval
            var newTimeInMillis = startTimeInMillis
            while (newTimeInMillis < System.currentTimeMillis()) {
                newTimeInMillis += intervalInMillis
            }

            // The target datetime is in the past, apply interval if possible
            if (endTimeInMillis == null) {
                // There is no end date, just apply
                now.timeInMillis = newTimeInMillis
            } else {
                if (newTimeInMillis < endTimeInMillis) {
                    now.timeInMillis = newTimeInMillis
                } else {
                    // The adjusted new time of this recurring, but limited, order would be past expiration
                    return null
                }
            }
        } else {
            // This is a dead work order, delete...
            return null
        }

        return now
    }
}