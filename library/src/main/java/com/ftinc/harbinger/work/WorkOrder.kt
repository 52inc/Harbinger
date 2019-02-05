package com.ftinc.harbinger.work

import com.ftinc.harbinger.util.support.PersistableBundleCompat
import com.ftinc.harbinger.util.extensions.isCalendarDay
import java.lang.IllegalArgumentException
import java.util.Calendar


/**
 * An order of work to schedule weekly jobs
 * @param id the id of the order/request
 * @param tag the tag of the order/request, used to find the WorkCreator
 * @param extras the extra data content related to this request
 * @param startTimeInMillis the start time during the [day] that this request should schedule for
 * @param endTimeInMillis the end time when this work should not execute. If set, exact will be set to true
 * @param day the day of the week that this work order should execute on
 * @param exact whether or not this order needs to be filled at the exact [startTimeInMillis], or if it can allow for doze/standby
 * @param intervalInMillis the interval in ms that this weekly order should repeat
 */
data class WorkOrder(
    val id: Int,
    val tag: String,
    val extras: PersistableBundleCompat,

    val startTimeInMillis: Long,
    val endTimeInMillis: Long?,
    val exact: Boolean,
    val day: Int?,
    val intervalInMillis: Long?
) {

    private constructor(builder: Builder): this(builder.id, builder.tag, builder.extras, builder.startTimeInMillis,
        builder.endTimeInMillis, builder.exact, builder.day, builder.intervalInMillis)


    class Builder(val tag: String) {
        var id: Int = NO_ID
        var extras: PersistableBundleCompat = PersistableBundleCompat()
        var startTimeInMillis: Long = 0L
        var endTimeInMillis: Long? = null
        var day: Int? = null
        var exact: Boolean = false
        var intervalInMillis: Long? = null

        fun extras(builder: Extras.() -> Unit) {
            extras = Extras().apply(builder).build()
        }

        fun build(): WorkOrder {
            if (startTimeInMillis == 0L) throw IllegalArgumentException("Start Time must be greater than 0")
            if (endTimeInMillis != null && startTimeInMillis > endTimeInMillis!!) throw IllegalArgumentException("Start time MUST be BEFORE end time")
            if (day?.isCalendarDay() == false) throw IllegalArgumentException("'day' must be one of the Calendar.DAY_OF_WEEK values")
            if (day == null && intervalInMillis != null && intervalInMillis!! < MIN_INTERVAL) throw IllegalArgumentException("Interval must be greater than 15 minutes when day is null") /* disabled for testing */
            if (day != null && intervalInMillis == null) throw IllegalArgumentException("You MUST set an interval if you have set a day")
            if (day != null && intervalInMillis != null && intervalInMillis!! % WEEK_INTERVAL != 0L) throw IllegalArgumentException("You MUST set an interval that is a multiple of 1 week in milliseconds if you have set a day")

            // If end time is specified, this is automatically set to exact
            if (endTimeInMillis != null || intervalInMillis == null) {
                exact = true
            }

            return WorkOrder(this)
        }
    }

    companion object {
        const val NO_ID = -1
        const val DEAD_ID = -2
        const val MIN_INTERVAL = 900000L
        const val WEEK_INTERVAL = 604800000L

        const val KEY_ID = "com.ftinc.harbinger.work.ID"
        const val KEY_TIME = "com.ftinc.harbinger.work.TIME"
    }
}

fun workOrder(tag: String, builder: WorkOrder.Builder.() -> Unit): WorkOrder {
    val b = WorkOrder.Builder(tag)
    b.builder()
    return b.build()
}

class Extras {
    private val bundle = PersistableBundleCompat()

    infix fun String.to(value: PersistableBundleCompat) = bundle.putPersistableBundleCompat(this, value)
    infix fun String.to(value: Boolean) = bundle.putBoolean(this, value)
    infix fun String.to(value: Int) = bundle.putInt(this, value)
    infix fun String.to(value: Long) = bundle.putLong(this, value)
    infix fun String.to(value: Double) = bundle.putDouble(this, value)
    infix fun String.to(value: String) = bundle.putString(this, value)
    infix fun String.to(value: LongArray) = bundle.putLongArray(this, value)
    infix fun String.to(value: IntArray) = bundle.putIntArray(this, value)
    infix fun String.to(value: DoubleArray) = bundle.putDoubleArray(this, value)
    infix fun String.to(value: Array<String>) = bundle.putStringArray(this, value)

    internal fun build(): PersistableBundleCompat = bundle
}