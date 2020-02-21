package com.dmon.rentalhere.model

import com.google.gson.annotations.SerializedName

data class BaseResult(@SerializedName("resultItem") val baseResultItem: BaseResultItem) {
    data class BaseResultItem(@SerializedName("method") val method: String,
                         @SerializedName("result") val result: String,
                         @SerializedName("message") val message: String)
}