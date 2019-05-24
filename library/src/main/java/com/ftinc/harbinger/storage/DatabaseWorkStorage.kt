package com.ftinc.harbinger.storage

import android.content.Context
import com.ftinc.harbinger.Database
import com.ftinc.harbinger.work.WorkOrder
import com.ftinc.harbinger.util.support.PersistableBundleCompat
import com.squareup.sqldelight.android.AndroidSqliteDriver


class DatabaseWorkStorage(context: Context) : WorkStorage {

    private val database by lazy {
        val driver = AndroidSqliteDriver(Database.Schema, context, DB_NAME)
        Database(driver)
    }

    override suspend fun insert(order: WorkOrder) {
        database.workQueries.insert(order.id, order.tag, order.extras.saveToXml(), order.startTimeInMillis,
            order.endTimeInMillis, order.day, if (order.exact) 1 else 0, order.intervalInMillis)
    }

    override suspend fun insert(orders: List<WorkOrder>) {
        database.transaction {
            orders.forEach { order ->
                database.workQueries.insert(order.id, order.tag, order.extras.saveToXml(), order.startTimeInMillis,
                    order.endTimeInMillis, order.day, if (order.exact) 1 else 0, order.intervalInMillis)
            }
        }
    }

    override suspend fun find(id: Int): WorkOrder? {
        return database.workQueries.forJobId(id, workMapper).executeAsOneOrNull()
    }

    override suspend fun getAll(): List<WorkOrder> {
        return database.workQueries.selectAll(workMapper).executeAsList()
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

    companion object {
        private const val DB_NAME = "harbinger_work_orders.db"

        private val workMapper = { _: Long, work_id: Int, tag: String, extras: String, start_time: Long, end_time: Long?, day: Int?, exact: Int, interval: Long? ->
            WorkOrder(
                work_id,
                tag,
                PersistableBundleCompat.fromXml(extras),
                start_time,
                end_time,
                exact == 1,
                day,
                interval
            )
        }
    }
}