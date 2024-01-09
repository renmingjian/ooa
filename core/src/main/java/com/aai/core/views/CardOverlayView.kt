package com.aai.core.views

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.aai.core.utils.toPx

/**
 * 拍照页面需要使用的遮罩
 */
class CardOverlayView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    init {
        // 背景是透明的，需要添加下面代码才可以生效
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    private val paint = Paint().apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
    }

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#55333340")
        maskFilter = BlurMaskFilter(10F, BlurMaskFilter.Blur.NORMAL)
    }

    private val rectF = RectF()

    private val lefMargin = 8.toPx().toFloat()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.drawRect(0F, 0F, measuredWidth.toFloat(), measuredHeight.toFloat(), bgPaint)
        rectF.left = lefMargin
        rectF.right = measuredWidth - lefMargin
        rectF.top = 200.toPx().toFloat()
        rectF.bottom = rectF.top + (measuredWidth - lefMargin * 2) * 256 / 414
        canvas.drawRoundRect(rectF, 10.toPx().toFloat(), 10.toPx().toFloat(), paint)
        canvas.restore()
    }

    fun getFrameRect(): RectF = rectF

}