package com.dmon.rentalhere

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dmon.rentalhere.model.UserInfoResult
import com.dmon.rentalhere.presenter.PREF_KEY
import com.dmon.rentalhere.retrofit.RetrofitClient
import com.dmon.rentalhere.retrofit.RetrofitService
import com.dmon.rentalhere.view.EDIT_USER_CODE
import com.dmon.rentalhere.view.LoginActivity
import com.dmon.rentalhere.view.SignUpActivity
import com.dmon.rentalhere.view.USER_MODEL_KEY
import kotlinx.android.synthetic.main.activity_client_main.*

open class BaseActivity : AppCompatActivity(){
    lateinit var retrofitService: RetrofitService
    lateinit var userModel: UserInfoResult.UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == EDIT_USER_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                it.getParcelableExtra<UserInfoResult.UserModel>(USER_MODEL_KEY)?.let { userModel ->
                    this.userModel = userModel
                }
            }
        }
    }

    /**
     *  내비게이션 뷰 열기 / 닫기
     */
    fun moveDrawer() {
        if(!drawer.isDrawerOpen(GravityCompat.END))
            drawer.openDrawer(GravityCompat.END)
        else drawer.closeDrawer(GravityCompat.END)

    }

    /**
     * 정보 수정 (SignUpActivity 실행)
     */
    fun editUser(userModel: UserInfoResult.UserModel) {
        startActivityForResult(Intent(this, SignUpActivity::class.java).apply{
            putExtra(USER_MODEL_KEY, userModel)
        }, EDIT_USER_CODE)
    }

    /**
     * 로그아웃
     */
    fun logOut(){
        val editor = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE).edit()
        with(editor){
            clear()
            commit()
        }
        startActivity(Intent(this, LoginActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }
}