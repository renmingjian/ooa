package com.aai.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.aai.core.R
import com.aai.core.processManager.model.NodeCode
import com.aai.core.utils.ImageLoader
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setContentFont
import com.aai.core.utils.setDeclineButtonTheme
import com.aai.core.utils.setHeadingFont
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPendingButtonTheme
import com.aai.core.utils.setSuccessButtonTheme

class ResultLayout(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    private var tvTitle: TextView
    private var tvContent: TextView
    private var ivResult: ImageView
    private var btnResult: OSPButton
    private var logo: ImageView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_result, this, true)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvContent = view.findViewById(R.id.tvContent)
        ivResult = view.findViewById(R.id.ivResult)
        btnResult = view.findViewById(R.id.btnResult)
        logo = view.findViewById(R.id.logo)
    }

    companion object {
        const val IMAGE_SUCCESS = "img-success.svg"
        const val IMAGE_DECLINE = "img-decline.svg"
        const val IMAGE_PENDING = "img-pending.svg"
    }

    fun setElements(
        titleText: String,
        contentText: String,
        imageUrl: String?,
        btnText: String,
        nodeCode: String,
        btnClickListener: () -> Unit
    ) {
        setHeadingFont(tvTitle, titleText)
        setContentFont(tvContent, contentText)
        setImage(imageUrl)
        when (nodeCode) {
            NodeCode.FINISH_ONBOARDING_SUCCESS -> setSuccessButtonTheme(btnResult, btnText)
            NodeCode.FINISH_ONBOARDING_DECLINE -> setDeclineButtonTheme(btnResult, btnText)
            NodeCode.FINISH_ONBOARDING_PENDING -> setPendingButtonTheme(btnResult, btnText)
            else -> setCommonButtonTheme(btnResult, btnText)
        }
        setLogo(logo, "")
        btnResult.setOnClickListener {
            btnClickListener.invoke()
        }
    }

    private fun setImage(imageUrl: String?) {
        val map = mapOf(
            IMAGE_SUCCESS to R.drawable.result_success,
            IMAGE_DECLINE to R.drawable.result_decline,
            IMAGE_PENDING to R.drawable.result_pending,
        )
        if (imageUrl.isNullOrEmpty()) {
            ivResult.setImageResource(R.drawable.result_success)
        } else {
            if (imageUrl.endsWith("svg")) {
                map[imageUrl]?.let {
                    ivResult.setImageResource(it)
                }
            } else {
                ImageLoader.load(imageUrl, ivResult)
            }
        }
    }

}