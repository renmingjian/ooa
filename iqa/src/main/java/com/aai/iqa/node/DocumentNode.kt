package com.aai.iqa.node

import android.os.Bundle
import com.aai.core.Initializer
import com.aai.core.processManager.OSPNode
import com.aai.core.processManager.ProcessInterceptor
import com.aai.core.processManager.model.BundleConst
import com.aai.iqa.DocInitializer

class DocumentNode : OSPNode() {

    companion object {
        // NODE的介绍页
        const val FRAGMENT_INTRO = "FRAGMENT_INTRO"

        // 选择国家页面
        const val FRAGMENT_SELECT_COUNTRY = "FRAGMENT_SELECT_COUNTRY"

        // 选择Document type页面
        const val FRAGMENT_SELECT_TYPE = "FRAGMENT_SELECT_TYPE"

        // 选择上传或者拍照页面
        const val FRAGMENT_DOC_UPLOAD = "FRAGMENT_DOC_UPLOAD"

        // retry页面
        const val FRAGMENT_RETRY = "FRAGMENT_RETRY"

        // failure页面
        const val FRAGMENT_FAILURE = "FRAGMENT_FAILURE"

        // 自定义相机拍照页面
        const val FRAGMENT_TAKE_PHOTO = "FRAGMENT_TAKE_PHOTO"

        // 开启Microblink页面
        const val MICRO_BLINK = "MICRO_BLINK"

        // 展示选择拍照或者上传的弹框
        const val IQA_SELECT = "IQA_SELECT"
    }

    var fragmentTag = FRAGMENT_INTRO
    override var processInterceptor: ProcessInterceptor? = DocProcessInterceptor(this)
    override var initializer: Initializer? = DocInitializer()

    override fun start() {
        jump(
            targetActivity = DocumentActivity::class.java,
        )
    }

    override fun getBundle(): Bundle {
        val bundle = super.getBundle()
        bundle.putString(BundleConst.FRAGMENT_TAG, fragmentTag)
        return bundle
    }

    override fun copy(): DocumentNode = DocumentNode()
}