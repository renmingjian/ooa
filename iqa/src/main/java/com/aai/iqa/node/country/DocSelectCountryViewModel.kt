package com.aai.iqa.node.country

import com.aai.iqa.node.DocumentNode
import com.aai.iqa.node.DocumentPageViewModel

class DocSelectCountryViewModel {

    var viewModel: DocumentPageViewModel? = null

    fun getFragmentTag(): String {
        return if (viewModel?.configParser?.docPageConfig?.skipSelectDocTypeIfPossible == true
            && viewModel?.currentCountry?.types?.size == 1
        ) { // 忽略type选择
            viewModel?.getTagWhenDocTypeConfirmed() ?: DocumentNode.FRAGMENT_DOC_UPLOAD
        } else {
            DocumentNode.FRAGMENT_SELECT_TYPE // 进入选择证件类型页面
        }
    }

}