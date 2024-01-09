package com.aai.iqa.node.upload

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.os.bundleOf
import com.aai.core.OSPSdk
import com.aai.core.mvvm.BaseViewModelFragment
import com.aai.core.processManager.FragmentJumper
import com.aai.core.processManager.model.BundleConst
import com.aai.core.processManager.model.CaptureMethods
import com.aai.core.processManager.model.OSPCountryType
import com.aai.core.processManager.model.ProcessEvent
import com.aai.core.utils.runOnUIThread
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.showInvalidImageDialog
import com.aai.core.utils.showToast
import com.aai.core.utils.textWithKey
import com.aai.core.views.OSPButton
import com.aai.core.views.TitleLayout
import com.aai.iqa.R
import com.aai.iqa.node.DocumentNode
import com.aai.iqa.node.DocumentPageViewModel
import com.aai.iqa.node.SelectCaptureMethodPopup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class DocUploadFragment : BaseViewModelFragment<DocumentPageViewModel>() {

    private val fragmentViewModel = DocUploadViewModel()
    private lateinit var rlRoot: View
    private lateinit var logo: ImageView
    private lateinit var frontLayout: TakePictureLayout
    private lateinit var backLayout: TakePictureLayout
    private lateinit var btnNext: OSPButton
    private lateinit var llBackSideTip: LinearLayoutCompat
    private var isFront = true
    private var capturedMethod = CaptureMethods.BOTH
    private lateinit var titleLayout: TitleLayout

    override fun getLayoutId(): Int = R.layout.fragment_doc_upload

    override fun initView(view: View) {
        rlRoot = view.findViewById(R.id.rlRoot)
        titleLayout = view.findViewById(R.id.titleLayout)
        frontLayout = view.findViewById(R.id.frontLayout)
        backLayout = view.findViewById(R.id.backLayout)
        logo = view.findViewById(R.id.logo)
        btnNext = view.findViewById(R.id.btnNext)
        llBackSideTip = view.findViewById(R.id.llBackSideTip)
        btnNext.setOnClickListener {
            if (btnNext.btnEnabled) {
                activityViewModel.commit()
            }
        }
    }

    override fun initData() {
        capturedMethod = arguments?.getString(BundleConst.PHOTO_METHOD)
            ?: activityViewModel.configParser.docPageConfig.documentVerificationConfig.instructionConfig.imageCaptureMethods.mobileNative
        val documentUpload = activityViewModel.configParser.docPageConfig.pages.documentUpload
        setPageBackgroundColor(rlRoot)
        titleLayout.setElements(textWithKey(documentUpload.headerTitle, "doc_intro_title"), true)
        titleLayout.backClick = { clickBack() }
        setLogo(logo, activityViewModel.nodeCode)
        activityViewModel.currentDocType?.let { type ->
            if (!activityViewModel.haveBackPhoto()) {
                rlRoot.post {
                    frontLayout.setImageSize()
                    frontLayout.visibility = View.VISIBLE
                }
                setFront(
                    type,
                    "document_verification_tap_to_replace_image",
                    "doc_upload_replace_image"
                )
            } else {
                rlRoot.post {
                    frontLayout.setImageSize()
                    backLayout.setImageSize()
                    frontLayout.visibility = View.VISIBLE
                    backLayout.visibility = View.VISIBLE
                }
                setFront(
                    type,
                    "document_verification_tap_to_replace_image_front",
                    "doc_upload_replace_image_front"
                )
                setBack(type)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setBtnEnable()
    }

    private fun setBtnEnable() {
        val documentUpload = activityViewModel.configParser.docPageConfig.pages.documentUpload
        val haveBackPhoto = activityViewModel.haveBackPhoto()
        val enable = if (haveBackPhoto) {
            activityViewModel.frontBitmap != null && activityViewModel.backBitmap != null
        } else {
            activityViewModel.frontBitmap != null
        }
        setCommonButtonTheme(
            btnNext,
            text = textWithKey(documentUpload.button, "osp_next"),
            enabled = enable
        )
    }

    private fun setFront(type: OSPCountryType, replaceTipKey: String, replaceAssetsTipKey: String) {
        val text = replaceTemplateVariable(
            textWithKey(
                "id_phone_takephoto_tip_new",
                "doc_upload_tip_image_front"
            ), textWithKey(type.labelKey, "")
        )
        frontLayout.setElements(
            tipText = text,
            replaceText = textWithKey(replaceTipKey, replaceAssetsTipKey),
            isFront = true,
            clickListener = { isFront ->
                clickPictureEvent(isFront)
            }
        )
    }

    private fun setBack(type: OSPCountryType) {
        val text = replaceTemplateVariable(
            textWithKey(
                "id_phone_takephoto_backside_tip_new",
                "doc_upload_tip_image_back"
            ), textWithKey(type.labelKey, "")
        )
        backLayout.setElements(
            tipText = text,
            replaceText = textWithKey(
                "document_verification_tap_to_replace_image_end",
                "doc_upload_replace_image_back"
            ),
            isFront = false,
            clickListener = { isFront ->
                clickPictureEvent(isFront)
            }
        )
    }

    fun replaceTemplateVariable(template: String, replacement: String): String {
        // 正则表达式匹配 %{任意字符}
        val regex = Regex("%\\{.*?\\}")
        return template.replace(regex, replacement)
    }

    /**
     * 当点击上传图片的图标后，根据实际情况判断是要展示弹框让用户选择使用哪种方式上传还是直接进入目标页面进行上传
     */
    private fun clickPictureEvent(isFront: Boolean) {
        this.isFront = isFront
        when (capturedMethod) {
            CaptureMethods.BOTH -> showSelectMethodPopup()
            CaptureMethods.TAKE_PHOTO -> gotoCustomPhotoPage()
            else -> gotoSystemAlbum()
        }
    }

    private fun showSelectMethodPopup() {
        context?.let {
            val popup = SelectCaptureMethodPopup(it)
            popup.setText(
                textWithKey(
                    "nationalid_button_take_photo",
                    "doc_upload_popup_take_photo"
                ), textWithKey("upload_file", "doc_upload_popup_upload_file")
            )
            popup.onTakePhotoClick = {
                gotoCustomPhotoPage()
            }
            popup.onUploadPhotoClick = {
                gotoSystemAlbum()
            }
            popup.show(rlRoot)
        }
    }

    private fun gotoCustomPhotoPage() {
        if (isFront) activityViewModel.frontMethod =
            DocumentPageViewModel.METHOD_CAPTURED else activityViewModel.backMethod =
            DocumentPageViewModel.METHOD_CAPTURED
        if (activity is FragmentJumper) {
            val bundle = bundleOf(
                BundleConst.IS_FRONT to isFront
            )
            (activity as FragmentJumper).jump(DocumentNode.FRAGMENT_TAKE_PHOTO, bundle = bundle)
        }
        OSPSdk.instance.getProcessCallback()?.onEvent(
            eventName = ProcessEvent.EVENT_DOCUMENT_CAMERA_CAPTURE,
            params = mutableMapOf("type" to if (isFront) "front" else "back")
        )
    }

    private fun gotoSystemAlbum() {
        if (isFront) activityViewModel.frontMethod =
            DocumentPageViewModel.METHOD_UPLOAD else activityViewModel.backMethod =
            DocumentPageViewModel.METHOD_UPLOAD
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        OSPSdk.instance.getProcessCallback()?.onEvent(
            eventName = ProcessEvent.EVENT_DOCUMENT_UPLOAD,
            params = mutableMapOf("type" to if (isFront) "front" else "back")
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === PICK_IMAGE_REQUEST && resultCode === Activity.RESULT_OK && data != null && data.data != null) {

            val imageUri: Uri = data.data!!
            val mimeType = activity?.contentResolver?.getType(imageUri)
            try {
                if (mimeType?.startsWith("image/") == true) {
                    compressAndDisplayImage(imageUri)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                activity?.let {
                    showToast(it, "Please select a picture file")
                }
            }
        }
    }

    private fun compressAndDisplayImage(imageUri: Uri) {
        GlobalScope.launch(Dispatchers.IO) {
            val resultBitmap = fragmentViewModel.compressPhoto(context!!, imageUri)
            runOnUIThread {
                if (resultBitmap == null) {
                    // 图片分辨率或者大小不满足条件
                    activity?.let {
                        showInvalidImageDialog(activity!!)
                    }
                } else {
                    setPhoto(resultBitmap)
                }
            }
        }
    }

    fun setPhoto(bitmap: Bitmap) {
        val photoLayout = if (isFront) frontLayout else backLayout
        photoLayout.setPhoto(bitmap)
        // 保存图片并修改一些tip提示
        if (isFront) {
            if (activityViewModel.haveBackPhoto() && activityViewModel.backBitmap == null) {
                setShowBackTip(true)
            }
            activityViewModel.frontBitmap = bitmap
        } else {
            activityViewModel.backBitmap = bitmap
            setShowBackTip(false)
        }
        setBtnEnable()
    }

    private fun setShowBackTip(show: Boolean) {
        llBackSideTip.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun reset() {
        frontLayout.reset()
        backLayout.reset()
        setBtnEnable()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityViewModel.frontBitmap = null
        activityViewModel.backBitmap = null
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1001
    }

}