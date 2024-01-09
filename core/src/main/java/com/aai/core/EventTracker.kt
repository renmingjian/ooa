package com.aai.core

import android.view.View
import com.aai.core.processManager.model.NodeCode
import com.aai.core.utils.OSPLog
import org.json.JSONObject

class EventTracker {

    companion object {

        /**
         * 每次切换页面的时候需要调用
         */
        fun registerDynamicSuperProperties(map: MutableMap<String, Any?>?) {
            OSPLog.log("trackEvent-registerDynamicSuperProperties, map = $map")
            map?.let {

            }
        }

        fun trackEvent(eventName: String, map: MutableMap<String, Any?>?, view: View? = null) {
            try {

            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        private fun getJsonObjectByMap(map: MutableMap<String, Any?>?): JSONObject? {
            return if (map == null) null else {
                try {
                    val properties = JSONObject()
                    for ((key, value) in map) {
                        value?.let {
                            properties.put(key, it)
                        }
                    }
                    properties
                } catch (e: Throwable) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

}

/**
 * 页面中的一个属性
 */
object EventPageTitle {
    const val START_ONBOARDING = "Start onboarding"
    const val PENDING_PAGE = "Pending page"
    const val SUCCESS_PAGE = "Success page"
    const val DECLINE_PAGE = "Decline page"
    const val SELECT_COUNTRY = "Select country"
    const val SELECT_DOCUMENT_TYPE = "Select document type"
    const val ID_CARD = "ID Card"
    const val SUBMIT_AN_ID_DOCUMENT = "Submit an ID document"
    const val CONTINUE_ON_ANOTHER_DEVICE = "Continue on another device"
    const val SELECT_THE_ISSUING_COUNTRY = "Select the issuing country"
    const val UPLOAD_DOCUMENTS = "Upload documents"
    const val ENABLE_CAMERA = "Enable camera"
    const val RETRY = "Retry"
    const val FAILURE_PAGE = "Failure page"
    const val TAKE_A_SELFIE_PHOTO = "Take a selfie photo"
    const val I_AM_READY = "I am Ready"
    const val FULL_SCREEN = "Full screen"
    const val SEC_CAMERA_FEED = "Sec camera feed"
    const val CUSTOMNIZED = "Customized"
    const val RESULT_PAGE = "Result page"
    const val LOADING = "Loading"
}

/**
 * 埋点所属的页面的名称与上面的PageTitle不一样
 */
object EventPageName {

}

object EventName {
    const val CLICK_NEXT = "clickNext"
    const val CLICK_GO_BACK = "clickGoback"
    const val CAMERA_PERMISSION = "cameraPermission"
    const val PAGE_VIEW = "pageView"
}

object EventNodeName {
    const val SELFIE_VERIFICATION = "selfie verification"
    const val START_ONBOARDING = ""

    fun convertToNodeName(nodeCode: String) = when (nodeCode) {
        NodeCode.START_ONBOARDING -> START_ONBOARDING
        NodeCode.SELFIE_VERIFICATION -> SELFIE_VERIFICATION
        else -> ""
    }
}

object EventGoBackTrigger {
    const val CLICK_BUTTON = "click button"
    const val SLIDE = "slide"
}

object EventEnv {
    const val ID = "id"
    const val SG = "sg"
}