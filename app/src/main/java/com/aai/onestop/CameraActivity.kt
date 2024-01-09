package com.aai.onestop

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.aai.core.utils.CameraUtils2
import com.aai.core.utils.getFullScreenSize
import com.aai.core.utils.stateBar
import com.aai.core.views.CardOverlayView
import com.aai.core.views.GuardianCameraView

class CameraActivity : AppCompatActivity() {

    private lateinit var guardianCameraView: GlobalQACameraView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateBar(true)

        // 使状态栏和导航栏透明
        window.apply {
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }


        setContentView(R.layout.activity_camera)
        guardianCameraView = findViewById(R.id.guardianCameraView)
        guardianCameraView.open(CameraUtils2.getBackCameraId())
        val overlayView = findViewById<CardOverlayView>(R.id.overlayView)

        val preViewSize = guardianCameraView.mPreviewSize
        val lp = guardianCameraView.layoutParams
        lp.width = preViewSize.width
        lp.height = preViewSize.height
        guardianCameraView.layoutParams = lp
        val ivResult = findViewById<ImageView>(R.id.ivResult)
        guardianCameraView.setAspRatio(preViewSize.width, preViewSize.height)
        findViewById<View>(R.id.btnTake).setOnClickListener {
            val rectF = overlayView.getFrameRect()
            val screenSize = getFullScreenSize(this)

            val bitmapResultWidth = screenSize.y.toFloat() / preViewSize.height * preViewSize.width
            val widthRatio = bitmapResultWidth / screenSize.x
            val rect = Rect(
                (rectF.left.toInt() * widthRatio).toInt(),
                rectF.top.toInt(),
                (rectF.right.toInt() * widthRatio).toInt(),
                rectF.bottom.toInt()
            )
            println("rect: left = ${rect.left}, top = ${rect.top}, right = ${rect.right}, bottom = ${rect.bottom}")
            guardianCameraView.takeCropPhoto(guardianCameraView.cropParams.cropRect) { bitmap ->

                println("bitmapWidth = ${bitmap.width}, height = ${bitmap.height}, screenWidth =")
                ivResult.setImageBitmap(bitmap)
                val lp = ivResult.layoutParams
                lp.width = bitmap.width
                lp.height = bitmap.height
                ivResult.layoutParams = lp
            }
        }
    }

    override fun onResume() {
        super.onResume()
        guardianCameraView.post {
            val preViewSize = guardianCameraView.mPreviewSize
            println("preViewWidht = ${preViewSize.width}, height = ${preViewSize.height}")
            guardianCameraView.setAspRatio(preViewSize.width, preViewSize.height)

        }
    }

}