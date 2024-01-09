package com.aai.selfie.processors

import android.content.Context
import com.aai.core.network.HttpUrlConnectionClient
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.network.NetWorkCallback
import com.aai.core.network.OSPRequestBody
import com.aai.core.processManager.dataparser.OSPDataParser
import com.aai.core.processManager.model.ResponseCode
import com.aai.core.processManager.model.UrlConst
import com.aai.core.utils.OSPLog
import com.aai.core.utils.showToast
import com.aai.core.utils.showToastByCode
import com.aai.core.utils.textWithKey
import com.aai.selfie.SelfieNode
import com.aai.selfie.trheed.Base3DViewModel
import com.facetec.sdk.FaceTecFaceScanProcessor
import com.facetec.sdk.FaceTecFaceScanResultCallback
import com.facetec.sdk.FaceTecSDK
import com.facetec.sdk.FaceTecSessionActivity
import com.facetec.sdk.FaceTecSessionResult
import com.facetec.sdk.FaceTecSessionStatus
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class LivenessCheckProcessor(
    sessionToken: String?,
    private val sdkToken: String,
    private val context: Context,
    private val viewModel: Base3DViewModel<out OSPDataParser>,
) :
    Processor(), FaceTecFaceScanProcessor {
    private var success = false
    var sessionResult: FaceTecSessionResult? = null
    private var faceScanResultCallback: FaceTecFaceScanResultCallback? = null

    init {
        FaceTecSessionActivity.createAndLaunchSession(
            context,
            this@LivenessCheckProcessor,
            sessionToken
        )
    }

    override fun processSessionWhileFaceTecSDKWaits(
        sessionResult: FaceTecSessionResult,
        faceScanResultCallback: FaceTecFaceScanResultCallback
    ) {
        faceScanResultCallback.uploadMessageOverride(
            textWithKey(
                "still_uploading",
                "FaceTec_result_facescan_upload_message"
            )
        )
        this.faceScanResultCallback = faceScanResultCallback
        // 用户主动退出会调用
        this.sessionResult = sessionResult
        if (sessionResult.status != FaceTecSessionStatus.SESSION_COMPLETED_SUCCESSFULLY) {
            OSPLog.log(
                "processSessionWhileFaceTecSDKWaits failed: status = ${sessionResult.status}",
                SelfieNode.TAG
            )
            faceScanResultCallback.cancel()
            return
        }

        val parameters = JSONObject()
        try {
            parameters.put("faceScan", sessionResult.faceScanBase64)
            parameters.put("auditTrailImage", sessionResult.auditTrailCompressedBase64[0])
            parameters.put(
                "lowQualityAuditTrailImage",
                sessionResult.lowQualityAuditTrailCompressedBase64[0]
            )
            parameters.put("sessionId", sessionResult.sessionId)
        } catch (e: JSONException) {
            e.printStackTrace()
            OSPLog.log("request sessionToken failed: ${e.message}", SelfieNode.TAG)
        }

        val netRequest = NetRequest(
            url = UrlConst.LIVENESS_3D,
            method = NetMethod.POST,
            requestBody = OSPRequestBody.OSPJsonRequestBody(json = parameters.toString()),
            headers = mutableMapOf(
                "Content-Type" to "application/json",
                "X-Device-Key" to sdkToken,
                "User-Agent" to
                        FaceTecSDK.createFaceTecAPIUserAgentString(sessionResult.sessionId),
                "X-User-Agent" to
                        FaceTecSDK.createFaceTecAPIUserAgentString(sessionResult.sessionId)
            )
        )
        updateUploadProgress()
        HttpUrlConnectionClient.instance.sendRequest(
            netRequest,
            object : NetWorkCallback {
                override fun onSuccess(response: String) {
                    faceScanResultCallback.uploadProgress(1f)
                    parserUploadResult(response, faceScanResultCallback)
                    timer?.cancel()
                }

                override fun onError(code: String, message: String) {
                    context?.let {
                        showToastByCode(it, "", it.getString(com.aai.core.R.string.net_work_error))
                    }
                    faceScanResultCallback.cancel()
                    timer?.cancel()
                }
            }
        )
    }

    /**
     * 模拟上传进度
     */
    var timer: Timer? = null
    private fun updateUploadProgress() {
        timer = Timer()
        val period = 100L
        var count = 0

        val task = object : TimerTask() {
            override fun run() {
                count++
                faceScanResultCallback?.uploadProgress(count.toFloat() / 100)
            }
        }
        timer?.scheduleAtFixedRate(task, 0, period)
    }

    private fun parserUploadResult(
        response: String,
        faceScanResultCallback: FaceTecFaceScanResultCallback
    ) {
        try {
            val responseJSON = JSONObject(response)
            val code = responseJSON.optString("code")
            OSPLog.log("upload selfie image response: $response", SelfieNode.TAG)
            if (code == ResponseCode.CODE_SUCCESS) {
                val data = responseJSON.optJSONObject("data")?.toString() ?: ""
                val isSuccess = viewModel.selfieResultHandler.handleResult(
                    context,
                    data,
                    faceScanResultCallback
                )
                val endTime = viewModel.getCurrentISO8601FormattedTime(Calendar.getInstance().time)
                viewModel.endTime = endTime
                viewModel.record(responseJSON.optJSONObject("data"), viewModel.selfieResultHandler.shouldCommit)
                viewModel.startTime = endTime
            } else {
                val message = responseJSON.optString("message")
                context?.let {
                    showToast(it, it.getString(com.aai.core.R.string.net_work_error))
                }
                faceScanResultCallback.cancel()
            }
        } catch (e: JSONException) {
            // CASE:  Parsing the response into JSON failed --> You define your own API contracts with yourself and may choose to do something different here based on the error.  Solid server-side code should ensure you don't get to this case.
            e.printStackTrace()
            OSPLog.log("upload selfie image failed: ${e.message}", SelfieNode.TAG)
            faceScanResultCallback.cancel()
        }
    }

    override fun isSuccess(): Boolean {
        return success
    }
}