package com.ftinc.harbinger.util

import com.ftinc.harbinger.util.extensions.*
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
}