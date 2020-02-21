package com.dmon.rentalhere.retrofit

import com.dmon.rentalhere.model.BaseResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

// METHODS
const val SERVER_URL = "app/app_main/"
const val CHECK_ID_METHOD = "id_ck"
const val SIGNUP_METHOD = "adduser"
const val LOGIN_METHOD = "log_ck"
const val GET_USER_METHOD = "get_user"

//FIELDS
const val FIELD_USER_ID = "mem_userid"
const val FIELD_USER_PW = "mem_password"
const val FIELD_USER_NAME = "mem_username"
const val FIELD_USER_EMAIL = "mem_email"
const val FIELD_USER_CP_NUM = "mem_phone"
const val FIELD_USER_DIV = "mem_manager"
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
}