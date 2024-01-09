package com.aai.core.processManager

import android.os.Bundle

interface FragmentJumper {

    fun jump(tag: String, bundle: Bundle? = null)

}