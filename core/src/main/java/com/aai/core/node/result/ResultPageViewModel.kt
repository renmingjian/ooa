package com.aai.core.node.result

import android.os.Bundle
import com.aai.core.EventPageTitle
import com.aai.core.mvvm.BaseViewModel
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.processManager.dataparser.OSPResultPageDataParser
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.UrlConst

class ResultPageViewModel : BaseViewModel<OSPResultPageDataParser>() {

    override fun createParser(): OSPResultPageDataParser = OSPResultPageDataParser()

    override fun getCommitRequest(): NetRequest = NetRequest(
        url = UrlConst.RESULT_COMMIT_URL,
        method = NetMethod.POST,
        queryParameters = mutableMapOf("sdkToken" to sdkToken),
    )

    override fun initData(bundle: Bundle) {
        super.initData(bundle)
        trackSuperProperties(
            when (nodeCode) {
                NodeCode.FINISH_ONBOARDING_PENDING -> EventPageTitle.PENDING_PAGE
                NodeCode.FINISH_ONBOARDING_DECLINE -> EventPageTitle.DECLINE_PAGE
                else -> EventPageTitle.SUCCESS_PAGE
            }
        )
    }
}