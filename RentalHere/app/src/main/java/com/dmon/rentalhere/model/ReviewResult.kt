package com.dmon.rentalhere.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ReviewResult(@SerializedName("resultItem") val reviewResultItem: ReviewResultItem){
    data class ReviewResultItem(@SerializedName("review") val reviewModelList: ArrayList<ReviewModel>,
                                   @SerializedName("method") val method: String,
                                   @SerializedName("msg") val message: String,
                                   @SerializedName("result") val result: String)
    data class ReviewModel(@SerializedName("cr_text") val reviewText: String,
                           @SerializedName("cr_score") val reviewScore: String,
                           @SerializedName("mem_userid") val reviewerId: String? = null,
                           // 아래는 내 리뷰목록에 포함
                           @SerializedName("cs_idx") var shopIdx: String? = null,
                           @SerializedName("cs_name") var shopName: String? = null,
                           @SerializedName("cs_add") var shopAddress: String? = null,
                           @SerializedName("cr_date") var date: String? = null,
                           @SerializedName("cr_idx") var reviewIdx: String? = null): Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(reviewText)
            parcel.writeString(reviewScore)
            parcel.writeString(reviewerId)
            parcel.writeString(shopIdx)
            parcel.writeString(shopName)
            parcel.writeString(shopAddress)
            parcel.writeString(date)
            parcel.writeString(reviewIdx)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<ReviewModel> {
            override fun createFromParcel(parcel: Parcel): ReviewModel {
                return ReviewModel(parcel)
            }

            override fun newArray(size: Int): Array<ReviewModel?> {
                return arrayOfNulls(size)
            }
        }
    }
}