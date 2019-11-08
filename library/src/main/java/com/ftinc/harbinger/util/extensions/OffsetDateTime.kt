package com.ftinc.harbinger.util.extensions

import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

fun OffsetDateTime.isoTime(): String = format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
