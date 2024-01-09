package com.aai.core.popup

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.aai.core.EventName
import com.aai.core.EventTracker
import com.aai.core.R
import com.aai.core.processManager.loading.BasePopup
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setContentFont
import com.aai.core.utils.setHeadingFont
import com.aai.core.utils.setPopupNegativeButtonTheme
import com.aai.core.views.OSPButton

class CommonPopup(
    context: Context,
    var trigger: String = "click button",
    var titleText: String? = null,
    var contentText: String? = null,
    var yesText: String? = null,
    var noText: String? = null,
    var noBtnBgColor: String? = null,
    var noBtnTextColor: String? = null,
    var onYesClick: (() -> Unit)? = null,
    var onNoClick: (() -> Unit)? = null,
    var onDismiss: (() -> Unit)? = null,
) : BasePopup(context, R.layout.popup_common) {
    private lateinit var commonPopupTitle: AppCompatTextView
    private lateinit var commonPopupContent: AppCompatTextView
    private lateinit var btnYes: OSPButton
    private lateinit var btnNo: OSPButton

    override fun initView(view: View) {
        commonPopupTitle = view.findViewById(R.id.commonPopupTitle)
        commonPopupContent = view.findViewById(R.id.commonPopupContent)
        btnYes = view.findViewById(R.id.btnYes)
        btnNo = view.findViewById(R.id.btnNo)
        btnYes.setOnClickListener {
            onYesClick?.invoke()
            event("stay on")
            dismiss()
            onDismiss?.invoke()
        }
        btnNo.setOnClickListener {
            onNoClick?.invoke()
            event("quit")
            dismiss()
        }
        popupWindow?.setOnDismissListener {
            event()
            setStatusBarColor(0)
        }
        invalidate()
        setStatusBarColor(153)
    }

    init {
        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.argb(153, 0, 0, 0))) // 50% 透明度的黑色
    }

    private fun setStatusBarColor(alpha: Int) {
        if (context is Activity) {
            context.window.statusBarColor = Color.argb(alpha, 0, 0, 0)
        }
    }

    private fun event(option: String? = null) {
        EventTracker.trackEvent(
            EventName.CLICK_GO_BACK,
            mutableMapOf("toC_goBackTrigger" to trigger, "toC_goBackOption" to option)
        )
    }

    private fun setTextAndVisible(view: TextView, text: String?, isTitle: Boolean) {
        if (text.isNullOrEmpty()) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
            if (isTitle) {
                view.text = text
                setHeadingFont(view, text)
            } else {
                setContentFont(view, text, "center")
            }
        }
    }

    private fun setBtnTextAndVisible(view: OSPButton, text: String?, enable: Boolean) {
        if (text.isNullOrEmpty()) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
        if (enable) {
            setCommonButtonTheme(view, text ?: "", true)
        } else {
            setPopupNegativeButtonTheme(
                view,
                text ?: "",
                bgColor = noBtnBgColor ?: "#F5F5F5",
                textColor = noBtnTextColor ?: "#666666"
            )
        }
    }

    private fun invalidate() {
        setTextAndVisible(commonPopupTitle, titleText, true)
        setTextAndVisible(commonPopupContent, contentText, false)
        setBtnTextAndVisible(btnYes, yesText, true)
        setBtnTextAndVisible(btnNo, noText, false)
    }

}