package com.aai.onestop

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aai.core.OSPOptions
import com.aai.core.OSPProcessCallback
import com.aai.core.OSPSdk
import com.aai.core.network.HeaderCallback
import com.aai.core.network.HttpUrlConnectionClient
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.network.NetWorkCallback
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.UrlConst
import com.aai.core.utils.OSPLog
import com.aai.core.utils.showToast
import com.aai.core.utils.stateBar
import com.aai.iqa.node.DocumentNode
import com.aai.iqa.node.SelectCaptureMethodPopup
import com.aai.selfie.SelfieNode
import com.caverock.androidsvg.SVG
import java.io.File
import java.io.InputStreamReader
import java.net.URLDecoder
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {
    private lateinit var netTest: Button
    private lateinit var etSdkToken: EditText
    private lateinit var btnStartFlow: Button
    private lateinit var btnClearToken: Button
    private lateinit var tvSdkToken: TextView
    private lateinit var tvText: TextView
    private lateinit var popupWindow: SelectCaptureMethodPopup
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateBar(false)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        netTest = findViewById(R.id.netTest)
        tvSdkToken = findViewById(R.id.tvSdkToken)
        tvText = findViewById(R.id.tvText)
        btnClearToken = findViewById(R.id.btnClearToken)
        etSdkToken = findViewById(R.id.etSdkToken)
        btnStartFlow = findViewById(R.id.btnStartFlow)
        netTest.setOnClickListener {
            getSDKToken()
        }
        etSdkToken.setText("https://pre-oop-client.advai.cn/intl/openapi/hostLink/start?accountSdkToken=cd2595e5-2d7c-4ffe-964d-d140b995c919")
        etSdkToken.setText("https://uat-oop-client.advai.net/intl/openapi/hostLink/start?accountSdkToken=78e114f9-08db-43c0-a68b-78d3b1000723")
//        etSdkToken.setText("https://dev-oop-client.advai.cn/intl/openapi/hostLink/start?accountSdkToken=f4827c9e-5242-4d90-ae24-640db8236258")
//        etSdkToken.setText("https://uat-oop-client.advai.net/intl/openapi/hostLink/start?accountSdkToken=9f246863-f752-463a-92db-66f5ca2b8178") // 晓彤的链接
        btnClearToken.setOnClickListener {
            etSdkToken.setText("")
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<Button>(R.id.btnScan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        val btnTest = findViewById<Button>(R.id.btnTest)
        btnTest.setOnClickListener {
            popupWindow = SelectCaptureMethodPopup(this)
            popupWindow.show(netTest)
//            startActivity(Intent(this, Selfie2DActivity::class.java))

        }

        btnStartFlow.setOnClickListener {
            val token = tvSdkToken.text.toString()
            OSPSdk.instance.init(
                OSPOptions(
                    context = MyApp.getInstance(),
                    "microblink",
                    sdkToken = token,
                    openLog = true,
                    processCallback = object : OSPProcessCallback {
                        override fun onReady() {
                            OSPLog.log("processCallback: onReady")
                        }

                        override fun onExit(nodeCode: String) {
                            OSPLog.log("processCallback: onExit(nodeCode: $nodeCode)")
                        }

                        override fun onError(message: String?) {
                            OSPLog.log("processCallback: onError(message: $message)")
                            showToast(this@MainActivity, message ?: "")
                        }

                        override fun onEvent(
                            eventName: String,
                            params: MutableMap<String, String>?
                        ) {
                            OSPLog.log("processCallback: onEvent(eventName: $eventName, params: $params)")
                        }

                        override fun onComplete() {
                            OSPLog.log("processCallback: onComplete")
                        }

                    }
                )
            )
            if (token.isEmpty()) return@setOnClickListener
            val instance = OSPSdk.instance
            instance.registerNode(NodeCode.SELFIE, SelfieNode())
            instance.registerNode(NodeCode.DOCUMENT_VERIFICATION, DocumentNode())
            instance.startFlow(this@MainActivity)
        }

        val data = """
            <link rel="stylesheet" type="text/css" href="test.css" /><div class="ql-editor"><p>Camera permissions </p><p>isabled. Please check your operating system and </p><ol><li>rowser settings</li><li><br></li><li>Camera permissions disabled.</li><li>Please check your operating s</li><li>stem and browser settings.</li><li>Camera permissions disable</li></ol><p><br></p><p><strong>Getting</strong> <span class="ql-size-small">Started</span><span class="ql-size-large">Getting Started</span><u>Getting</u> StartedGett</p></div>
        """.trimIndent()

//        tvText.text = Html.fromHtml(data)

        val webView = findViewById<WebView>(R.id.webView)
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                webView.postDelayed({
                    webView.evaluateJavascript(
                        "(function() { return Math.max( document.body.scrollHeight, document.body.offsetHeight, document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight ); })();"
                    ) {
                        // value is the height of the document
                        println("contentHeight,js = $it")
                    }
                }, 1000)
            }
        }
    }

//    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
//        if (event.action == MotionEvent.ACTION_DOWN) {
//            if (popupWindow.isShowing()) {
//                // 在这里检查点击位置是否在PopupWindow外部
//
//            }
//        }
//        return super.dispatchTouchEvent(event)
//    }

    private fun getSDKToken() {
        val url = etSdkToken.text.toString()
        if (url.isEmpty()) {
            Toast.makeText(this, "你得输入一个URL才行", Toast.LENGTH_SHORT).show()
            return
        }
        val baseUrlMap = mutableMapOf(
            "sandbox" to "https://sandbox",
            "uat" to "https://uat",
            "production" to "https://production",
            "pre" to "https://pre",
            "dev" to "https://dev",
            "sg" to "https://sg",
            "id" to "https://id"
        )
        for ((key, value) in baseUrlMap) {
            if (url.startsWith(value)) {
                UrlConst.currentEvn = key
                break
            }
        }
        val request = NetRequest(
            url = etSdkToken.text.toString(),
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
                        val sdkToken = Uri.parse(it).getQueryParameter("sdkToken")
                        println("sdkToken = $sdkToken")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "yes", Toast.LENGTH_SHORT).show()
                            tvSdkToken.text = sdkToken
                        }
                    }
                }
            }
        )
    }

    fun processSVGFiles() {
        val flagsMap = mutableMapOf<String, String>()
        val assetManager = assets
        val svgPattern = Pattern.compile("data:image/svg\\+xml,(.*)") // 正则表达式匹配SVG数据

        // 读取assets目录下的flags.css文件
        assetManager.open("flags.css").use { inputStream ->
            InputStreamReader(inputStream).useLines { lines ->
                lines.forEach { line ->
                    if (line.trim().startsWith(".flag")) {
                        val countryCode = line.substringAfter(".flag:").substringBefore("{").trim()
                        val matcher = svgPattern.matcher(line)

                        if (matcher.find()) {
                            val encodedSvg = matcher.group(1)
                            val decodedSvg =
                                URLDecoder.decode(encodedSvg, "UTF-8").replace("\")}", "")
                                    .replace("\'", "\"")
                            println("decodedSvg = $decodedSvg")
                            // 确保解码后的内容是正确的SVG格式
                            if (decodedSvg.trim().startsWith("<svg") && decodedSvg.trim()
                                    .endsWith("</svg>")
                            ) {
                                flagsMap[countryCode] = decodedSvg
                                val svg = SVG.getFromString(decodedSvg)
                                val bitmap = Bitmap.createBitmap(
                                    svg.documentWidth.toInt(),
                                    svg.documentHeight.toInt(),
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = Canvas(bitmap)
                                svg.renderToCanvas(canvas)

                                // 保存Bitmap为JPG
                                val countryFile = File(filesDir, "country")
                                val jpgFile = File(countryFile, "$countryCode.jpg")
//                                FileOutputStream(jpgFile).use { out ->
//                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
//                                }
                            }
                        }
                    }
                }
            }
        }

        // 处理或输出解析后的映射关系
        flagsMap.forEach { (countryCode, svg) ->
            println("Country Code: $countryCode, SVG Data: $svg")
        }
    }


    fun uploadImage(imageFile: File): String {
        // 使用OkHttp上传图像并返回URL
        // 这里应该替换为你的具体逻辑
        return "http://example.com/image/${imageFile.name}"
    }

    fun handleFlagMap(flagMap: Map<String, String>) {
        // 处理国家码和URL映射
        // 可能是显示它们，保存到数据库，或其他操作
    }

}