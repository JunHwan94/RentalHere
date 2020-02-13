package com.dmon.rentalhere

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**

 * Created by jjun on 2020-02-13.

 */
const val BASE_URL = "http://dmonster926.cafe24.com/"
const val KAKAO_BASE_URL = "http://dapi.kakao.com"
class RetrofitClient {
    private lateinit var retrofit: Retrofit
    private var retrofit_kakao: Retrofit? = null

    fun getRetrofitInstance(): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }

    fun getKakaoRetrofitInstance(): Retrofit? {
        if (retrofit_kakao == null) {
            retrofit_kakao = Retrofit.Builder()
                .baseUrl(KAKAO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit_kakao
    }
}