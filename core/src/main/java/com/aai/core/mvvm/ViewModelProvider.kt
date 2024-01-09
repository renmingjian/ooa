package com.aai.core.mvvm

import com.aai.core.processManager.dataparser.OSPDataParser

interface ViewModelProvider<T: BaseViewModel<out OSPDataParser>> {

    fun getActivityViewModel(): T

}