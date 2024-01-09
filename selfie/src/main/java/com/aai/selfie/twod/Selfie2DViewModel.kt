package com.aai.selfie.twod

import android.os.Bundle
import com.aai.core.EventPageTitle
import com.aai.core.mvvm.BaseViewModel
import com.aai.core.network.ByteRequestBody
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.network.OSPRequestBody
import com.aai.core.processManager.model.UrlConst
import com.aai.selfie.parser.SelfieConfigParser
import java.io.File

class Selfie2DViewModel : BaseViewModel<SelfieConfigParser>() {

    var imagePath: String? = null
    var moveForward = false

    override fun createParser(): SelfieConfigParser = SelfieConfigParser()

    override fun getCommitRequest(): NetRequest = NetRequest(
        url = UrlConst.TWO_D_COMMIT_URL,
        method = NetMethod.POST,
        queryParameters = mutableMapOf("sdkToken" to sdkToken),
        requestBody = OSPRequestBody.OSPMultiPartRequestBody(
            values = getValues()
        )
    )

    private fun getValues(): List<ByteRequestBody> {
        val list = mutableListOf<ByteRequestBody>()
        list.add(
            ByteRequestBody(
                key = "facePhotoUri",
                file = File(imagePath ?: ""),
                fileName = File(imagePath ?: "").name,
                contentType = "image/jpeg"
            )
        )
        if (moveForward) {
            list.add(
                ByteRequestBody(
                    key = "forceNext",
                    value = "1",
                    contentType = "application/x-www-form-urlencoded",
                    fileName = ""
                )
            )
        }
        return list
    }

    override fun initData(bundle: Bundle) {
        super.initData(bundle)
        trackSuperProperties(EventPageTitle.TAKE_A_SELFIE_PHOTO)
    }

}