package com.aai.core.utils

import android.content.Context
import com.aai.core.processManager.loading.SimpleLoading

class LoadingUtils(private val context: Context) {

    var loadingPopup = SimpleLoading(context)

    fun showLoading() {
        loadingPopup.show()
    }

    fun dismissLoading() {
        if (loadingPopup.isShow() == true) {
            loadingPopup.dismiss()
        }
    }

}