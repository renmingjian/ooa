package com.aai.core.network

import java.io.File

data class NetRequest(
    var url: String,
    val method: String = NetMethod.GET,
    val queryParameters: MutableMap<String, Any>? = null,
    val headers: MutableMap<String, Any>? = null,
    val requestBody: OSPRequestBody? = null,
) {
    init {
        url = buildUrlWithQueryParams(url, queryParameters)
    }

    private fun buildUrlWithQueryParams(url: String, queryParams: Map<String, Any>?): String {
        if (queryParams.isNullOrEmpty()) {
            return url
        }
        val queryString = queryParams.map { (key, value) ->
            "$key=$value"
        }.joinToString("&")
        return if (url.contains("?")) {
            "$url$queryString"
        } else {
            "$url?$queryString"
        }
    }
}

object NetMethod {
    val GET = "GET"
    val POST = "POST"
    val HEAD = "HEAD"
}