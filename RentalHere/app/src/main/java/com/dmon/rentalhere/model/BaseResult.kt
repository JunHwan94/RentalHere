package com.dmon.rentalhere.model

import com.google.gson.annotations.SerializedName

data class BaseResult(@SerializedName("resultItem") val baseModel: BaseModel){
    data class BaseModel(@SerializedName("method") val method: String? = null,
                         @SerializedName("result") val result: String,
                         @SerializedName("msg") val message: String)
}