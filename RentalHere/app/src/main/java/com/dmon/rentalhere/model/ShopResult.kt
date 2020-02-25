package com.dmon.rentalhere.model

import com.google.gson.annotations.SerializedName

data class ShopResult(@SerializedName("resultItem") val shopResultItem: ShopResultItem) {
    data class ShopResultItem(@SerializedName("method") val method: String,
                              @SerializedName("result") val result: String,
                              @SerializedName("message") val message: String,
                              @SerializedName("cs_idx") val shopIdx: String,
                              @SerializedName("mem_id") val managerIdx: String,
                              @SerializedName("cs_name") val shopName: String,
                              @SerializedName("cs_phone") val shopTelNum: String,
                              @SerializedName("cs_add") val shopAddress: String,
                              @SerializedName("cs_keyword") val shopKeyword: String,
                              @SerializedName("cs_info") val shopInfo: String,
                              @SerializedName("cs_lat") val shopLatitude: String,
                              @SerializedName("cs_lng") val shopLongitude: String,
                              @SerializedName("mem_userid") val managerId: String,
                              @SerializedName("mem_email") val managerEmail: String,
                              @SerializedName("poto1_url") val shopProfileImageUrl: String)
}