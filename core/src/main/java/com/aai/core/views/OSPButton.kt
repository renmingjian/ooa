package com.aai.core.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.aai.core.R
import com.aai.core.processManager.model.ButtonTextTransform
import com.aai.core.utils.OSPLog
import com.aai.core.utils.getDefaultTypeface
import com.aai.core.utils.spToPx
import com.aai.core.utils.toPx

class OSPButton(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
        typeface =
            Typeface.create(getDefaultTypeface(), Typeface.BOLD)
    }
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    var text: String = ""

    private val rect = RectF()
    var radius: Float = 30.toPx().toFloat()
        set(value) {
            field = value
            invalidate()
        }
    var btnEnabled = true

    init {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OSPButton)
        text = typedArray.getString(R.styleable.OSPButton_buttonText) ?: ""
        btnEnabled = typedArray.getBoolean(R.styleable.OSPButton_buttonEnabled, true)
        val buttonSelectedColor =
            typedArray.getColor(
                R.styleable.OSPButton_buttonSelectedColor,
                context.resources.getColor(R.color.primaryColor)
            )
        val buttonUnSelectedColor =
            typedArray.getColor(
                R.styleable.OSPButton_buttonUnSelectedColor,
                context.resources.getColor(R.color.primaryButtonUnavailableFillColor)
            )
        bgPaint.color = if (btnEnabled) buttonSelectedColor else buttonUnSelectedColor
        val textSize = typedArray.getDimension(R.styleable.OSPButton_buttonTextSize, 14f)
        textPaint.textSize = spToPx(textSize)

        radius = typedArray.getDimension(R.styleable.OSPButton_buttonRadius, 30f)
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.save()
        drawBg(canvas)
        drawText(canvas)
        canvas?.restore()
    }

    private fun drawBg(canvas: Canvas?) {
        rect.left = 5.toPx().toFloat()
        rect.top = 5.toPx().toFloat()
        rect.right = measuredWidth.toFloat() - 5.toPx().toFloat()
        rect.bottom = measuredHeight.toFloat() - 5.toPx().toFloat()
        if (bgPaint.maskFilter != null) {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
        canvas?.drawRoundRect(rect, radius.toPx().toFloat(), radius.toPx().toFloat(), bgPaint)
    }

    private fun drawText(canvas: Canvas?) {
        val metrics = Paint.FontMetrics()
        textPaint.getFontMetrics(metrics)
        val offset = (metrics.ascent + metrics.descent) / 2
        val baseLine = (measuredHeight shr 1) - offset
        canvas?.drawText(text, measuredWidth / 2F, baseLine, textPaint)
    }

    fun setBgTheme(
        radius: Float,
        color: String,
        disableColor: String,
        enabled: Boolean,
        hasBlur: Boolean
    ) {
        this.radius = radius
        val needColor = if (enabled) color else disableColor
        this.btnEnabled = enabled
        val colorInt = Color.parseColor(needColor)
        bgPaint.color = colorInt
        if (hasBlur) {
            bgPaint.setShadowLayer(5.toPx().toFloat(), 0f, 0f, colorInt)
        }
        OSPLog.log("btnEnabled = $enabled")
        invalidate()
    }

    fun setBgColor(color: Int) {
        bgPaint.color = color
        invalidate()
    }

    fun setTextTheme(
        text: String,
        textSize: Float,
        textColor: String,
        textIsBold: Boolean,
        transform: String,
        typeFace: Typeface
    ) {
        OSPLog.log("transform = $transform")
        val resultText = when (transform) {
            ButtonTextTransform.CAPITALIZE -> text.capitalizeWords()
            ButtonTextTransform.LOWERCASE -> text.lowercase()
            ButtonTextTransform.UPPERCASE -> text.uppercase()
            else -> text
        }
        if (resultText.isNotBlank()) {
            this.text = resultText
        }

        textPaint.textSize = spToPx(textSize)
        textPaint.color = Color.parseColor(textColor)
        textPaint.typeface =
            Typeface.create(typeFace, if (textIsBold) Typeface.BOLD else Typeface.NORMAL)
        invalidate()
    }

}

fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it ->
    it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}