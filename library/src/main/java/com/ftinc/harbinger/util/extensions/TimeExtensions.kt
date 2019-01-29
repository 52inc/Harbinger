package com.ftinc.harbinger.util.extensions

import java.util.concurrent.TimeUnit


fun Int.seconds(): Long = TimeUnit.SECONDS.toMillis(this.toLong())
fun Int.minutes(): Long = TimeUnit.MINUTES.toMillis(this.toLong())
fun Int.hours(): Long = TimeUnit.HOURS.toMillis(this.toLong())
fun Int.days(): Long = TimeUnit.DAYS.toMillis(this.toLong())
fun Int.weeks(): Long = TimeUnit.DAYS.toMillis((this * 7).toLong())