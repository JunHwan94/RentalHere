package com.dmon.rentalhere.presenter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import com.dmon.rentalhere.R
import com.dmon.rentalhere.constants.LoginConstants
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.CustomDialog
import com.dmon.rentalhere.model.LoginModel
import com.dmon.rentalhere.retrofit.FIELD_USER_ID
import com.dmon.rentalhere.retrofit.FIELD_USER_PW
import com.dmon.rentalhere.retrofit.RetrofitClient
import com.dmon.rentalhere.retrofit.RetrofitService
import com.dmon.rentalhere.view.FindUserActivity
import com.dmon.rentalhere.view.ClientMainActivity
import com.dmon.rentalhere.view.OwnerMainActivity
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
const val ID_KEY = "idKey"
const val PREF_KEY = "prefKey"
class LoginPresenter(private val loginView: LoginConstants.View, private val context: Context, private val intent: Intent? = null): LoginConstants.Presenter, AnkoLogger {
    override val loggerTag: String get() = "LoginPresenter"
    private val loginModel: LoginModel = LoginModel()
    private var retrofitService: RetrofitService

    init {
        loginView.setButtons()
        when{
            intent != null -> {
                loginView.showLoginLayout()
                loginModel.userType = intent.getIntExtra(TYPE_KEY, 0)
            }
            else -> loginView.startLoginDivideFadeInAnim()
        }
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.clientLoginButton -> {
                loginModel.userType = CLIENT_TYPE
                context.run{
                    CustomDialog(this, getString(R.string.login_or_not)).showLoginOrNot(
                        { loginView.startLoginDivideFadeOutAnim() }
                    ){
                        startActivity(Intent(this, ClientMainActivity::class.java).apply{
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        })
                        loginView.finish()
                    }
                }
            }
            R.id.ownerLoginButton -> {
                loginModel.userType = OWNER_TYPE
                loginView.run{
                    startLoginDivideFadeOutAnim()
                }
            }
            R.id.signUpButton -> {
                context.startActivity(Intent(context, TermsActivity::class.java).apply { putExtra(TYPE_KEY, loginModel.userType) })
            }
            R.id.loginButton -> if(loginView.checkBlank()) postLogin()
            R.id.findIdButton -> startFindActivity(ID_TYPE)
            R.id.findPwButton -> startFindActivity(PW_TYPE)
        }
    }

    /**
     * 아이디, 비밀번호 찾기 액티비티 실행
     */
    private fun startFindActivity(type: Int) = context.startActivity(Intent(context, FindUserActivity::class.java).apply{ putExtra(TYPE_KEY, type) })

    /**
     * 로그인 요청 구분
     */
    override fun postLogin() {
        val map = HashMap<String, Any>().apply{
            this[FIELD_USER_ID] = loginView.userId()
            this[FIELD_USER_PW] = loginView.userPw()
        }
        when(loginModel.userType) {
            CLIENT_TYPE -> requestLogin(retrofitService.postClientLogin(map), ClientMainActivity::class.java)
            OWNER_TYPE -> requestLogin(retrofitService.postOwnerLogin(map), OwnerMainActivity::class.java)
        }
    }

    /**
     * 로그인 요청
     */
    private fun <T> requestLogin(call: Call<BaseResult>, activityClass: Class<T>){
        call.enqueue(object : Callback<BaseResult>{
            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                val result = response.body()!!.baseModel
                when{
                    result.result == "Y" -> {
                        startMainActivity(activityClass)
                        if (loginView.autoLoginEnabled()) setAutoLogin()
                    }
                    result.result == "N" ->{
                        context.run {
                            Log.d("받은 값", result.message)
                            when (result.message) {
                                getString(R.string.login_failed) ->
                                    toast(getString(R.string.toast_check_id_pw))
                                else -> toast(result.message)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                context.run{
                    toast(getString(R.string.toast_request_failed))
                }
                error("실패")
            }
        })
    }

    /**
     * 메인 액티비티 실행
     */
    private fun <T> startMainActivity(java: Class<T>) {
        context.startActivity(Intent(context, java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK
            val userId = loginView.userId()
            info(userId)
            putExtra(ID_KEY, userId)
        })
        loginView.finish()
    }

    /**
     * 자동로그인 설정
     */
    private fun setAutoLogin() {
        val editor = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE).edit()
        with(editor){
            putString(ID_KEY, loginView.userId())
            putInt(TYPE_KEY, loginModel.userType!!)
            commit()
        }
    }

    override fun onBackPressed() {
        loginView.startLoginLayoutFadeOutAnim()
    }
}