package com.aai.iqa.node.takephoto

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aai.core.camera.EnableCameraActivity
import com.aai.core.mvvm.BaseViewModelFragment
import com.aai.core.processManager.model.BundleConst
import com.aai.core.utils.OSPLog
import com.aai.core.utils.getFullScreenSize
import com.aai.core.utils.runOnUIThread
import com.aai.core.utils.textWithKey
import com.aai.core.utils.toPx
import com.aai.core.views.CardOverlayView
import com.aai.core.views.GuardianCameraView
import com.aai.iqa.R
import com.aai.iqa.node.DocumentPageViewModel


class DocTakePhotoFragment : BaseViewModelFragment<DocumentPageViewModel>() {

    private lateinit var guardianCameraView: GuardianCameraView
    private lateinit var rlOverlay: RelativeLayout
    private lateinit var ivTakePicture: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvPrompt: TextView
    private lateinit var overlayView: CardOverlayView

    override fun getLayoutId(): Int = R.layout.fragment_doc_take_picture

    override fun initView(view: View) {
        view.findViewById<ImageView>(com.aai.core.R.id.ivBack).setOnClickListener {
            clickBack()
        }
        rlOverlay = view.findViewById(R.id.rlOverlay)
        ivTakePicture = view.findViewById(R.id.ivTakePicture)
        guardianCameraView = view.findViewById(R.id.guardianCameraView)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvPrompt = view.findViewById(R.id.tvPrompt)
        overlayView = view.findViewById(R.id.overlayView)
        guardianCameraView.setAutoFocusEnable()
        ivTakePicture.setOnClickListener {
            val preViewSize = guardianCameraView.mPreviewSize
            val rectF = overlayView.getFrameRect()
            val screenSize = getFullScreenSize(requireActivity())

            val bitmapResultWidth = screenSize.y.toFloat() * preViewSize.width / preViewSize.height
            val widthRatio = bitmapResultWidth / screenSize.x
            val rect = Rect(
                (rectF.left.toInt()/* * widthRatio*/).toInt(),
                rectF.top.toInt(),
                (rectF.right.toInt() * widthRatio).toInt(),
                rectF.bottom.toInt()
            )
            println("rect: left = ${rect.left}, top = ${rect.top}, right = ${rect.right}, bottom = ${rect.bottom}")
            guardianCameraView.takeCropPhoto(rect) { bitmap ->
                runOnUIThread {
                    if (activity is ITakePictureListener) {
                        OSPLog.log("activity is $activity")
                        (activity as ITakePictureListener).onPictureTake(bitmap)
                        clickBack()
                    }
                }
            }
        }
    }

    override fun initData() {
        if (!hasCameraPermission()) {
            requestCameraPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission()) {
            openCamera()
        }
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), 111)
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity!!, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openCamera() {
        guardianCameraView.openBackCamera()
        // 从上一个页面进入Fragment，但是Fragment还没展示出来的时候，overlayView就已经出来了，会把前一个页面
        // 进行截取显示，所以延迟展示
        rlOverlay.postDelayed({
            rlOverlay.visibility = View.VISIBLE
            overlayView.visibility = View.VISIBLE
            val isFront = arguments?.getBoolean(BundleConst.IS_FRONT) ?: true
            ivTakePicture.postDelayed({
                tvPrompt.visibility = View.VISIBLE
                if (isFront) {
                    val title = "${
                        textWithKey(
                            "document_front",
                            "document_front"
                        )
                    } ${textWithKey("of_certificate", "of_certificate")}"
                    tvTitle.text = title
                    tvPrompt.text =
                        textWithKey("position_the_front_of_your_ceritifate_in_frame", "")
                } else {
                    val title =
                        "${textWithKey("document_back", "")} ${textWithKey("of_certificate", "")}"
                    tvTitle.text = title
                    tvPrompt.text = textWithKey("position_the_back_of_your_ceritifate_in_frame", "")
                }
                val rectF = overlayView.getFrameRect()
                val titleLP = tvTitle.layoutParams as RelativeLayout.LayoutParams
                titleLP.topMargin = (rectF.top - 44.toPx() - tvTitle.height).toInt()
                tvTitle.layoutParams = titleLP
                val promptLP = tvPrompt.layoutParams as RelativeLayout.LayoutParams
                promptLP.topMargin = (rectF.bottom + 36.toPx()).toInt()
                tvPrompt.layoutParams = promptLP
            }, 100)
        }, 1000)
    }

    override fun onPause() {
        super.onPause()
        overlayView.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        guardianCameraView.closeCamera()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        OSPLog.log("onRequestPermissionsResult")
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 权限被授予
            openCamera()
        } else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!,
                Manifest.permission.CAMERA
            )
        ) {
            OSPLog.log("permission-denied and do not ask again")
            // 用户拒绝权限请求，并且选择了“不再询问”
            val intent = Intent(activity, EnableCameraActivity::class.java)
            activityViewModel.addExtraForCameraIntent(intent)
            startActivityForResult(intent, 100)
        } else {
            OSPLog.log("permission-denied")
            clickBack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                val hasPermission = data?.getBooleanExtra(BundleConst.HAVE_PERMISSION, false)
                if (hasPermission == true) {
                    openCamera()
                }
            }
        }
    }

}