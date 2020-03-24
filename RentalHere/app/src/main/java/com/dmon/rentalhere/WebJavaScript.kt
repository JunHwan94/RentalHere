package com.dmon.rentalhere

import android.webkit.JavascriptInterface
import com.dmon.rentalhere.model.ShopResult
import com.dmon.rentalhere.presenter.CLIENT_TYPE
import com.dmon.rentalhere.retrofit.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.dmon.rentalhere.retrofit.FIELD_SHOP_IDX
import com.dmon.rentalhere.view.SEARCH_TYPE
import com.dmon.rentalhere.view.ShopInfoFragment
import com.dmon.rentalhere.view.WebViewFragment
import org.jetbrains.anko.toast

class WebJavaScript(private var retrofitService: RetrofitService, private var callback: WebViewFragment.OnFragmentInteractionListener, private var fragmentType: Int){
    @JavascriptInterface
    fun getIndex(idx :String) {
//        context.toast(idx)
        val map = HashMap<String, Any>().apply{ this[FIELD_SHOP_IDX] = idx }
        retrofitService.postGetShop(map).enqueue(object : Callback<ShopResult>{
            override fun onResponse(call: Call<ShopResult>, response: Response<ShopResult>) {
                val shopModel = response.body()!!.shopModel
                if(shopModel.result == "Y"){
//                    context.toast("성공")
//                    Log.d("WJS", result.shopIdx)
                    val shopInfoFragment = ShopInfoFragment.newInstance(shopModel, CLIENT_TYPE)
                    when(fragmentType){
                        SEARCH_TYPE -> callback.showShopInfoFragmentInContainer3(shopInfoFragment, shopModel.shopName)
                        else -> callback.showShopInfoFragmentInContainer2(shopInfoFragment, shopModel.shopName)
                    }
                }
            }

            override fun onFailure(call: Call<ShopResult>, t: Throwable) {
                callback.showFailedToast()
            }
        })
    }
}