package com.aai.core.processManager.loading

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView
import com.aai.core.R

class SimpleLoading(context: Context) : BasePopup(context, R.layout.popup_simple_loading) {

    private lateinit var ivStateIcon: AppCompatImageView
    private  var rotationAnimator: ObjectAnimator? = null

    override fun initView(view: View) {
        ivStateIcon = view.findViewById(R.id.ivStateIcon)
        rotationAnimator = ObjectAnimator.ofFloat(ivStateIcon, "rotation", 0f, 360f)
        startAnim()
    }

    private fun startAnim() {
        rotationAnimator?.duration = 1000
        rotationAnimator?.repeatCount = ObjectAnimator.INFINITE
        rotationAnimator?.interpolator = LinearInterpolator()
        rotationAnimator?.start()
    }

    override fun dismiss() {
        rotationAnimator?.cancel()
        super.dismiss()
    }

}