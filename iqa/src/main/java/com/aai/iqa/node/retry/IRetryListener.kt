package com.aai.iqa.node.retry

interface IRetryListener {

    /**
     * 拍照或者上传失败，需要retry
     */
    fun tryAgain()

}