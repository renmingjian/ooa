package com.aai.core.mvvm

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aai.core.EventGoBackTrigger
import com.aai.core.processManager.dataparser.OSPDataParser
import com.aai.core.utils.OSPLog
import com.aai.core.utils.showBackDialog

abstract class BaseViewModelFragment<T: BaseViewModel<out OSPDataParser>> : Fragment() {

    protected lateinit var activityViewModel: T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OSPLog.log("OSPFragment: ${this::class.java.simpleName}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        beforeInitData()
        initData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityViewModel = (requireActivity() as ViewModelProvider<T>).getActivityViewModel()
    }

    abstract fun getLayoutId(): Int

    abstract fun initView(view: View)

    abstract fun initData()

    fun beforeInitData() {

    }

    fun clickBack() {
        val backStackEntryCount = activity?.supportFragmentManager?.backStackEntryCount ?: 1
        if (backStackEntryCount > 1) {
            activity?.supportFragmentManager?.popBackStackImmediate()
        } else {
            activity?.let {
                showBackDialog(it, EventGoBackTrigger.CLICK_BUTTON)
            }
        }
    }

}