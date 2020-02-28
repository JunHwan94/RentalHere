package com.dmon.rentalhere.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dmon.rentalhere.R
import com.dmon.rentalhere.model.UserInfoResult
import com.dmon.rentalhere.presenter.PW_TYPE
import com.dmon.rentalhere.presenter.TYPE_KEY
import com.dmon.rentalhere.retrofit.FIELD_USER_CP_NUM
import com.dmon.rentalhere.retrofit.FIELD_USER_NAME
import com.dmon.rentalhere.retrofit.RetrofitClient
import com.dmon.rentalhere.retrofit.RetrofitService
import kotlinx.android.synthetic.main.activity_find_user.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.view.inputmethod.InputMethodManager


class FindUserActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_user)
        setViewListener()
        processIntent()
    }

    private fun processIntent() {
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
        when(intent.getIntExtra(TYPE_KEY, 0)){
            PW_TYPE -> {
                topTextView.text = getString(R.string.find_pw)
                findButton.text = getString(R.string.find_pw)
            }
        }
    }

    private fun setViewListener() {
        backButton.setOnClickListener(this)
        findButton.setOnClickListener(this)
        loginButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            backButton -> finish()
            findButton -> findID()
            loginButton -> startLoginActivity()
        }
    }

    /**
     * 아이디 찾기 요청
     */
    private fun findID() {
        when{
            nameEditText.text.isEmpty() -> toast(getString(R.string.toast_type_id))
            cpEditText.text.isEmpty() -> toast(getString(R.string.toast_type_cp))
            else -> {
                val map = HashMap<String, Any>().apply{
                    this[FIELD_USER_NAME] = nameEditText.text.toString()
                    this[FIELD_USER_CP_NUM] = cpEditText.text.toString()
                }
                retrofitService.postFindUserID(map).enqueue(object : Callback<UserInfoResult>{
                    override fun onResponse(call: Call<UserInfoResult>, response: Response<UserInfoResult>) {
                        val result = response.body()!!.userModel
                        if(result.result == "Y"){
                            setViewAfterFind(result.userId)
                            hideKeyBoard()

                        }else toast(getString(R.string.cant_find))
                    }

                    override fun onFailure(call: Call<UserInfoResult>, t: Throwable) {

                    }
                })
            }
        }
    }

    /**
     * 키보드 가리기
     */
    fun hideKeyBoard(){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(cpEditText.windowToken, 0)
        imm.hideSoftInputFromWindow(nameEditText.windowToken, 0)
    }

    /**
     * 아이디 찾은 후 뷰 설정
     */
    fun setViewAfterFind(userId: String){
        backButton.visibility = View.INVISIBLE
        topTextView.run{ text = "$text ${getString(R.string.result)}" }
        idOrShopTextView.run{ text = getString(R.string.id) + " : " + userId}
        findLayout.visibility = View.GONE
        resultLayout.visibility = View.VISIBLE
    }

    /**
     * 로그인 액티비티로 돌아가기
     */
    private fun startLoginActivity() =
        startActivity(Intent(this, LoginActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
}
