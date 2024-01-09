package com.aai.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.aai.core.R
import com.aai.core.utils.getStatusBarHeight
import com.aai.core.utils.setHeadingFont
import com.aai.core.utils.toPx

class TitleLayout(context: Context, attributeSet: AttributeSet) :
    RelativeLayout(context, attributeSet) {

    private val ivBack: ImageView
    private val tvTitle: TextView
    private val placeHolder: View
    var backClick: (() -> Unit)? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.title_layout, this, true)
        ivBack = view.findViewById(R.id.ivBack)
        tvTitle = view.findViewById(R.id.tvTitle)
        placeHolder = view.findViewById(R.id.placeHolder)
        ivBack.setOnClickListener { backClick?.invoke() }
        val lp = placeHolder.layoutParams
        lp.height = getStatusBarHeight(context) + 16.toPx()
        placeHolder.layoutParams = lp
        initAttrs(context, attributeSet)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleLayout)
        val text = typedArray.getString(R.styleable.TitleLayout_osp_title_text)
        val showBack = typedArray.getBoolean(R.styleable.TitleLayout_osp_title_show_back, true)
        setElements(text, showBack)
        typedArray.recycle()
    }

    fun setElements(title: String?, showBack: Boolean = true) {
        setHeadingFont(tvTitle, title)
        ivBack.visibility = if (showBack) View.VISIBLE else View.GONE
    }

}