package com.aai.core.network

interface NetWorkClient {
    fun sendRequest(
        request: NetRequest,
        netWorkCallback: NetWorkCallback,
        // 有的接口只关注header，而不关心Body
        onHeaderCallback: HeaderCallback? = null
    )
}