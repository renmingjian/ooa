package com.aai.selfie.trheed

import android.content.Context
import com.aai.core.OSPSdk
import com.aai.core.processManager.model.ProcessEvent
import com.aai.core.utils.OSPLog
import com.aai.selfie.SelfieNode
import com.facetec.sdk.FaceTecFaceScanResultCallback
import org.json.JSONObject

/**
 * FACE_PHOTO走3d完成后，成功页面显示FaceTec的成功页面
 */
class SelfieCaptureResultHandler : SelfieResultHandler() {

    override fun handle(
        context: Context,
        data: String,
        faceScanResultCallback: FaceTecFaceScanResultCallback?
    ): Boolean {
        val dataJson = JSONObject(data)

        // 1.如果wasProcessed = false，则直接取消FaceTec流程
        val wasProcessed = dataJson.optBoolean("wasProcessed")
        if (!wasProcessed) {
            faceScanResultCallback?.cancel()
            return false
        }

        // 2.进入成功或者重试页面，这取决于proceedToNextStep方法的返回值
        val scanResultBlob = dataJson.optString("scanResultBlob")
        val success = faceScanResultCallback?.proceedToNextStep(scanResultBlob)
        OSPLog.log("retry or success: isSuccess = $success, maxRetry = $maxRetry", SelfieNode.TAG)
        if (success == false) {
            // 3.如果重试次数已经用完，则进入自己的失败页面
            if (maxRetry <= 0) {
                OSPLog.log(
                    "Exceeded the maximum number of retries，go to failure page",
                    SelfieNode.TAG
                )
                toFailed(context)
                faceScanResultCallback.cancel()
                return false
            }
            // 4.设置重试页面的次数
            maxRetry--
            OSPSdk.instance.getProcessCallback()?.onEvent(
                eventName = ProcessEvent.EVENT_RETRY,
                params = mutableMapOf("source" to "selie")
            )
            return false
        } else {
            commit(true)
            return true
        }
    }

}