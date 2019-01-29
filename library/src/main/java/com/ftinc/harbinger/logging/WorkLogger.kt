package com.ftinc.harbinger.logging

import android.util.Log


interface WorkLogger {

    val explicitTag: ThreadLocal<String>
        get() = ThreadLocal()

    fun getTag(): String? {
        val tag = explicitTag.get()
        if (tag != null) {
            explicitTag.remove()
        }
        return tag
    }

    fun v(message: String?, throwable: Throwable? = null) = log(Log.VERBOSE, getTag(), message, throwable)
    fun a(message: String?, throwable: Throwable? = null) = log(Log.ASSERT, getTag(), message, throwable)
    fun d(message: String?, throwable: Throwable? = null) = log(Log.DEBUG, getTag(), message, throwable)
    fun i(message: String?, throwable: Throwable? = null) = log(Log.INFO, getTag(), message, throwable)
    fun w(message: String?, throwable: Throwable? = null) = log(Log.WARN, getTag(), message, throwable)
    fun e(message: String?, throwable: Throwable? = null) = log(Log.ERROR, getTag(), message, throwable)


    fun log(priority: Int, tag: String?, message: String?, throwable: Throwable?)
}