package com.aai.core.node.result

import android.app.ActionBar
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.aai.core.OSPSdk
import com.aai.core.R
import com.aai.core.banner.CustomBanner
import com.aai.core.mvvm.BaseViewModelActivity
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.ProcessEvent
import com.aai.core.utils.BannerUtils
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setDeclineButtonTheme
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.setPendingButtonTheme
import com.aai.core.utils.setSubtitleFont
import com.aai.core.utils.setSuccessButtonTheme
import com.aai.core.utils.textWithKey
import com.aai.core.views.OSPButton
import com.aai.core.views.ResultLayout
import com.aai.core.views.TitleLayout

class ResultPageActivity : BaseViewModelActivity<ResultPageViewModel>() {

    override val viewModel = ResultPageViewModel()
    private lateinit var rlRoot: View
    private lateinit var bannerView: CustomBanner<String>
    private lateinit var tvContent: TextView
    private lateinit var btnResult: OSPButton
    private lateinit var logo: ImageView
    private var imageSize = ActionBar.LayoutParams.WRAP_CONTENT

    override fun layoutId(): Int = R.layout.activity_result

    override fun initView() {
        rlRoot = findViewById(R.id.rlRoot)
        tvContent = findViewById(R.id.tvContent)
        bannerView = findViewById(R.id.banner)
        btnResult = findViewById(R.id.btnResult)
        logo = findViewById(R.id.logo)

        btnResult.setOnClickListener {
            viewModel.commit()
        }
    }

    override fun initData() {
        super.initData()
        setPageBackgroundColor(rlRoot)
        val config = viewModel.configParser.resultPageConfig
        setPageBackgroundColor(rlRoot)
        val titleLayout = findViewById<TitleLayout>(R.id.titleLayout)
        val title: String
        val content: String
        val btnText = textWithKey(config.button, "result_button")
        val eventName: String
        when (viewModel.nodeCode) {
            NodeCode.FINISH_ONBOARDING_SUCCESS -> {
                title = textWithKey(config.headerTitle, "result_success_title")
                content = textWithKey(config.subTitle, "result_success_content")
                setSuccessButtonTheme(btnResult, btnText)
                eventName = ProcessEvent.EVENT_SUCCESS
            }

            NodeCode.FINISH_ONBOARDING_DECLINE -> {
                title = textWithKey(config.headerTitle, "result_decline_title")
                content = textWithKey(config.subTitle, "result_decline_content")
                setDeclineButtonTheme(btnResult, btnText)
                eventName = ProcessEvent.EVENT_DECLINE
            }

            NodeCode.FINISH_ONBOARDING_PENDING -> {
                title = textWithKey(config.headerTitle, "result_pending_title")
                content = textWithKey(config.subTitle, "result_pending_content")
                setPendingButtonTheme(btnResult, btnText)
                eventName = ProcessEvent.EVENT_PENDING
            }

            else -> {
                title = textWithKey(config.headerTitle, "result_success_title")
                content = textWithKey(config.subTitle, "result_success_content")
                setCommonButtonTheme(btnResult, btnText)
                eventName = ProcessEvent.EVENT_SUCCESS
            }
        }
        titleLayout.setElements(title, showBack = false)
        setSubtitleFont(tvContent, content)

        config.images?.let { images ->
            val list = images.toList().map { image ->
                image.imageUrl
            }
            val bannerUtils = BannerUtils()
            val map = mapOf(
                ResultLayout.IMAGE_SUCCESS to R.drawable.result_success,
                ResultLayout.IMAGE_DECLINE to R.drawable.result_decline,
                ResultLayout.IMAGE_PENDING to R.drawable.result_pending,
            )
            bannerUtils.setBannerData(bannerView, config.props.height, list, map)
        }
        setLogo(logo, viewModel.nodeCode)

        OSPSdk.instance.getProcessCallback()?.onEvent(eventName)
    }

    override fun onBackPressed() {

    }
}