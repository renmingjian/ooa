package com.aai.onestop

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SPHelper {

    private const val TOKEN = "token"

    lateinit var sp: SharedPreferences

    fun init(context: Context) {
        sp = context.getSharedPreferences("SPOffline", Context.MODE_PRIVATE)
    }

    fun saveToken(key: String, token: String) {
        sp.edit {
            putString(key, token)
        }
    }

    fun getToken(key: String): String? = sp.getString(key, null)

    fun clearAll() {
        sp.edit {
            clear()
        }
    }

}