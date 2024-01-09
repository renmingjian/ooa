package com.aai.iqa.node

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aai.core.utils.colorToInt
import com.aai.core.utils.getThemeBasic
import com.aai.core.utils.getThemeColor
import com.aai.core.utils.runOnUIThread
import com.aai.iqa.R
import kotlinx.coroutines.delay


class SelectCaptureMethodPopup(private val context: Context) : PopupWindow(context), PopupWindow.OnDismissListener {

    var onTakePhotoClick: (() -> Unit)? = null
    var onUploadPhotoClick: (() -> Unit)? = null
    private var tvTakePicture: TextView
    private var tvUploadPicture: TextView

    init {
        // Inflate the custom layout/view
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.capture_method_layout, null)
        tvTakePicture = popupView.findViewById(R.id.tvTakePicture)
        tvUploadPicture = popupView.findViewById(R.id.tvUploadPicture)
        val ivTakeSelected = popupView.findViewById<ImageView>(R.id.ivTakeSelected)
        val ivUploadSelected = popupView.findViewById<ImageView>(R.id.ivUploadSelected)
        val rlTakePicture = popupView.findViewById<RelativeLayout>(R.id.rlTakePicture)
        val rlUploadPicture = popupView.findViewById<RelativeLayout>(R.id.rlUploadPicture)
        contentView = popupView
        if (context is Activity) {
            val lp = context.window.attributes
            lp.alpha = 0.6f
            context.window.attributes = lp
        }

        rlTakePicture.setOnClickListener {
            tvTakePicture.setTextColor(colorToInt(getThemeBasic().primaryColor))
            modifyImageColor(ivTakeSelected)
            ivTakeSelected.visibility = View.VISIBLE
            runOnUIThread {
                delay(100)
                dismiss()
                delay(250)
                onTakePhotoClick?.invoke()
            }
        }
        rlUploadPicture.setOnClickListener {
            tvUploadPicture.setTextColor(colorToInt(getThemeBasic().primaryColor))
            modifyImageColor(ivUploadSelected)
            ivUploadSelected.visibility = View.VISIBLE
            runOnUIThread {
                delay(100)
                dismiss()
                delay(250)
                onUploadPhotoClick?.invoke()
            }
        }

        // Set width and height
        width = FrameLayout.LayoutParams.MATCH_PARENT
        height = FrameLayout.LayoutParams.MATCH_PARENT

        // Set focusable
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(Color.parseColor("#00000000")))
        // Set animation style
        isOutsideTouchable = true
        animationStyle = com.aai.core.R.style.PopupAnimation
        setOnDismissListener(this)
    }

    override fun onDismiss() {
        if (context is Activity) {
            val lp = context.window.attributes
            lp.alpha = 1f
            context.window.attributes = lp
        }
    }

    fun setText(takePhotoText: String, uploadPhotoText: String) {
        tvTakePicture.text = takePhotoText
        tvUploadPicture.text = uploadPhotoText
//        setHeadingFont(tvTakePicture, takePhotoText)
//        setHeadingFont(tvUploadPicture, uploadPhotoText)
    }

    private fun modifyImageColor(imageView: ImageView) {
        val drawable =
            ContextCompat.getDrawable(contentView.context, com.aai.core.R.drawable.icon_success)
        drawable!!.colorFilter =
            PorterDuffColorFilter(colorToInt(getThemeBasic().primaryColor), PorterDuff.Mode.SRC_IN)
        imageView.setImageDrawable(drawable)

    }

    // Function to show the popup window
    fun show(parentView: View) {
        showAtLocation(parentView, Gravity.BOTTOM, 0, 0)
    }
}