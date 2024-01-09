package com.aai.selfie.trheed

import android.content.Intent
import android.os.Bundle
import com.aai.core.EventPageTitle
import com.aai.core.OSPSdk
import com.aai.core.processManager.model.BundleConst
import com.aai.core.processManager.model.UrlConst
import com.aai.core.utils.textWithKey
import com.aai.selfie.parser.SelfieConfigParser
import com.aai.selfie.processors.Config
import org.json.JSONObject

/**
 * nodeCode == FACE_PHOTO && selfieType == SELFIE_3D_PHOTO
 */
class Selfie3DViewModel : Base3DViewModel<SelfieConfigParser>() {

    override fun commitUrl(): String = UrlConst.SELFIE_CAPTURE_COMMIT_URL

    override fun initData(bundle: Bundle) {
        super.initData(bundle)
        val dataJson = JSONObject(data)
        val retry = dataJson.optJSONObject("nodeConfig")?.optJSONObject("selfieCaptureConfig")
            ?.optJSONObject("generalConfig")?.optString("maximumRetries")
        maxRetry = retry?.toIntOrNull() ?: 3
        selfieResultHandler.maxRetry = maxRetry
        val pages = configParser.ospSelfieConfig.pages
        selfieResultHandler.successKey =
            pages.successPage.subTitle ?: ""
        selfieResultHandler.failedKey = pages.failurePage.subTitle ?: ""
        errorMsg = textWithKey(selfieResultHandler.failedKey, "")
        val customization = Config.retrieveConfigurationWizardCustomization()
        customization.guidanceCustomization.retryScreenSubtextAttributedString =
            textWithKey(configParser.ospSelfieConfig.pages.retryPage.subTitle, "")
//        customization.guidanceCustomization.retryScreenSubtextAttributedString =
//            OSPSdk.instance.context.getString(com.aai.core.R.string.retry_times, maxRetry)
        trackSuperProperties(EventPageTitle.TAKE_A_SELFIE_PHOTO)
    }

    override fun createParser(): SelfieConfigParser = SelfieConfigParser()

    override fun createRetryHandler(): SelfieResultHandler = SelfieCaptureResultHandler()

    override fun addExtraForCameraIntent(intent: Intent) {
        val enableCameraPage = configParser.ospSelfieConfig.pages.enableCameraPage
        intent.putExtra(BundleConst.TITLE_KEY, enableCameraPage.headerTitle)
        intent.putExtra(BundleConst.CONTENT_KEY, enableCameraPage.content)
        intent.putExtra(BundleConst.BUTTON_KEY, enableCameraPage.button)
        intent.putExtra(BundleConst.NODE_CODE, nodeCode)
    }

}