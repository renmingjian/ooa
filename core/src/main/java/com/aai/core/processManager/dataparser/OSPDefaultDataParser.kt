package com.aai.core.processManager.dataparser

import org.json.JSONObject

abstract class OSPDefaultDataParser : OSPDataParser {

    fun parseResponse(response: String): String {
        val jsonObject = JSONObject(response)
        return if (jsonObject.has("data")) jsonObject.get("data").toString() else response
    }
}