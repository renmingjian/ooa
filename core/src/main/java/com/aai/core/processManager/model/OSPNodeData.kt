package com.aai.core.processManager.model

import android.app.Activity
import androidx.fragment.app.Fragment

/**
 * 对每个节点的配置
 */
data class OSPNodePage(
    val activity: Activity,
    val backDisplay: Boolean = false,
)

/**
 * 对每个节点内部页面的配置
 */
data class OSPInsideNodePage(
    val fragment: Fragment,
    val backDisplay: Boolean = false,
    val isFirst: Boolean = false,
)