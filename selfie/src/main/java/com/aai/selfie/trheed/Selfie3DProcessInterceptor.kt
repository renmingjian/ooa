package com.aai.selfie.trheed

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.aai.core.OSPSdk
import com.aai.core.R
import com.aai.core.processManager.OSPNode
import com.aai.core.processManager.ProcessInterceptor
import com.aai.core.processManager.model.BundleConst
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.OSPNodeData
import com.aai.core.processManager.model.ResponseCode
import com.aai.core.utils.OSPLog
import com.aai.core.utils.hasAndNotNull
import com.aai.core.utils.textWithKey
import com.aai.selfie.parser.SelfieConfigParser
import com.aai.selfie.parser.SelfieVerificationConfigParser
import com.aai.selfie.state.SelfieStateActivity
import org.json.JSONObject

/**
 * 3d的流程要做一层拦截处理，因为3d节点做完后会继续请求currentNode接口做下一个节点。但是在3d做完后会有这样的情况：
 * 1.后端在与IQA的证件照对比失败后，仍然返回3d节点的数据，只是此时finalStatus是失败的；
 * 2.重试次数使用完后，要调用commit接口结束整个流程，之后还会在调用submit接口，此时也是失败的；
 * 遇到这种情况，需要跳转到一个失败页面，这个是3d特有的情况
 */
class Selfie3DProcessInterceptor(private val ospNode: OSPNode) : ProcessInterceptor {
    override fun interceptor(context: Context, nodeData: OSPNodeData): Boolean {
        val nodeCode = nodeData.nodeCode
        if ((nodeCode == NodeCode.SELFIE_VERIFICATION || nodeCode == NodeCode.FACE_PHOTO)
            && nodeData.finalStatus == ResponseCode.CODE_FAILED
        ) {
            OSPLog.log("selfie 3d intercepted, nodeCode = $nodeCode")
            var failureMessage = OSPSdk.instance.options.context.getString(R.string.selfie_3d_state_failed)
            val json = JSONObject(ospNode.data)
            if (nodeData.nodeCode == NodeCode.SELFIE_VERIFICATION) {
                val parser = SelfieVerificationConfigParser()
                if (json.hasAndNotNull("nodeConfig")) {
                    parser.parse(json.getJSONObject("nodeConfig").toString())
                    OSPLog.log("nodeConfigData = ${ospNode.data}")
                    parser.ospSelfieConfig.pages.failurePage.subTitle?.let {
                        failureMessage = textWithKey(it, "")
                    }
                }
            } else {
                val parser = SelfieConfigParser()
                if (json.hasAndNotNull("nodeConfig")) {
                    parser.parse(json.getJSONObject("nodeConfig").toString())
                    OSPLog.log("nodeConfigData = ${ospNode.data}")
                    parser.ospSelfieConfig.pages.failurePage.subTitle?.let {
                        failureMessage = textWithKey(it, "")
                    }
                }
            }
            val intent = Intent(context, SelfieStateActivity::class.java)
            intent.putExtra(BundleConst.NODE_CODE, nodeCode)
            intent.putExtra(BundleConst.TITLE_KEY, failureMessage)
            intent.putExtra(BundleConst.IS_SUCCESS, false)
            if (context is AppCompatActivity) {
                context.startActivityForResult(intent, BaseSelfie3DIntroActivity.STATE_REQUEST_CODE)
            }
            return true
        }
        OSPLog.log("selfie 3d do not intercept")
        return false
    }
}