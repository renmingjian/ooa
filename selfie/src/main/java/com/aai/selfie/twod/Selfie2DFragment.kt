package com.aai.selfie.twod

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.aai.core.EventName
import com.aai.core.EventTracker
import com.aai.core.mvvm.BaseViewModelFragment
import com.aai.core.utils.BitmapHandler
import com.aai.core.utils.OSPLog
import com.aai.core.utils.colorToInt
import com.aai.core.utils.getThemeBasic
import com.aai.core.utils.getThemeColor
import com.aai.core.utils.getThemeFont
import com.aai.core.utils.getTypeface
import com.aai.core.utils.runOnUIThread
import com.aai.core.utils.screenWidth
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.setSubtitleFont
import com.aai.core.utils.textWithKey
import com.aai.core.utils.toPx
import com.aai.core.views.CustomBulletSpan
import com.aai.core.views.FaceOverlayView
import com.aai.core.views.GuardianCameraView
import com.aai.core.views.OSPButton
import com.aai.core.views.TitleLayout
import com.aai.selfie.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Selfie2DFragment : BaseViewModelFragment<Selfie2DViewModel>() {

    private lateinit var logo: ImageView
    private lateinit var btnCapture: OSPButton
    private lateinit var overlayView: FaceOverlayView
    private lateinit var titleLayout: TitleLayout
    private var startTime: Long = 0
    private lateinit var guardianCameraView: GuardianCameraView

    override fun getLayoutId(): Int = R.layout.fragment_selfie_2d2

    override fun initView(view: View) {
        btnCapture = view.findViewById(R.id.btnCapture)
        logo = view.findViewById(R.id.logo)
        titleLayout = view.findViewById(R.id.titleLayout)
        overlayView = view.findViewById(R.id.overlayView)
        guardianCameraView = view.findViewById(R.id.guardianCameraView)
        val rlRoot = view.findViewById<RelativeLayout>(R.id.rlRoot)
        setPageBackgroundColor(rlRoot)
        overlayView.strokeColor = getThemeBasic().primaryColor.toColorInt()
        guardianCameraView.setAutoFocusEnable()
        guardianCameraView.postDelayed({
            overlayView.updateLocation()
        }, 100)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(), mutableListOf(
                    Manifest.permission.CAMERA,
                ).toTypedArray(), 111
            )
        }
    }

    private fun allPermissionsGranted() = mutableListOf(
        Manifest.permission.CAMERA,
    ).toTypedArray().all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            openCamera()
        }
        startTime = System.currentTimeMillis()
    }

    private fun openCamera() {
        guardianCameraView.openFrontCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        guardianCameraView.closeCamera()
    }

    /**
     * 拍照失败后会弹弹框，如果点击继续，则调用该方法，重新打开摄像头
     */
    fun continueProcess() {
        guardianCameraView.restartCamera()
    }

    override fun initData() {
        val twoDConfig =
            activityViewModel.configParser.ospSelfieConfig.pages.takeSelfiePhotoPage.component.SELFIE_2D_PHOTO
        titleLayout.setElements(
            textWithKey(twoDConfig.headerTitle, "doc_select_country_title"),
            true
        )
        titleLayout.backClick = {
            clickBack()
        }
        OSPLog.log("content2d = ${textWithKey(twoDConfig.content, "selfie_2d_content")}")
        setCommonButtonTheme(btnCapture, text = textWithKey(twoDConfig.button, "selfie_2d_capture"))
        setLogo(logo, activityViewModel.nodeCode)
        btnCapture.setOnClickListener {
            takePhoto()
            EventTracker.trackEvent(EventName.CLICK_NEXT, null)
        }
    }

    private fun takePhoto() {
        val rectF = overlayView.getFrameRect()
        val rect = Rect(
            screenWidth() / 8,
            32.toPx(),
            screenWidth() * 7 / 8,
            rectF.bottom.toInt()
        )

        val rectNew = Rect(
            0,
            rectF.top.toInt(),
            screenWidth(),
            rectF.bottom.toInt()
        )

        guardianCameraView.takeCropPhoto(rectNew) { bitmap ->
            runOnUIThread {
                guardianCameraView.stopPreviewAndCallBack()
                val file = saveBitmapToFile(bitmap, "${System.currentTimeMillis()}.jpg")
                if (file != null) {
                    val resultFile = BitmapHandler.compressImage(file, 50, "compress_${file.name}")
                    activityViewModel.imagePath = resultFile?.path
                    activityViewModel.commit()
                }
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, filename: String): File? {
        // 获取应用的文件目录，注意替换为适合你app的存储位置
        val parentDir = File(requireContext().filesDir, "osp")
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }
        val file = File(parentDir, filename)

        var fileOutputStream: FileOutputStream? = null
        try {
            // 创建文件输出流
            fileOutputStream = FileOutputStream(file)
            // 压缩并写入Bitmap到文件中
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            // 处理异常
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
                // 处理异常
            }
        }
        return null
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        OSPLog.log("onRequestPermissionsResult")
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 权限被授予
            openCamera()
        } else {
            OSPLog.log("permission-denied")
            clickBack()
        }
    }

}