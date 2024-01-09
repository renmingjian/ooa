package com.aai.selfie

import com.aai.core.processManager.OSPNode
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.SelfieType
import com.aai.core.utils.hasAndNotNull
import com.aai.selfie.trheed.Selfie3DIntroActivity
import com.aai.selfie.trheed.Selfie3DProcessInterceptor
import com.aai.selfie.trheed.Selfie3DVerificationIntroActivity
import com.aai.selfie.twod.Selfie2DActivity
import org.json.JSONObject

class SelfieNode : OSPNode() {
    override fun start() {
        val json = JSONObject(data)
        val nodeCode = json.getString("nodeCode")
        val config = json.optJSONObject("nodeConfig")?.toString()
        val jsonObjectConfig = JSONObject(config ?: "")
        val is2D = nodeCode == NodeCode.FACE_PHOTO
                && jsonObjectConfig.hasAndNotNull("selfieType")
                && jsonObjectConfig.optString("selfieType") == SelfieType.SELFIE_2D_PHOTO
        val targetActivityClass = if (is2D) {
            Selfie2DActivity::class.java
        } else {
            processInterceptor = Selfie3DProcessInterceptor(this)
            if (nodeCode == NodeCode.FACE_PHOTO) Selfie3DIntroActivity::class.java else Selfie3DVerificationIntroActivity::class.java
        }


        jump(
            targetActivity = targetActivityClass,
        )
    }

    override fun copy(): SelfieNode = SelfieNode()

    companion object {
        const val TAG = "Selfie"
        const val FRAGMENT_2D_INTRO = "FRAGMENT_2D_INTRO"
        const val FRAGMENT_2D = "FRAGMENT_2D"
    }

}