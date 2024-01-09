package com.aai.iqa.node

import android.content.Context
import com.aai.core.processManager.FragmentJumper
import com.aai.core.processManager.ProcessInterceptor
import com.aai.core.processManager.model.OSPNodeData
import com.aai.core.processManager.model.ResponseCode
import com.aai.core.utils.OSPLog

/**
 * Document对流程做特定的拦截处理，如果需要retry，则跳转进入到retry页面。这个逻辑不是通用逻辑，所以在特定节点内做
 * 如果节点已经做完，但是没有成功，则进入失败页面
 */
class DocProcessInterceptor(private val ospNode: DocumentNode) : ProcessInterceptor {
    override fun interceptor(context: Context, nodeData: OSPNodeData): Boolean {
        val nodeCode = nodeData.nodeCode
        if (nodeData.retry == true) {
            OSPLog.log("doc intercepted, nodeCode = $nodeCode")
            ospNode.fragmentTag = DocumentNode.FRAGMENT_RETRY
            // 如果当前已经有Activity了，需要在之前的node基础上做一些东西，不能跳转到一个新的Activity页面
            val activity = ospNode.activity
            if (activity != null) {
                if (activity is FragmentJumper) {
                    activity.jump(DocumentNode.FRAGMENT_RETRY)
                }
            } else {
                ospNode.jump(DocumentActivity::class.java)
            }
            return true
        }
        if (nodeData.completedFlag == true && nodeData.finalStatus == ResponseCode.CODE_FAILED) {
            val activity = ospNode.activity
            if (activity != null) {
                if (activity is FragmentJumper) {
                    activity.jump(DocumentNode.FRAGMENT_FAILURE)
                }
            } else {
                ospNode.jump(DocumentActivity::class.java)
            }
            return true
        }
        OSPLog.log("doc do not intercept")
        return false
    }
}