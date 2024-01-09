package com.aai.core.processManager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aai.core.Initializer
import com.aai.core.processManager.model.BundleConst

/**
 * 抽象的节点封装，可以使用接口开启一个节点和结束一个点击
 */
abstract class OSPNode {

    open var finishWhenComplete = true
    var activity: AppCompatActivity? = null
    open var processInterceptor: ProcessInterceptor? = null
    var nodeCode: String = ""

    // 不是当前NODE所关联的Activity，而是上一个页面的Context，该Context的主要作用就是用来跳转Activity
    var context: AppCompatActivity? = null
    var sdkToken: String? = null
    var data: String? = null
    var callback: OSPNodeCallback? = null
    open var initializer: Initializer? = null

    fun init(
        context: AppCompatActivity,
        sdkToken: String,
        data: String,
        callback: OSPNodeCallback?
    ) {
        this.context = context
        this.sdkToken = sdkToken
        this.data = data
        this.callback = callback
    }

    /**
     * 开启一个节点
     * @param config 当前Node节点所需要的configs数据，是currentNode接口中的nodeConfig字段
     */
    abstract fun start()

    /**
     * 结束一个节点
     */
    open fun end() {
        if (finishWhenComplete) {
            activity?.finish()
        }
    }

    abstract fun copy(): OSPNode

    fun <T : AppCompatActivity> jump(
        targetActivity: Class<T>,
    ) {
        val intent = Intent(context!!, targetActivity)
        intent.putExtras(getBundle())
        context!!.startActivity(intent)
    }

    open fun getBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString(BundleConst.SDK_TOKEN, sdkToken)
        bundle.putString(BundleConst.NODE_DATA, data)
        bundle.putBoolean(BundleConst.NODE_FINISH_WHEN_COMPLETED, finishWhenComplete)
        return bundle
    }

}