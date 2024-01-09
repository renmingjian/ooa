package com.aai.core.node

import com.aai.core.processManager.model.OSPResponse

interface LoadingCallback {

    fun onLoading()
    fun onSuccess(response: OSPResponse)
    fun onError(code: String, message: String)

}