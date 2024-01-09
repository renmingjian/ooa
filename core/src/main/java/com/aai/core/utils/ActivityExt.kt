package com.aai.core.utils

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat

inline fun Activity.stateBar(
    lightIcon: Boolean,
    fullScreen: Boolean = true,
    statusBarColor: Int = android.R.color.transparent
) {
    var flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        flag = if (lightIcon) View.SYSTEM_UI_FLAG_VISIBLE else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    if (fullScreen) {
        flag = flag or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    window.decorView?.systemUiVisibility = flag
    window.statusBarColor = ContextCompat.getColor(this, statusBarColor)
}