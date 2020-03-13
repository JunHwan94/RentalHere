package com.dmon.rentalhere.model

import com.google.gson.annotations.SerializedName

data class BaseResult(@SerializedName("resultItem") val baseModel: BaseModel){
    data class BaseModel(@SerializedName("method") val method: String? = null,
                         @SerializedName("result") val result: String,
                         @SerializedName("msg") val message: String,
                         @SerializedName("in") val info: Info)
    data class Info(@SerializedName("mem_id") val id: String,
                    @SerializedName("cs_name") val name: String,
                    @SerializedName("cs_phone") val tel: String,
                    @SerializedName("cs_add") val addr: String,
                    @SerializedName("cs_lat") val lat: String,
                    @SerializedName("cs_lng") val lng: String,
                    @SerializedName("cs_keyword") val keyword: String,
                    @SerializedName("cs_main_poto") val mainNo: String,
                    @SerializedName("cs_type") val type: String)
}