package com.dmon.rentalhere.model

import com.google.gson.annotations.SerializedName

data class ReviewResult(@SerializedName("resultItem") val reviewResultItem: ReviewResultItem){
    data class ReviewResultItem(@SerializedName("review") val reviewModelList: ArrayList<ReviewModel>,
                                   @SerializedName("method") val method: String,
                                   @SerializedName("msg") val message: String,
                                   @SerializedName("result") val result: String)
    data class ReviewModel(@SerializedName("cr_text") val reviewText: String,
                           @SerializedName("cr_score") val reviewScore: String,
                           @SerializedName("mem_userid") val reviewerId: String)
}