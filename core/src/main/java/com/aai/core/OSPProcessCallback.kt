package com.aai.core

/**
 * 流程回调
 */
interface OSPProcessCallback {

    /**
     *
     */
    fun onReady()

    /**
     * 退出了流程，但是流程没有做完
     */
    fun onExit(nodeCode: String)

    /**
     * 异常
     */
    fun onError(message: String?)

    /**
     * 各种事件
     */
    fun onEvent(eventName: String, params: MutableMap<String, String>? = null)

    /**
     * 整个流程完成
     */
    fun onComplete()

}