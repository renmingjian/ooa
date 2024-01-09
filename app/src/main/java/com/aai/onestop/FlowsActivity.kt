package com.aai.onestop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aai.core.OSPOptions
import com.aai.core.OSPSdk
import com.aai.core.network.HeaderCallback
import com.aai.core.network.HttpUrlConnectionClient
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.network.NetWorkCallback
import com.aai.core.network.OSPRequestBody
import com.aai.core.processManager.loading.CommonLoading
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.UrlConst
import com.aai.core.utils.hasAndNotNull
import com.aai.core.webview.WebViewActivity
import com.aai.iqa.node.DocumentNode
import com.aai.selfie.SelfieNode
import org.json.JSONException
import org.json.JSONObject

class FlowsActivity : AppCompatActivity() {

    private lateinit var spinner1: Spinner
    private lateinit var spinner2: Spinner
    private lateinit var adapter1: FlowAdapter
    private lateinit var adapter2: FlowAdapter
    private val list1: MutableList<String> = mutableListOf()
    private val list1Flow: MutableList<FlowData> = mutableListOf()
    private val list2: MutableList<String> = mutableListOf()
    private var loading: CommonLoading? = null
    private lateinit var btnStart: Button
    private lateinit var btnGetFlows: Button
    private lateinit var token: String
    private var sdkToken = ""
    private var currentFlow: FlowData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flows)
        initViews()
        initData()
    }

    private fun initViews() {
        spinner1 = findViewById(R.id.spinner1)
        spinner2 = findViewById(R.id.spinner2)
        btnStart = findViewById(R.id.btnStart)
        btnGetFlows = findViewById(R.id.btnGetFlows)

        adapter1 = FlowAdapter(this, list1)
        adapter2 = FlowAdapter(this, list2)
        spinner1.adapter = adapter1
        spinner2.adapter = adapter2

        btnStart.setOnClickListener {
            val instance = OSPSdk.instance
            instance.init(OSPOptions(this, "microblink", sdkToken, openLog = true))
            instance.registerNode(NodeCode.SELFIE, SelfieNode())
            instance.registerNode(NodeCode.DOCUMENT_VERIFICATION, DocumentNode())
            instance.startFlow(this@FlowsActivity)
        }

        findViewById<Button>(R.id.btnWebView).setOnClickListener {
            startActivity(
                WebViewActivity.newIntent(
                    this@FlowsActivity,
                    currentFlow?.originalUrl ?: ""
                )
            )
        }

        findViewById<Button>(R.id.btnBrowser).setOnClickListener {
            val url = currentFlow?.redirectUrl
            if (!url.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
        }

        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentFlow = list1Flow[position]
                getH5Url()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("onNothingSelected")
            }
        }

        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = list2[position]
                sdkToken = item.split("_")[1]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    // https://sandbox-oop.advai.net/onestop-management/integration/api/view/v2/getH5UrlForHostedFlow
    // // search flow: https://sandbox-oop.advai.net/onestop-management/journey/v2/view/search?size=1000&page=0&sort=name,asc, jsonBody = {"status":"PUBLISHED"}
    private fun initData() {
        token = intent.getStringExtra("token") ?: ""
        requestFlows()
    }

    private fun requestFlows() {
        btnStart.post { loading?.show() }
        val request = NetRequest(
            url = UrlConst.getBaseUrl() + "onestop-management/journey/v2/view/search",
            method = NetMethod.POST,
            queryParameters = mutableMapOf("size" to 1000, "page" to 0, "sort" to "name,asc"),
            headers = mutableMapOf("X-Access-Token" to (token ?: "")),
            requestBody = OSPRequestBody.OSPJsonRequestBody(
                json = """
                {"status":"PUBLISHED"}
            """.trimIndent()
            )
        )
        HttpUrlConnectionClient.instance.sendRequest(request, object : NetWorkCallback {
            override fun onSuccess(response: String) {
                loading?.dismiss()
                println("flowsResponse = $response")
                parseFlow(response)
                btnGetFlows.visibility = View.GONE
            }

            override fun onError(code: String, message: String) {
                loading?.dismiss()
                Toast.makeText(this@FlowsActivity, "获取Flows失败，请重试", Toast.LENGTH_SHORT)
                    .show()
                btnGetFlows.visibility = View.VISIBLE
            }

        })
    }

    private fun parseFlow(response: String) {
        val json = JSONObject(response)
        val data = json.getJSONObject("data")
        if (data.hasAndNotNull("content")) {
            list1.clear()
            list1Flow.clear()
            val array = data.getJSONArray("content")
            try {
                for (i in 0 until array.length()) {
                    val flowData = FlowData()
                    val element = array.getJSONObject(i)
                    if (element.hasAndNotNull("id")) {
                        flowData.id = element.getInt("id")
                    }
                    if (element.hasAndNotNull("name")) {
                        flowData.name = element.getString("name")
                    }
                    list1Flow.add(flowData)
                }
                list1.addAll(list1Flow.map { it.name })
                adapter1.notifyDataSetChanged()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun getH5Url() {
        val url =
            "${UrlConst.getBaseUrl()}onestop-management/integration/api/view/v2/getH5UrlForHostedFlow"
        val map = mutableMapOf("journeyId" to currentFlow?.id)
        val json = JSONObject(map as Map<*, *>?).toString()
        val request = NetRequest(
            url = url,
            method = NetMethod.POST,
            requestBody = OSPRequestBody.OSPJsonRequestBody(
                json = json
            ),
            headers = mutableMapOf("X-Access-Token" to token)
        )
        HttpUrlConnectionClient.instance.sendRequest(request, object : NetWorkCallback {
            override fun onSuccess(response: String) {
                println("getH5UrlResponse: $response")
                val responseJason = JSONObject(response)
                val h5Url = responseJason.getJSONObject("data").getString("url") ?: ""
                currentFlow?.originalUrl = h5Url
                getSDKToken(h5Url, currentFlow!!)
            }

            override fun onError(code: String, message: String) {
                Toast.makeText(this@FlowsActivity, message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getSDKToken(url: String, flowData: FlowData) {
        if (url.isEmpty()) {
            Toast.makeText(this, "你得输入一个URL才行", Toast.LENGTH_SHORT).show()
            return
        }
        val request = NetRequest(
            url = url,
            method = NetMethod.GET,
        )
        HttpUrlConnectionClient.instance.sendRequest(request,
            netWorkCallback = object : NetWorkCallback {
                override fun onSuccess(response: String) {
                    println("sdkToken: Success: $response")
                }

                override fun onError(code: String, message: String) {
                    println("sdkToken: error: $message")
                }

            },
            onHeaderCallback = object : HeaderCallback {
                override fun onGetHeaders(map: Map<String, List<String>>) {
                    map["Location"]?.get(0)?.let {
                        currentFlow?.redirectUrl = it
                        val sdkToken = Uri.parse(it).getQueryParameter("sdkToken")
                        println("sdkToken = $sdkToken")
                        list2.add("${flowData.name}_$sdkToken")
                        adapter2.notifyDataSetChanged()
                    }
                }
            }
        )
    }

}