package com.aai.core.processManager.loading

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.aai.core.R
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.textWithKey
import com.aai.core.views.ResultLayout

class RefreshLoading(
    context: Context,
    private val refreshClick: () -> Unit,
    private val backClick: () -> Unit
) :
    BasePopup(context, R.layout.popup_refresh_loading) {

    private lateinit var resultLayout: ResultLayout
    private lateinit var ivBack: ImageView

    override fun initView(view: View) {
        resultLayout = view.findViewById(R.id.resultLayout)
        ivBack = view.findViewById(R.id.ivBack)
        val flRoot = view.findViewById<View>(R.id.flRoot)
        setPageBackgroundColor(flRoot)
        ivBack.setOnClickListener {
            backClick.invoke()
            dismiss()
        }
        resultLayout.setElements(
            titleText = textWithKey("loading_title", "refresh_loading_title"),
            contentText = textWithKey("loading_text", "refresh_loading_text"),
            imageUrl = ResultLayout.IMAGE_PENDING,
            nodeCode = "",
            btnText = textWithKey("loading_btntext", "refresh_loading_button"),
            btnClickListener = {
                // 点击refresh后会显示loading, 当前refresh页面不会销毁，重新loading后，不会重新轮询
                refreshClick.invoke()
            }
        )
    }

}