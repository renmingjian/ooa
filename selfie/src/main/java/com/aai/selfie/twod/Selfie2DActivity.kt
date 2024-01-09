package com.aai.selfie.twod

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.aai.core.mvvm.BaseViewModelActivity
import com.aai.core.popup.CommonPopup
import com.aai.core.processManager.FragmentJumper
import com.aai.core.processManager.model.ResponseCode
import com.aai.core.utils.getTextFromAssets
import com.aai.core.utils.setPageBackgroundColor
import com.aai.selfie.R
import com.aai.selfie.SelfieNode

class Selfie2DActivity : BaseViewModelActivity<Selfie2DViewModel>(), FragmentJumper {

    override val viewModel = Selfie2DViewModel()
    private lateinit var flContainer: FrameLayout

    override fun layoutId(): Int = R.layout.activity_2d_selfie

    override fun initView() {
        flContainer = findViewById(R.id.flContainer)
    }

    override fun initData() {
        super.initData()
        val rlRoot = findViewById<View>(R.id.flRoot)
        setPageBackgroundColor(rlRoot)
    }

    override fun beforeInflate() {
        super.beforeInflate()
        addFragment(Selfie2DIntroFragment())
    }

    private fun addFragment(fragment: Fragment) {
        val bt = supportFragmentManager.beginTransaction()
        bt.add(R.id.flContainer, fragment, SelfieNode.FRAGMENT_2D).addToBackStack(null).commit()
    }

    override fun onCommitError(code: String, message: String) {
        if (code == ResponseCode.MSG_IMAGE_INVALID) {
            CommonPopup(
                context = this,
                titleText = getTextFromAssets("selfie_2d_retry_title")
                    ?: getString(com.aai.core.R.string.selfie_2d_retry_title),
                contentText = getTextFromAssets("selfie_2d_retry_content")
                    ?: getString(com.aai.core.R.string.selfie_2d_retry_content),
                yesText = getTextFromAssets("selfie_2d_yes")
                    ?: getString(com.aai.core.R.string.selfie_2d_yes),
                noText = getTextFromAssets("selfie_2d_move_forward")
                    ?: getString(com.aai.core.R.string.selfie_2d_move_forward),
                onNoClick = {
                    viewModel.moveForward = true
                    viewModel.commit()
                },
                onDismiss = {
                    val fragment = supportFragmentManager.findFragmentByTag(SelfieNode.FRAGMENT_2D)
                    if (fragment is Selfie2DFragment) {
                        fragment.continueProcess()
                    }
                }
            ).show()
        } else {
            super.onCommitError(code, message)
        }
    }

    override fun jump(tag: String, bundle: Bundle?) {
        addFragment(Selfie2DFragment(), tag)
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
    }

}