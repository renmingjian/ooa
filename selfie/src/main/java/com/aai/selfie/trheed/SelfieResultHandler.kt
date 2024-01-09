package com.aai.selfie.trheed

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.aai.core.processManager.model.BundleConst
import com.aai.core.utils.OSPLog
import com.aai.core.utils.runOnUIThread
import com.aai.core.utils.textWithKey
import com.aai.selfie.state.SelfieStateActivity
import com.facetec.sdk.FaceTecFaceScanResultCallback

abstract class SelfieResultHandler {

    var nodeCode: String = ""
    var maxRetry: Int = 3
    var successKey = ""
    var failedKey = ""
    var onResultCallback: OnResultCallback? = null
    var faceScanResultCallback: FaceTecFaceScanResultCallback? = null

    // 做完FaceTec后，如果超出了重试次数，该值变为true，此值只有在超出重试次数时才会使用
    var shouldCommit = false

    fun handleResult(
        context: Context,
        data: String,
        faceScanResultCallback: FaceTecFaceScanResultCallback
    ): Boolean {
        this.faceScanResultCallback = faceScanResultCallback
        return handle(context, data, faceScanResultCallback)
    }

    abstract fun handle(
        context: Context,
        data: String,
        faceScanResultCallback: FaceTecFaceScanResultCallback?
    ): Boolean

    fun toSuccess(context: Context) {
        showPopup(context, successKey, true)
    }

    fun toFailed(context: Context) {
        OSPLog.log("toFailed")
        commit(false)
//        showPopup(context, failedKey, false)
    }

    private fun showPopup(context: Context, title: String, isSuccess: Boolean) {
        runOnUIThread {
            faceScanResultCallback?.cancel()
            val intent = Intent(context, SelfieStateActivity::class.java)
            intent.putExtra(BundleConst.NODE_CODE, nodeCode)
            intent.putExtra(BundleConst.TITLE_KEY, textWithKey(title, ""))
            OSPLog.log("title =, key = $title, text = ${textWithKey(title, "")}")
            intent.putExtra(BundleConst.IS_SUCCESS, isSuccess)
            if (context is AppCompatActivity) {
                context.startActivityForResult(intent, BaseSelfie3DIntroActivity.STATE_REQUEST_CODE)
            }
        }
    }

    fun commit(isSuccess: Boolean) {
        OSPLog.log("toFailed, commitCallback")
        onResultCallback?.onResult(isSuccess)
    }

    open fun commitSuccess() {
        faceScanResultCallback?.cancel()
    }
}

interface OnResultCallback {
    fun onResult(isSuccess: Boolean)
}