package com.aai.core.network.intercept

import com.aai.core.network.AINetworkException
import com.aai.core.processManager.model.ResponseCode
import com.aai.core.utils.OSPLog
import org.json.JSONObject

/**
 * 该类的作用是：拿到code后，对所有的接口做一层code的判断，如果code != "SUCCESS"，则统一抛错。接口调用处会得到错误
 *  的回调，不用每个接口都对code解析一次。
 */
class DefaultNetworkInterceptor : NetworkInterceptor {

    /**
     * 有的接口在失败时是code、message、data这样的统一数据结构，但是成功后就不再是这样。
     * 所以如果数据返回没有code，我们就不对code进行解析，把数据原样返回，否则对code解析一遍。
     */
    override fun intercept(resultString: String): String {
        val jsonObject: JSONObject?
        try {
            jsonObject = JSONObject(resultString)
        } catch (e: Throwable) {
            return resultString
        }

        return if (jsonObject.has("code")) {
            val code = jsonObject.getString("code")
            val message: String = if (jsonObject.has("message")) {
                jsonObject.getString("message")
            } else ""
            OSPLog.log("code == $code")
            if (code != ResponseCode.CODE_SUCCESS) {
                OSPLog.log("AINetworkException: code = $code, message = $message")
                throw AINetworkException(code, message)
            } else {
                resultString
            }
        } else resultString
    }

}