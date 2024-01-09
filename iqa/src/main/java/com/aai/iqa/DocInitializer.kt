package com.aai.iqa

import android.content.Context
import com.aai.core.Initializer
import com.aai.core.network.HttpUrlConnectionClient
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.network.NetWorkCallback
import com.aai.core.processManager.model.UrlConst
import com.aai.core.utils.OSPLog
import com.aai.core.utils.hasAndNotNull
import com.microblink.blinkid.MicroblinkSDK
import org.json.JSONObject

class DocInitializer : Initializer {

    override var isInitialized: Boolean = false

    override fun init(context: Context, key: String, sdkToken: String?) {
        getLicense(context, key, sdkToken)
    }

    private fun getLicense(context: Context, key: String, sdkToken: String?) {
        // 接口请求，拿到license后，初始化SDK
        val client = HttpUrlConnectionClient.instance
        val request = NetRequest(
            url = UrlConst.DOCUMENT_VERIFICATION_LICENSE_URL,
            method = NetMethod.GET,
            queryParameters = mutableMapOf(
                "key" to key,
                "os" to "android",
                "sdkToken" to (sdkToken ?: ""),
            )
        )
        client.sendRequest(request, object : NetWorkCallback {
            override fun onSuccess(response: String) {
                OSPLog.log("doc license = $response")
                val json = JSONObject(response)
                if (json.hasAndNotNull("data")) {
                    val license = json.getString("data")
                    isInitialized = true
                    initMicroBlink(context, license)
                } else {
                    isInitialized = false
                }
            }

            override fun onError(code: String, message: String) {
                isInitialized = false
            }
        })
    }

    private fun initMicroBlink(context: Context, license: String) {
        try {
            MicroblinkSDK.setLicenseKey(license, context)
        } catch (e: Throwable) {
            e.printStackTrace()
            isInitialized = false
        }
    }

}