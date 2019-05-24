package com.ftinc.harbinger.storage

import com.ftinc.harbinger.work.WorkOrder


interface WorkStorage {

    suspend fun insert(order: WorkOrder)
    suspend fun insert(orders: List<WorkOrder>)
    suspend fun find(id: Int): WorkOrder?
    suspend fun getAll(): List<WorkOrder>
    suspend fun delete(id: Int)
    suspend fun delete(ids: List<Int>)
    suspend fun deleteAll()
}