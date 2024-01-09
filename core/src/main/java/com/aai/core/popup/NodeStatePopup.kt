package com.aai.core.popup

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.aai.core.OSPSdk
import com.aai.core.R
import com.aai.core.processManager.loading.BasePopup
import com.aai.core.utils.OSPLog
import com.aai.core.utils.runOnUIThread
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import kotlinx.coroutines.delay

/**
 * 目前这个PopupWindow只适合于3d的场景
 * 当状态是Success的时候，需要停留2秒，然后dismiss掉页面，并且回调出去让外面请求调用commit接口，继续流程
 * 当状态是非Success的时候，一直停留在当前页面，但是把回调跑出去，调用commit接口
 */
class NodeStatePopup(
    context: Context,
    private val nodeCode: String,
    private val title: String,
    private val isSuccess: Boolean
) :
    BasePopup(context, R.layout.popup_node_state) {

    private lateinit var ivStateIcon: AppCompatImageView
    private lateinit var tvStateTitle: AppCompatTextView
    private lateinit var logo: AppCompatImageView
    var callback: (() -> Unit)? = null

    override fun initView(view: View) {
        tvStateTitle = view.findViewById(R.id.tvStateTitle)
        ivStateIcon = view.findViewById(R.id.ivStateIcon)
        ivStateIcon.setImageResource(if (isSuccess) R.drawable.icon_success else R.drawable.icon_failed)
        logo = view.findViewById(R.id.logo)
        val rlRoot = view.findViewById<View>(R.id.rlRoot)
        setPageBackgroundColor(rlRoot)
        setLogo(logo, nodeCode)
        tvStateTitle.text = title
        if (isSuccess) {
            runOnUIThread {
                delay(2000)
                callback?.invoke()
                dismiss()
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        OSPSdk.instance.ospProcessorManager?.endProcess()
    }

    override fun handBack(): Boolean = false

}