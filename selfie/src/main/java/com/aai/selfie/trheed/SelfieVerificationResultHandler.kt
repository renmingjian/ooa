package com.aai.selfie.trheed

import android.content.Context
import com.aai.core.OSPSdk
import com.aai.core.processManager.model.ProcessEvent
import com.aai.core.utils.OSPLog
import com.aai.selfie.SelfieNode
import com.facetec.sdk.FaceTecFaceScanResultCallback
import org.json.JSONObject

/**
 * SELFIE_VERIFICATION当3d流程成功后，要跳转自己的成功页面，不使用FaceTec的成功页面
 */
class SelfieVerificationResultHandler : SelfieResultHandler() {

    override fun handle(
        context: Context,
        data: String,
        faceScanResultCallback: FaceTecFaceScanResultCallback?
    ): Boolean {
        val dataJson = JSONObject(data)
        OSPLog.log("upload selfie image handle: $dataJson", SelfieNode.TAG)
        // 1.如果wasProcessed = false，则直接取消FaceTec流程
        val wasProcessed = dataJson.optBoolean("wasProcessed")
        if (!wasProcessed) {
            OSPLog.log("wasProcessed is false, cancel selfie", SelfieNode.TAG)
            faceScanResultCallback?.cancel()
            return false
        }

        // 2.如果ospResult = "PASS"，进入自己的成功页面，不调用proceedToNextStep
        // faceScanResultCallback.proceedToNextStep(scanResultBlob)
        val ospResult = dataJson.optString("ospResult")
        if (ospResult == "PASS") {
            OSPLog.log("ospResult is PASS, cancel selfie and go to success page", SelfieNode.TAG)
            faceScanResultCallback?.cancel() // 销毁FaceTec的页面
            shouldCommit = false
//            toSuccess(context) // 进入自己的成功页面，先暂时去掉，设计改成不进入成功页面，直接继续流程
            commit(true)
            return true
        }

        // 先减1，因为重试次数包含当前这一次捕获，比如如果接口返回的重试次数为1，那么其实是不能重试的
        maxRetry--

        // 3.如果重试次数已经用完，则进入自己的失败页面
        if (maxRetry <= 0) {
            OSPLog.log("Exceeded the maximum number of retries，go to failure page", SelfieNode.TAG)
//            toFailed(context)
//            onResultCallback?.onResult(false)
            shouldCommit = true
            faceScanResultCallback?.cancel()
            return false
        }

        // 4.要进入重试页面，对重试次数进行减1操作，并通知后端接口也做修改
        OSPLog.log("retry, maxRetry = $maxRetry", SelfieNode.TAG)
        OSPSdk.instance.getProcessCallback()?.onEvent(
            eventName = ProcessEvent.EVENT_RETRY,
            params = mutableMapOf("source" to "selie")
        )
        val scanResultBlob = dataJson.optString("scanResultBlob")
        return faceScanResultCallback?.proceedToNextStep(scanResultBlob) ?: false
    }


}