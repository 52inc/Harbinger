package com.ftinc.harbinger.work

/**
 * An interface to define how a [Worker] is created. This is registered in [Harbinger] with an associated 'tag'
 * that is later referenced by a [com.ftinc.harbinger.work.WorkOrder]
 */
interface WorkCreator {

    fun createWorker(): Worker
}