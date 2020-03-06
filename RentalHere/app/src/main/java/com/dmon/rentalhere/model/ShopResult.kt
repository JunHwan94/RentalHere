package com.dmon.rentalhere.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ShopResult(@SerializedName("resultItem") val shopModel: ShopModel) {
    data class ShopModel(@SerializedName("method") val method: String? = null,
                         @SerializedName("result") val result: String? = null,
                         @SerializedName("msg") val message: String? = null,
                         @SerializedName("cs_idx") val shopIdx: String,
                         @SerializedName("mem_id") val managerIdx: String,
                         @SerializedName("cs_name") val shopName: String,
                         @SerializedName("cs_phone") val shopTelNum: String,
                         @SerializedName("cs_add") val shopAddress: String,
                         @SerializedName("cs_keyword") val shopKeyword: String,
                         @SerializedName("cs_info") val shopInfo: String,
                         @SerializedName("cs_lat") val shopLatitude: String,
                         @SerializedName("cs_lng") val shopLongitude: String,
                         @SerializedName("cs_main_poto") val mainPicNum: String,
                         @SerializedName("mem_userid") val managerId: String,
                         @SerializedName("poto1_url") val shopProfileImageUrl1: String,
                         @SerializedName("poto2_url") val shopProfileImageUrl2: String,
                         @SerializedName("poto3_url") val shopProfileImageUrl3: String,
                         @SerializedName("poto4_url") val shopProfileImageUrl4: String,
                         @SerializedName("poto5_url") val shopProfileImageUrl5: String): Parcelable {
        constructor(parcel: Parcel) : this(
            "",
            "",
            "",
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(shopIdx)
            parcel.writeString(managerIdx)
            parcel.writeString(shopName)
            parcel.writeString(shopTelNum)
            parcel.writeString(shopAddress)
            parcel.writeString(shopKeyword)
            parcel.writeString(shopInfo)
            parcel.writeString(shopLatitude)
            parcel.writeString(shopLongitude)
            parcel.writeString(mainPicNum)
            parcel.writeString(managerId)
            parcel.writeString(shopProfileImageUrl1)
            parcel.writeString(shopProfileImageUrl2)
            parcel.writeString(shopProfileImageUrl3)
            parcel.writeString(shopProfileImageUrl4)
            parcel.writeString(shopProfileImageUrl5)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<ShopModel> {
            override fun createFromParcel(parcel: Parcel): ShopModel {
                return ShopModel(parcel)
            }

            override fun newArray(size: Int): Array<ShopModel?> {
                return arrayOfNulls(size)
            }
        }
    }
}