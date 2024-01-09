package com.aai.core.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.aai.core.utils.colorToInt
import com.aai.core.utils.getThemeBasic
import com.aai.core.utils.screenWidth
import com.aai.core.utils.toPx

/**
 * 拍照页面需要使用的遮罩
 */
class FaceOverlayView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var strokePaintWidth = 5.toPx().toFloat()
    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = strokePaintWidth
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#30B043")
        style = Paint.Style.STROKE
        strokeWidth = strokePaintWidth
    }

    private val rectF = RectF()
    private val rectFStroke = RectF()
    private var circleTop = 0
    private var circleBottom = 0
    var strokeColor = Color.parseColor("#30B043")
        set(value) {
            field = value
            invalidate()
        }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.drawColor(colorToInt(getThemeBasic().backgroundColor))
        canvas.drawOval(rectFStroke, strokePaint)
        canvas.drawOval(rectF, paint)
        canvas.restore()
    }

    fun updateLocation() {
        val location = IntArray(2)
        getLocationOnScreen(location)
        circleTop = 0
        val circleWidth = screenWidth() * 3 / 4
        val circleHeight = circleWidth * 242 / 182
        circleBottom = circleTop + circleHeight
        rectF.left = screenWidth() / 8F
        rectF.top = circleTop + strokePaintWidth / 2F
        rectF.right = screenWidth() * 7 / 8F
        rectF.bottom = circleBottom - strokePaintWidth / 2F
        rectFStroke.left = rectF.left
        rectFStroke.top = rectF.top
        rectFStroke.right = rectF.right
        rectFStroke.bottom = rectF.bottom
        invalidate()
    }

    fun getFrameRect() = rectF

    fun circleHeight(): Float = rectF.bottom - rectF.top
}