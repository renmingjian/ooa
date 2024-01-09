package com.aai.iqa.node.failure

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.aai.core.mvvm.BaseViewModelFragment
import com.aai.core.utils.setContentFont
import com.aai.core.utils.setHeadingFont
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.textWithKey
import com.aai.iqa.R
import com.aai.iqa.node.DocumentPageViewModel

/**
 * 失败页面，各个节点有不同，不需要统一
 */
class DocFailureFragment : BaseViewModelFragment<DocumentPageViewModel>() {

    private lateinit var rlRoot: View
    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var logo: ImageView
    private lateinit var ivIcon: ImageView

    override fun getLayoutId(): Int = R.layout.fragment_doc_failure

    override fun initView(view: View) {
        rlRoot = view.findViewById(R.id.rlRoot)
        ivIcon = view.findViewById(R.id.ivIcon)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvContent = view.findViewById(R.id.tvContent)
        logo = view.findViewById(R.id.logo)
    }

    override fun initData() {
        val failurePage = activityViewModel.configParser.docPageConfig.pages.failurePage
        setPageBackgroundColor(rlRoot)
        setHeadingFont(tvTitle, textWithKey(failurePage.subTitle, "selfie_3d_state_failed"))
        if (activityViewModel.message.isNotEmpty()) {
            val messages = activityViewModel.message.filter { it == "AGE_BELOW_LIMIT" }
            if (messages.isNotEmpty()) {
                tvContent.visibility = View.VISIBLE
                var message = textWithKey("exceedEngiteenYears", "doc_failure_content")
                if (messages.isEmpty()) {
                    message = resources.getString(com.aai.core.R.string.doc_failure_content)
                }
                setContentFont(tvContent, message)
            }
        }
        setLogo(logo, activityViewModel.nodeCode)
    }

}