package com.aai.core.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.util.TypedValue


fun Int.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
fun Int.toSp(): Int = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()


fun Float.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Float.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
fun Float.toSp(): Int = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()


fun Double.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Double.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
fun Double.toSp(): Int = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()

fun spToPx(sp: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().displayMetrics)

fun dpToPx(dp: Float): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, dp,
    Resources.getSystem().displayMetrics
).toInt()

fun screenWidth(): Int = Resources.getSystem().configuration.screenWidthDp.toPx()

// 获取屏幕的总高度，包括状态栏和导航栏的高度
fun getFullScreenSize(activity: Activity): Point {
    val display = activity.windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    return size
}

fun getStatusBarHeight(context: Context): Int {
    var result = 30.toPx()
    val resourceId: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = context.resources.getDimensionPixelSize(resourceId)
    }
    return result
}


