package com.aai.core.node

import com.aai.core.network.HttpUrlConnectionClient
import com.aai.core.network.NetRequest
import com.aai.core.network.NetWorkCallback
import com.aai.core.processManager.model.OSPResponse
import com.aai.core.processManager.model.ResponseCode
import org.json.JSONObject

class CommitHelper {

    val client = HttpUrlConnectionClient.instance
    val commitResponse = OSPResponse()
    var commitCallback: LoadingCallback? = null

    /**
     * 每个节点需要提交
     */
    fun commit(request: NetRequest) {
        commitCallback?.onLoading()
        client.sendRequest(request, object : NetWorkCallback {
            override fun onSuccess(response: String) {
                parseCommitData(response)
            }

            override fun onError(code: String, message: String) {
                commitCallback?.onError(code, message)
            }
        })
    }

    fun parseCommitData(response: String) {
        val jsonObject = JSONObject(response)
        if (jsonObject.has("code")) {
            val code = jsonObject.getString("code")
            commitResponse.code = code
            if (code == ResponseCode.CODE_SUCCESS) {
                commitCallback?.onSuccess(commitResponse)
            } else {
                val message = if (jsonObject.has("message")) jsonObject.getString("message") else ""
                commitResponse.message = message
                commitCallback?.onError(code, message)
            }
        }
    }

}