package com.dmon.rentalhere.model

import android.os.Parcel
import android.os.Parcelable
import com.dmon.rentalhere.retrofit.*
import com.google.gson.annotations.SerializedName

data class ShopResult(@SerializedName("resultItem") val shopModel: ShopModel) {
    data class ShopModel(@SerializedName("method") val method: String? = null,
                         @SerializedName("result") val result: String? = null,
                         @SerializedName("msg") val message: String? = null,
                         @SerializedName(FIELD_SHOP_IDX) val shopIdx: String,
                         @SerializedName(FIELD_SHOP_MANAGER_ID) val managerIdx: String,
                         @SerializedName(FIELD_SHOP_NAME) val shopName: String,
                         @SerializedName(FIELD_SHOP_TEL_NUM) val shopTelNum: String,
                         @SerializedName(FIELD_SHOP_ADDRESS) val shopAddress: String,
//                         @SerializedName(FIELD_SHOP_KEYWORD) val shopKeyword: String,
                         @SerializedName(FIELD_SHOP_INFO) val shopInfo: String, // todo 없애기
                         @SerializedName(FIELD_SHOP_LAT) val shopLatitude: String,
                         @SerializedName(FIELD_SHOP_LNG) val shopLongitude: String,
                         @SerializedName(FIELD_SHOP_MAIN_PIC_NUM) val mainPicNum: String,
                         @SerializedName(FIELD_SHOP_ITEM_KINDS) val shopItemKinds: String,
                         @SerializedName(FIELD_USER_ID) val managerId: String,
                         @SerializedName("poto1_url") val shopProfileImageUrl1: String,
                         @SerializedName("poto2_url") val shopProfileImageUrl2: String,
                         @SerializedName("poto3_url") val shopProfileImageUrl3: String,
                         @SerializedName("poto4_url") val shopProfileImageUrl4: String,
                         @SerializedName("poto5_url") val shopProfileImageUrl5: String,
                         @SerializedName(FIELD_SHOP_BC_PIC_URL) val shopBcImageUrl: String? = null): Parcelable {
        constructor(parcel: Parcel) : this(
            "",
            "",
            "",
//            parcel.readString()!!,
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
//            parcel.writeString(shopKeyword)
            parcel.writeString(shopInfo)
            parcel.writeString(shopLatitude)
            parcel.writeString(shopLongitude)
            parcel.writeString(mainPicNum)
            parcel.writeString(shopItemKinds)
            parcel.writeString(managerId)
            parcel.writeString(shopProfileImageUrl1)
            parcel.writeString(shopProfileImageUrl2)
            parcel.writeString(shopProfileImageUrl3)
            parcel.writeString(shopProfileImageUrl4)
            parcel.writeString(shopProfileImageUrl5)
            parcel.writeString(shopBcImageUrl)
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