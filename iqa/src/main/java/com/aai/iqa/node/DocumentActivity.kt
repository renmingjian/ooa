package com.aai.iqa.node

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.aai.core.OSPSdk
import com.aai.core.mvvm.BaseViewModelActivity
import com.aai.core.processManager.FragmentJumper
import com.aai.core.processManager.model.BundleConst
import com.aai.core.processManager.model.CaptureMethods
import com.aai.core.processManager.model.ProcessEvent
import com.aai.core.utils.OSPLog
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.showToast
import com.aai.core.utils.textWithKey
import com.aai.iqa.R
import com.aai.iqa.node.country.DocSelectCountryFragment
import com.aai.iqa.node.failure.DocFailureFragment
import com.aai.iqa.node.intro.DocIntroFragment
import com.aai.iqa.node.retry.DocRetryFragment
import com.aai.iqa.node.retry.IRetryListener
import com.aai.iqa.node.takephoto.DocTakePhotoFragment
import com.aai.iqa.node.takephoto.ITakePictureListener
import com.aai.iqa.node.type.DocSelectTypeFragment
import com.aai.iqa.node.upload.DocUploadFragment
import com.microblink.blinkid.entities.recognizers.Recognizer
import com.microblink.blinkid.entities.recognizers.RecognizerBundle
import com.microblink.blinkid.entities.recognizers.blinkid.generic.BlinkIdMultiSideRecognizer
import com.microblink.blinkid.entities.recognizers.blinkid.generic.BlinkIdSingleSideRecognizer
import com.microblink.blinkid.entities.recognizers.successframe.SuccessFrameGrabberRecognizer
import com.microblink.blinkid.uisettings.ActivityRunner
import com.microblink.blinkid.uisettings.BlinkIdUISettings
import com.microblink.blinkid.util.RecognizerCompatibility
import com.microblink.blinkid.util.RecognizerCompatibilityStatus

/**
 * Document节点，里面包含的多个页面使用Fragment来做展示，由此Activity管理
 * 目前Microblink是直接跳转的SDK中的Activity，所以此页面不由Activity管理。之后UI会做完全定制，以后页面需要自己实现，
 * 之后的实现会使用Fragment
 */
class DocumentActivity : BaseViewModelActivity<DocumentPageViewModel>(), FragmentJumper,
    ITakePictureListener, IRetryListener {

    override val viewModel = DocumentPageViewModel()
    private lateinit var flContainer: FrameLayout
    private var currentFragment: Fragment? = null
    private var oneSideRecognizer: BlinkIdSingleSideRecognizer? = null
    private var twoSideRecognizer: BlinkIdMultiSideRecognizer? = null
    private var recognizerBundle: RecognizerBundle? = null

    override fun layoutId(): Int = R.layout.activity_document

    override fun initView() {
        flContainer = findViewById(R.id.flContainer)
    }

    override fun initData() {
        super.initData()
        val flRoot = findViewById<View>(R.id.flRoot)
        setPageBackgroundColor(flRoot)
        addFragment(getFragmentByTag(viewModel.fragmentTag), viewModel.fragmentTag)
    }

    private fun addFragment(fragment: Fragment, tag: String, bundle: Bundle? = null) {
        val preFragment = supportFragmentManager.findFragmentByTag(tag)
        val bt = supportFragmentManager.beginTransaction()
        // 要添加的Fragment跟目前的Fragment是同一个，先移除之前的，再添加新的
        if (preFragment != null) {
            bt.remove(preFragment)
        }
        if (bundle != null) {
            fragment.arguments = bundle
        }
        bt.add(R.id.flContainer, fragment, tag).addToBackStack(null).commit()
        currentFragment = fragment
    }

    private fun getFragmentByTag(tag: String): Fragment {
        return when (tag) {
            DocumentNode.FRAGMENT_INTRO -> DocIntroFragment()
            DocumentNode.FRAGMENT_SELECT_COUNTRY -> DocSelectCountryFragment()
            DocumentNode.FRAGMENT_SELECT_TYPE -> DocSelectTypeFragment()
            DocumentNode.FRAGMENT_DOC_UPLOAD -> DocUploadFragment()
            DocumentNode.FRAGMENT_RETRY -> DocRetryFragment()
            DocumentNode.FRAGMENT_TAKE_PHOTO -> DocTakePhotoFragment()
            DocumentNode.FRAGMENT_FAILURE -> DocFailureFragment()
            else -> DocIntroFragment()
        }
    }

    /**
     * 管理Document的跳转
     */
    override fun jump(tag: String, bundle: Bundle?) {
        when (tag) {
            DocumentNode.MICRO_BLINK -> { // 开启IQA & 只有拍照，进入mb流程
                viewModel.frontMethod = DocumentPageViewModel.METHOD_CAPTURED
                viewModel.backMethod = DocumentPageViewModel.METHOD_CAPTURED
                gotoMicroBlink()
            }

            DocumentNode.IQA_SELECT -> { // 开启IQA & 既可以拍照有可以上传，弹弹框让用户选择
                showIQASelectMethodPopup()
            }

            else -> { // 进入到Doc的其他页面
                if (tag == DocumentNode.FRAGMENT_FAILURE) { // 把其他Fragment给清空
                    val bt = supportFragmentManager.beginTransaction()
                    supportFragmentManager.fragments.forEach {
                        bt.remove(it)
                    }
                    bt.commitNow()
                    while (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStackImmediate()
                    }
                }
                addFragment(getFragmentByTag(tag = tag), tag, bundle)
            }
        }
    }

    override fun onPictureTake(bitmap: Bitmap) {
        val fragment = supportFragmentManager.findFragmentByTag(DocumentNode.FRAGMENT_DOC_UPLOAD)
        if (fragment is DocUploadFragment) {
            fragment.setPhoto(bitmap)
        }
    }

    private fun gotoMicroBlink() {
        val supported =
            when (val status = RecognizerCompatibility.getRecognizerCompatibilityStatus(this)) {
                RecognizerCompatibilityStatus.RECOGNIZER_SUPPORTED -> {
                    true
                }

                RecognizerCompatibilityStatus.NO_CAMERA -> {
                    showToast(this, "BlinkID is supported only via Direct API!")
                    false
                }

                RecognizerCompatibilityStatus.PROCESSOR_ARCHITECTURE_NOT_SUPPORTED -> {
                    showToast(this, "BlinkID is not supported on current processor architecture!")
                    false
                }

                else -> {
                    showToast(this, "BlinkID is not supported! Reason: " + status.name)
                    false
                }
            }
        if (!supported) return
        val isInitialized =
            OSPSdk.instance.ospProcessorManager?.currentNode?.initializer?.isInitialized == true
        if (isInitialized) {
            if (viewModel.haveBackPhoto()) {
                twoSideRecognizer = BlinkIdMultiSideRecognizer().also {
                    val success = SuccessFrameGrabberRecognizer(it)
                    it.setReturnFullDocumentImage(true)
                    it.setReturnFaceImage(true)
                    recognizerBundle = RecognizerBundle(success)
                }
            } else {
                oneSideRecognizer = BlinkIdSingleSideRecognizer().also {
                    val success = SuccessFrameGrabberRecognizer(it)
                    it.setReturnFullDocumentImage(true)
                    it.setReturnFaceImage(true)
                    recognizerBundle = RecognizerBundle(success)
                }
            }
            val settings = BlinkIdUISettings(recognizerBundle)
            ActivityRunner.startActivityForResult(this, REQUEST_CODE, settings)
        } else {
            // todo Microblink初始化失败
            showToast(this, "doc init failed")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                recognizerBundle?.loadFromIntent(data)

                val firstRecognizer: Recognizer<*> = recognizerBundle!!.recognizers[0]
                val successFrameGrabberRecognizer = firstRecognizer as SuccessFrameGrabberRecognizer
                if (viewModel.haveBackPhoto()) {
                    val blinkIdRecognizer =
                        successFrameGrabberRecognizer.slaveRecognizer as BlinkIdMultiSideRecognizer
                    val frontBitmap =
                        blinkIdRecognizer.result.fullDocumentFrontImage?.convertToBitmap()
                    val backBitmap =
                        blinkIdRecognizer.result.fullDocumentBackImage?.convertToBitmap()
                    OSPLog.log("resultState = ${blinkIdRecognizer.result.resultState}, frontBitmap = $frontBitmap, backBitmap = $backBitmap")
                    if (frontBitmap != null && backBitmap != null) {
                        viewModel.frontBitmap = frontBitmap
                        viewModel.backBitmap = backBitmap
                        viewModel.frontMethod = DocumentPageViewModel.METHOD_CAPTURED
                        viewModel.backMethod = DocumentPageViewModel.METHOD_CAPTURED
                        commit()
                    }
                } else {
                    val blinkIdRecognizer =
                        successFrameGrabberRecognizer.slaveRecognizer as BlinkIdSingleSideRecognizer
                    val bitmap = blinkIdRecognizer.result.fullDocumentImage?.convertToBitmap()
                    OSPLog.log("resultState = ${blinkIdRecognizer.result.resultState}, fullImage = $bitmap")
                    if (bitmap != null) {
                        viewModel.frontBitmap = bitmap
                        viewModel.frontMethod = DocumentPageViewModel.METHOD_CAPTURED
                        commit()
                    }
                }
            }
        }
    }

    /**
     * 当开启IQA并且既可以选择拍照又可以选择上传
     * 展示一个弹框，当用户点击拍照时，初始化Microblink，并且直接进入Microblink页面；当用户点击上传时
     * 进入Document Upload页面，在该页面再点击上传时不展示弹框，直接进入系统图片选择页面
     */
    private fun showIQASelectMethodPopup() {
        val popup = SelectCaptureMethodPopup(this)
        popup.setText(
            textWithKey("nationalid_button_take_photo", "doc_upload_popup_take_photo"),
            textWithKey("upload_file", "doc_upload_popup_upload_file")
        )
        popup.onTakePhotoClick = {
            gotoMicroBlink()
        }
        popup.onUploadPhotoClick = {
            jump(
                DocumentNode.FRAGMENT_DOC_UPLOAD,
                bundle = bundleOf(BundleConst.PHOTO_METHOD to CaptureMethods.FROM_LOCAL_FILE)
            )
        }
        popup.show(flContainer)
    }

    override fun tryAgain() {
        viewModel.frontBitmap = null
        viewModel.backBitmap = null
        val fragment = supportFragmentManager.findFragmentByTag(DocumentNode.FRAGMENT_DOC_UPLOAD)
        OSPLog.log("doc reset fragment = $fragment")
        if (fragment is DocUploadFragment) {
            OSPLog.log("doc reset")
            fragment.reset()
        }
    }

    companion object {
        const val REQUEST_CODE = 111
    }

}