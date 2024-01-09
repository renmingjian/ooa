package com.aai.core.processManager

import com.aai.core.processManager.model.ResponseCode
import java.io.Serializable

/**
 * 每个节点做完后结果的回调
 */
class OSPNodeResult : Serializable {
    var nodeName: String? = null
    var isSuccess = false
    var message: String = ResponseCode.CODE_SUCCESS
    // 当最后一个节点提交成功后代表完成
//    var isComplete: Boolean = false
}