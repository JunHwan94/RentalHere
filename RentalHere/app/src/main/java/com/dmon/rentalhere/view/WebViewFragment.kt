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
import com.dmon.rentalhere.presenter.TYPE_KEY
import com.dmon.rentalhere.retrofit.RetrofitClient
import com.dmon.rentalhere.retrofit.RetrofitService

const val DIST_ORDER_MAP_URL = "https://softer013.cafe24.com/mapview/android_map"
const val REC_ORDER_MAP_URL = "https://softer013.cafe24.com/mapview/android_map_key"
const val SEARCH_MAP_URL = "https://softer013.cafe24.com/mapview/find_map"
const val WEB_APP_ID = "RentalHere"
class WebViewFragment : Fragment() {
    private lateinit var binding: FragmentWebViewBinding
    private lateinit var retrofitService: RetrofitService
    private var callback: OnFragmentInteractionListener? = null
    private var fragmentType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{ fragmentType = it.getInt(TYPE_KEY) }
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
        binding.webView.addJavascriptInterface(WebJavaScript(retrofitService, callback!!, fragmentType), WEB_APP_ID)
        when(fragmentType){
            DIST_TYPE -> binding.webView.loadUrl(DIST_ORDER_MAP_URL)
            REC_TYPE -> binding.webView.loadUrl(REC_ORDER_MAP_URL)
            SEARCH_TYPE -> binding.webView.loadUrl(SEARCH_MAP_URL)
        }
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
        fun showShopInfoFragmentInContainer2(fragment: Fragment, shopName: String)
        fun showShopInfoFragmentInContainer3(fragment: Fragment, shopName: String)
        fun showFailedToast()
    }

    companion object {
        @JvmStatic
        fun newInstance(fragmentType: Int) =
            WebViewFragment().apply {
                arguments = Bundle().apply {
                    putInt(TYPE_KEY, fragmentType)
                }
            }
    }
}
