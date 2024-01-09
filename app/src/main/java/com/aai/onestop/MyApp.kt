package com.aai.onestop

import android.app.Application

class MyApp: Application() {


    init {
        instance = this
    }

    companion object {
        private var instance: MyApp? = null

        fun getInstance(): MyApp {
            return instance ?: throw IllegalStateException("Application not created yet")
        }
    }

}