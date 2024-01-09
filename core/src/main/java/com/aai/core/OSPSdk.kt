package com.aai.core

import androidx.appcompat.app.AppCompatActivity
import com.aai.core.processManager.OSPNode
import com.aai.core.processManager.OSPProcessorManager
import com.aai.core.utils.OSPLog


class OSPSdk {

    var ospProcessorManager: OSPProcessorManager? = OSPProcessorManager()
    lateinit var options: OSPOptions
    var openLog = true

    companion object {

        const val TAG = "OSPLog: "
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            OSPSdk()
        }
    }

    fun init(options: OSPOptions, openLog: Boolean = true) {
        this.options = options
        this.openLog = openLog
        ospProcessorManager = OSPProcessorManager()
    }

    fun registerNode(name: String, node: OSPNode) {
        if (ospProcessorManager == null) {
            ospProcessorManager = OSPProcessorManager()
        }
        // 如果子模块需要做一些初始化工作，例如集成了第三方SDK，三方需要初始化，可以在这里做
        node.initializer?.init(options.context, options.key, options.sdkToken)
        ospProcessorManager?.registerNode(name, node)
    }

    fun getProcessCallback() = options.processCallback

    fun startFlow(context: AppCompatActivity) {
        getProcessCallback()?.onReady()
        ospProcessorManager?.reset()
        ospProcessorManager?.startFlow(context, options.sdkToken)
    }

    fun release() {
        OSPLog.log("OSPSdk release")
        ospProcessorManager = null
    }

}