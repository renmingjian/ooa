package com.aai.core.utils

import android.util.Log
import com.aai.core.OSPSdk

class OSPLog {

    companion object {
        fun log(log: String, tag: String? = null) {
            val sdk = OSPSdk.instance
            if (sdk.openLog) {
                Log.d(OSPSdk.TAG + (tag ?: ""), log)
            }
        }
    }

}