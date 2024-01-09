package com.aai.core.node.result

import com.aai.core.processManager.OSPNode

class ResultPageNode : OSPNode() {

    override fun start() {
        jump(
            targetActivity = ResultPageActivity::class.java,
        )
    }

    override fun copy(): ResultPageNode = ResultPageNode()

}