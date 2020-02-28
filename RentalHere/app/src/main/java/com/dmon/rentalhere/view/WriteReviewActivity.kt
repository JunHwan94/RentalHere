package com.dmon.rentalhere.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dmon.rentalhere.R
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.retrofit.*
import kotlinx.android.synthetic.main.activity_write_review.*
import org.jetbrains.anko.AnkoLogger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WriteReviewActivity : AppCompatActivity(), View.OnClickListener, AnkoLogger {
    override val loggerTag: String
        get() = "WriteReviewAc"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_review)
        setViewListener()
    }

    private fun setViewListener() {
        backButton.setOnClickListener(this)
        postButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            backButton -> finish()
            postButton -> postReview()
        }
    }

    private fun postReview() {
        val retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
        val map = HashMap<String, Any>().apply{
            this[FIELD_SHOP_IDX] = intent.getStringExtra(FIELD_SHOP_IDX)!!
            this[FIELD_USER_IDX] = intent.getStringExtra(FIELD_USER_IDX)!!
            this[FIELD_REVIEW_CONTENT] = editText.text.toString()
            this[FIELD_REVIEW_SCORE] = ratingBar.rating.toString()
        }

        retrofitService.postReview(map).enqueue(object : Callback<BaseResult>{
            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                val result = response.body()!!.baseModel.result
                if(result == "Y") {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }

            override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                error("요청 실패")
            }
        })
    }
}
