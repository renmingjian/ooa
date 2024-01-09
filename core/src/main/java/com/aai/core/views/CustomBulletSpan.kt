package com.aai.core.views

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.style.LeadingMarginSpan
import com.aai.core.utils.getThemeFont
import com.aai.core.utils.getTypeface

class CustomBulletSpan(
    private val color: Int,
    private val radius: Int,
    private val leadingMargin: Int,
    private val typeface: Typeface
) :
    LeadingMarginSpan {

    override fun getLeadingMargin(first: Boolean): Int {
        return radius * 2 + leadingMargin
    }

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int, text: CharSequence?,
        start: Int, end: Int, first: Boolean, layout: Layout?
    ) {
        if (first) {
            val style = paint.style
            val oldColor = paint.color

            paint.color = color
            paint.style = Paint.Style.FILL

            val y = (top + bottom) / 2f
            val cx = x + radius + 1
            canvas.drawCircle(cx.toFloat(), y, radius.toFloat(), paint)

            paint.style = style
            paint.color = oldColor
            paint.typeface = typeface
        }
    }
}