package com.aai.core.crash

import com.aai.core.utils.OSPLog
import java.lang.Thread.UncaughtExceptionHandler

class CrashHandler {

    private val oldCrashHandler: UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    fun handleUnCaughtException() {
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            OSPLog.log("exception = ${e.message}")
            oldCrashHandler?.uncaughtException(t, e)
        }
    }

}