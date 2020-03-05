package com.dmon.rentalhere.retrofit

import com.dmon.rentalhere.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

// METHODS
const val SERVER_URL = "app/app_main/"
const val CHECK_ID_METHOD = "id_ck"
const val SIGNUP_METHOD = "adduser"
const val CLIENT_LOGIN_METHOD = "log_ck"
const val GET_USER_METHOD = "get_user"
const val GET_SHOP_METHOD = "get_shop"
const val GET_REVIEW_METHOD = "get_review"
const val POST_REVIEW_METHOD = "add_review"
const val FIND_USER_ID_METHOD = "get_userid"
const val FIND_USER_PW_METHOD = "find_pass"
const val EDIT_USER_METHOD = "put_user"
const val GET_MY_REVIEW_METHOD = "get_myreview"
const val OWNER_LOGIN_METHOD = "log_manager"
const val GET_MY_SHOPS_METHOD = "get_myshop"
const val REGISTER_SHOP_METHOD = "add_company"
const val DELETE_REVIEW_METHOD = "del_review"
const val DELETE_SHOP_METHOD = "del_company"
const val EDIT_SHOP_METHOD = "up_company"

//FIELDS
const val FIELD_USER_IDX = "mem_id"
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
const val FIELD_SHOP_MAIN_PICTURE = "cs_poto1"
const val FIELD_SHOP_LAT = "cs_lat"
const val FIELD_SHOP_LNG = "cs_lng"
const val FIELD_SHOP_PHOTO1_URL = "poto1_url"
const val FIELD_SHOP_PHOTO2_URL = "poto2_url"

const val FIELD_REVIEW_CONTENT = "cr_text"
const val FIELD_REVIEW_SCORE = "cr_score"
const val FIELD_REVIEW_IDX = "cr_idx"
interface RetrofitService {
    // 아이디 중복 확인
    @Multipart
    @POST(SERVER_URL + CHECK_ID_METHOD)
    fun postCheckIdDup(@Part(FIELD_USER_ID) userId: RequestBody): Call<BaseResult>

    // 회원가입 요청
    @FormUrlEncoded
    @POST(SERVER_URL + SIGNUP_METHOD)
    fun postSignUp(@FieldMap param: HashMap<String, Any>): Call<BaseResult>

    // 회원 정보 수정 요청
    @FormUrlEncoded
    @POST(SERVER_URL + EDIT_USER_METHOD)
    fun postEditUserInfo(@FieldMap param: HashMap<String, Any>): Call<BaseResult>

    // 사용자 로그인 요청
    @FormUrlEncoded
    @POST(SERVER_URL + CLIENT_LOGIN_METHOD)
    fun postClientLogin(@FieldMap param: HashMap<String, Any>): Call<BaseResult>

    // 업주 로그인 요청
    @FormUrlEncoded
    @POST(SERVER_URL + OWNER_LOGIN_METHOD)
    fun postOwnerLogin(@FieldMap param: HashMap<String, Any>): Call<BaseResult>

    // 회원정보 요청
    @FormUrlEncoded
    @POST(SERVER_URL + GET_USER_METHOD)
    fun postGetUser(@FieldMap param: HashMap<String, Any>): Call<UserInfoResult>

    // 아이디 찾기 요청
    @FormUrlEncoded
    @POST(SERVER_URL + FIND_USER_ID_METHOD)
    fun postFindUserID(@FieldMap param: HashMap<String, Any>): Call<UserInfoResult>

    // 비밀번호 찾기 요청
    @FormUrlEncoded
    @POST(SERVER_URL + FIND_USER_PW_METHOD)
    fun postFindUserPW(@FieldMap param: HashMap<String, Any>): Call<UserInfoResult>

    // 매장 정보 요청
    @FormUrlEncoded
    @POST(SERVER_URL + GET_SHOP_METHOD)
    fun postGetShop(@FieldMap param: HashMap<String, Any>): Call<ShopResult>

    // 리뷰 목록 요청
    @FormUrlEncoded
    @POST(SERVER_URL + GET_REVIEW_METHOD)
    fun postGetReview(@FieldMap param: HashMap<String, Any>): Call<ReviewResult>

    // 내 리뷰 목록 요청
    @FormUrlEncoded
    @POST(SERVER_URL + GET_MY_REVIEW_METHOD)
    fun postGetMyReview(@FieldMap param: HashMap<String, Any>): Call<ReviewResult>

    // 리뷰 등록
    @FormUrlEncoded
    @POST(SERVER_URL + POST_REVIEW_METHOD)
    fun postReview(@FieldMap param: HashMap<String, Any>): Call<BaseResult>

    // 내 매장 목록 요청
    @FormUrlEncoded
    @POST(SERVER_URL + GET_MY_SHOPS_METHOD)
    fun postGetMyShops(@FieldMap param: HashMap<String, Any>): Call<MyShopsResult>

    /** 이미지 업로드를 위해 @Multipart 사용  */
    @Multipart
    @POST(SERVER_URL + REGISTER_SHOP_METHOD)
    fun postRegisterShop(@Part(FIELD_USER_IDX) userIdx: RequestBody,
                         @Part(FIELD_SHOP_NAME) shopName: RequestBody,
                         @Part(FIELD_SHOP_TEL_NUM) shopTelNum: RequestBody,
                         @Part(FIELD_SHOP_KEYWORD) shopKeyword: RequestBody,
                         @Part(FIELD_SHOP_INFO) shopInfo: RequestBody,
                         @Part(FIELD_SHOP_ADDRESS) shopAddress: RequestBody,
                         @Part(FIELD_SHOP_LAT) shopLatitude: RequestBody,
                         @Part(FIELD_SHOP_LNG) shopLongitude: RequestBody,
                         @Part part1: MultipartBody.Part? = null,
                         @Part part2: MultipartBody.Part? = null,
                         @Part part3: MultipartBody.Part? = null,
                         @Part part4: MultipartBody.Part? = null,
                         @Part part5: MultipartBody.Part? = null
    ): Call<BaseResult>

    // 리뷰 삭제
    @FormUrlEncoded
    @POST(SERVER_URL + DELETE_REVIEW_METHOD)
    fun postDeleteReview(@FieldMap param: HashMap<String, Any>): Call<BaseResult>

    // 매장 삭제
    @FormUrlEncoded
    @POST(SERVER_URL + DELETE_SHOP_METHOD)
    fun postDeleteShop(@FieldMap param: HashMap<String, Any>): Call<BaseResult>

    // 매장 삭제
    @FormUrlEncoded
    @POST(SERVER_URL + EDIT_SHOP_METHOD)
    fun postEditShop(@FieldMap param: HashMap<String, Any>): Call<BaseResult>
}