package com.aai.core.mvvm

import android.os.Bundle
import com.aai.core.EventGoBackTrigger
import com.aai.core.EventName
import com.aai.core.EventTracker
import com.aai.core.OSPSdk
import com.aai.core.node.LoadingCallback
import com.aai.core.processManager.OSPNodeResult
import com.aai.core.processManager.dataparser.OSPDataParser
import com.aai.core.processManager.loading.SimpleLoading
import com.aai.core.processManager.model.OSPResponse
import com.aai.core.processManager.model.ResponseCode
import com.aai.core.utils.OSPLog
import com.aai.core.utils.showBackDialog
import com.aai.core.utils.showToastByCode

abstract class BaseViewModelActivity<T : BaseViewModel<out OSPDataParser>> : BaseActivity(),
    OSPDateUpdate, ViewModelProvider<T> {

    abstract val viewModel: T
    private lateinit var loadingPopup: SimpleLoading
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nodeList = OSPSdk.instance.ospProcessorManager?.nodeList
        if (!nodeList.isNullOrEmpty()) {
            nodeList.last().activity = this
        }

        loadingPopup = SimpleLoading(this)
        observe()
    }

    override fun getActivityViewModel(): T = viewModel

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        val duration = System.currentTimeMillis() - startTime
        EventTracker.trackEvent(EventName.PAGE_VIEW, mutableMapOf("toC_eventDuration" to duration))
    }


    override fun initData() {
        intent.extras?.let {
            viewModel.initData(it)
        }
    }

    override fun updateOSPData(bundle: Bundle) {
        OSPLog.log("updateOSPData")
        viewModel.initData(bundle)
    }

    fun commit() {
        viewModel.commit()
    }

    fun showLoading() {
        loadingPopup.show()
    }

    fun dismissLoading() {
        if (loadingPopup.isShow() == true) {
            loadingPopup.dismiss()
        }
    }

    open fun observe() {
        viewModel.commitCallback = object : LoadingCallback {

            override fun onLoading() {
                showLoading()
            }

            override fun onSuccess(response: OSPResponse) {
                dismissLoading()
                commitResult()
            }

            override fun onError(code: String, message: String) {
                dismissLoading()
                onCommitError(code, message)
            }
        }
    }

    open fun onCommitSuccess() {

    }

    open fun onCommitError(code: String, message: String) {
        showToastByCode(this, code, message)
    }

    /**
     * 如果commit完成后，则销毁当前页面，有的页面不需要销毁，有的需要
     */
    fun commitResult() {
        System.currentTimeMillis()
        val commitResponse = viewModel.commitHelper.commitResponse
        val activityResultData = OSPNodeResult()
        activityResultData.nodeName = viewModel.nodeCode
        activityResultData.message = commitResponse.message
        activityResultData.isSuccess = commitResponse.code == ResponseCode.CODE_SUCCESS
        OSPSdk.instance.ospProcessorManager?.onResult(activityResultData)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
            EventTracker.trackEvent(
                EventName.CLICK_GO_BACK,
                mutableMapOf("toC_goBackTrigger" to EventGoBackTrigger.SLIDE)
            )
        } else {
            showBackDialog(this, EventGoBackTrigger.SLIDE)
        }
    }

    override fun onDestroy() {
        OSPSdk.instance.ospProcessorManager?.removeNode(this)
        super.onDestroy()
    }

}