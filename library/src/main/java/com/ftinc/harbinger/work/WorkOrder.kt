package com.ftinc.harbinger.work

import com.ftinc.harbinger.util.support.PersistableBundleCompat
import org.threeten.bp.*


/**
 * An order of work to schedule weekly jobs
 *
 * @param id the id of the order/request
 * @param tag the tag of the order/request, used to find the WorkCreator
 * @param extras the extra data content related to this request
 *
 * @param startTime the start time during the [daysOfWeek] that this request should schedule for
 * @param endTime the end time when this work should not execute. If set, exact will be set to true
 *
 * @param daysOfWeek the days of the week that this work order should execute on
 * @param interval the interval duration that this weekly order should repeat
 */
data class WorkOrder(
    val id: Int,
    val tag: String,
    val extras: PersistableBundleCompat,

    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime?,

    val daysOfWeek: Set<DayOfWeek>,
    val interval: Duration?
) {

    private constructor(builder: Builder): this(builder.id, builder.tag, builder.extras, builder.startTime!!,
        builder.endTime, builder.days, builder.interval)

    class Builder(val tag: String, val id: Int) {
        var extras: PersistableBundleCompat = PersistableBundleCompat()
        var startTime: OffsetDateTime? = null
        var endTime: OffsetDateTime? = null
        var days: Set<DayOfWeek> = emptySet()
        var interval: Duration? = null

        fun extras(builder: Extras.() -> Unit) {
            extras = Extras().apply(builder).build()
        }

        fun build(): WorkOrder {
            require(startTime != null) { "startTime must be greater than 0" }
            require(startTime?.offset == ZoneOffset.UTC) { "startTime must be offset to UTC time" }
            require(!(endTime != null && endTime!!.offset != ZoneOffset.UTC)) { "endTime must be offset to UTC time" }
            require(!(endTime != null && startTime!!.isAfter(endTime!!))) { "startTime MUST be BEFORE endTime" }
            require(!(days.isEmpty() && interval != null && interval!!.toMillis() < MIN_INTERVAL)) { "Interval must be greater than 15 minutes when day is null" } /* disabled for testing */
            require(!(days.isNotEmpty() && interval == null)) { "You MUST set an interval if you have set a day" }
            require(!(days.isNotEmpty() && interval != null && interval!!.toDays() % 7 != 0L)) { "You MUST set an interval that is a multiple of 1 week in milliseconds if you have set a day" }
            return WorkOrder(this)
        }
    }

    companion object {
        const val NO_DAY = -1
        const val NO_ID = -1
        const val DEAD_ID = -2
        const val MIN_INTERVAL = 900000L

        const val KEY_ID = "com.ftinc.harbinger.work.ID"
        const val KEY_DAY = "com.ftinc.harbinger.work.DAY_OF_WEEK"
        const val KEY_TIME = "com.ftinc.harbinger.work.ISO_TIME"
    }
}

fun workOrder(tag: String, id: Int, builder: WorkOrder.Builder.() -> Unit): WorkOrder {
    val b = WorkOrder.Builder(tag, id)
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
