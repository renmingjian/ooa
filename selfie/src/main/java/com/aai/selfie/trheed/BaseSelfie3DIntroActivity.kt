package com.aai.selfie.trheed

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aai.core.EventGoBackTrigger
import com.aai.core.EventName
import com.aai.core.EventTracker
import com.aai.core.OSPSdk
import com.aai.core.banner.CustomBanner
import com.aai.core.camera.EnableCameraActivity
import com.aai.core.mvvm.BaseViewModelActivity
import com.aai.core.node.LoadingCallback
import com.aai.core.processManager.dataparser.OSPDataParser
import com.aai.core.processManager.model.BundleConst
import com.aai.core.processManager.model.OSPImage
import com.aai.core.processManager.model.OSPResponse
import com.aai.core.processManager.model.OSPSelfiePhotoComponentPage
import com.aai.core.processManager.model.OSPSessionToken
import com.aai.core.utils.BannerUtils
import com.aai.core.utils.ImageLoader
import com.aai.core.utils.OSPLog
import com.aai.core.utils.runOnUIThread
import com.aai.core.utils.screenWidth
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.setSubtitleFont
import com.aai.core.utils.showBackDialog
import com.aai.core.utils.showToast
import com.aai.core.utils.showToastByCode
import com.aai.core.utils.textWithKey
import com.aai.core.utils.toDP
import com.aai.core.utils.toPx
import com.aai.core.views.OSPButton
import com.aai.core.views.ResultLayout
import com.aai.core.views.TitleLayout
import com.aai.selfie.R
import com.aai.selfie.processors.Config
import com.aai.selfie.processors.LivenessCheckProcessor
import com.facetec.sdk.FaceTecSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException

abstract class BaseSelfie3DIntroActivity<T : Base3DViewModel<out OSPDataParser>> :
    BaseViewModelActivity<T>() {

    lateinit var bannerView: CustomBanner<String>
    private lateinit var btnGetStarted: OSPButton

    override fun layoutId(): Int = R.layout.activity_selfie_3d_intro_page
    override fun initView() {
        bannerView = findViewById(R.id.banner)
    }

    override fun initData() {
        super.initData()
        EventTracker.trackEvent(
            EventName.CAMERA_PERMISSION,
            mutableMapOf("toC_isPermission" to allPermissionsGranted())
        )
        val config = getConfig()
        getImages()?.let { images ->
            val list = images.toList().map { image ->
                image.imageUrl
            }
            val bannerUtils = BannerUtils()
            val map = mapOf(
                "selfie-photo.svg" to R.drawable.selfie_3d_intro,
            )
            val drawable = ContextCompat.getDrawable(this, R.drawable.selfie_3d_intro)
            // 计算图片的高度，先从接口取，如果接口没有，则按照图片的大小比做
            var height = config.props.height
            if (height == 0) {
                if (drawable != null) {
                    val ratio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight
                    height = ((screenWidth() - 16.toPx()) / ratio).toInt().toDP()
                }
            }
            if (height == 0) height = 300
            bannerUtils.setBannerData(bannerView, height, list, map)
        }

        val titleLayout = findViewById<TitleLayout>(R.id.titleLayout)
        val startPageSubTitle = findViewById<TextView>(R.id.startPageSubTitle)
        val rlRoot = findViewById<RelativeLayout>(R.id.rlRoot)
        btnGetStarted = findViewById(R.id.btnGetStarted)
        val logo = findViewById<ImageView>(R.id.logo)
        setPageBackgroundColor(rlRoot)
        titleLayout.setElements(textWithKey(config.headerTitle, "selfie_3d_intro_title"), true)
        titleLayout.backClick = {
            showBackDialog(
                this@BaseSelfie3DIntroActivity,
                EventGoBackTrigger.CLICK_BUTTON
            )
        }
        setSubtitleFont(startPageSubTitle, textWithKey(config.subTitle, "selfie_3d_intro_content"))
        setBtnEnable(false)
        setLogo(logo, viewModel.nodeCode)
        btnGetStarted.setOnClickListener {
            EventTracker.trackEvent(EventName.CLICK_NEXT, null)
            startSelfie()
        }
        initSDK()
        setFaceTecLanguage()
    }

    open fun getBannerHeight(): Int = getConfig().props.height

    /**
     * 设置FaceTec的语言
     */
    private fun setFaceTecLanguage() {
        GlobalScope.launch(Dispatchers.IO) {
            val strings = getTranslationsForCountry()
            if (strings.isNotEmpty()) {
                val faceTecStrings = mutableMapOf<Int, String>()
                for ((key, value) in strings) {
                    if (key.startsWith("FaceTec_")) {
                        val resId = resources.getIdentifier(key, "string", packageName)
                        if (resId != 0) {
                            faceTecStrings[resId] = value
                        }
                    }
                }
                if (faceTecStrings.isNotEmpty()) {
                    FaceTecSDK.setDynamicStrings(faceTecStrings)
                }
            }
        }
    }

    private fun startSelfie() {
        if (btnGetStarted.btnEnabled) {
            if (hasCameraPermission()) {
                to3DProcess()
            } else {
                val intent = Intent(this, EnableCameraActivity::class.java)
                viewModel.addExtraForCameraIntent(intent)
                startActivityForResult(intent, PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                val hasPermission = data?.getBooleanExtra(BundleConst.HAVE_PERMISSION, false)
                if (hasPermission == true) {
                    startSelfie()
                }
            } else if (requestCode == STATE_REQUEST_CODE) {
                commit()
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 中间图片展示的逻辑：
     * 当nodeCode = FACE_PHOTO时，接口是不返回images字段的，因为OSP平台不支持配置，但是此时UI中一定要展示图片。
     * 当nodeCode = SELFIE_VERIFICATION时，接口返回images字段，OSP可以配置，并且可以删除配置，也就是不显示图片。
     */
    abstract fun getImages(): List<OSPImage>?

    private fun setBtnEnable(enable: Boolean) {
        setCommonButtonTheme(
            btnGetStarted,
            text = textWithKey(getConfig().button, "selfie_3d_intro_btn"),
            enabled = enable
        )
    }

    abstract fun getConfig(): OSPSelfiePhotoComponentPage

    private fun to3DProcess() {
        getSessionToken()
    }

    private fun initSDK() {
        val config = Config.retrieveConfigurationWizardCustomization()
        FaceTecSDK.setCustomization(config)
        FaceTecSDK.setDynamicDimmingCustomization(config)
        FaceTecSDK.setLowLightCustomization(config)
        Config.initializeFaceTecSDKFromAutogeneratedConfig(
            this,
            object : FaceTecSDK.InitializeCallback() {
                override fun onCompletion(successful: Boolean) {
                    onSDKInitCompleted(successful)
                }
            })
    }

    private fun getTranslationsForCountry(): Map<String, String> {
        val translationsMap = mutableMapOf<String, String>()
        try {
            // 获取特定国家代码的翻译
            val countryTranslations =
                OSPSdk.instance.ospProcessorManager?.submitParser?.getAssetsLanguageObj()
            if (countryTranslations != null) {
                val keys = countryTranslations.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    translationsMap[key] = countryTranslations.getString(key)
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }
        return translationsMap
    }

    open fun onSDKInitCompleted(successful: Boolean) {
        OSPLog.log("selfie sdk init: $successful")
        runOnUIThread {
            setBtnEnable(successful)
        }
    }

    private fun getSessionToken() {
        viewModel.loadingCallback = object : LoadingCallback {
            override fun onLoading() {
                showLoading()
            }

            override fun onSuccess(response: OSPResponse) {
                dismissLoading()
                OSPLog.log("sessionToken success: $response")
                val data = response.data
                if (data is OSPSessionToken && data.success == true) {
                    viewModel.livenessCheckProcessor = LivenessCheckProcessor(
                        data.sessionToken,
                        viewModel.sdkToken,
                        this@BaseSelfie3DIntroActivity,
                        viewModel
                    )
                } else {
                    showToast(
                        this@BaseSelfie3DIntroActivity,
                        getString(com.aai.core.R.string.get_session_token_error)
                    )
                }
            }

            override fun onError(code: String, message: String) {
                OSPLog.log("sessionToken error: $message")
                dismissLoading()
                showToastByCode(this@BaseSelfie3DIntroActivity, code, message)
            }
        }
        viewModel.getSessionToken()
    }

    private fun allPermissionsGranted() = mutableListOf(
        Manifest.permission.CAMERA,
    ).toTypedArray().all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
        const val STATE_REQUEST_CODE = 101
    }

}