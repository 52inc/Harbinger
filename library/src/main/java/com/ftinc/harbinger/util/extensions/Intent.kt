package com.ftinc.harbinger.util.extensions

import android.content.Intent
import com.ftinc.harbinger.work.WorkOrder
import org.threeten.bp.DayOfWeek
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

fun Intent.getOffsetDateTimeExtra(key: String): OffsetDateTime? {
    return getStringExtra(key)?.let { isoTime ->
        OffsetDateTime.parse(isoTime, DateTimeFormatter.ISO_TIME)
    }
}

fun Intent.getDayOfWeekExtra(key: String): DayOfWeek? {
    return getIntExtra(key, WorkOrder.NO_DAY).let { day ->
        if (day != WorkOrder.NO_DAY) {
            DayOfWeek.of(day)
        } else {
            null
        }
    }
}
