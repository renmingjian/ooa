package com.aai.iqa.node.retry

import android.app.ActionBar
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.aai.core.OSPSdk
import com.aai.core.banner.CustomBanner
import com.aai.core.mvvm.BaseViewModelFragment
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.ProcessEvent
import com.aai.core.utils.ImageLoader
import com.aai.core.utils.JsonUtils
import com.aai.core.utils.OSPLog
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setHeadingFont
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.setSubtitleFont
import com.aai.core.utils.textWithKey
import com.aai.core.utils.toPx
import com.aai.core.views.OSPButton
import com.aai.iqa.R
import com.aai.iqa.node.DocumentPageViewModel

class DocRetryFragment : BaseViewModelFragment<DocumentPageViewModel>() {

    private lateinit var rlRoot: View
    private lateinit var bannerView: CustomBanner<String>
    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var btnNext: OSPButton
    private lateinit var logo: ImageView
    private var imageSize = ActionBar.LayoutParams.WRAP_CONTENT

    override fun getLayoutId(): Int = R.layout.fragment_doc_retry

    override fun initView(view: View) {
        rlRoot = view.findViewById(R.id.rlRoot)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvContent = view.findViewById(R.id.tvContent)
        bannerView = view.findViewById(R.id.banner)
        btnNext = view.findViewById(R.id.btnNext)
        logo = view.findViewById(R.id.logo)

        btnNext.setOnClickListener {
            if (activity is IRetryListener) {
                OSPLog.log("doc retry")
                (activity as IRetryListener).tryAgain()
            }
            clickBack()
        }
    }

    override fun initData() {
        val retryPage = activityViewModel.configParser.docPageConfig.pages.retryPage
        setPageBackgroundColor(rlRoot)
        OSPLog.log("retry message = ${activityViewModel.message}")
        // 设置retry信息
        if (activityViewModel.message.isNotEmpty()) {
            val messageKey = activityViewModel.message[0]
            val jsonObject = JsonUtils.convertObjToJson(retryPage.contentData)
            OSPLog.log("document, jsonObject = ${jsonObject}")
            if (jsonObject.has(messageKey)) {
                val messageKey2 = jsonObject.getString(messageKey)
                val text = textWithKey(messageKey2, "doc_retry_content")
                setSubtitleFont(tvContent, text)
            }
        }
        OSPLog.log("retry title = ${textWithKey(retryPage.headerTitle, "doc_retry_title") + " " + activityViewModel.attemptsRemain}")
        setHeadingFont(tvTitle, textWithKey(retryPage.headerTitle, "doc_retry_title") + " " + activityViewModel.attemptsRemain)
        setCommonButtonTheme(btnNext, text = textWithKey(retryPage.button, "doc_retry_button"))
        val images = retryPage.images
        bannerView.showIndicatorsAccordingSize(images.size)
        setBean(images.toList().map { image ->
            if (image.width > imageSize) {
                imageSize = image.width
            }
            image.imageUrl
        })
        bannerView.setBannerSize(imageSize + 16.toPx())
        setLogo(logo, activityViewModel.nodeCode)

        OSPSdk.instance.getProcessCallback()?.onEvent(
            eventName = ProcessEvent.EVENT_RETRY,
            params = mutableMapOf("source" to "document")
        )
    }

    private fun setBean(beans: List<String>) {
        val map = mapOf(
            "document-retry.svg" to R.drawable.doc_retry_image,
        )
        bannerView.setPages(object : CustomBanner.ViewCreator<String> {
            override fun createView(context: Context?, position: Int): View {
                val imageView = ImageView(context)
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                return imageView
            }

            override fun updateUI(context: Context?, view: View?, position: Int, t: String) {
                if (view is ImageView) {
                    if (t.endsWith("svg")) {
                        map[t]?.let {
                            view.setImageResource(it)
                        }
                    } else {
                        ImageLoader.load(t, view)
                    }
                }
            }
        }, beans)
            .startTurning(2000)
    }

}