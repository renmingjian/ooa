package com.aai.core.node.start

import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import com.aai.core.EventPageTitle
import com.aai.core.network.NetRequest
import com.aai.core.processManager.model.UrlConst
import com.aai.core.mvvm.BaseViewModel
import com.aai.core.network.NetMethod
import com.aai.core.processManager.dataparser.OSPStartPageNodeDataParser
import com.aai.core.utils.getLocal
import java.io.IOException

class StartPageViewModel : BaseViewModel<OSPStartPageNodeDataParser>() {

    override fun getCommitRequest(): NetRequest =
        NetRequest(
            url = UrlConst.START_PAGE_COMMIT_URL,
            method = NetMethod.POST,
            queryParameters = mutableMapOf("sdkToken" to sdkToken)
        )

    override fun createParser(): OSPStartPageNodeDataParser = OSPStartPageNodeDataParser()

    override fun initData(bundle: Bundle) {
        super.initData(bundle)
        trackSuperProperties(EventPageTitle.START_ONBOARDING)
    }

    fun getPrivacyUrl(assetManager: AssetManager): String {
        val local = getLocal()
        val preFix = "OSPDraft_"
        var htmlName = "$preFix$local.html"
        val exist = isAssetExists(htmlName, assetManager)
        if (!exist) {
            htmlName = "OSPDraft.html"
        }
        return "file:///android_asset/$htmlName"
    }

    private fun isAssetExists(assetPath: String, assetManager: AssetManager): Boolean {
        var assetExists = false
        try {
            val fileList = assetManager.list("")
            if (fileList != null) {
                for (file in fileList) {
                    if (file == assetPath) {
                        assetExists = true
                        break
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return assetExists
    }
}