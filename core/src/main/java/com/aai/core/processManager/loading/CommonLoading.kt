package com.aai.core.processManager.loading

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.aai.core.EventPageTitle
import com.aai.core.EventTracker
import com.aai.core.OSPSdk
import com.aai.core.R
import com.aai.core.utils.OSPLog
import com.aai.core.utils.setHeadingFont
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.setSubtitleFont
import com.aai.core.utils.textWithKey

class CommonLoading(context: Context) : BasePopup(context, R.layout.popup_common_loading) {

    private lateinit var ivStateIcon: AppCompatImageView
    private lateinit var tvStateTitle: AppCompatTextView
    private lateinit var tvStateContent: AppCompatTextView
    private lateinit var rlRoot: RelativeLayout
    private lateinit var logo: AppCompatImageView
    private lateinit var rotationAnimator: ObjectAnimator
    private lateinit var alphaAnimator: ObjectAnimator

    override fun initView(view: View) {
        ivStateIcon = view.findViewById(R.id.ivStateIcon)
        tvStateTitle = view.findViewById(R.id.tvStateTitle)
        tvStateContent = view.findViewById(R.id.tvStateContent)
        logo = view.findViewById(R.id.logo)
        rlRoot = view.findViewById(R.id.rlRoot)
        rotationAnimator = ObjectAnimator.ofFloat(ivStateIcon, "rotation", 0f, 360f)
        alphaAnimator = ObjectAnimator.ofFloat(rlRoot, "alpha", 0f, 1f)
        val title = textWithKey("loading_title", "dialog_loading")
        OSPLog.log("loadingTitle = $title")
        val content = textWithKey("loading_wait_we_have_received_your_request", "dialog_loading_content")
        setLogo(logo, "")
        setPageBackgroundColor(rlRoot)
        setHeadingFont(tvStateTitle, title)
        setSubtitleFont(tvStateContent, content.ifEmpty { context.getString(R.string.dialog_loading_content) })
        EventTracker.registerDynamicSuperProperties(
            OSPSdk.instance.ospProcessorManager?.superProperties()
                ?.also { it["toC_pageTitle"] = EventPageTitle.LOADING })
        startAnim()
    }

    fun showContent(show: Boolean) {
        tvStateContent.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun startAnim() {
        rotationAnimator.duration = 1000
        rotationAnimator.repeatCount = ObjectAnimator.INFINITE
        rotationAnimator.interpolator = LinearInterpolator()
        rotationAnimator.start()

        alphaAnimator.duration = 600
        alphaAnimator.start()
    }

    override fun dismiss() {
        rotationAnimator.cancel()
        super.dismiss()
    }

}