package com.aai.core.utils

import android.app.Activity
import com.aai.core.R
import com.aai.core.popup.CommonPopup

fun showBackDialog(context: Activity, trigger: String) {
    CommonPopup(
        context = context,
        trigger = trigger,
        titleText = getTextFromAssets("dialog_back_title")
            ?: context.getString(R.string.dialog_back_title),
        contentText = getTextFromAssets("dialog_back_content")
            ?: context.getString(R.string.dialog_back_content),
        yesText = getTextFromAssets("dialog_back_yes")
            ?: context.getString(R.string.dialog_back_yes),
        noText = getTextFromAssets("dialog_back_no") ?: context.getString(R.string.dialog_back_no),
        noBtnBgColor = "#00000000",
        onNoClick = {
            context.finish()
        }
    ).show()
}

fun showInvalidImageDialog(context: Activity) {
    CommonPopup(
        context = context,
        titleText = getTextFromAssets("nationalid_warn")
            ?: context.getString(R.string.nationalid_warn),
        contentText = getTextFromAssets("nationalid_error_invalid_image_size") ?: context.getString(
            R.string.nationalid_error_invalid_image_size
        ),
        yesText = getTextFromAssets("osp_ok") ?: context.getString(R.string.osp_ok),
    ).show()
}
