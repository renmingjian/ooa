package com.aai.core.processManager.model

import com.aai.core.BuildConfig

/**
 * 其他变量
 */
object Const {
    const val MAX_ROLLUP_TIMES = 10
}

/**
 * 网络访问使用的URL
 */
object UrlConst {
    private const val SUBMIT_PATH = "intl/openapi/sdk/v2/submit"
    private const val THEME_PATH = "intl/openapi/sdk/v2/getTheme"
    private const val CURRENT_NODE_PATH = "intl/openapi/sdk/v2/currentNode"
    private const val START_PAGE_COMMIT_PATH = "intl/openapi/sdk/v2/startOnBoarding/commit"
    private const val TWO_D_COMMIT_PATH = "intl/openapi/sdk/v2/selfie2dPhoto/commit"
    private const val RESULT_COMMIT_PATH = "intl/openapi/sdk/v2/finishOnBoarding/commit"
    private const val SESSION_TOKEN_PATH = "intl/openapi/sdk/v2/session-token"
    private const val LIVENESS_3D_PATH = "intl/openapi/sdk/v2/liveness-3d"
    private const val RETRY_RECORD_PATH = "intl/openapi/sdk/v2/liveness3d/retry/record"
    private const val SELFIE_VERIFICATION_COMMIT_PATH =
        "intl/openapi/sdk/v2/selfieVerification/commit"
    private const val SELFIE_CAPTURE_COMMIT_PATH = "intl/openapi/sdk/v2/selfie3dVideo/commit"
    private const val DOCUMENT_VERIFICATION_COMMIT_PATH =
        "intl/openapi/sdk/v2/documentVerification/commit"
    private const val DOCUMENT_VERIFICATION_LICENSE_PATH = "intl/openapi/sdk/v2/license"
    var currentEvn: String = BuildConfig.OSP_EVN
        set(value) {
            field = value
            SUBMIT_URL = "${getBaseUrl()}$SUBMIT_PATH"
            THEME_URL = "${getBaseUrl()}$THEME_PATH"
            CURRENT_NODE_URL = "${getBaseUrl()}$CURRENT_NODE_PATH"
            START_PAGE_COMMIT_URL =
                "${getBaseUrl()}$START_PAGE_COMMIT_PATH"
            TWO_D_COMMIT_URL =
                "${getBaseUrl()}$TWO_D_COMMIT_PATH"
            RESULT_COMMIT_URL =
                "${getBaseUrl()}$RESULT_COMMIT_PATH"
            SESSION_TOKEN = "${getBaseUrl()}$SESSION_TOKEN_PATH"
            LIVENESS_3D = "${getBaseUrl()}$LIVENESS_3D_PATH"
            RETRY_RECORD = "${getBaseUrl()}$RETRY_RECORD_PATH"
            SELFIE_VERIFICATION_COMMIT_URL =
                "${getBaseUrl()}$SELFIE_VERIFICATION_COMMIT_PATH"
            SELFIE_CAPTURE_COMMIT_URL = "${getBaseUrl()}$SELFIE_CAPTURE_COMMIT_PATH"
            DOCUMENT_VERIFICATION_COMMIT_URL = "${getBaseUrl()}$DOCUMENT_VERIFICATION_COMMIT_PATH"
            DOCUMENT_VERIFICATION_LICENSE_URL = "${getBaseUrl()}$DOCUMENT_VERIFICATION_LICENSE_PATH"
        }
    private val baseUrlMap = mutableMapOf(
        "sandbox" to "https://sandbox-oop.advai.net/",
        "uat" to "https://uat-oop-client.advai.net/",
        "production" to "https://uat-oop-client.advai.net/",
        "pre" to "https://pre-oop-client.advai.cn/",
        "dev" to "https://dev-oop-client.advai.cn/",
        "sg" to "https://sg-oop.advance.ai/",
        "id" to "https://id-oop.advance.ai/"
    )

    var SUBMIT_URL = "${getBaseUrl()}$SUBMIT_PATH"
        private set
    var THEME_URL = "${getBaseUrl()}$THEME_PATH"
    var CURRENT_NODE_URL = "${getBaseUrl()}$CURRENT_NODE_PATH"
    var START_PAGE_COMMIT_URL =
        "${getBaseUrl()}$START_PAGE_COMMIT_PATH"
    var TWO_D_COMMIT_URL =
        "${getBaseUrl()}$TWO_D_COMMIT_PATH"
    var RESULT_COMMIT_URL =
        "${getBaseUrl()}$RESULT_COMMIT_PATH"
    var SESSION_TOKEN = "${getBaseUrl()}$SESSION_TOKEN_PATH"
    var LIVENESS_3D = "${getBaseUrl()}$LIVENESS_3D_PATH"
    var RETRY_RECORD = "${getBaseUrl()}$RETRY_RECORD_PATH"
    var SELFIE_VERIFICATION_COMMIT_URL =
        "${getBaseUrl()}$SELFIE_VERIFICATION_COMMIT_PATH"
    var SELFIE_CAPTURE_COMMIT_URL = "${getBaseUrl()}$SELFIE_CAPTURE_COMMIT_PATH"
    var DOCUMENT_VERIFICATION_COMMIT_URL = "${getBaseUrl()}$DOCUMENT_VERIFICATION_COMMIT_PATH"
    var DOCUMENT_VERIFICATION_LICENSE_URL = "${getBaseUrl()}$DOCUMENT_VERIFICATION_LICENSE_PATH"

    fun getBaseUrl(): String = baseUrlMap[currentEvn] ?: ""
}

/**
 * 页面间传递数据所使用的key
 */
object BundleConst {
    const val SDK_TOKEN = "sdk_token"
    const val NODE_CODE = "node_code"
    const val NODE_FINISH_WHEN_COMPLETED = "node_finish_when_completed"
    const val NODE_DATA = "node_data"
    const val TITLE_KEY = "title_key"
    const val CONTENT_KEY = "content_key"
    const val BUTTON_KEY = "button_key"
    const val HAVE_PERMISSION = "have_permission"
    const val FRAGMENT_TAG = "fragment_tag"
    const val IS_FRONT = "is_front"
    const val PHOTO_METHOD = "photo_method"
    const val IS_SUCCESS = "is_success"
}

object NodeCode {
    const val START_ONBOARDING = "START_ONBOARDING"
    const val FACE_PHOTO = "FACE_PHOTO"
    const val SELFIE_VERIFICATION = "SELFIE_VERIFICATION"
    const val FINISH_ONBOARDING_SUCCESS = "FINISH_ONBOARDING_SUCCESS"
    const val FINISH_ONBOARDING_DECLINE = "FINISH_ONBOARDING_DECLINE"
    const val FINISH_ONBOARDING_PENDING = "FINISH_ONBOARDING_PENDING"
    const val DOCUMENT_VERIFICATION = "DOCUMENT_VERIFICATION"

    // 该code接口不会返回，SDK提供用户调用时使用，因为Selfie支持FACE_PHOTO(2D\3D)+SELFIE__VERIFICATION3种，
    // 给用户注册node节点时，不要提供多种，使用起来麻烦，给人乱的感觉。给用户注册时之需要一种code，所以自定义一个
    // code "selfie"，遇到注册的此code，就支持以上三种，SDK内部根据接口返回的nodeCode来对应找用户注册的node。
    const val SELFIE = "selfie"
    const val RESULT = "result"

    fun mappingNode(node: String): String = when (node) {
        FACE_PHOTO, SELFIE_VERIFICATION -> SELFIE
        FINISH_ONBOARDING_SUCCESS, FINISH_ONBOARDING_DECLINE, FINISH_ONBOARDING_PENDING -> RESULT
        else -> node
    }
}

object ResponseCode {
    const val CODE_SUCCESS = "SUCCESS"
    const val CODE_FAILED = "FAILED"
    const val MSG_IMAGE_INVALID = "MSG_IMAGE_INVALID"
    const val CUSTOM_CODE = "CUSTOM_ERROR_CODE"
}

object SelfieType {
    const val SELFIE_2D_PHOTO = "SELFIE_2D_PHOTO"
    const val SELFIE_3D_PHOTO = "SELFIE_3D_PHOTO"
}

object Typeface {
    const val ITALIANA = "italiana"
    const val ROBOTO = "roboto"
    const val SHARPSANS = "sharpSans"
}

object LogoShow {
    const val ALL_PAGES = "ALL_PAGES"
    const val START_PAGE = "START_PAGE"
    const val NO_PAGE = "NO_PAGE"
}

object ButtonStyle {
    const val FLAT = "flat"
    const val RAISED = "raised"
}

object ButtonTextTransform {
    // 首字母大写
    const val CAPITALIZE = "capitalize"

    // 全部大写
    const val UPPERCASE = "uppercase"

    // 全部小写
    const val LOWERCASE = "lowercase"

    // 不做处理
    const val NONE = "none"
}

enum class NodeState {
    NODE_STATE_SUCCESS,
    NODE_STATE_CANCELED
}

object CaptureMethods {
    const val FROM_LOCAL_FILE = "FROM_LOCAL_FILE" // 只上传文件
    const val TAKE_PHOTO = "TAKE_PHOTO" // 只拍照
    const val BOTH = "BOTH" // 两者都可以
}

/**
 * SDK回调事件的名称
 */
object ProcessEvent {
    const val EVENT_START = "start"
    const val EVENT_DOCUMENT_COUNTRY_SELECT = "document-country-select"
    const val EVENT_DOCUMENT_TYPE_SELECT = "document-type-select"
    const val EVENT_DOCUMENT_CAMERA_CAPTURE = "document-camera-capture"
    const val EVENT_DOCUMENT_UPLOAD = "document-upload"
    const val EVENT_SELFIE_CAMERA_CAPTURE = "selfie-camera-capture"
    const val EVENT_SELFIE_UPLOAD = "selfie-photo-upload"
    const val EVENT_ENABLE_CAMERA = "enable-camera"
    const val EVENT_RETRY = "retry"
    const val EVENT_FAILURE = "failure"
    const val EVENT_FORM = "form"
    const val EVENT_FORM_SUBMIT = "form-submit"
    const val EVENT_DECLINE = "decline"
    const val EVENT_SUCCESS = "success"
    const val EVENT_PENDING = "pending"
}

