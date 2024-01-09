package com.aai.iqa.node.type

import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.aai.core.OSPSdk
import com.aai.core.mvvm.BaseViewModelFragment
import com.aai.core.processManager.FragmentJumper
import com.aai.core.processManager.model.ProcessEvent
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.setSubtitleFont
import com.aai.core.utils.textWithKey
import com.aai.core.views.TitleLayout
import com.aai.iqa.R
import com.aai.iqa.node.DocumentPageViewModel

class DocSelectTypeFragment : BaseViewModelFragment<DocumentPageViewModel>() {

    private val fragmentViewModel = DocTypeViewModel()
    private lateinit var rlRoot: View
    private lateinit var tvContent: TextView
    private lateinit var logo: ImageView
    private lateinit var typeListView: ListView
    private lateinit var titleLayout: TitleLayout

    override fun getLayoutId(): Int = R.layout.fragment_doc_select_type

    override fun initView(view: View) {
        rlRoot = view.findViewById(R.id.rlRoot)
        titleLayout = view.findViewById(R.id.titleLayout)
        tvContent = view.findViewById(R.id.tvContent)
        logo = view.findViewById(R.id.logo)
        typeListView = view.findViewById(R.id.typeListView)
    }

    override fun initData() {
        fragmentViewModel.viewModel = activityViewModel
        val documentType = activityViewModel.configParser.docPageConfig.pages.documentType
        setPageBackgroundColor(rlRoot)
        titleLayout.setElements(textWithKey(documentType.headerTitle, "doc_select_type_title"), true)
        titleLayout.backClick = { clickBack() }
        setSubtitleFont(tvContent, textWithKey(documentType.subTitle, "doc_select_type_subtitle"))
        setLogo(logo, activityViewModel.nodeCode)
        val currentCountry = activityViewModel.currentCountry
        val types = currentCountry?.types
        if (!types.isNullOrEmpty()) {
            activityViewModel.currentDocType = types[0]
            context?.let {
                val adapter = DocTypeAdapter(it, types)
                typeListView.adapter = adapter
                typeListView.setOnItemClickListener { _, _, position, _ ->
                    activityViewModel.currentDocType = types[position]
                    if (activity is FragmentJumper) {
                        (activity as FragmentJumper).jump(fragmentViewModel.getFragmentTag())
                    }
                    OSPSdk.instance.getProcessCallback()?.onEvent(
                        ProcessEvent.EVENT_DOCUMENT_TYPE_SELECT, mutableMapOf(
                            "country" to (textWithKey(activityViewModel.currentDocType?.labelKey, ""))
                        )
                    )
                }
            }
        }
    }

}