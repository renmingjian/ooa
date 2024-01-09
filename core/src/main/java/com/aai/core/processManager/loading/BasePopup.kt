package com.aai.core.processManager.loading

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow

abstract class BasePopup(val context: Context, layoutId: Int) {

    var popupWindow: PopupWindow? = null
    private var view: View

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(layoutId, null)

        view.isFocusableInTouchMode = true
        view.isFocusable = true
        view.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // 当按下返回键时，拦截该事件，不做任何处理
                return@OnKeyListener handBack()
            }
            false
        })

        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        popupWindow = PopupWindow(
            view,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )
    }

    open fun handBack(): Boolean = true

    abstract fun initView(view: View)

    open fun show() {
        if (popupWindow?.isShowing == false) {
            popupWindow?.showAtLocation(
                (context as Activity).findViewById(android.R.id.content),
                Gravity.CENTER,
                0,
                0
            )
        }
        view.requestFocus()
        initView(view)
        initData()
    }

    open fun dismiss() {
        popupWindow?.dismiss()
        setActivityAlpha(1f)
    }

    fun setActivityAlpha(alpha: Float) {
        if (context is Activity) {
            val lp = context.window.attributes
            lp.alpha = alpha
            context.window.attributes = lp
        }
    }

    fun isShow() = popupWindow?.isShowing

    open fun initData() {

    }

}