package com.aai.core.banner

import android.content.Context
import android.view.animation.Interpolator
import android.widget.Scroller

class ViewPagerScroller : Scroller {
    var scrollDuration = 360
    var isSudden = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, interpolator: Interpolator?) : super(context, interpolator)
    constructor(
        context: Context?, interpolator: Interpolator?,
        flywheel: Boolean
    ) : super(context, interpolator, flywheel)

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, if (isSudden) 0 else scrollDuration)
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
        super.startScroll(startX, startY, dx, dy, if (isSudden) 0 else scrollDuration)
    }
}