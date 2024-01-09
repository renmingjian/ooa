package com.aai.iqa.node

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.aai.core.mvvm.BaseViewModel
import com.aai.core.network.ByteRequestBody
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.network.OSPRequestBody
import com.aai.core.processManager.model.BundleConst
import com.aai.core.processManager.model.CaptureMethods
import com.aai.core.processManager.model.OSPCountryType
import com.aai.core.processManager.model.OSPSupportCountry
import com.aai.core.processManager.model.UrlConst
import com.aai.core.utils.OSPLog
import com.aai.core.utils.hasAndNotNull
import com.aai.iqa.parser.OSPDocNodeDataParser
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class DocumentPageViewModel : BaseViewModel<OSPDocNodeDataParser>() {

    var attemptsRemain = 3
    var message = mutableListOf<String>()
    var fragmentTag = DocumentNode.FRAGMENT_SELECT_COUNTRY
    var currentCountry: OSPSupportCountry? = null
    var currentDocType: OSPCountryType? = null
    var frontBitmap: Bitmap? = null
    var backBitmap: Bitmap? = null
    var frontMethod = METHOD_UPLOAD
    var backMethod = METHOD_CAPTURED

    companion object {
        const val METHOD_UPLOAD = "UPLOAD"
        const val METHOD_CAPTURED = "CAPTURED"
    }

    override fun getCommitRequest(): NetRequest =
        NetRequest(
            url = UrlConst.DOCUMENT_VERIFICATION_COMMIT_URL,
            method = NetMethod.POST,
            queryParameters = mutableMapOf("sdkToken" to sdkToken),
            requestBody = getBody()
        )

    private fun getBody(): OSPRequestBody.OSPMultiPartRequestBody {
        val values = mutableListOf(
            ByteRequestBody(
                key = "countryName",
                value = currentCountry?.country ?: "",
                fileName = "",
                contentType = "text/plain"
            ),
            ByteRequestBody(
                key = "countryCode",
                value = currentCountry?.countryCode ?: "",
                fileName = "",
                contentType = "text/plain"
            ),
            ByteRequestBody(
                key = "documentType",
                value = currentDocType?.type ?: "",
                fileName = "",
                contentType = "text/plain"
            ),
            ByteRequestBody(
                key = "uploadMethods",
                value = if (haveBackPhoto()) "$frontMethod,$backMethod" else frontMethod,
                fileName = "",
                contentType = "text/plain"
            ),
            ByteRequestBody(
                key = "front",
                fileName = "front.png",
                contentType = "image/png",
                bytes = getByteArray(frontBitmap)
            ),
        )
        if (currentDocType?.pages?.size == 2) {
            values.add(
                ByteRequestBody(
                    key = "back",
                    fileName = "back.png",
                    contentType = "image/png",
                    bytes = getByteArray(backBitmap)
                )
            )
        }
        return OSPRequestBody.OSPMultiPartRequestBody(values = values)
    }

    private fun getByteArray(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) return null
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    override fun createParser(): OSPDocNodeDataParser = OSPDocNodeDataParser()

    override fun initData(bundle: Bundle) {
        super.initData(bundle)
        currentCountry =
            configParser.docPageConfig.documentVerificationConfig.instructionConfig.enableCountriesAndIdTypes[0]
        currentDocType = currentCountry!!.types[0]
        fragmentTag =
            bundle.getString(BundleConst.FRAGMENT_TAG) ?: DocumentNode.FRAGMENT_SELECT_COUNTRY
        val json = JSONObject(data)
        if (json.hasAndNotNull("attemptsRemain")) {
            attemptsRemain = json.getInt("attemptsRemain")
            OSPLog.log("document, attemptsRemain = ${json.getInt("attemptsRemain")}")
        }
        if (json.hasAndNotNull("message")) {
            val messageArray = json.getJSONArray("message")
            for (i in 0 until messageArray.length()) {
                message.add(messageArray.getString(i))
                OSPLog.log("document, message = ${messageArray.getString(i)}")
            }
        }
    }

    /**
     *  1.开启IQA，如果既可以拍照又可以上传，则展示一个弹框让用户选择，如果选择拍照则使用Microblink，否则进入Document Upload页面
     *  2.开启IQA，如果只可以拍照，则直接开启Microblink
     *  3.如果只可以上传或者没有使用IQA，则一律进入Document Upload页面。
     */
    fun getTagWhenDocTypeConfirmed(): String {
        val mobileNative =
            configParser.docPageConfig.documentVerificationConfig.instructionConfig.imageCaptureMethods.mobileNative
        return if (useIQA() && mobileNative == CaptureMethods.BOTH) {
            DocumentNode.IQA_SELECT
        } else if (useIQA() && mobileNative == CaptureMethods.TAKE_PHOTO) {
            DocumentNode.MICRO_BLINK
        } else {
            DocumentNode.FRAGMENT_DOC_UPLOAD // 进入Document upload页面
        }
    }

    fun addExtraForCameraIntent(intent: Intent) {
        val enableCameraPage = configParser.docPageConfig.pages.enableCameraPage
        intent.putExtra(BundleConst.TITLE_KEY, enableCameraPage.headerTitle)
        intent.putExtra(BundleConst.CONTENT_KEY, enableCameraPage.content)
        intent.putExtra(BundleConst.BUTTON_KEY, enableCameraPage.button)
        intent.putExtra(BundleConst.NODE_CODE, nodeCode)
    }

    fun useIQA(): Boolean =
        configParser.docPageConfig.documentVerificationConfig.instructionConfig.iqa.enabled

    fun haveBackPhoto() = currentDocType?.pages?.size == 2

}