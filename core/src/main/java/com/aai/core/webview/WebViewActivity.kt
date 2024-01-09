package com.aai.core.webview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.aai.core.EventName
import com.aai.core.EventTracker
import com.aai.core.R
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.stateBar

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateBar(false)
        setContentView(R.layout.activity_webview)
        webView = findViewById(R.id.webView)
        // 这个页面不使用主题背景色，因为html页面有自己的背景色，这里就算修改了，h5中的背景还是无法修改
//        val llRoot = findViewById<View>(R.id.llRoot)
//        setPageBackgroundColor(llRoot)
        findViewById<View>(R.id.ivBack).setOnClickListener { finish() }
        initSettings()
        val url = intent.getStringExtra(EXTRA_URL)
        url?.let { webView.loadUrl(it) }
    }

    private fun initSettings() {
        webView.settings.javaScriptEnabled = true
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN;
        webView.settings.setSupportZoom(true)
        webView.settings.useWideViewPort = true
        webView.settings.builtInZoomControls = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.displayZoomControls = false
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            EventTracker.trackEvent(EventName.CLICK_GO_BACK, null)
            super.onBackPressed()
        }
    }

    companion object {
        const val EXTRA_URL = "EXTRA_URL"

        fun newIntent(context: Context, url: String): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            return intent
        }
    }

}