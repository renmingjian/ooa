package com.aai.core.processManager

import android.content.Context
import com.aai.core.processManager.model.OSPNodeData

interface ProcessInterceptor {

    fun interceptor(context: Context, nodeData: OSPNodeData): Boolean

}