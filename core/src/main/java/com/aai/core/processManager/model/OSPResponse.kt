package com.aai.core.processManager.model

import android.graphics.Bitmap
import java.io.Serializable

class OSPResponse : Serializable {
    var code: String = ResponseCode.CODE_SUCCESS
    var message: String = ResponseCode.CODE_SUCCESS
    var data: Any = "{}"
    val transactionId: String? = null
    val datetime: String = ""
    val extra: Any? = null
    val timestamp: Long = 0
}

class OSPThemeData : Serializable {
    var color = OSPThemeColorData()
    var font = OSPThemeFontData()
    var buttons = OSPThemeButtonData()
    var advanced = OSPThemeAdvanceData()
    var basicSetting = OSPThemeBasicData()
}

class OSPThemeBasicData : Serializable {
    companion object {
        var logoBitmap: Bitmap? = null
    }
    // 轮播图圆点、首页Privacy Policy、enable Camera 图标
    // start page:
    var primaryColor: String = "#30B043"

    // Button的文字颜色
    var primaryButtonTextColor: String = "#FFFFFF"

    // Button的背景颜色
    var primaryButtonFillColor: String = "#30B043"

    // 页面的背景颜色
    var backgroundColor: String = "#FFFFFF"
    var buttonBorderRadius: String = "3rem"

    // Button文字和body文字
    // body文案有：doc节点选择country、type、Upload页面图片的tip、FaceTec try again页面中间文案
    var bodyFont: String = "sharpSans"
    var logo: String = ""
    var logoShow: String = LogoShow.START_PAGE
    var logoWidth: String = "11.6rem"
}

class OSPThemeColorData : Serializable {
    // 一些展位图图片中的颜色，例如首页轮播图、pending页面中某部分区域的颜色。
    // 图片边框颜色，例如FaceTec try again中边框的颜色
    var defaultImageFillColor: String = "#55C265"

    // success 页面展位图某部分区域的颜色
    var defaultSuccessImageFillColor: String = "#55C265"

    // Decline 页面展位图某部分区域的颜色
    var defaultDeclineImageFillColor: String = "#FF8D46"

    // 图标的填充颜色，例如选择doc type左侧图标的颜色
    var iconFillColor: String = "#DEF6E5"

    // 图标的部分颜色，例如选择doc type左侧图标中具体形状的颜色，iconFillColor是填充色，可以认为是背景色
    var iconStrokeColor: String = "#30B043"

    // success页面Button的填充色
    var successButtonFillColor: String = "#30B043"

    // success页面Button的文字颜色
    var successButtonTextColor: String = "#FFFFFF"

    // decline页面Button的填充色
    var declineButtonFillColor: String = "#F5F5F5"

    // decline页面Button的文字颜色
    var declineButtonTextColor: String = "#666666"

    // pending页面Button的填充色
    var pendingButtonFillColor: String = "#F5F5F5"

    // pending页面Button的文字颜色
    var pendingButtonTextColor: String = "#30B043"

    // title文案颜色
    var headingTextColor: String = "#333333"

    // subtitle文案颜色:目前页面中处于title下面或者title位置的图标下面的文案都是subtitle，不使用content
    var subtitleTextColor: String = "#333333"

    // body文案颜色，适用范围与body font保持一致
    var bodyTextColor: String = "#666666"

    // 小文案颜色，目前只有Start Page页面的Privacy Policy
    var smallTextColor: String = "#666666"
}

class OSPThemeFontData : Serializable {
    var headingFont: String = "sharpSans"
    var headingFontSize: String = "1.6rem"
    var headingFontWeight: String = "700"
    var subTitleFont: String = "sharpSans"
    var subTitleFontSize: String = "1.4rem"
    var bodyFontSize: String = "1.4rem"
    var smallTextFont: String = "sharpSans"
    var smallTextFontSize: String = "1.2rem"
    var buttonFontWeight: String = "700"

    // 只适用于body和small，对title和subtitle无用。
    var textAlign: String = "left"
}

class OSPThemeButtonData : Serializable {
    // Button是否带有阴影
    var buttonStyle: String = ButtonStyle.FLAT
    // Button文案大小写
    var buttonTextTransform: String = ButtonTextTransform.NONE
}

class OSPThemeAdvanceData : Serializable {
    // 目前有：首页Privacy Policy是否加下划线
    var link: String = "none"

    // 目前有：doc 选择country的背景圆角
    var modalBorderRadius: String = "0.4rem"
}

class OSPNodeData : Serializable {
    var completedFlag: Boolean? = null
    var transId: String? = null
    var nodeType: String? = null
    var nodeCode: String? = null
    var finalStatus: String? = null
    var message: MutableList<String>? = null

    var presetValue: Any? = null
    var nodeConfig: String? = null
    var waiting: Boolean? = null
    var attemptsRemain: Int? = null
    var retry: Boolean? = null
}

data class OSPCommitResult(
    val message: String?
) : Serializable

class OSPStartPageConfig : Serializable {
    var pageName: String = ""
    var button: String? = null
    var subTitle: String? = null
    var headerTitle: String? = null
    var displayLogo: Boolean? = null
    var images: MutableList<OSPImage>? = null
    var props = OSPProps()
}

class OSPImage : Serializable {
    var width: Int = 200
    var imageUrl: String = ""
    var imageName: String? = null
}

class OSPSelfieConfig : Serializable {
    var pageName: String = ""
    var selfieType: String = SelfieType.SELFIE_3D_PHOTO
    var displayLogo: Boolean = true
    var selfieCaptureConfig = OSPSelfieCaptureConfig()
    var pages = OSPSelfiePage()
}

class OSPSelfieVerificationConfig : Serializable {
    var pageName: String = ""
    var selfieType: String = SelfieType.SELFIE_3D_PHOTO
    var displayLogo: Boolean = true
    var selfieCaptureConfig = OSPSelfieCaptureConfig()
    var pages = OSPSelfieVerificationPage()
}

class OSPSelfieCaptureConfig : Serializable {
    var generalConfig: OSPSelfieGeneralConfig = OSPSelfieGeneralConfig()
}

class OSPSelfieGeneralConfig : Serializable {
    var maximumRetries: String = "3"
    var continueWithAnotherDevice: Boolean = true
}

class OSPSelfiePage : Serializable {
    var retryPage = OSPSelfieStatePage()
    var failurePage = OSPSelfieStatePage()
    var successPage = OSPSelfieStatePage()
    var cameraIssuePage = OSPSelfieStatePage()
    var enableCameraPage = OSPSelfieStatePage()
    var takeSelfiePhotoPage = OSPSelfiePhotoPage()
}

class OSPSelfieVerificationPage : Serializable {
    var retryPage = OSPSelfieStatePage()
    var failurePage = OSPSelfieStatePage()
    var successPage = OSPSelfieStatePage()
    var cameraIssuePage = OSPSelfieStatePage()
    var enableCameraPage = OSPSelfieStatePage()
    var takeSelfiePhotoPage = OSPSelfiePhotoComponentPage()
}

open class OSPSelfieStatePage : Serializable {
    var text: String? = null
    var button: String? = null
    var content: String? = null
    var pageName: String? = null
    var subTitle: String? = null
    var iconText_1: String? = null
    var iconText_2: String? = null
    var headerTitle: String? = null
}

class OSPSelfiePhotoPage : Serializable {
    var pageName: String? = null
    var selfieType: String? = null
    var component = OSPSelfiePhotoComponent()
}

class OSPSelfiePhotoComponent : Serializable {
    var SELFIE_2D_PHOTO = OSPSelfiePhotoComponentPage()
    var SELFIE_3D_VIDEO = OSPSelfiePhotoComponentPage()
}

class OSPSelfiePhotoComponentPage : Serializable {
    var pageName: String? = null
    var button: String? = null
    var content: String? = null
    var subTitle: String? = null
    var headerTitle: String? = null
    var images: MutableList<OSPImage>? = null
    var props = OSPProps()
}

class OSPResultConfig : Serializable {
    var pageName: String = ""
    var headerTitle: String? = null
    var subTitle: String? = null
    var button: String? = null
    var displayLogo: Boolean = true
    var images: MutableList<OSPImage>? = null
    var props = OSPProps()
}

class OSPProps: Serializable {
    var max: Int = 350
    var min: Int = 50
    var height: Int = 240
}

class OSPSessionToken : Serializable {
    var sessionToken: String? = null
    var success: Boolean? = null
}

class OSPDocConfig : Serializable {
    var pages = OSPDocumentPage()
    var skipSelectCountryIfPossible = true
    var skipSelectDocTypeIfPossible = true
    var documentVerificationConfig = OSPDocVerificationConfig()
}

class OSPDocumentPage : Serializable {
    var retryPage = OSPDocRetryPage()
    var failurePage = OSPSelfieStatePage()
    var documentType = OSPDocTypePage()
    var enableCameraPage = OSPSelfieStatePage()
    var selectCountry = OSPDocSelectCountryPage()
    var documentUpload = OSPSelfieStatePage()
    var howToUploadDocument = OSPHowToUploadPage()
}

class OSPDocRetryPage : OSPSelfieStatePage() {
    var images: MutableList<OSPImage> = mutableListOf()
    var contentData = OSPDocRetryContentData()
}

// 这里的值不要删除，代码是通过反射遍历属性列表来匹配的
class OSPDocRetryContentData : Serializable {
    var IQA_FAILED: String = ""
    var OTHER_REASON: String = ""
    var MAX_RETRY_EXCEED: String = ""
    var ID_FORGERY_FAILED: String = ""
    var CERTIFICATE_FAILED: String = ""
    var AGE_COMPARISON_FAILED: String = ""
    var NUMBER_CONSISTENCY_FAILED: String = ""
}

class OSPDocTypePage : OSPSelfieStatePage() {
    var skipSelectDocTypeIfPossible: Boolean = true
}

class OSPDocSelectCountryPage : OSPSelfieStatePage() {
    var iqaEnabled = true
    var cuntryCount = 1
    var skipSelectCountryIfPossible = true
    var supportCountries = mutableListOf<OSPSupportCountry>()
}

class OSPSupportCountry : Serializable {
    var id = 0
    var label: String = ""
    var country: String = ""
    var countryCode: String = ""
    var types = mutableListOf<OSPCountryType>()
}

class OSPCountryType : Serializable {
    var type = ""
    var labelKey = ""
    var pages = mutableListOf<String>()
}

class OSPHowToUploadPage : OSPSelfieStatePage() {
    var images: MutableList<OSPImage> = mutableListOf()
    var props = OSPProps()
}

class OSPDocVerificationConfig : Serializable {
    var instructionConfig = OSPDocInstructionConfig()
}

class OSPDocInstructionConfig : Serializable {
    var iqa = OSPDocIQA()
    var enableCountriesAndIdTypes = mutableListOf<OSPSupportCountry>()
    var imageCaptureMethods = OSPCaptureMethods()
}

class OSPDocIQA : Serializable {
    var enabled = true
}

class OSPCaptureMethods : Serializable {
    var mobileNative: String = CaptureMethods.BOTH
}

