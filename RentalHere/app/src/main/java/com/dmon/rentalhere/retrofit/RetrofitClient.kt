package com.dmon.rentalhere.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**

 * Created by jjun on 2020-02-13.

 */
const val BASE_URL = "http://softer013.cafe24.com/"
const val KAKAO_BASE_URL = "http://dapi.kakao.com"
class RetrofitClient {
    companion object {
        private var retrofit: Retrofit? = null
        private var kakaoRetrofit: Retrofit? = null
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
            if (kakaoRetrofit == null) {
                kakaoRetrofit = Retrofit.Builder()
                    .baseUrl(KAKAO_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return kakaoRetrofit
        }
    }
}