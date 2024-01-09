package com.aai.onestop

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.aai.core.network.HttpUrlConnectionClient
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.network.NetWorkCallback
import com.aai.core.network.OSPRequestBody
import com.aai.core.processManager.model.UrlConst
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPwd: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
        initSpinner()
    }

    private fun initView() {
        etName = findViewById(R.id.etName)
        etPwd = findViewById(R.id.etPwd)
        btnLogin = findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {
            login()
        }
        clearListener(etName)
        clearListener(etPwd)
    }

    private fun login() {
        val request = NetRequest(
            url = UrlConst.getBaseUrl() + "onestop-management/login",
            method = NetMethod.POST,
            requestBody = OSPRequestBody.OSPFormUrlRequestBody(
                mutableMapOf("username" to etName.text, "password" to etPwd.text)
            )
        )
        HttpUrlConnectionClient.instance.sendRequest(request, object : NetWorkCallback {
            override fun onSuccess(response: String) {
                println("response: $response")
                val json = JSONObject(response)
                val data = json.optJSONObject("data")
                val intent = Intent(this@LoginActivity, FlowsActivity::class.java)
                intent.putExtra("token", data?.optString("token") ?: "")
                intent.putExtra("customerId", data?.optInt("customerId") ?: 0)
                intent.putExtra("accountId", data?.optInt("accountId") ?: 0)
                startActivity(intent)
            }

            override fun onError(code: String, message: String) {

            }

        })
    }


    private fun clearListener(editText: EditText) {
        editText.setOnTouchListener(View.OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (editText.right - editText.compoundDrawables[2].bounds.width())) {
                    // 清除文本
                    editText.text.clear()
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    private fun initSpinner() {
        val spinner: Spinner = findViewById(R.id.my_spinner)

        // 创建一个ArrayAdapter来绑定字符串数组
        ArrayAdapter.createFromResource(
            this,
            R.array.spinner_items,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // 指定下拉菜单的样式
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // 将适配器应用到Spinner
            spinner.adapter = adapter
        }

        // 设置选项选择监听器
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // 当选中某个选项时触发
                val item = parent.getItemAtPosition(position).toString()
                UrlConst.currentEvn = item
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 未选择任何项时触发
            }
        }
    }

}