package com.ftinc.harbinger.storage

import android.content.Context
import com.ftinc.harbinger.Database
import com.ftinc.harbinger.util.extensions.isoTime
import com.ftinc.harbinger.util.extensions.offsetDateTime
import com.ftinc.harbinger.work.WorkOrder
import com.ftinc.harbinger.util.support.PersistableBundleCompat
import com.ftinc.harbinger.work.EventType
import com.ftinc.harbinger.work.OrderEvent
import com.squareup.sqldelight.android.AndroidSqliteDriver
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter


class DatabaseWorkStorage(context: Context) : WorkStorage {

    private val database by lazy {
        val driver = AndroidSqliteDriver(Database.Schema, context, DB_NAME)
        Database(driver)
    }

    override suspend fun insert(order: WorkOrder) {
        insertWorkOrder(order)
    }

    override suspend fun insert(orders: List<WorkOrder>) {
        database.transaction {
            orders.forEach { order ->
                insertWorkOrder(order)
            }
        }
    }

    override suspend fun insert(event: OrderEvent) {
        database.workEventsQueries.insert(
            event.workId,
            event.scheduledTime.isoTime(),
            event.deliveredTime.isoTime(),
            event.dayOfWeek?.value,
            event.event.ordinal,
            event.eventDescription
        )
    }

    override suspend fun find(id: Int): WorkOrder? {
        return database.workQueries.forJobId(id, workMapper).executeAsOneOrNull()
    }

    override suspend fun getAll(): List<WorkOrder> {
        return database.workQueries.selectAll(workMapper).executeAsList()
    }

    override suspend fun getAllEvents(workId: Int): List<OrderEvent> {
        return database.workEventsQueries.forWorkId(workId, orderEventMapper).executeAsList()
    }

    override suspend fun delete(id: Int) {
        database.workQueries.deleteByJobId(id)
    }

    override suspend fun delete(ids: List<Int>) {
        database.workQueries.deleteByMultipleJobIds(ids)
    }

    override suspend fun deleteAll() {
        database.workQueries.deleteAll()
    }

    private fun insertWorkOrder(order: WorkOrder) {
        database.workQueries.insert(
            order.id,
            order.tag,
            order.extras.saveToXml(),
            order.startTime.isoTime(),
            order.endTime?.isoTime(),
            order.daysOfWeek.compress(),
            order.interval?.seconds,
            order.interval?.nano
        )
    }

    companion object {
        private const val DB_NAME = "harbinger_work_orders_v2.db"
        private const val DAY_OF_WEEK_SEPARATOR = ","

        private val workMapper = { _: Long, work_id: Int, tag: String, extras: String, start_time: String, end_time: String?, days_of_week: String?, interval_seconds: Long?, interval_nano_adjustment: Int? ->
            val interval = if (interval_seconds != null && interval_nano_adjustment != null) {
                Duration.ofSeconds(interval_seconds, interval_nano_adjustment.toLong())
            } else {
                null
            }
            WorkOrder(
                work_id,
                tag,
                PersistableBundleCompat.fromXml(extras),
                OffsetDateTime.parse(start_time, DateTimeFormatter.ISO_TIME),
                OffsetDateTime.parse(end_time, DateTimeFormatter.ISO_TIME),
                days_of_week.decompress(),
                interval
            )
        }

        private val orderEventMapper = {_: Long, work_id: Int, scheduled_time: String, delivered_time: String, day_of_week: Int?, event: Int, event_description: String? ->
            OrderEvent(
                work_id,
                scheduled_time.offsetDateTime(),
                delivered_time.offsetDateTime(),
                day_of_week?.let { DayOfWeek.of(it) },
                EventType.of(event),
                event_description
            )
        }

        private fun Set<DayOfWeek>.compress(): String {
            return joinToString(DAY_OF_WEEK_SEPARATOR) { it.value.toString() }
        }

        private fun String?.decompress(): Set<DayOfWeek> {
            return if (this != null) {
                val parts = split(DAY_OF_WEEK_SEPARATOR)
                parts.mapNotNull {
                    it.toIntOrNull()?.let { value ->
                        if (value in 1..7) {
                            DayOfWeek.of(value)
                        } else {
                            null
                        }
                    }
                }.toSet()
            } else {
                emptySet()
            }
        }
    }
}
