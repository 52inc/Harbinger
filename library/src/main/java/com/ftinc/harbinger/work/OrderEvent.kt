package com.ftinc.harbinger.work

import androidx.annotation.Keep
import org.threeten.bp.DayOfWeek
import org.threeten.bp.OffsetDateTime

/**
 * This class represents an event that fired off due to a scheduled [WorkOrder]
 */
data class OrderEvent(
    val workId: Int,
    val scheduledTime: OffsetDateTime,
    val deliveredTime: OffsetDateTime,
    val dayOfWeek: DayOfWeek?,
    val event: EventType,
    val eventDescription: String? = null
)

@Keep
enum class EventType {
    SUCCESS,
    NO_CREATOR,
    MISSING_WORK_ORDER,
    INVALID_ID;

    companion object {

        fun of(ordinal: Int): EventType {
            return values()[ordinal]
        }
    }
}
