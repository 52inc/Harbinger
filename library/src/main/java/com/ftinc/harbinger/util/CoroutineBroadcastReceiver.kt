package com.ftinc.harbinger.util

import android.content.BroadcastReceiver
import com.ftinc.harbinger.Harbinger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlin.coroutines.CoroutineContext


abstract class CoroutineBroadcastReceiver : BroadcastReceiver(), CoroutineScope {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Harbinger.logger.e("Unknown error has occurred in Receiver", throwable)
    }

    override val coroutineContext: CoroutineContext
        get() = Harbinger.workExecutor.asCoroutineDispatcher() + coroutineExceptionHandler
}