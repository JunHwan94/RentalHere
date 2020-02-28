package com.dmon.rentalhere.model

import com.google.gson.annotations.SerializedName

data class UserInfoResult(@SerializedName("resultItem") val userModel: UserModel) {
    data class UserModel(@SerializedName("result") val result: String,
                         @SerializedName("mem_id") val userIdx: String,
                         @SerializedName("mem_userid") val userId: String,
                         @SerializedName("mem_email") val userEmail: String,
                         @SerializedName("mem_username") val userName: String,
                         @SerializedName("mem_phone") val userCpNum: String)
}