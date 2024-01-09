package com.aai.core.network

sealed class OSPRequestBody {

    class OSPJsonRequestBody(val json: String) : OSPRequestBody()
    class OSPTextRequestBody(val text: String) : OSPRequestBody()
    class OSPFormUrlRequestBody(val map: MutableMap<String, Any>) : OSPRequestBody()
    class OSPMultiPartRequestBody(
        val values: List<ByteRequestBody>
    ) : OSPRequestBody()

}