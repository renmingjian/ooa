package com.aai.iqa.node.country

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import com.aai.core.OSPSdk
import com.aai.core.mvvm.BaseViewModelFragment
import com.aai.core.processManager.FragmentJumper
import com.aai.core.processManager.model.ProcessEvent
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.setSubtitleFont
import com.aai.core.utils.textWithKey
import com.aai.core.views.OSPButton
import com.aai.core.views.TitleLayout
import com.aai.iqa.R
import com.aai.iqa.node.DocumentPageViewModel

class DocSelectCountryFragment : BaseViewModelFragment<DocumentPageViewModel>() {

    private val fragmentViewModel = DocSelectCountryViewModel()
    private lateinit var rlRoot: View
    private lateinit var tvContent: TextView
    private lateinit var btnNext: OSPButton
    private lateinit var logo: ImageView
    private lateinit var countrySpinner: Spinner
    private lateinit var titleLayout: TitleLayout


    override fun getLayoutId(): Int = R.layout.fragment_doc_select_country

    override fun initView(view: View) {
        rlRoot = view.findViewById(R.id.rlRoot)
        tvContent = view.findViewById(R.id.tvContent)
        btnNext = view.findViewById(R.id.btnNext)
        logo = view.findViewById(R.id.logo)
        titleLayout = view.findViewById(R.id.titleLayout)
        countrySpinner = view.findViewById(R.id.countrySpinner)

        btnNext.setOnClickListener {
            val tag = fragmentViewModel.getFragmentTag()
            if (activity is FragmentJumper) {
                (activity as FragmentJumper).jump(tag)
            }
        }
    }

    override fun initData() {
        fragmentViewModel.viewModel = activityViewModel
        val docPageConfig = activityViewModel.configParser.docPageConfig
        val selectCountry = docPageConfig.pages.selectCountry
        val enableCountriesAndIdTypes =
            docPageConfig.documentVerificationConfig.instructionConfig.enableCountriesAndIdTypes
        setPageBackgroundColor(rlRoot)
        titleLayout.setElements(
            textWithKey(selectCountry.headerTitle, "doc_select_country_title"),
            true
        )
        titleLayout.backClick = { clickBack() }
        setSubtitleFont(
            tvContent,
            textWithKey(selectCountry.subTitle, "doc_select_country_subtitle")
        )
        setCommonButtonTheme(btnNext, text = textWithKey(selectCountry.button, "osp_next"))
        setLogo(logo, activityViewModel.nodeCode)

        val adapter = CountryAdapter(context!!, enableCountriesAndIdTypes)
        countrySpinner.adapter = adapter
        countrySpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                adapter.selectedIndex = position
                activityViewModel.currentCountry = enableCountriesAndIdTypes[position]
                OSPSdk.instance.getProcessCallback()?.onEvent(
                    ProcessEvent.EVENT_DOCUMENT_COUNTRY_SELECT, mutableMapOf(
                        "country" to (activityViewModel.currentCountry?.label ?: "")
                    )
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }


}