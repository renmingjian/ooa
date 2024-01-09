package com.aai.core.network

interface NetWorkCallback {

    fun onSuccess(response: String)

    fun onError(code: String, message: String)

}