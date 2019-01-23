package com.ftinc.harbinger.util

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
        val dow = now[DAY_OF_WEEK]
        when {
            dow < day -> now[DAY_OF_WEEK] = day
            dow > day -> now.add(DAY_OF_WEEK, (day - dow) + 7)
            dow == day -> if (now[HOUR_OF_DAY] >= hour) {
                 now.add(DAY_OF_WEEK, 7)
            }
        }

        // Setup Time
        now[HOUR_OF_DAY] = hour
        now[MINUTE] = 0
        now[SECOND] = 0
        now[MILLISECOND] = 0

        return now
    }
}