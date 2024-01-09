package com.aai.selfie.trheed

import android.content.Intent
import android.os.Bundle
import com.aai.core.OSPSdk
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.network.NetWorkCallback
import com.aai.core.network.OSPRequestBody
import com.aai.core.processManager.model.BundleConst
import com.aai.core.processManager.model.UrlConst
import com.aai.core.utils.OSPLog
import com.aai.core.utils.hasAndNotNull
import com.aai.core.utils.textWithKey
import com.aai.selfie.parser.SelfieVerificationConfigParser
import com.aai.selfie.processors.Config
import org.json.JSONObject

/**
 * nodeCode == SELFIE_VERIFICATION
 */
class Selfie3DVerificationViewModel : Base3DViewModel<SelfieVerificationConfigParser>() {

    override fun createParser(): SelfieVerificationConfigParser = SelfieVerificationConfigParser()

    override fun initData(bundle: Bundle) {
        super.initData(bundle)
        val dataJson = JSONObject(data)
        if (dataJson.hasAndNotNull("attemptsRemain")) {
            maxRetry = dataJson.getInt("attemptsRemain")
        }
        selfieResultHandler.maxRetry = maxRetry
        val pages = configParser.ospSelfieConfig.pages
        selfieResultHandler.successKey =
            pages.successPage.subTitle ?: ""
        selfieResultHandler.failedKey = pages.failurePage.subTitle ?: ""
        OSPLog.log(
            "failedKey = ${selfieResultHandler.failedKey}, text = ${
                textWithKey(
                    selfieResultHandler.failedKey,
                    ""
                )
            }"
        )
        val customization = Config.retrieveConfigurationWizardCustomization()
        customization.guidanceCustomization.retryScreenSubtextAttributedString =
            textWithKey(configParser.ospSelfieConfig.pages.retryPage.subTitle, "")
//        customization.guidanceCustomization.retryScreenSubtextAttributedString =
//            OSPSdk.instance.context.getString(com.aai.core.R.string.retry_times, maxRetry)
    }

    override fun commitUrl(): String = UrlConst.SELFIE_VERIFICATION_COMMIT_URL

    override fun createRetryHandler(): SelfieResultHandler = SelfieVerificationResultHandler()

    override fun addExtraForCameraIntent(intent: Intent) {
        val enableCameraPage = configParser.ospSelfieConfig.pages.enableCameraPage
        intent.putExtra(BundleConst.TITLE_KEY, enableCameraPage.headerTitle)
        intent.putExtra(BundleConst.CONTENT_KEY, enableCameraPage.content)
        intent.putExtra(BundleConst.BUTTON_KEY, enableCameraPage.button)
        intent.putExtra(BundleConst.NODE_CODE, nodeCode)
    }

    override fun record(response: JSONObject?, shouldCommit: Boolean) {
        OSPSdk.instance.ospProcessorManager?.showLoading()
        val map = mutableMapOf<String, Any?>()
        val data: String? = response?.toString()
        val result = if (response == null) "FAILED" else response.optString("ospResult")
        val failedMsg =
            if (result == "FAILED") textWithKey("selfie_3d_error", "selfie_3d_error") else null
        map["image"] =
            livenessCheckProcessor?.sessionResult?.lowQualityAuditTrailCompressedBase64?.get(0)
        map["result"] = result
        map["data"] = data
        map["failedMsg"] = failedMsg
        map["startTime"] = startTime
        map["endTime"] = endTime
        val request = NetRequest(
            url = UrlConst.RETRY_RECORD,
            method = NetMethod.POST,
            requestBody = OSPRequestBody.OSPJsonRequestBody(JSONObject(map).toString()),
            queryParameters = mutableMapOf("sdkToken" to sdkToken)
        )
        client.sendRequest(request, object : NetWorkCallback {
            override fun onSuccess(response: String) {
                OSPLog.log("recordSuccess")
                if (shouldCommit) commit(true)
            }

            override fun onError(code: String, message: String) {
                OSPLog.log("recordError: $message")
            }
        })
    }

    override fun onCommitSuccess() {
        super.onCommitSuccess()
        selfieResultHandler.commitSuccess()
    }

}