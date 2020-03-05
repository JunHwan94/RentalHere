package com.dmon.rentalhere.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.dmon.rentalhere.BaseActivity
import com.dmon.rentalhere.R
import com.dmon.rentalhere.WebJavaScript
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    private fun initWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = newWebChromeClient()
//        binding.webView.addJavascriptInterface(WebJavascript1.getInstance(activity), WEB_APP_ID)
//        webView.addJavascriptInterface(WebJavaScript(retrofitService), WEB_APP_ID)
    }

    private fun newWebChromeClient() = object : WebChromeClient(){
        override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
            super.onGeolocationPermissionsShowPrompt(origin, callback)
            callback!!.invoke(origin, true, false)
        }
    }
}
