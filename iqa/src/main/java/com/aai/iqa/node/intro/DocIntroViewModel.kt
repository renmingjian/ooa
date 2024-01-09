package com.aai.iqa.node.intro

import com.aai.core.processManager.model.OSPDocConfig
import com.aai.iqa.node.DocumentNode
import com.aai.iqa.node.DocumentPageViewModel

class DocIntroViewModel {

    var ospDocConfig = OSPDocConfig()
    var viewModel: DocumentPageViewModel? = null

    fun getFragmentTag(): String {
        val config = ospDocConfig
        val enableCountriesAndIdTypes =
            config.documentVerificationConfig.instructionConfig.enableCountriesAndIdTypes

        return if (config.skipSelectCountryIfPossible && enableCountriesAndIdTypes.size == 1) { // 忽略国家选择
            val country = enableCountriesAndIdTypes[0]
            if (config.skipSelectDocTypeIfPossible && country.types.size == 1) { // 忽略type选择
                viewModel?.getTagWhenDocTypeConfirmed() ?: DocumentNode.FRAGMENT_DOC_UPLOAD
            } else {
                DocumentNode.FRAGMENT_SELECT_TYPE // 进入选择证件类型页面
            }
        } else {
            DocumentNode.FRAGMENT_SELECT_COUNTRY // 进入选择国家页面
        }
    }

}