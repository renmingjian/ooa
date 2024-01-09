package com.aai.selfie.state

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.aai.core.OSPSdk
import com.aai.core.mvvm.BaseActivity
import com.aai.core.processManager.model.BundleConst
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.ProcessEvent
import com.aai.core.utils.runOnUIThread
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.setSubtitleFont
import com.aai.selfie.R
import kotlinx.coroutines.delay

/**
 * Selfie状态页面：Success | Failed
 */
class SelfieStateActivity : BaseActivity() {

    private lateinit var ivStateIcon: AppCompatImageView
    private lateinit var tvStateTitle: AppCompatTextView
    private lateinit var logo: AppCompatImageView

    override fun layoutId(): Int = R.layout.activity_state

    override fun initView() {
        ivStateIcon = findViewById(com.aai.core.R.id.ivStateIcon)
        tvStateTitle = findViewById(com.aai.core.R.id.tvStateTitle)
        logo = findViewById(com.aai.core.R.id.logo)
        findViewById<View>(R.id.ivBack).setOnClickListener { onBackPressed() }
    }

    override fun initData() {
        super.initData()
        val title = intent.getStringExtra(BundleConst.TITLE_KEY)
        val isSuccess = intent.getBooleanExtra(BundleConst.IS_SUCCESS, true)
        val nodeCode = intent.getStringExtra(BundleConst.NODE_CODE)
        val rlRoot = findViewById<View>(com.aai.core.R.id.rlRoot)
        setPageBackgroundColor(rlRoot)
        ivStateIcon.setImageResource(if (isSuccess) com.aai.core.R.drawable.icon_success else com.aai.core.R.drawable.icon_failed)
        setLogo(logo, nodeCode ?: NodeCode.SELFIE_VERIFICATION)
        setSubtitleFont(tvStateTitle, title)
        if (isSuccess) {
            runOnUIThread {
                delay(2000)
                val intent = Intent()
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        if (!isSuccess) {
            OSPSdk.instance.getProcessCallback()?.onEvent(
                eventName = ProcessEvent.EVENT_FAILURE,
                params = mutableMapOf("source" to "selie")
            )
        }
    }

    override fun onBackPressed() {
        OSPSdk.instance.ospProcessorManager?.endProcess()
        finish()
    }

}