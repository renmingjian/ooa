package com.aai.iqa.node.intro

import android.app.ActionBar
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.aai.core.banner.CustomBanner
import com.aai.core.mvvm.BaseViewModelFragment
import com.aai.core.processManager.FragmentJumper
import com.aai.core.utils.BannerUtils
import com.aai.core.utils.ImageLoader
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.setSubtitleFont
import com.aai.core.utils.textWithKey
import com.aai.core.views.OSPButton
import com.aai.core.views.TitleLayout
import com.aai.iqa.R
import com.aai.iqa.node.DocumentPageViewModel

class DocIntroFragment : BaseViewModelFragment<DocumentPageViewModel>() {

    private val fragmentViewModel = DocIntroViewModel()
    private lateinit var rlRoot: View
    private lateinit var bannerView: CustomBanner<String>
    private lateinit var tvContent: TextView
    private lateinit var btnNext: OSPButton
    private lateinit var logo: ImageView
    private lateinit var titleLayout: TitleLayout
    private var imageSize = ActionBar.LayoutParams.WRAP_CONTENT

    override fun getLayoutId(): Int = R.layout.fragment_doc_intro

    override fun initView(view: View) {
        rlRoot = view.findViewById(R.id.rlRoot)
        tvContent = view.findViewById(R.id.tvContent)
        bannerView = view.findViewById(R.id.banner)
        btnNext = view.findViewById(R.id.btnNext)
        logo = view.findViewById(R.id.logo)
        titleLayout = view.findViewById(R.id.titleLayout)

        btnNext.setOnClickListener {
            val tag = fragmentViewModel.getFragmentTag()
            if (activity is FragmentJumper) {
                (activity as FragmentJumper).jump(tag)
            }
        }
    }

    override fun initData() {
        fragmentViewModel.viewModel = activityViewModel
        fragmentViewModel.ospDocConfig = activityViewModel.configParser.docPageConfig
        val howToUploadDocument = fragmentViewModel.ospDocConfig.pages.howToUploadDocument
        setPageBackgroundColor(rlRoot)
        titleLayout.setElements(
            textWithKey(howToUploadDocument.headerTitle, "doc_intro_title"),
            true
        )
        titleLayout.backClick = { clickBack() }
        setSubtitleFont(tvContent, textWithKey(howToUploadDocument.content, "doc_intro_content"))
        setCommonButtonTheme(btnNext, text = textWithKey(howToUploadDocument.button, "osp_next"))

        howToUploadDocument.images.let { images ->
            val list = images.toList().map { image ->
                image.imageUrl
            }
            val bannerUtils = BannerUtils()
            val map = mapOf(
                "document-verification.svg" to R.drawable.doc_intro,
            )
            bannerUtils.setBannerData(bannerView, howToUploadDocument.props.height, list, map)
        }
        setLogo(logo, activityViewModel.nodeCode)
    }

    private fun setBean(beans: List<String>) {
        val map = mapOf(
            "document-verification.svg" to R.drawable.doc_intro,
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