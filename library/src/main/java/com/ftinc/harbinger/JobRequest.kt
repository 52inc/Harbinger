package com.ftinc.harbinger

import com.ftinc.harbinger.util.PersistableBundleCompat


data class JobRequest(
    val id: Int = -1,
    val tag: String? = null,
    val extras: PersistableBundleCompat,

    // Exact, or execution window
    val startTimeInMillis: Long,
    val endTimeInMillis: Long,
    val exact: Boolean = false,

    // Periodic
    val intervalInMillis: Long? = null
) {

    val isPeriodic: Boolean
        get() = intervalInMillis != null

    constructor(builder: Builder): this(builder.id, builder.tag, builder.extras, builder.startTimeInMillis,
        builder.endTimeInMillis, builder.exact, builder.intervalInMillis)

    class Builder(val tag: String) {
        var id: Int = -1
        var exact: Boolean = false
        var extras = PersistableBundleCompat()
        internal var startTimeInMillis: Long = -1L
        internal var endTimeInMillis: Long = -1L
        internal var intervalInMillis: Long? = null

        fun setExecutionWindow(startTimeInMillis: Long, endTimeInMillis: Long): Builder {
            this.startTimeInMillis = startTimeInMillis
            this.endTimeInMillis = endTimeInMillis
            return this
        }

        fun setExact(timeInMillis: Long): Builder{
            this.exact = true
            startTimeInMillis = timeInMillis
            endTimeInMillis = timeInMillis
            return this
        }

        fun setPeriodic(intervalInMillis: Long): Builder {
            this.intervalInMillis = intervalInMillis
            return this
        }

        fun setExtras(extras: PersistableBundleCompat): Builder {
            this.extras = extras
            return this
        }

        fun build(): JobRequest = JobRequest(this)
    }

    companion object {
        const val KEY_EXTRAS = "com.ftinc.harbinger.JOB_EXTRAS"
    }
}