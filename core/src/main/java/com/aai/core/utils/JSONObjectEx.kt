package com.aai.core.utils

import org.json.JSONObject

fun JSONObject.hasAndNotNull(key: String?): Boolean =
    if (key.isNullOrEmpty()) false else has(key) && !isNull(key)