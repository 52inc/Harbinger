package com.ftinc.harbinger.util.extensions

import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun String.toDate(): Date {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(this)
}

fun String.fromISO8601(): Long {
    return this.toDate().time
}

fun Date.iso8601(): String {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(this)
}

fun Calendar.iso8601(): String {
    return this.time.iso8601()
}

fun Long.iso8601(): String {
    return Date(this).iso8601()
}

fun Int.isCalendarDay(): Boolean = (Calendar.SUNDAY..Calendar.SATURDAY).contains(this)

var Calendar.year by CalendarProperty(Calendar.YEAR)
var Calendar.month by CalendarProperty(Calendar.MONTH)
var Calendar.weekOfYear by CalendarProperty(Calendar.WEEK_OF_YEAR)
var Calendar.dayOfMonth by CalendarProperty(Calendar.DAY_OF_MONTH)
var Calendar.dayOfYear by CalendarProperty(Calendar.DAY_OF_YEAR)
var Calendar.dayOfWeek by CalendarProperty(Calendar.DAY_OF_WEEK)
var Calendar.hourOfDay by CalendarProperty(Calendar.HOUR_OF_DAY)
var Calendar.hour by CalendarProperty(Calendar.HOUR)
var Calendar.minute by CalendarProperty(Calendar.MINUTE)
var Calendar.second by CalendarProperty(Calendar.SECOND)
var Calendar.millisecond by CalendarProperty(Calendar.MILLISECOND)

internal class CalendarProperty(private val field: Int) : ReadWriteProperty<Calendar, Int> {

    override fun getValue(thisRef: Calendar, property: KProperty<*>): Int {
        return thisRef[field]
    }

    override fun setValue(thisRef: Calendar, property: KProperty<*>, value: Int) {
        thisRef[field] = value
    }
}