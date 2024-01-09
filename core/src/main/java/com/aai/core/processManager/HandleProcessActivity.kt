package com.aai.core.processManager

import android.content.Intent
import com.aai.core.OSPSdk
import com.aai.core.R
import com.aai.core.mvvm.BaseActivity
import com.aai.core.utils.OSPLog

class HandleProcessActivity : BaseActivity() {
    override fun layoutId(): Int = R.layout.activity_handle_process

    override fun initView() {

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        OSPLog.log("HandleProcessActivity--onNewIntent")
        OSPSdk.instance.ospProcessorManager?.endProcess()
        finish()
    }

    override fun initData() {
        super.initData()
        OSPLog.log("HandleProcessActivity--start")
        OSPSdk.instance.ospProcessorManager?.start(this)
    }
}