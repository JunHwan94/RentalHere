package com.dmon.rentalhere.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*

import com.dmon.rentalhere.R
import androidx.databinding.DataBindingUtil
import com.dmon.rentalhere.WebJavaScript
import com.dmon.rentalhere.databinding.FragmentWebViewBinding
import com.dmon.rentalhere.retrofit.RetrofitClient
import com.dmon.rentalhere.retrofit.RetrofitService

const val MAP_URL = "https://softer013.cafe24.com/mapview/android_map"
const val WEB_APP_ID = "RentalHere"
class WebViewFragment : Fragment() {
    private lateinit var binding: FragmentWebViewBinding
    private lateinit var retrofitService: RetrofitService
    private var callback: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_web_view, container, false)
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
        initWebView()
        return binding.root
    }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = WebViewClient()
        binding.webView.webChromeClient = newWebChromeClient()
//        binding.webView.addJavascriptInterface(WebJavascript1.getInstance(activity), WEB_APP_ID)
        binding.webView.addJavascriptInterface(WebJavaScript(context!!, retrofitService, callback!!), WEB_APP_ID)
        binding.webView.loadUrl(MAP_URL)
    }

    private fun newWebChromeClient() = object : WebChromeClient(){
        override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
            super.onGeolocationPermissionsShowPrompt(origin, callback)
            callback!!.invoke(origin, true, false)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnFragmentInteractionListener) callback = context
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    interface OnFragmentInteractionListener {
        fun replaceFragment(fragment: Fragment, shopName: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            WebViewFragment().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
                }
            }
    }
}
