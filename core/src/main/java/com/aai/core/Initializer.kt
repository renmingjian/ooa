package com.aai.core

import android.content.Context

interface Initializer {

    var isInitialized: Boolean

    fun init(context: Context, key: String, sdkToken: String? = null)

}