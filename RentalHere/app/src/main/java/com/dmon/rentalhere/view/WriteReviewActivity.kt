package com.dmon.rentalhere.view

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dmon.rentalhere.BaseActivity
import com.dmon.rentalhere.R
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.retrofit.*
import kotlinx.android.synthetic.main.activity_write_review.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val WRITE_TAG = "WriteReviewActivity"
class WriteReviewActivity : BaseActivity(), View.OnClickListener, AnkoLogger {
    override val loggerTag: String get() = WRITE_TAG

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
            postButton -> checkBlank()
        }
    }

    private fun checkBlank(){
        when{
            ratingBar.rating == 0f -> toast(getString(R.string.toast_set_rating))
            editText.text.isEmpty() -> toast(getString(R.string.toast_type_review))
            else -> postReview()
        }
    }

    private fun postReview() {
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
