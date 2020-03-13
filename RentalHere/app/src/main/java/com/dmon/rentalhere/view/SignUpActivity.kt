package com.dmon.rentalhere.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.dmon.rentalhere.R
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.CustomDialog
import com.dmon.rentalhere.presenter.TYPE_KEY
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.WindowManager
import com.dmon.rentalhere.BaseActivity
import com.dmon.rentalhere.model.UserInfoResult
import com.dmon.rentalhere.presenter.CLIENT_TYPE
import com.dmon.rentalhere.presenter.OWNER_TYPE
import com.dmon.rentalhere.retrofit.*
import kotlinx.android.synthetic.main.activity_sign_up.backButton
import kotlinx.android.synthetic.main.activity_sign_up.elecCheckBox
import kotlinx.android.synthetic.main.activity_sign_up.scrollView
import kotlinx.android.synthetic.main.activity_sign_up.topTextView

const val SIGNUP_TAG = "SignUpActivity"
class SignUpActivity : BaseActivity(), View.OnClickListener, AnkoLogger {
    override val loggerTag: String get() = SIGNUP_TAG
    private var userType: Int = 0
    private var isIdChecked = false
    private var isSignedUp = false
    private var isEditPage :Boolean = false
    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userCpNum: String

    private val idWatcher = object : TextWatcher{
        override fun afterTextChanged(s: Editable?) { isIdChecked = false }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
    private val cpWatcher = object : TextWatcher{
        override fun afterTextChanged(s: Editable?) {
            if(s.toString().length > 11) {
                cpEditText.setText(s.toString().dropLast(1))
                cpEditText.setSelection(11) // todo : 수정?
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // edittext에 포커스 생겼을때 키보드에 가려지지 않게
        processIntent()
        setViewListener()
    }

    private fun setViewListener() {
        dupCheckButton.setOnClickListener(this)
        completeButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
        findButton.setOnClickListener(this)
        idEditText.addTextChangedListener(idWatcher)
        cpEditText.addTextChangedListener(cpWatcher)
    }

    private fun processIntent() {
        // 일반 / 업주 구분
        userType = intent.getIntExtra(TYPE_KEY, 0)
        when(userType){
            CLIENT_TYPE -> topTextView.text = getString(R.string.client_sign_up)
            OWNER_TYPE -> setOwnerView()
        }

        setEditView()
    }

    /**
     * 정보 수정일 때 뷰 설정
     */
    private fun setEditView(){
        val userModel = intent.getParcelableExtra<UserInfoResult.UserModel>(USER_MODEL_KEY)
        userModel?.let{
            isIdChecked = true
            topTextView.text = getString(R.string.edit_info)
            isEditPage = true
            idEditText.run{ isEnabled = false; background = null }
            dupCheckButton.visibility = View.INVISIBLE
            idEditText.setText(it.userId)
            nameEditText.setText(it.userName)
            cpEditText.setText(it.userCpNum.replace("-", ""))
            emailEditText.setText(it.userEmail)
            if(userType == CLIENT_TYPE) checkJobKinds(userModel)
        }
    }

    private fun checkJobKinds(userModel: UserInfoResult.UserModel){
        userModel.userJobKinds.run{
            if(contains(getString(R.string.electric))) elecCheckBox.isChecked = true
            if(contains(getString(R.string.fireFighting))) fireCheckBox.isChecked = true
            if(contains(getString(R.string.iron))) ironCheckBox.isChecked = true
            if(contains(getString(R.string.plastere))) plasCheckBox.isChecked = true
            if(contains(getString(R.string.etc))) etcCheckBox.isChecked = true
        }
    }

    /**
     * 업주일 때 뷰 설정
     */
    private fun setOwnerView(){
        topTextView.text = getString(R.string.owner_sign_up)
        jobKindsTextView.visibility = View.GONE
        elecCheckBox.visibility = View.GONE
        fireCheckBox.visibility = View.GONE
        ironCheckBox.visibility = View.GONE
        plasCheckBox.visibility = View.GONE
        etcCheckBox.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when(v){
            dupCheckButton -> checkId()
            completeButton -> checkBlanks()
            findButton -> startLoginActivity()
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
            cpEditText.text.isEmpty() or (cpEditText.text.length < 11) -> toast(getString(R.string.toast_type_cp))
//            emailEditText.text.isEmpty() -> toast(getString(R.string.toast_type_email))
            !elecCheckBox.isChecked &&
                    !fireCheckBox.isChecked &&
                    !ironCheckBox.isChecked &&
                    !plasCheckBox.isChecked &&
                    !etcCheckBox.isChecked -> toast(getString(R.string.toast_check_jobs))
            !isIdChecked -> toast(getString(R.string.toast_check_dup))
            else -> {
                userId = idEditText.text.toString()
                userName = nameEditText.text.toString()
                userEmail = emailEditText.text.toString()
                userCpNum = cpEditText.text.toString()
                val map = HashMap<String, Any>().apply{
                    this[FIELD_USER_ID] = userId
                    this[FIELD_USER_PW] = pwEditText.text.toString()
                    this[FIELD_USER_NAME] = userName
                    this[FIELD_USER_EMAIL] = userEmail
                    this[FIELD_USER_CP_NUM] = userCpNum
//                    this[FIELD_USER_COMPANY] =
                    this[FIELD_USER_JOB_KINDS] = getJobKinds()
                }
                if(isEditPage) postEditUserInfo(map) else postSignUp(map)
            }
        }
    }

    private fun getJobKinds(): String{
        var s = ""
        if(elecCheckBox.isChecked) s += elecCheckBox.text.toString()
        if(fireCheckBox.isChecked) s += fireCheckBox.text.toString()
        if(ironCheckBox.isChecked) s += ironCheckBox.text.toString()
        if(plasCheckBox.isChecked) s += plasCheckBox.text.toString()
        if(etcCheckBox.isChecked) s += etcCheckBox.text.toString()
        return s
    }

    /**
     * 정보 수정 요청
     */
    private fun postEditUserInfo(map: HashMap<String, Any>) {
        retrofitService.postEditUserInfo(map).enqueue(object : Callback<BaseResult>{
            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                val responseResult: BaseResult = response.body()!!
                when(responseResult.baseModel.result){
                    "Y" -> {
                        toast(getString(R.string.edit_complete))
                        setResult(Activity.RESULT_OK, Intent().apply{ putExtra(USER_MODEL_KEY, UserInfoResult.UserModel(
                            userId, userName, userEmail, userCpNum, getJobKinds()
                        )) })
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                error("실패")
            }
        })
    }

    /**
     * 회원 가입 요청
     */
    private fun postSignUp(map: HashMap<String, Any>) {
        map[FIELD_USER_DIV] = userType
        retrofitService.postSignUp(map).enqueue(object : Callback<BaseResult>{
            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                val responseResult: BaseResult = response.body()!!
                when(responseResult.baseModel.result){
                    "Y" -> {
                        isSignedUp = true
                        congTextView.visibility = View.VISIBLE
                        findButton.visibility = View.VISIBLE
                        scrollView.visibility = View.INVISIBLE
                        backButton.setOnClickListener { startLoginActivity() } }
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
                    val result = responseResult.baseModel.result
                    info(result)
                    when(result){
                        "N" -> CustomDialog(this@SignUpActivity, getString(R.string.id_is_exist)).showIdCheck()
                        else -> {
                            CustomDialog(this@SignUpActivity, idEditText.text.toString()).showIdCheck()
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

    override fun onBackPressed() {
        if(isSignedUp) startLoginActivity()
        else super.onBackPressed()
    }
}
