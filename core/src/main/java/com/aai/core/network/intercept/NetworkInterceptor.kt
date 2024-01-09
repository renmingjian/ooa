package com.aai.core.network.intercept

interface NetworkInterceptor {

   fun intercept(resultString: String): String

}