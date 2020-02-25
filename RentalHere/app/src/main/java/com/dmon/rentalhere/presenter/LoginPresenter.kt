package com.dmon.rentalhere.presenter

import android.content.Context
import android.content.Intent
import android.view.View
import com.dmon.rentalhere.R
import com.dmon.rentalhere.constants.LoginConstants
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.LoginModel
import com.dmon.rentalhere.retrofit.FIELD_USER_ID
import com.dmon.rentalhere.retrofit.FIELD_USER_PW
import com.dmon.rentalhere.retrofit.RetrofitClient
import com.dmon.rentalhere.retrofit.RetrofitService
import com.dmon.rentalhere.view.FindUserActivity
import com.dmon.rentalhere.view.MainActivity
import com.dmon.rentalhere.view.TermsActivity
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**

 * Created by jjun on 2020-02-13.

 */
const val CLIENT_TYPE = 0
const val OWNER_TYPE = 1
const val TYPE_KEY = "typeKey"
const val ID_TYPE = 0
const val PW_TYPE = 1
class LoginPresenter(private val loginView: LoginConstants.View, private val context: Context): LoginConstants.Presenter, AnkoLogger {
    override val loggerTag: String get() = "LoginPresenter"
    private val loginModel: LoginModel = LoginModel()
    private var retrofitService: RetrofitService

    init {
        loginView.setButtons()
        loginView.startLoginDivideFadeInAnim()
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.clientLoginButton -> {
                loginModel.userType = CLIENT_TYPE
                loginView.startLoginDivideFadeOutAnim()
            }
            R.id.ownerLoginButton -> {
                loginModel.userType = OWNER_TYPE
                loginView.startLoginDivideFadeOutAnim()
            }
            R.id.signUpButton -> {
                context.startActivity(Intent(context, TermsActivity::class.java).apply { putExtra(TYPE_KEY, loginModel.userType) })
            }
            R.id.findButton -> if(loginView.checkBlank()) postLogin()
            R.id.findIdButton -> startFindActivity(ID_TYPE)
            R.id.findPwButton -> startFindActivity(PW_TYPE)
        }
    }

    /**
     * 아이디, 비밀번호 찾기 액티비티 실행
     */
    private fun startFindActivity(type: Int) = context.startActivity(Intent(context, FindUserActivity::class.java).apply{ putExtra(TYPE_KEY, type) })

    /**
     * 로그인 요청
     */
    override fun postLogin() {
        val map = HashMap<String, Any>().apply{
            this[FIELD_USER_ID] = loginView.userId()
            this[FIELD_USER_PW] = loginView.userPw()
        }
        retrofitService.postLogin(map).enqueue(object : Callback<BaseResult> {
            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                val result = response.body()!!.baseResultItem.result
                if(result == "Y") context.startActivity(Intent(context, MainActivity::class.java))
                else context.toast(context.getString(R.string.toast_check_id_pw))
            }

            override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                error("실패")
            }
        })
    }

    override fun onBackPressed() {
        loginView.startLoginLayoutFadeOutAnim()
    }
}