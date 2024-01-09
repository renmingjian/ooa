package com.aai.core.processManager

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.aai.core.OSPSdk
import com.aai.core.mvvm.OSPDateUpdate
import com.aai.core.network.HttpUrlConnectionClient
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.network.NetWorkCallback
import com.aai.core.node.result.ResultPageNode
import com.aai.core.node.start.StartPageNode
import com.aai.core.processManager.dataparser.OSPNodeDataParser
import com.aai.core.processManager.dataparser.OSPSubmitDataParser
import com.aai.core.processManager.dataparser.OSPThemeParser
import com.aai.core.processManager.loading.CommonLoading
import com.aai.core.processManager.loading.RefreshLoading
import com.aai.core.processManager.model.Const
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.OSPThemeBasicData
import com.aai.core.processManager.model.ResponseCode
import com.aai.core.processManager.model.UrlConst
import com.aai.core.utils.ImageLoader
import com.aai.core.utils.OSPLog
import com.aai.core.utils.getThemeBasic
import com.aai.core.utils.runOnUIThread
import com.aai.core.utils.showToast
import com.aai.core.utils.showToastByCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 用来管理节点之间的跳转
 */
class OSPProcessorManager {

    private val client = HttpUrlConnectionClient.instance
    var submitParser = OSPSubmitDataParser()
    var nodeDataParser = OSPNodeDataParser()
    var themeParser = OSPThemeParser()
    private var pollTime = 0

    // 请求submit、getTheme、getCurrentNode接口时展示的loading
    private var loadingPopup: CommonLoading? = null

    // currentNode接口轮询超过10次后，展示refreshLoading的页面
    private var refreshLoading: RefreshLoading? = null

    // 用来存储注册的每个节点
    private val nodeMaps = mutableMapOf<String, OSPNode>()

    // 用来存储当前已经加载过的节点，每开启一个节点就要存储
    val nodeList = mutableListOf<OSPNode>()
    var currentNode: OSPNode? = null
    var sdkToken: String = ""
    var context: AppCompatActivity? = null

    /**
     * 开启流程
     * 给外界调用者只会有3个节点的注册：表单、证件照和活体。
     * 但是OSP是可以设置开始和结束页面的，所以这两个页面在内部注册
     */
    fun startFlow(context: AppCompatActivity, sdkToken: String) {
        this.context = context
        this.sdkToken = sdkToken
        registerNode(NodeCode.START_ONBOARDING, StartPageNode())
        // 注意：result页面有3个-Success、Decline、Pending，这3个页面对应不同的code，但是这里只注册一个Result。
        // 具体要展示哪个Result页面，需要根据nodeCode来做判断
        registerNode(NodeCode.RESULT, ResultPageNode())
        context.startActivity(Intent(context, HandleProcessActivity::class.java))
    }

    /**
     * 妥协处理：
     * 在FaceTec流程中，如果失败会进retry页面，但是自己平台逻辑有retry次数的限制，超过限制要进入失败页面，在失败页面
     * 点击返回需要结束整个WorkFlow流程。问题是FaceTec的流程没有提供结束流程的api，点击返回还会进入到FaceTec的页面。
     * 解决方案：[startFlow]方法就不再直接调用submit开启流程，而是跳转一个Activity，该Activity的launchMode为
     * singleTask，当在失败页面点击返回时直接跳转该页面，这样会把栈中的FaceTec页面给销毁。然后finish掉当前页面即可
     */
    fun start(context: AppCompatActivity) {
        submit()
        this.context = context
    }

    fun registerNode(name: String, node: OSPNode) {
        OSPLog.log("resisterNode, name = $name")
        nodeMaps[name] = node
    }

    private fun submit() {
        OSPLog.log("start submit")
        val request = NetRequest(
            url = UrlConst.SUBMIT_URL,
            method = NetMethod.GET,
            queryParameters = mutableMapOf("sdkToken" to sdkToken)
        )
        // step1. 展示loading
        showLoading()
        client.sendRequest(request, object : NetWorkCallback {
            override fun onSuccess(response: String) {
                OSPLog.log("submit success")
                // step2. 解析数据
                submitParser.parse(response)
                val themeId = submitParser.themeId
                OSPLog.log("submit success, themeId = $themeId")
                if (themeId != null) {
                    getTheme(themeId)
                } else {
                    // step3. 请求currentNode接口
                    getCurrentNode(nodeChanged = false)
                }
            }

            override fun onError(code: String, message: String) {
                OSPLog.log("submit error, message = $message")
                loadingPopup?.dismiss()
                context?.let {
                    showToastByCode(it, code, message)
                }
            }
        })
    }

    fun showLoading() {
        try {
            loadingPopup = CommonLoading(context!!)
            loadingPopup?.show()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

//        var loadingContext = currentNode?.activity
//        if (loadingContext == null) loadingContext = context
//        loadingContext?.let {
//            if (!loadingContext.isFinishing) {
//                loadingPopup = CommonLoading(it)
//                loadingPopup?.show()
//            }
//        }
    }

    private fun getTheme(themeId: Int) {
        OSPLog.log("getTheme, themeId = $themeId")
        val request = NetRequest(
            url = UrlConst.THEME_URL,
            method = NetMethod.GET,
            queryParameters = mutableMapOf("sdkToken" to sdkToken, "themeId" to themeId)
        )
        client.sendRequest(request, object : NetWorkCallback {
            override fun onSuccess(response: String) {
                // 对theme进行解析，解析后请求currentNode接口
                OSPLog.log("getTheme success: $response")
                themeParser.parse(response)
                val logo = themeParser.themeData.basicSetting.logo
                if (logo.startsWith("http")) {
                    ImageLoader.loadLogoBitmap(getThemeBasic().logo)
                }
                getCurrentNode(nodeChanged = false)
            }

            override fun onError(code: String, message: String) {
                getCurrentNode(nodeChanged = false)
            }
        })
    }

    private fun getCurrentNode(isWaiting: Boolean = false, nodeChanged: Boolean = true) {
        OSPLog.log("getCurrentNode isWaiting: $isWaiting")
        val request = NetRequest(
            url = UrlConst.CURRENT_NODE_URL,
            method = NetMethod.GET,
            queryParameters = mutableMapOf("sdkToken" to sdkToken)
        )

        // 展示loading，先判断loading是否展示，如果没展示才做展示
        OSPLog.log("loadingPopup isShow: ${loadingPopup?.isShow()}, context = ${context?.isFinishing}")
        if (nodeChanged && currentNode?.activity != null && currentNode?.activity?.isFinishing == false) {
            loadingPopup = CommonLoading(currentNode?.activity!!)
        }
        try {
            if (loadingPopup?.isShow() != true) {
                loadingPopup?.show()
                loadingPopup?.showContent(isWaiting)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        client.sendRequest(request, object : NetWorkCallback {
            override fun onSuccess(response: String) {
                OSPLog.log("getCurrentNode success")
                // 每次解析Node节点，必须新new一个，否则会使用上一个的parser数据
                nodeDataParser = OSPNodeDataParser()
                nodeDataParser.parse(response)
                handleProcess()
                pollTime = 0
            }

            override fun onError(code: String, message: String) {
                OSPLog.log("getCurrentNode error, pollTime = $pollTime, message = $message")
                pollTime++
                startPolling()
            }
        })
    }

    /**
     * 处理nodeConfig数据，决定下一步怎么走
     */
    private fun handleProcess() {
        OSPLog.log("handleProcess")
        val nodeData = nodeDataParser.ospNodeData

        var node = nodeMaps[NodeCode.mappingNode(nodeData.nodeCode ?: "")]

        node?.nodeCode = nodeData.nodeCode ?: ""
        node?.init(
            context = context!!,
            sdkToken = sdkToken,
            data = nodeDataParser.ospNodeDataJson,
            callback = null
        )
        if (node?.processInterceptor != null) {
            // 当前要加载的是node，currentNode此时还代表前一个node，如果nodeCode相等，说明是同一个node节点。
            // 如果是同一个node节点，则有可能还不能销毁，因为当前要加载的node可能还要在之前的基础上做一些操作。也有
            // 可能需要销毁而开启一个新的节点，这就需要在interceptor中做了
            // 如果不是同一个node，说明当前node为新开的node，那么就可以销毁了。
            if (currentNode?.nodeCode == node?.nodeCode) {
                val activity = currentNode?.activity
                if (activity is OSPDateUpdate) {
                    currentNode?.data = nodeDataParser.ospNodeDataJson
                    currentNode?.getBundle()?.let { activity.updateOSPData(it) }
                }
            } else {
                currentNode?.end()
            }
            val interceptor = node?.processInterceptor?.interceptor(context!!, nodeData)
            if (interceptor == true) {
                runOnUIThread {
                    delay(100)
                    loadingPopup?.dismiss()
                    currentNode = node
                }
                return
            }
        }
        // 如果是相同的节点，则新new一个Node，防止多个相同的节点使用同一个Node对象，造成数据的混乱
        if (currentNode?.nodeCode == node?.nodeCode) {
            node = node?.copy()
            node?.nodeCode = nodeData.nodeCode ?: ""
            node?.init(
                context = context!!,
                sdkToken = sdkToken,
                data = nodeDataParser.ospNodeDataJson,
                callback = null
            )
        }
        OSPLog.log("handleProcess: completedFlag = ${nodeData.completedFlag}")
        if (nodeData.completedFlag == true) {
            // 节点做完，退出流程
            endProcess()
        } else {
            if (nodeData.nodeConfig != null) {
                // 根据接口的nodeCode来找用户注册的node节点
                if (node != null) {
                    node.start()
                    nodeList?.add(node)

                    // dismiss current node page and show the next node page
                    if (currentNode?.finishWhenComplete == true) {
                        currentNode?.activity?.finish()
                        nodeList?.remove(currentNode)
                    }
                } else {
                    endProcess()
                    OSPSdk.instance.getProcessCallback()?.onError("Don't support this node.")
                    return
                }
                // delay 100毫秒dismiss loading，不然现象会是loading先消失，然后回退到当前页面，接着跳转下一个页面。
                // delay后现象是loading跳转到下一个页面，loading消失
                runOnUIThread {
                    delay(100)
                    currentNode?.end()
                    loadingPopup?.dismiss()
                    currentNode = node
                }
            } else {
                OSPLog.log("handleProcess: config = null, pollTime = $pollTime")
                if (pollTime >= Const.MAX_ROLLUP_TIMES) {
                    loadingPopup?.dismiss()
                    context?.let {
                        OSPLog.log("handleProcess: config = null, pollTime >= 10, show RefreshLoading")
                        if (refreshLoading == null) {
                            refreshLoading = RefreshLoading(
                                it,
                                refreshClick = {
                                    getCurrentNode(nodeData.waiting == true, nodeChanged = false)
                                },
                                backClick = {
                                    endProcess()
                                }
                            )
                        }
                        refreshLoading?.show()
                    }
                } else {
                    OSPLog.log("handleProcess: config = null, pollTime = $pollTime, start poll")
                    startPolling(nodeData.waiting == true) // poll currentNode
                }
            }
        }
    }

    private fun startPolling(waiting: Boolean = false) = CoroutineScope(Dispatchers.IO).launch {
        if (pollTime > Const.MAX_ROLLUP_TIMES) return@launch
        if (pollTime > 5) {
            delay(5000)
        } else {
            delay(2000)
        }
        withContext(Dispatchers.Main) {
            getCurrentNode(waiting, nodeChanged = false)
        }
    }

    fun onResult(nodeResult: OSPNodeResult) {
        OSPLog.log("nodeResult = $nodeResult")
        if (nodeResult.isSuccess) {
            getCurrentNode()
        }
    }

    /**
     * 当一个节点销毁的时候要移除
     */
    fun removeNode(activity: Activity) {
        val node = nodeList?.find { it.activity == activity }
        if (node != null) {
            nodeList.remove(node)
        }
        if (nodeList?.isEmpty() == true) {
            endProcess()
        }
    }

    /**
     * 结束整个WorkFlow流程
     */
    fun endProcess() {
        OSPLog.log("endProcess")
        val nodeData = nodeDataParser.ospNodeData
        if (nodeData.completedFlag == true && nodeData.finalStatus != ResponseCode.CODE_FAILED) {
            OSPSdk.instance.getProcessCallback()?.onComplete()
        } else if (nodeData.completedFlag == true && nodeData.finalStatus == ResponseCode.CODE_FAILED) {
            // todo 失败信息
            OSPSdk.instance.getProcessCallback()?.onError(null)
        } else {
            OSPSdk.instance.getProcessCallback()?.onExit(nodeData.nodeCode ?: "")
        }
        context?.startActivity(Intent(context, HandleProcessActivity::class.java))
        nodeList?.forEach {
            it.activity?.finish()
        }
        runOnUIThread {
            delay(100)
            loadingPopup?.dismiss()
        }
        nodeList?.clear()
        OSPThemeBasicData.logoBitmap = null
        reset()
        OSPSdk.instance.release()
    }

    // 事件的公共属性
    fun superProperties(): MutableMap<String, Any?>? {
        if (currentNode?.nodeCode.isNullOrEmpty()) return null
        val map = mutableMapOf<String, Any?>()
        map["toC_nodeName"] = currentNode?.nodeCode ?: ""
        map["toC_flowId"] = submitParser.journeyId
        map["toC_tenantId"] = submitParser.tenantId
        map["toC_serviceEnv"] = "ID"
        return map
    }

    /**
     * 重置资源。
     * 该方法在实际SDK使用时其实没用，在测试期间可以走多个WorkFlow流程，每个流程都可以重新设置theme等主题，由于上一次
     * 的资源可能还没有释放，导致下一次走流程还使用上一次的theme，所以在每次走新流程前，要重新设置一下
     */
    fun reset() {
        submitParser = OSPSubmitDataParser()
        nodeDataParser = OSPNodeDataParser()
        themeParser = OSPThemeParser()
        pollTime = 0
        nodeList.clear()
        currentNode = null
        context = null
        OSPThemeBasicData.logoBitmap = null
    }

}