package com.aai.core.node.start

import com.aai.core.processManager.OSPNode

class StartPageNode : OSPNode() {

    override var finishWhenComplete: Boolean = true

    override fun start() {
        jump(
            targetActivity = StartPageActivity::class.java,
        )
    }

    override fun copy(): StartPageNode = StartPageNode()

}