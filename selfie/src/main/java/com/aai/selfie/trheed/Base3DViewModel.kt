package com.aai.selfie.trheed

import android.content.Intent
import android.os.Bundle
import com.aai.core.OSPSdk
import com.aai.core.mvvm.BaseViewModel
import com.aai.core.network.ByteRequestBody
import com.aai.core.network.HttpUrlConnectionClient
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.network.NetWorkCallback
import com.aai.core.network.OSPRequestBody
import com.aai.core.node.LoadingCallback
import com.aai.core.processManager.dataparser.OSPDataParser
import com.aai.core.processManager.model.UrlConst
import com.aai.core.utils.OSPLog
import com.aai.selfie.SelfieNode
import com.aai.selfie.parser.SessionTokenParser
import com.aai.selfie.processors.LivenessCheckProcessor
import com.facetec.sdk.FaceTecSDK
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

abstract class Base3DViewModel<T : OSPDataParser> : BaseViewModel<T>() {

    var startTime = ""
    var endTime = ""

    var loadingCallback: LoadingCallback? = null
    val client = HttpUrlConnectionClient.instance
    lateinit var selfieResultHandler: SelfieResultHandler
    var livenessCheckProcessor: LivenessCheckProcessor? = null
    var maxRetry = 3
    var isSuccess = true

    override fun initData(bundle: Bundle) {
        super.initData(bundle)
        selfieResultHandler = createRetryHandler()
        selfieResultHandler.nodeCode = nodeCode
        selfieResultHandler.onResultCallback = object : OnResultCallback {
            override fun onResult(isSuccess: Boolean) {
                OSPLog.log("upload callback, start commit, isSuccess = $isSuccess", SelfieNode.TAG)
                this@Base3DViewModel.isSuccess = isSuccess
//                if (isSuccess) {
//                    commit(this@Base3DViewModel.isSuccess)
//                } else {
//                    OSPSdk.instance.ospProcessorManager?.endProcess()
//                }
                commit()
            }
        }
    }

    override fun getCommitRequest(): NetRequest =
        NetRequest(
            url = commitUrl(),
            method = NetMethod.POST,
            queryParameters = mutableMapOf("sdkToken" to sdkToken),
            requestBody = getRequestBody(),
        )

    private fun getRequestBody(): OSPRequestBody {
        if (isSuccess) {
            val map = mutableMapOf(
                "userAgent" to FaceTecSDK.createFaceTecAPIUserAgentString(null)
            )
            val json = JSONObject(map as Map<*, *>?)

            return OSPRequestBody.OSPMultiPartRequestBody(
                values = mutableListOf(
                    ByteRequestBody(
                        key = "input",
                        value = json.toString(),
                        fileName = "blob",
                        contentType = "application/json"
                    ),
                    ByteRequestBody(
                        key = "faceScan",
                        value = livenessCheckProcessor?.sessionResult?.faceScanBase64,
                        fileName = "faceScan.base64",
                        contentType = "text/plain"
                    ),
                    ByteRequestBody(
                        key = "facePhotoUri",
                        bytes = android.util.Base64.decode(
                            livenessCheckProcessor?.sessionResult?.auditTrailCompressedBase64?.get(
                                0
                            ), android.util.Base64.DEFAULT
                        ),
                        fileName = "xxx.jpg",
                        contentType = "application/octet-stream"
                    ),
                    ByteRequestBody(
                        key = "lowQualityAuditTrailImage",
                        bytes = android.util.Base64.decode(
                            livenessCheckProcessor?.sessionResult?.lowQualityAuditTrailCompressedBase64?.get(
                                0
                            ), android.util.Base64.DEFAULT
                        ),
                        fileName = "xxx.jpg",
                        contentType = "application/octet-stream"
                    )
                )
            )
        } else {
            return OSPRequestBody.OSPFormUrlRequestBody(mutableMapOf("transactionStatus" to "FAILED"))
        }
    }

    abstract fun commitUrl(): String

    fun getSessionToken() {
        loadingCallback?.onLoading()
        val request = NetRequest(
            url = UrlConst.SESSION_TOKEN,
            method = NetMethod.GET,
            headers = mutableMapOf(
                "X-Device-Key" to sdkToken,
                "User-Agent" to FaceTecSDK.createFaceTecAPIUserAgentString(""),
                "X-User-Agent" to FaceTecSDK.createFaceTecAPIUserAgentString("")
            ),
            queryParameters = mutableMapOf("sdkToken" to sdkToken)
        )
        client.sendRequest(request, object : NetWorkCallback {
            override fun onSuccess(response: String) {
                OSPLog.log("sessionTokenResult = $response")
                val parser = SessionTokenParser()
                parser.parse(response)
                loadingCallback?.onSuccess(parser.ospResponse)
            }

            override fun onError(code: String, message: String) {
                OSPLog.log("sessionTokenError = $message")
                loadingCallback?.onError(code, message)
            }
        })
    }

    abstract fun createRetryHandler(): SelfieResultHandler

    open fun record(response: JSONObject?, shouldCommit: Boolean = false) {

    }

    abstract fun addExtraForCameraIntent(intent: Intent)

    fun getCurrentISO8601FormattedTime(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(date)
    }
}