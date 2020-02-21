package com.dmon.rentalhere.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.dmon.rentalhere.R
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.CustomDialog
import com.dmon.rentalhere.presenter.TYPE_KEY
import kotlinx.android.synthetic.main.activity_sign_up.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.WindowManager
import com.dmon.rentalhere.presenter.CLIENT_TYPE
import com.dmon.rentalhere.presenter.OWNER_TYPE
import com.dmon.rentalhere.retrofit.*

class SignUpActivity : AppCompatActivity(), View.OnClickListener, AnkoLogger, TextWatcher {
    override val loggerTag: String
        get() = "SignUpActivity"
    private var userType: Int = 0
    private lateinit var retrofitService: RetrofitService
    private var isIdChecked = false
    private var isSignedUp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // edittext에 포커스 생겼을때 키보드에 가려지지 않게
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
        processIntent()
        setViewListener()
    }

    private fun setViewListener() {
        dupCheckButton.setOnClickListener(this)
        completeButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
        loginButton.setOnClickListener(this)
        idEditText.addTextChangedListener(this)
    }

    private fun processIntent() {
        userType = intent.getIntExtra(TYPE_KEY, 0)
        when(userType){
            CLIENT_TYPE -> topTextView.text = getString(R.string.client_sign_up)
            OWNER_TYPE -> topTextView.text = getString(R.string.owner_sign_up)
        }
    }

    override fun onClick(v: View?) {
        when(v){
            dupCheckButton -> checkId()
            completeButton -> checkBlanks()
            loginButton -> startLoginActivity()
            backButton -> finish()
        }
    }

    /**
     * 빈칸 체크
     */
    private fun checkBlanks() {
        when{
            idEditText.text.isEmpty() -> toast(getString(R.string.toast_type_id))
            pwEditText.text.isEmpty() -> toast(getString(R.string.toast_type_pw))
            pwEditText2.text.isEmpty() -> toast(getString(R.string.toast_type_pw2))
            pwEditText.text.toString() != pwEditText2.text.toString() -> toast(getString(R.string.toast_type_pw2))
            nameEditText.text.isEmpty() -> toast(getString(R.string.toast_type_name))
            cpEditText.text.isEmpty() -> toast(getString(R.string.toast_type_cp))
            emailEditText.text.isEmpty() -> toast(getString(R.string.toast_type_email))
            !isIdChecked -> toast(getString(R.string.toast_check_dup))
            else -> postSignUp()
        }
    }

    /**
     * 회원 가입 요청
     */
    private fun postSignUp() {
        val map = HashMap<String, Any>().apply{
            this[FIELD_USER_ID] = idEditText.text.toString()
            this[FIELD_USER_PW] = pwEditText.text.toString()
            this[FIELD_USER_NAME] = nameEditText.text.toString()
            this[FIELD_USER_EMAIL] = emailEditText.text.toString()
            this[FIELD_USER_CP_NUM] = cpEditText.text.toString()
            this[FIELD_USER_DIV] = userType
        }
        retrofitService.postSignUp(map).enqueue(object : Callback<BaseResult>{
            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                val responseResult: BaseResult = response.body()!!
                when(responseResult.baseResultItem.result){
                    "Y" -> {
                        isSignedUp = true
                        congTextView.text = getString(R.string.sign_up_complete)
                        congTextView.visibility = View.VISIBLE
                        loginButton.visibility = View.VISIBLE
                        scrollView.visibility = View.INVISIBLE
                        backButton.setOnClickListener { v -> startLoginActivity() } }
                }
            }

            override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                error("실패")
            }
        })
    }

    private fun startLoginActivity() =
        startActivity(Intent(this@SignUpActivity, LoginActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })


    /**
     * 아이디 중복 체크 요철
     */
    private fun checkId() {
        info("checkId called")
        val idBody = getRequestBody(idEditText.text.toString())
        if(idEditText.text.isNotEmpty()) {
            retrofitService.postCheckIdDup(idBody).enqueue(object : Callback<BaseResult> {
                override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                    val responseResult: BaseResult = response.body()!!
                    val result = responseResult.baseResultItem.result
                    info(result)
                    when(result){
                        "N" -> CustomDialog(this@SignUpActivity, getString(R.string.id_is_exist)).show()
                        else -> {
                            CustomDialog(this@SignUpActivity, idEditText.text.toString()).show()
                            isIdChecked = true
                        }
                    }
                }

                override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                    error("실패")
                }
            })
        }else toast(getString(R.string.toast_type_id))
    }

    val getRequestBody: (String) -> RequestBody = { RequestBody.create(MediaType.parse("text/plain"), it) }

    override fun onBackPressed() {
        if(isSignedUp) startLoginActivity()
        else super.onBackPressed()
    }

    /**
     * implements TextWatcher
     */
    override fun afterTextChanged(s: Editable?) {
        isIdChecked = false
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
}
