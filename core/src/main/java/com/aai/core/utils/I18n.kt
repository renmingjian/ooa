package com.aai.core.utils

import com.aai.core.OSPSdk

fun textWithKey(key: String?, assetsKey: String?, defaultValue: String = ""): String =
    OSPSdk.instance.ospProcessorManager?.submitParser?.getValueWithKey(key, assetsKey)
        ?: defaultValue

fun textWithKeyAndLocal(
    key: String?, assetsKey: String?, local: String, defaultValue: String = ""
): String =
    OSPSdk.instance.ospProcessorManager?.submitParser?.getValueWithKeyAndLocal(
        local,
        key,
        assetsKey
    ) ?: defaultValue

fun getLocal() = OSPSdk.instance.ospProcessorManager?.submitParser?.local ?: "en"