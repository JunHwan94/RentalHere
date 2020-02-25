package com.dmon.rentalhere.retrofit

import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.ShopResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

// METHODS
const val SERVER_URL = "app/app_main/"
const val CHECK_ID_METHOD = "id_ck"
const val SIGNUP_METHOD = "adduser"
const val LOGIN_METHOD = "log_ck"
const val GET_USER_METHOD = "get_user"
const val GET_SHOP_METHOD = "get_shop"

//FIELDS
const val FIELD_USER_ID = "mem_userid"
const val FIELD_USER_PW = "mem_password"
const val FIELD_USER_NAME = "mem_username"
const val FIELD_USER_EMAIL = "mem_email"
const val FIELD_USER_CP_NUM = "mem_phone"
const val FIELD_USER_DIV = "mem_manager"

const val FIELD_SHOP_IDX = "cs_idx"
const val FIELD_SHOP_MANAGER_ID = "mem_id"
const val FIELD_SHOP_NAME = "cs_name"
const val FIELD_SHOP_TEL_NUM = "cs_phone"
const val FIELD_SHOP_ADDRESS = "cs_add"
const val FIELD_SHOP_KEYWORD = "cs_keyword"
const val FIELD_SHOP_INFO = "cs_info"
const val FIELD_SHOP_LAT = "cs_lat"
const val FIELD_SHOP_LNG = "cs_lng"
const val FIELD_SHOP_PHOTO1_URL = "poto1_url"
const val FIELD_SHOP_PHOTO2_URL = "poto2_url"
interface RetrofitService {
    // 아이디 중복 확인
    @Multipart
    @POST(SERVER_URL + CHECK_ID_METHOD)
    fun postCheckIdDup(@Part(FIELD_USER_ID) userId: RequestBody): Call<BaseResult>

    // 회원가입 요청
    @FormUrlEncoded
    @POST(SERVER_URL + SIGNUP_METHOD)
    fun postSignUp(@FieldMap param: HashMap<String, Any>): Call<BaseResult>

    // 로그인 요청
    @FormUrlEncoded
    @POST(SERVER_URL + LOGIN_METHOD)
    fun postLogin(@FieldMap param: HashMap<String, Any>): Call<BaseResult>

    // 매장 정보 요청
    @FormUrlEncoded
    @POST(SERVER_URL + GET_SHOP_METHOD)
    fun postGetShop(@FieldMap param: HashMap<String, Any>): Call<ShopResult>
}