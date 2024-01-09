package com.aai.core.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.aai.core.R
import com.aai.core.utils.toPx


class RoundRectImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    AppCompatImageView(context, attrs, defStyle) {
    private val mPaint: Paint
    private var mCorner: Int

    // 是否画Filter
    private var mDrawFilter = true
    private var mTouch = false

    init {
        isClickable = true
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundRectImageView)
        mCorner = typedArray.getDimension(
            R.styleable.RoundRectImageView_image_radius, DEFAULT_CORNER.toPx().toFloat()
        ).toInt()
        typedArray.recycle()
    }

    /**
     * 设置圆角大小
     *
     * @param corner 单位为dp
     */
    fun setCorner(corner: Int) {
        mCorner = corner.toPx()
        invalidate()
    }

    /**
     * 设置触摸图片后是否显示选中的颜色
     */
    fun setDrawFilter(drawFilter: Boolean) {
        mDrawFilter = drawFilter
    }

    /**
     * 绘制圆角矩形图片
     */
    override fun onDraw(canvas: Canvas) {
        val drawable = drawable
        if (mDrawFilter && mTouch || drawable == null) {
            super.onDraw(canvas)
        } else {
            drawRound(canvas)
        }
    }

    /**
     * 画圆角图片
     */
    private fun drawRound(canvas: Canvas) {
        val drawable = drawable
        var bitmap: Bitmap? = null
        if (drawable is ColorDrawable) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(drawable.color)
        } else if (drawable is BitmapDrawable) {
            // 正常加载图片
            bitmap = drawable.bitmap
        }
        if (bitmap == null) {
            // 图片加载异常，例如URL链接错误，此时bitmap无法获取，不能draw图片，可以调用super.onDraw
            // 不过调用super的onDraw则不显示圆角，此时自己画一个圆角矩形
            val rect = RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
            mPaint.color = Color.parseColor("#dcdcdc")
            canvas.drawRoundRect(rect, mCorner.toFloat(), mCorner.toFloat(), mPaint)
            return
        }
        val roundBitmap = getRoundBitmap(bitmap, mCorner)
        val rectSrc = Rect(0, 0, roundBitmap.width, roundBitmap.height)
        val rectDest = Rect(0, 0, width, height)
        mPaint.reset()
        canvas.drawBitmap(roundBitmap, rectSrc, rectDest, mPaint)
    }

    /**
     * 裁剪图片
     */
    private fun getRoundBitmap(bitmap: Bitmap, corner: Int): Bitmap {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val rect = Rect(
            paddingLeft,
            paddingTop, bitmap.width - paddingRight, bitmap.height - paddingBottom
        )
        val rectF = RectF(rect)
        mPaint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        mPaint.color = Color.WHITE
        canvas.drawRoundRect(rectF, corner.toFloat(), corner.toFloat(), mPaint)
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, mPaint)
        return output
    }

    companion object {
        private const val DEFAULT_CORNER = 10f
    }
}
