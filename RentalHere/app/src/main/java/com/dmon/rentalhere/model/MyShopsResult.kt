package com.dmon.rentalhere.model

import com.google.gson.annotations.SerializedName

data class MyShopsResult(@SerializedName("resultItem") val myShopsResultItem: MyShopsResultItem) {
    data class MyShopsResultItem(@SerializedName("shop") val shopModelList: ArrayList<ShopResult.ShopModel>,
                                 @SerializedName("msg") val message: String,
                                 @SerializedName("result") val result: String)
}