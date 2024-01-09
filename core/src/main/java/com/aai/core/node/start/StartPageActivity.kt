package com.aai.core.node.start

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.aai.core.EventName
import com.aai.core.EventTracker
import com.aai.core.OSPSdk
import com.aai.core.R
import com.aai.core.banner.CustomBanner
import com.aai.core.mvvm.BaseViewModelActivity
import com.aai.core.processManager.model.ProcessEvent
import com.aai.core.utils.BannerUtils
import com.aai.core.utils.getThemeAdvance
import com.aai.core.utils.getThemeBasic
import com.aai.core.utils.getThemeFont
import com.aai.core.utils.getTypeface
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.setSmallFont
import com.aai.core.utils.setSubtitleFont
import com.aai.core.utils.textWithKey
import com.aai.core.views.OSPButton
import com.aai.core.views.TitleLayout
import com.aai.core.webview.WebViewActivity

class StartPageActivity : BaseViewModelActivity<StartPageViewModel>() {

    private lateinit var bannerView: CustomBanner<String>
    private var imageSize = 0
    override val viewModel = StartPageViewModel()


    override fun layoutId(): Int = R.layout.activity_start_page
    override fun initView() {
        bannerView = findViewById(R.id.banner)
    }

    override fun initData() {
        super.initData()
        OSPSdk.instance.getProcessCallback()?.onEvent(ProcessEvent.EVENT_START)
        val config = viewModel.configParser.startPageConfig
        config.images?.let { images ->
            val list = images.toList().map { image ->
                image.imageUrl
            }
            val bannerUtils = BannerUtils()
            val map = mapOf(
                "start-1.svg" to R.drawable.start1,
                "start-2.svg" to R.drawable.start2,
                "start-3.svg" to R.drawable.start3,
            )
            bannerUtils.setBannerData(bannerView, config.props.height, list, map)
        }
        val rlRoot = findViewById<RelativeLayout>(R.id.rlRoot)
        val startPageSubTitle = findViewById<TextView>(R.id.startPageSubTitle)
        val btnGetStarted = findViewById<OSPButton>(R.id.btnGetStarted)
        val logo = findViewById<ImageView>(R.id.logo)
        val titleLayout = findViewById<TitleLayout>(R.id.titleLayout)
        titleLayout.setElements(
            textWithKey(config.headerTitle, "start_title")
        )
        titleLayout.backClick = { onBackPressed() }
        setPageBackgroundColor(rlRoot)
        setSubtitleFont(startPageSubTitle, textWithKey(config.subTitle, "start_subtitle"))
        setPrivacy()
        setCommonButtonTheme(btnGetStarted, text = textWithKey(config.button, "start_button"))
        setLogo(logo, viewModel.nodeCode)
        btnGetStarted.setOnClickListener {
            EventTracker.trackEvent(EventName.CLICK_NEXT, null)
            commit()
        }
    }

    private fun setPrivacy() {
        val tvPrivacy = findViewById<TextView>(R.id.tvPrivacy)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openPrivacy()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = getThemeAdvance().link == "underline"
                ds.typeface =
                    Typeface.create(getTypeface(getThemeFont().smallTextFont), Typeface.BOLD)
                ds.color = Color.parseColor(getThemeBasic().primaryColor)
            }
        }

        val privacy = textWithKey("start_onboarding_privacy_text", "start_privacy1")
        val privacyLink = textWithKey("start_onboarding_privacy_policy", "start_privacy2")
        val hole = privacy.trim() + " " + privacyLink
        setSmallFont(textView = tvPrivacy, text = hole, align = "left")
        val spannableString = SpannableString(hole)
        spannableString.setSpan(
            clickableSpan,
            privacy.length,
            hole.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvPrivacy.text = spannableString
        tvPrivacy.movementMethod = LinkMovementMethod.getInstance()
        tvPrivacy.highlightColor = Color.TRANSPARENT
    }

    private fun openPrivacy() {
        startActivity(WebViewActivity.newIntent(this, url = viewModel.getPrivacyUrl(assets)))
    }

}