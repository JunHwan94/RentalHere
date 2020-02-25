package com.dmon.rentalhere

import android.content.Context
import android.webkit.JavascriptInterface
import com.dmon.rentalhere.model.ShopResult
import com.dmon.rentalhere.retrofit.RetrofitService
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.dmon.rentalhere.retrofit.FIELD_SHOP_IDX
import com.dmon.rentalhere.view.ShopInfoFragment
import com.dmon.rentalhere.view.WebViewFragment

class WebJavaScript(private var context: Context, private var retrofitService: RetrofitService, private var callback: WebViewFragment.OnFragmentInteractionListener) {
    @JavascriptInterface
    fun getIndex(idx :String) {
//        context.toast(idx)
        val map = HashMap<String, Any>().apply{
            this[FIELD_SHOP_IDX] = idx
        }
        retrofitService.postGetShop(map).enqueue(object : Callback<ShopResult>{
            override fun onResponse(call: Call<ShopResult>, response: Response<ShopResult>) {
                val result = response.body()!!.shopResultItem
                if(result.result == "Y"){
//                    context.toast("성공")
                    val shopInfoFragment = ShopInfoFragment.newInstance(result.shopIdx, result.shopAddress, result.shopTelNum, result.shopInfo, result.shopProfileImageUrl)
                    callback.replaceFragment(shopInfoFragment, result.shopName)
                }
            }

            override fun onFailure(call: Call<ShopResult>, t: Throwable) {}
        })
    }
}