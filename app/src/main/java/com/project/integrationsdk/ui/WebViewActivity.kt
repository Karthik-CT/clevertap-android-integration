package com.project.integrationsdk.ui

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CTWebInterface
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.databinding.ActivityWebViewBinding


class WebViewActivity : AppCompatActivity() {

    lateinit var binding: ActivityWebViewBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webView.apply {
//            loadUrl("https://karthik-ct.github.io/clevertap-web-integration/")
            loadUrl("file:///android_asset/webViewHTMLPage.html")
            settings.apply {
                javaScriptEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                allowFileAccessFromFileURLs = false
                domStorageEnabled = true
            }
            addJavascriptInterface(
                CTWebInterface(CleverTapAPI.getDefaultInstance(this@WebViewActivity)),
                "com_project_integrationsdk"
            )
        }
        binding.webView.webViewClient = object : WebViewClient() {}

        //suspend inapp
//        CleverTapAPI.getDefaultInstance(applicationContext)?.suspendInAppNotifications()
        // Get the Intent and URI data
        val intent = intent
        val data = intent.data

        if (data != null) {
            // Extract the query parameter
            Log.d("WebViewActivity", "value: $data")
        }
    }
}