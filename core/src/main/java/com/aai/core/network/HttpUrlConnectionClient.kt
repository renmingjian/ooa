package com.aai.core.network

import com.aai.core.network.intercept.DefaultNetworkInterceptor
import com.aai.core.network.intercept.InterceptorHandler
import com.aai.core.network.intercept.NetworkInterceptor
import com.aai.core.utils.OSPLog
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * 对网络结果的解析统一使用JSONObject解析，不使用Gson。原因如下：
 * 1.网络数据submit接口很多字段是随机生成的，比如text_header434898，无法事先定义一个数据结构进行对应，无法统一解析
 * 2.有的接口数据成功是一个结构，失败又是另外一个结构，也无法像使用Retrofit预先定义一个数据结构统一使用Gson解析，只能
 *      根据结果去解析，有的接口拿到确定的结果后倒是可以使用Gson，但是引用第三方库会稍微增大体积包
 */
class HttpUrlConnectionClient private constructor() : NetWorkClient {

    private val tag = "HttpUrlConnectionClient: "

    private val mainThreadExecutor = MainThreadExecutor()

    private val executorService = Executors.newCachedThreadPool()
    private val interceptorHandler = InterceptorHandler()

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            HttpUrlConnectionClient()
        }
    }

    override fun sendRequest(
        request: NetRequest,
        netWorkCallback: NetWorkCallback,
        onHeaderCallback: HeaderCallback?
    ) {
        executorService.submit {
            var connection: HttpURLConnection? = null
            NetworkSettings.set(NetworkConfig(enablePacketCaptureDebug = true))
            try {
                HttpURLConnection.setFollowRedirects(false)
                val url = URL(request.url)
                connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false

                // set method
                connection.requestMethod = request.method

                // set headers
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept-Encoding", "UTF-8")
                request.headers?.let {
                    for ((key, value) in it) {
                        connection.setRequestProperty(key, value.toString())
                    }
                }

                connection.doInput = true
                if (request.method == NetMethod.POST) {
                    request.requestBody?.let {
                        WriteRequestBody().write(connection, it)
                    }
                }

                // 获取响应
                val responseCode = connection.responseCode
                handleHeaders(connection.headerFields, onHeaderCallback)
                OSPLog.log("$tag responseCode = $responseCode")
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    var responseData = response.toString()

                    addInterceptor(DefaultNetworkInterceptor())
                    responseData = interceptorHandler.handleInterceptors(responseData)
                    mainThreadExecutor.execute {
                        netWorkCallback.onSuccess(responseData)
                    }
                } else {
                    val errorMessage = "HTTP Error: $responseCode"
                    mainThreadExecutor.execute {
                        netWorkCallback.onError(responseCode.toString(), errorMessage)
                    }
                }
            } catch (e: AINetworkException) {
                mainThreadExecutor.execute {
                    netWorkCallback.onError(e.code, e.message)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                // 切换到主线程处理异常
                mainThreadExecutor.execute {
                    netWorkCallback.onError("UN_KNOWN", "Network Error: ${e.message}")
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun handleHeaders(map: Map<String, List<String>>, onHeaderCallback: HeaderCallback?) {
        mainThreadExecutor.execute {
            onHeaderCallback?.onGetHeaders(map)
        }
    }

    fun addInterceptor(interceptor: NetworkInterceptor) {
        interceptorHandler.addInterceptor(interceptor)
    }

}
