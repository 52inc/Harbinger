package com.ftinc.harbinger

import android.support.v4.app.NotificationManagerCompat


class NotificationChannelBuilder(
    var name: String = "",
    var description: String = "",
    var importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
    var enableVibration: Boolean? = null,
    val group: String? = null,
    val lightColor: Int? = null,
    val showBadge: Boolean? = null
)