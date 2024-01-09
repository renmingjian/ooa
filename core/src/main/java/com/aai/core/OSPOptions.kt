package com.aai.core

import android.content.Context

data class OSPOptions(
    val context: Context,
    // 用户初始化，后端校验的key，如果key校验不过，不允许使用SDK。 TODO 目前后端还没有设置该key
    val key: String,
    val sdkToken: String,
    val processCallback: OSPProcessCallback? = null,
    val openLog: Boolean = true,
)
