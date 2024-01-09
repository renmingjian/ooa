package com.aai.core.network

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

/**
 * Used to switch to the main thread after a successful network request
 */
class MainThreadExecutor : Executor {

    private val handler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        handler.post(command)
    }

}