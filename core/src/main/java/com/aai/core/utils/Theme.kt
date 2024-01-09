package com.aai.core.utils

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.aai.core.OSPSdk
import com.aai.core.R
import com.aai.core.processManager.model.LogoShow
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.OSPThemeAdvanceData
import com.aai.core.processManager.model.OSPThemeBasicData
import com.aai.core.processManager.model.OSPThemeButtonData
import com.aai.core.processManager.model.OSPThemeColorData
import com.aai.core.processManager.model.OSPThemeData
import com.aai.core.processManager.model.OSPThemeFontData
import com.aai.core.views.OSPButton

private fun getThemeData(): OSPThemeData? =
    OSPSdk.instance.ospProcessorManager?.themeParser?.themeData

fun getThemeBasic(): OSPThemeBasicData = getThemeData()?.basicSetting ?: OSPThemeBasicData()

fun getThemeColor(): OSPThemeColorData = getThemeData()?.color ?: OSPThemeColorData()

fun getThemeFont(): OSPThemeFontData = getThemeData()?.font ?: OSPThemeFontData()

fun getThemeButton(): OSPThemeButtonData = getThemeData()?.buttons ?: OSPThemeButtonData()

fun getThemeAdvance(): OSPThemeAdvanceData = getThemeData()?.advanced ?: OSPThemeAdvanceData()

// 后端下发的文字大小是类似于"1.5rem"的值，需要转成我们能用的数字，例如1.5rem -> 15
fun convertRemToFloat(rem: String, defaultValue: Float): Float {
    val sp = rem.filter { it.isDigit() || it == '.' }.toFloatOrNull()
    if (sp != null) {
        return sp * 10
    }
    return defaultValue
}

// 页面背景颜色
fun setPageBackgroundColor(view: View) {
    view.setBackgroundColor(colorToInt(getThemeBasic().backgroundColor))
}

// title文字
fun setHeadingFont(textView: TextView, text: String?) {
    val font = getThemeFont()
    setTextFont(
        textView = textView,
        text = text,
        textColor = getThemeColor().headingTextColor,
        textSize = font.headingFontSize,
        defaultTextSize = 18f,
        textFontWeight = font.headingFontWeight,
        textFontFamily = font.headingFont,
        textAlign = null
    )
}

// subTitle没有fontWeight，默认加粗，居左展示
fun setSubtitleFont(textView: TextView, text: String?) {
    val font = getThemeFont()
    setTextFont(
        textView = textView,
        text = text,
        textColor = getThemeColor().subtitleTextColor,
        textSize = font.subTitleFontSize,
        defaultTextSize = 16f,
        textFontWeight = "500",
        textFontFamily = font.subTitleFont,
        textAlign = "left"
    )
}

// 内容文字
fun setContentFont(
    textView: TextView,
    text: String?,
    customTextAlign: String? = null,
    fontWeight: String = "500"
) {
    val font = getThemeFont()
    val basic = getThemeBasic()
    setTextFont(
        textView = textView,
        text = text,
        textColor = getThemeColor().bodyTextColor,
        textSize = font.bodyFontSize,
        defaultTextSize = 14f,
        textFontWeight = fontWeight,
        textFontFamily = basic.bodyFont,
        textAlign = if (customTextAlign.isNullOrEmpty()) font.textAlign else customTextAlign
    )
}

// 小字体
fun setSmallFont(textView: TextView, text: String?, align: String? = null) {
    val font = getThemeFont()
    val textAlign = if (align.isNullOrEmpty()) font.textAlign else align
    OSPLog.log("textAlign = $textAlign")
    setTextFont(
        textView = textView,
        text = text,
        textColor = getThemeColor().smallTextColor,
        textSize = font.smallTextFontSize,
        defaultTextSize = 12f,
        textFontWeight = "500",
        textFontFamily = font.smallTextFont,
        textAlign = textAlign
    )
}

// 页面中所有button的通用theme
fun setBtnTheme(button: Button) {
    val basic = getThemeBasic()
    val drawable =
        ContextCompat.getDrawable(button.context, R.drawable.shape_button_bg) as? GradientDrawable
    drawable?.cornerRadius =
        convertRemToFloat(basic.buttonBorderRadius, 30f).toPx().toFloat()
    drawable?.colorFilter =
        PorterDuffColorFilter(Color.parseColor(basic.primaryColor), PorterDuff.Mode.SRC_IN)
    button.background = drawable
}

// 设置文字，如果字符串为空，则会默认使用xml中的文案，但是仍然会对其他theme进行设置，例如color等
fun setTextFont(
    textView: TextView,
    text: String?,
    textColor: String,
    textSize: String,
    defaultTextSize: Float,
    textFontWeight: String,
    textFontFamily: String,
    textAlign: String? = null,
) {
    if (!text.isNullOrEmpty()) {
        textView.text = text
    }
    textView.setTextColor(Color.parseColor(textColor))
    val textSizePx = convertRemToFloat(textSize, defaultTextSize)
    textView.textSize = textSizePx
    val fontWeight = textFontWeight.filter { it.isDigit() }.toIntOrNull() ?: 500
    // 安卓原生不支持细体，后台配置支持细体，在Android遇到细体就现实正常体
    val bold = fontWeight > 500
    textView.setTypeface(
        getTypeface(textFontFamily),
        if (bold) Typeface.BOLD else Typeface.NORMAL
    )
    textView.gravity = when (textAlign) {
        "center" -> Gravity.CENTER
        "left" -> Gravity.START
        "right" -> Gravity.END
        else -> Gravity.CENTER
    }
}

/**
 * 根据后端返回的字体枚举返回一个Typeface
 */
fun getTypeface(typeFace: String) = ResourcesCompat.getFont(
    OSPSdk.instance.options.context, when (typeFace) {
        com.aai.core.processManager.model.Typeface.ITALIANA -> R.font.italiana_regular
        com.aai.core.processManager.model.Typeface.ROBOTO -> R.font.roboto_regular
        else -> R.font.sharpsans_medium
    }
) ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

fun getDefaultTypeface() =
    getTypeface(com.aai.core.processManager.model.Typeface.SHARPSANS)

fun getDrawableWithColor(context: Context, @DrawableRes drawableId: Int): Drawable? {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    drawable?.colorFilter =
        PorterDuffColorFilter(
            Color.parseColor(getThemeBasic().primaryColor),
            PorterDuff.Mode.SRC_IN
        )
    return drawable
}

fun setButtonTheme(
    btn: OSPButton,
    color: String,
    disableColor: String = "#7F30B043",
    enabled: Boolean = true,
    text: String,
    textColor: String,
    textSize: Float,
    bold: Boolean
) {
    val button = getThemeButton()
    val basic = getThemeBasic()
    OSPLog.log("button radius = ${basic.buttonBorderRadius}")
    btn.setBgTheme(
        radius = convertRemToFloat(basic.buttonBorderRadius, 30F),
        color = color,
        disableColor = disableColor,
        enabled = enabled,
        hasBlur = button.buttonStyle != "flat"
    )
    btn.setTextTheme(
        text = text,
        textSize = textSize,
        textColor = textColor,
        textIsBold = bold,
        transform = button.buttonTextTransform,
        typeFace = getTypeface(basic.bodyFont),
    )
}

fun setCommonButtonTheme(btn: OSPButton, text: String, enabled: Boolean = true) =
    setButtonTheme(
        btn,
        color = getThemeBasic().primaryButtonFillColor,
        disableColor = addTransParentForColor(getThemeBasic().primaryButtonFillColor, alpha = 0.5F),
        enabled = enabled,
        text = text,
        textColor = getThemeBasic().primaryButtonTextColor,
        textSize = 14F,
        bold = true,
    )

fun setSuccessButtonTheme(btn: OSPButton, text: String, enabled: Boolean = true) =
    setButtonTheme(
        btn, color = getThemeColor().successButtonFillColor, enabled = enabled,
        text = text,
        textColor = getThemeColor().successButtonTextColor,
        textSize = 14F,
        bold = true,
    )

fun setDeclineButtonTheme(btn: OSPButton, text: String, enabled: Boolean = true) =
    setButtonTheme(
        btn, color = getThemeColor().declineButtonFillColor, enabled = enabled,
        text = text,
        textColor = getThemeColor().declineButtonTextColor,
        textSize = 14F,
        bold = true,
    )

fun setPendingButtonTheme(btn: OSPButton, text: String, enabled: Boolean = true) =
    setButtonTheme(
        btn, color = getThemeColor().pendingButtonFillColor, enabled = enabled,
        text = text,
        textColor = getThemeColor().pendingButtonTextColor,
        textSize = 14F,
        bold = true,
    )

fun setPopupNegativeButtonTheme(btn: OSPButton, text: String, bgColor: String, textColor: String) =
    setButtonTheme(
        btn,
        color = getThemeBasic().primaryButtonFillColor,
        disableColor = bgColor,
        enabled = false,
        text = text,
        textColor = textColor,
        textSize = 14F,
        bold = true,
    )

/**
 * logo：接口有时候返回一个SVG格式的base64数据，有时候返回一个https://... 这样的URL
 * 由于Android原生并不支持SVG，所以需要引入第三方库
 */
fun setLogo(imageView: ImageView, nodeCode: String) {
    val basic = getThemeBasic()
    val shouldShowLogo =
        basic.logoShow == LogoShow.ALL_PAGES || (basic.logoShow == LogoShow.START_PAGE && nodeCode == NodeCode.START_ONBOARDING)
    if (!shouldShowLogo) {
        imageView.visibility = View.INVISIBLE
        return
    }

    val lp = imageView.layoutParams
    lp.height = 50.toPx()
    imageView.layoutParams = lp

    val logo = basic.logo
    if (logo.startsWith("http")) {
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        ImageLoader.loadLogo(
            logo,
            imageView,
        )
        return
    }
    // 如果接口返回的不是Http开头的URL，则接口数据类型是SVG格式的数据，此时是默认的图片，Android原生不支持加载，需要
    // 引入第三方库。为避免使用第三方库，这里不使用接口数据，而使用客户端存储的logo
    imageView.setImageResource(R.drawable.osp_logo)
}

fun colorToInt(color: String) = Color.parseColor(color)

/**
 * 给一个String类型的color添加一个透明度
 * 页面中的颜色都是以十六进制的字符串返回的，根据需求会给某一个颜色添加透明度，由于这些颜色是在配置平台动态配置的，无法
 * 在客户端写死，所以需要动态添加透明度
 */
fun addTransParentForColor(color: String, alpha: Float = 0.5F): String {
    var colorString = color.replace("#", "")
    if (colorString.length < 6) return colorString
    val hexAlpha = (alpha * 255).toInt().toString(16)
    if (colorString.length == 8) { // 如果已经有透明度了，则需要改成需要的透明度
        colorString = colorString.substring(colorString.length - 6)
    }
    return "#$hexAlpha$colorString"
}

fun getTextFromAssets(key: String): String? =
    OSPSdk.instance.ospProcessorManager?.submitParser?.getValueFromAssets(key)