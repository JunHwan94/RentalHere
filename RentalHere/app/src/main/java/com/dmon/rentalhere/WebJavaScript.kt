package com.dmon.rentalhere

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import com.dmon.rentalhere.model.ShopResult
import com.dmon.rentalhere.presenter.CLIENT_TYPE
import com.dmon.rentalhere.retrofit.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.dmon.rentalhere.retrofit.FIELD_SHOP_IDX
import com.dmon.rentalhere.view.ShopInfoFragment
import com.dmon.rentalhere.view.WebViewFragment
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast

class WebJavaScript(private var context: Context, private var retrofitService: RetrofitService, private var callback: WebViewFragment.OnFragmentInteractionListener){
    @JavascriptInterface
    fun getIndex(idx :String) {
//        context.toast(idx)
        val map = HashMap<String, Any>().apply{ this[FIELD_SHOP_IDX] = idx }
        retrofitService.postGetShop(map).enqueue(object : Callback<ShopResult>{
            override fun onResponse(call: Call<ShopResult>, response: Response<ShopResult>) {
                val result = response.body()!!.shopModel
                if(result.result == "Y"){
//                    context.toast("성공")
//                    Log.d("WJS", result.shopIdx)
                    val shopInfoFragment = ShopInfoFragment.newInstance(result.shopIdx, result.shopName, result.shopAddress, result.shopTelNum, result.shopInfo, result.shopProfileImageUrl, CLIENT_TYPE)
                    callback.replaceFragment(shopInfoFragment, result.shopName)
                }
            }

            override fun onFailure(call: Call<ShopResult>, t: Throwable) {}
        })
    }
}