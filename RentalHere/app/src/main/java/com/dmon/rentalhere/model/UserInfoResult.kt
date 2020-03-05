package com.dmon.rentalhere.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class UserInfoResult(@SerializedName("resultItem") val userModel: UserModel) {
    data class UserModel(@SerializedName("result") val result: String,
                         @SerializedName("mem_id") val userIdx: String,
                         @SerializedName("mem_userid") val userId: String,
                         @SerializedName("mem_username") val userName: String,
                         @SerializedName("mem_email") val userEmail: String,
                         @SerializedName("mem_phone") val userCpNum: String,
                         @SerializedName("pass") val pass: String? = null): Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()
        ) {
        }
        constructor(userId: String, userName: String, userEmail: String, userCpNum: String) : this("Y", "", userId, userName, userEmail, userCpNum, "")

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(result)
            parcel.writeString(userIdx)
            parcel.writeString(userId)
            parcel.writeString(userName)
            parcel.writeString(userEmail)
            parcel.writeString(userCpNum)
            parcel.writeString(pass)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<UserModel> {
            override fun createFromParcel(parcel: Parcel): UserModel {
                return UserModel(parcel)
            }

            override fun newArray(size: Int): Array<UserModel?> {
                return arrayOfNulls(size)
            }
        }
    }
}