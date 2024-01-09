package com.aai.core.network.intercept

class InterceptorHandler {

    private val interceptors = mutableListOf<NetworkInterceptor>()

    fun addInterceptor(interceptor: NetworkInterceptor) = apply {
        interceptors += interceptor
    }

    fun handleInterceptors(response: String): String =
        if (interceptors.isEmpty()) response else handle(0, response)

    private fun handle(index: Int, response: String): String {
        return if (index in 0 until interceptors.size) {
            val next = index + 1
            handle(next, interceptors[index].intercept(response))
        } else {
            response
        }
    }

}