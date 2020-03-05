package com.dmon.rentalhere.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dmon.rentalhere.R
import com.dmon.rentalhere.model.UserInfoResult
import com.dmon.rentalhere.presenter.PW_TYPE
import com.dmon.rentalhere.presenter.TYPE_KEY
import kotlinx.android.synthetic.main.activity_find_user.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.dmon.rentalhere.presenter.ID_TYPE
import com.dmon.rentalhere.retrofit.*
import kotlinx.android.synthetic.main.activity_find_user.backButton
import kotlinx.android.synthetic.main.activity_find_user.cpEditText
import kotlinx.android.synthetic.main.activity_find_user.emailEditText
import kotlinx.android.synthetic.main.activity_find_user.findButton
import kotlinx.android.synthetic.main.activity_find_user.nameEditText
import kotlinx.android.synthetic.main.activity_find_user.topTextView
import kotlinx.android.synthetic.main.activity_sign_up.*

class FindUserActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var retrofitService: RetrofitService
    private var findType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_user)
        setViewListener()
        processIntent()
    }

    private fun setView() {
        topTextView.text = getString(R.string.find_pw)
        findButton.text = getString(R.string.find_pw)
        emailEditText.visibility = View.VISIBLE
        emailTextView.visibility = View.VISIBLE
        nameTextView.text = getString(R.string.id_id)
        nameEditText.hint = getString(R.string.id)
        cpTextView.text = getString(R.string.name)
        cpEditText.run{
            hint = getString(R.string.name_hint)
            inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        }
        textView.visibility = View.GONE
        idTextView.run{
            val params = (layoutParams as ConstraintLayout.LayoutParams)
            params.topMargin = 300
            layoutParams = params
        }
        loginButton.run{
            val params = (layoutParams as ConstraintLayout.LayoutParams)
            params.topMargin = 300
            layoutParams = params
        }
    }

    private fun processIntent() {
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
        findType = intent.getIntExtra(TYPE_KEY, 0)
        when(findType){
            PW_TYPE -> setView()
            ID_TYPE -> setTelTextWatcher()
        }
    }

    private fun setTelTextWatcher() {
        cpEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s.toString().length > 11) {
                    cpEditText.setText(s.toString().dropLast(1))
                    cpEditText.setSelection(11) // todo : 수정?
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun setViewListener() {
        backButton.setOnClickListener(this)
        findButton.setOnClickListener(this)
        loginButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            backButton -> finish()
            findButton -> checkBlank()
            loginButton -> startLoginActivity()
        }
    }

    private fun checkBlank(){
        when(findType){
            ID_TYPE -> when{
                nameEditText.text.isEmpty() -> toast(getString(R.string.toast_type_id))
                cpEditText.text.isEmpty() -> toast(getString(R.string.toast_type_cp))
                else -> findID()
            }
            PW_TYPE -> when{
                nameEditText.text.isEmpty() -> toast(getString(R.string.toast_type_id))
                cpEditText.text.isEmpty() -> toast(getString(R.string.toast_type_name))
                emailEditText.text.isEmpty() -> toast(getString(R.string.toast_type_email))
                else -> findPW()
            }
        }
    }

    /**
     * 아이디 찾기 요청
     */
    private fun findID() {
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

    /**
     * 비밀번호 찾기 요청
     */
    private fun findPW() {
        val map = HashMap<String, Any>().apply{
            this[FIELD_USER_ID] = nameEditText.text.toString()
            this[FIELD_USER_NAME] = cpEditText.text.toString()
            this[FIELD_USER_EMAIL] = emailEditText.text.toString()
        }
        retrofitService.postFindUserPW(map).enqueue(object : Callback<UserInfoResult>{
            override fun onResponse(call: Call<UserInfoResult>, response: Response<UserInfoResult>) {
                val result = response.body()!!.userModel
                if(result.result == "Y"){
                    setViewAfterFind(result.pass!!)
                    hideKeyBoard()
                }else toast(getString(R.string.cant_find))
            }

            override fun onFailure(call: Call<UserInfoResult>, t: Throwable) {

            }
        })
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
     * 찾은 후 뷰 설정
     */
    fun setViewAfterFind(value: String){
        backButton.visibility = View.INVISIBLE
        topTextView.run{ text = "$text ${getString(R.string.result)}" }
        idTextView.run{ text =
            when(findType){
                ID_TYPE -> getString(R.string.id) + " : " + value
                PW_TYPE -> getString(R.string.temp_password) + " : " + value
                else -> ""
            }
        }
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
