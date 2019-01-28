package com.ftinc.harbinger.logging


class SilentLogger : JobLogger {

    override fun log(priority: Int, tag: String?, message: String?, throwable: Throwable?) {
        // Do Nothing
    }
}