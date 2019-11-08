package com.ftinc.harbinger.util.extensions

import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

fun String.offsetDateTime(): OffsetDateTime = OffsetDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
