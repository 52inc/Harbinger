package com.ftinc.harbinger.logging


class SilentLogger : WorkLogger {

    override fun log(priority: Int, tag: String?, message: String?, throwable: Throwable?) {
        // Do Nothing
    }
}