package com.aai.selfie.parser

import com.aai.core.processManager.dataparser.OSPDataParser
import com.aai.core.processManager.model.OSPResponse
import com.aai.core.processManager.model.OSPSessionToken
import com.aai.core.utils.hasAndNotNull
import org.json.JSONObject

class SessionTokenParser: OSPDataParser {

    val ospResponse = OSPResponse()

    override fun parse(response: String) {
        val sessionToken = OSPSessionToken()
        val jsonObject = JSONObject(response)
        if (jsonObject.hasAndNotNull("sessionToken")) {
            sessionToken.sessionToken = jsonObject.getString("sessionToken")
        }
        if (jsonObject.hasAndNotNull("success")) {
            sessionToken.success = jsonObject.getBoolean("success")
        }
        ospResponse.data = sessionToken
    }

}