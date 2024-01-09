package com.aai.core.utils

import android.content.Context
import android.widget.Toast
import com.aai.core.R
import com.aai.core.processManager.model.ResponseCode

fun showToastByCode(context: Context, code: String, message: String) {
    val error = if (code != ResponseCode.CODE_SUCCESS) {
        context.getString(R.string.net_work_error)
    } else message
    showToast(context, error)
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}