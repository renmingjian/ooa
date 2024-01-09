package com.aai.core.mvvm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aai.core.OSPSdk
import com.aai.core.processManager.loading.SimpleLoading
import com.aai.core.utils.OSPLog
import com.aai.core.utils.stateBar

abstract class BaseActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OSPLog.log("OSPActivity: $localClassName")
        beforeInflate()
        setContentView(layoutId())
        initView()
        beforeInitData()
        initData()
    }

    fun beforeInitData() {
        stateBar(false)
    }

    abstract fun layoutId(): Int

    open fun beforeInflate() {
        stateBar(false)
    }

    abstract fun initView()

    open fun initData() {

    }

}