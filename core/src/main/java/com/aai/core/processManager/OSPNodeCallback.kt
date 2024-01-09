package com.aai.core.processManager

/**
 *
 */
interface OSPNodeCallback {

    fun onSuccess(result: OSPNodeResult)
    fun onFailure(e: String)

}