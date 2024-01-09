package com.aai.core.mvvm

import android.os.Bundle
import com.aai.core.EventTracker
import com.aai.core.OSPSdk
import com.aai.core.network.NetRequest
import com.aai.core.node.CommitHelper
import com.aai.core.node.LoadingCallback
import com.aai.core.processManager.dataparser.OSPDataParser
import com.aai.core.processManager.model.BundleConst
import com.aai.core.processManager.model.OSPResponse
import com.aai.core.utils.OSPLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject


/**
 * 每个节点的ViewModel
 */
abstract class BaseViewModel<T : OSPDataParser> {

    lateinit var configParser: T

    var sdkToken: String = ""
    var nodeCode: String = ""
    var completedFlag: Boolean? = null
    var finalStatus: String? = null
    val commitHelper = CommitHelper()
    var data = ""
    var commitCallback: LoadingCallback? = null
    var errorMsg: String? = null

    fun commit(showLoading: Boolean = true) {
        val request = getCommitRequest()
        OSPLog.log("commit, url = ${request.url}")
        commitHelper.commitCallback = object : LoadingCallback {
            override fun onLoading() {
                if (showLoading) commitCallback?.onLoading()
            }

            override fun onSuccess(response: OSPResponse) {
                commitCallback?.onSuccess(response)
                onCommitSuccess()
            }

            override fun onError(code: String, message: String) {
                commitCallback?.onError(code, message)
            }

        }
        commitHelper.commit(getCommitRequest())
    }

    open fun initData(bundle: Bundle) {
        data = bundle.getString(BundleConst.NODE_DATA) ?: ""
        sdkToken = bundle.getString(BundleConst.SDK_TOKEN) ?: ""
        configParser = createParser()
        val json = JSONObject(data)
        nodeCode = json.getString("nodeCode")
        completedFlag = json.getBoolean("completedFlag")
        finalStatus = json.getString("finalStatus")
        val config = json.optJSONObject("nodeConfig")?.toString()
        config?.let {
            configParser.parse(it)
        }
    }

    fun trackSuperProperties(pageTitle: String) {
        GlobalScope.launch(Dispatchers.IO) {
            delay(150)
            val map = OSPSdk.instance.ospProcessorManager?.superProperties()
            map?.put("toC_pageTitle", pageTitle)
            EventTracker.registerDynamicSuperProperties(map)
        }
    }

    abstract fun createParser(): T

    abstract fun getCommitRequest(): NetRequest

    open fun onCommitSuccess() {

    }

}

