package com.ftinc.harbinger

import com.ftinc.harbinger.logging.WorkLogger

class TestLogger : WorkLogger {
    override fun log(priority: Int, tag: String?, message: String?, throwable: Throwable?) {
        println(message)
    }
}
