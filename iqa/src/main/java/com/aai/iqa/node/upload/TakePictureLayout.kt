package com.aai.iqa.node.upload

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.aai.core.utils.screenWidth
import com.aai.core.utils.setContentFont
import com.aai.core.utils.toPx
import com.aai.core.views.RoundRectImageView
import com.aai.iqa.R

class TakePictureLayout(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    private var rlBg: RelativeLayout
    private var tvTip: TextView
    private var tvReplace: TextView
    private var ivPhoto: RoundRectImageView

    private var isFront = true
    private var clickListener: ((isFront: Boolean) -> Unit)? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.photo_item, this, true)
        rlBg = view.findViewById(R.id.rlBg)
        tvTip = view.findViewById(R.id.tvTip)
        tvReplace = view.findViewById(R.id.tvReplace)
        ivPhoto = view.findViewById(R.id.ivPhoto)

        rlBg.setOnClickListener {
            clickListener?.invoke(isFront)
        }
        tvReplace.setOnClickListener {
            clickListener?.invoke(isFront)
        }
        ivPhoto.setOnClickListener {
            clickListener?.invoke(isFront)
        }
    }

    fun setImageSize() {
        val width = screenWidth() - 32.toPx()
        val height = (screenWidth() - 32.toPx()) * 256 / 414
        val rlLp = rlBg.layoutParams
        rlLp.width = width
        rlLp.height = height
        val ivLp = ivPhoto.layoutParams
        ivLp.width = width
        ivLp.height = height
        rlBg.layoutParams = rlLp
        ivPhoto.layoutParams = ivLp
    }

    fun setElements(
        tipText: String,
        replaceText: String,
        isFront: Boolean,
        clickListener: (isFront: Boolean) -> Unit
    ) {
        setContentFont(tvTip, tipText, "center")
        setContentFont(tvReplace, replaceText)
        this.isFront = isFront
        this.clickListener = clickListener
        val paint = tvReplace.paint
        paint.isUnderlineText = true
    }

    fun setPhoto(bitmap: Bitmap) {
        rlBg.visibility = View.GONE
        ivPhoto.visibility = View.VISIBLE
        ivPhoto.setImageBitmap(bitmap)
        tvReplace.visibility = View.VISIBLE
    }

    fun reset() {
        ivPhoto.visibility = View.INVISIBLE
        rlBg.visibility = View.VISIBLE
        tvReplace.visibility = View.GONE
    }

}