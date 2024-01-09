package com.aai.iqa.node.type

import com.aai.iqa.node.DocumentNode
import com.aai.iqa.node.DocumentPageViewModel

class DocTypeViewModel {

    var viewModel: DocumentPageViewModel? = null

    fun getFragmentTag(): String =
        viewModel?.getTagWhenDocTypeConfirmed() ?: DocumentNode.FRAGMENT_DOC_UPLOAD

}