package com.dmon.rentalhere.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import com.dmon.rentalhere.R
import com.dmon.rentalhere.model.CustomDialog
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.*
import android.view.WindowManager
import android.widget.EditText
import com.dmon.rentalhere.BaseActivity
import com.dmon.rentalhere.constants.SignUpConstants
import com.dmon.rentalhere.model.UserInfoResult
import com.dmon.rentalhere.presenter.CLIENT_TYPE
import com.dmon.rentalhere.presenter.SignUpPresenter
import kotlinx.android.synthetic.main.activity_sign_up.backButton
import kotlinx.android.synthetic.main.activity_sign_up.elecCheckBox
import kotlinx.android.synthetic.main.activity_sign_up.scrollView
import kotlinx.android.synthetic.main.activity_sign_up.topTextView
import kotlinx.coroutines.runBlocking

const val SIGNUP_TAG = "SignUpActivity"
class SignUpActivity : BaseActivity(), View.OnClickListener, AnkoLogger, SignUpConstants.View {
    override val loggerTag: String get() = SIGNUP_TAG
    private var userType: Int = 0
    private lateinit var userIdx: String
    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userCpNum: String
    private lateinit var presenter: SignUpPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // edittext에 포커스 생겼을때 키보드에 가려지지 않게
        presenter = SignUpPresenter(this, this, retrofitService)
        presenter.processIntent(intent)
    }

    override fun setViewListener() {
        dupCheckButton.setOnClickListener(this)
        completeButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
        findButton.setOnClickListener(this)
        idEditText.addTextChangedListener(presenter.getIdWatcher())
        cpEditText.addTextChangedListener(presenter.getCpWatcher())
    }

    /**
     * cpEditText watcher의 afterTextChanged 이벤트가 일어날때 실행되는 메소드
     * 전화번호 최대 11글자로 제한
     * @param e editText에서 받은 값
     */
    override fun limitCpTextView(e: Editable?) {
        if(e.toString().length > 11) {
            cpEditText.setText(e.toString().dropLast(1))
            cpEditText.setSelection(11) // todo : 수정?
        }
    }

    override fun setTopTextView(s: String) {
        topTextView.text = getString(R.string.client_sign_up)
    }

    /**
     * 정보 수정일 때 뷰 설정
     */
    override fun setEditView(){
        val userModel = intent.getParcelableExtra<UserInfoResult.UserModel>(USER_MODEL_KEY)
        userModel?.let{
            userIdx = userModel.userIdx
            runOnUiThread {
                presenter.run {
                    setIsIdChecked(true)
                    setIsEditPage(true)
                }
                info("중복확인 : ${presenter.isIdChecked()}")
                topTextView.text = getString(R.string.edit_info)
                idEditText.run { isEnabled = false; background = null }
                dupCheckButton.visibility = View.INVISIBLE
                idEditText.setText(it.userId)
                nameEditText.setText(it.userName)
                cpEditText.setText(it.userCpNum.replace("-", ""))
                emailEditText.setText(it.userEmail)
                if (userType == CLIENT_TYPE) checkJobKinds(userModel)
            }
        }
    }

    private fun checkJobKinds(userModel: UserInfoResult.UserModel){
        runOnUiThread {
            userModel.userJobKinds.run {
                if (contains(getString(R.string.electric))) elecCheckBox.isChecked = true
                if (contains(getString(R.string.fireFighting))) fireCheckBox.isChecked = true
                if (contains(getString(R.string.iron))) ironCheckBox.isChecked = true
                if (contains(getString(R.string.plastere))) plasCheckBox.isChecked = true
                if (contains(getString(R.string.etc))) etcCheckBox.isChecked = true
            }
        }
    }

    /**
     * 업주일 때 뷰 설정
     */
    override fun setOwnerView(){
        runOnUiThread {
            topTextView.text = getString(R.string.owner_sign_up)
            jobKindsTextView.visibility = View.GONE
            elecCheckBox.visibility = View.GONE
            fireCheckBox.visibility = View.GONE
            ironCheckBox.visibility = View.GONE
            plasCheckBox.visibility = View.GONE
            etcCheckBox.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        presenter.onClick(v)
    }

    /**
     * 빈칸 체크
     */
    override fun isThereAnyBlanks(): Boolean {
        info("중복확인 : ${presenter.isIdChecked()}")
        return when{
            idEditText.text.isEmpty() -> { toast(getString(R.string.toast_type_id)); true }
            pwEditText.text.isEmpty() -> { toast(getString(R.string.toast_type_pw)); true }
            pwEditText2.text.isEmpty() -> { toast(getString(R.string.toast_type_pw2)); true }
            pwEditText.text.toString() != pwEditText2.text.toString() -> { toast(getString(R.string.toast_type_pw2)); true }
            nameEditText.text.isEmpty() -> { toast(getString(R.string.toast_type_name)); true }
            cpEditText.text.isEmpty() or (cpEditText.text.length < 11) -> { toast(getString(R.string.toast_type_cp)); true }
//            emailEditText.text.isEmpty() -> toast(getString(R.string.toast_type_email))
            !elecCheckBox.isChecked &&
                    !fireCheckBox.isChecked &&
                    !ironCheckBox.isChecked &&
                    !plasCheckBox.isChecked &&
                    !etcCheckBox.isChecked && presenter.getUserType() == CLIENT_TYPE -> { toast(getString(R.string.toast_check_jobs)); true }
            !presenter.isIdChecked() -> { toast(getString(R.string.toast_check_dup)); true }
            else -> false
        }
    }

    override fun getJobKinds(): String{
        var s = ""
        if(elecCheckBox.isChecked) s += "${elecCheckBox.text},"
        if(fireCheckBox.isChecked) s += "${fireCheckBox.text},"
        if(ironCheckBox.isChecked) s += "${ironCheckBox.text},"
        if(plasCheckBox.isChecked) s += "${plasCheckBox.text},"
        if(etcCheckBox.isChecked) s += "${etcCheckBox.text}"
        return if(s.endsWith(',')) s.dropLast(1) else s
    }

    override fun getEditable(id: Int): Editable = findViewById<EditText>(id).text

    override val showDialog: (String) -> Unit = { runOnUiThread { CustomDialog(this@SignUpActivity, it).showIdCheck() } }
    override val showToast: (String) -> Unit = { runOnUiThread { toast(it) } }

    override fun setViewWhenComplete(){
        runOnUiThread {
            congTextView.visibility = View.VISIBLE
            findButton.visibility = View.VISIBLE
            scrollView.visibility = View.INVISIBLE
            backButton.setOnClickListener { presenter.startLoginActivity() }
        }
    }

    override fun setResultWithValue(){
        userId = idEditText.text.toString()
        userName = nameEditText.text.toString()
        userEmail = emailEditText.text.toString()
        userCpNum = cpEditText.text.toString()
        setResult(Activity.RESULT_OK, Intent().apply{ putExtra(
            USER_MODEL_KEY, UserInfoResult.UserModel(
                userIdx, userId, userName, userEmail, userCpNum, getJobKinds()
            )) })
        finish()
    }

    override fun finishActivity() = finish()

    override fun onBackPressed() {
        presenter.onBackPressed()
    }

//    private val idWatcher = object : TextWatcher{
//        override fun afterTextChanged(s: Editable?) { isIdChecked = false }
//        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//    }
//    private val cpWatcher = object : TextWatcher{
//        override fun afterTextChanged(s: Editable?) {
//            if(s.toString().length > 11) {
//                cpEditText.setText(s.toString().dropLast(1))
//                cpEditText.setSelection(11) // todo : 수정?
//            }
//        }
//        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//    }

//    private fun processIntent() {
//        // 일반 / 업주 구분
//        userType = intent.getIntExtra(TYPE_KEY, 0)
//        when(userType){
//            CLIENT_TYPE -> topTextView.text = getString(R.string.client_sign_up)
//            OWNER_TYPE -> setOwnerView()
//        }
//
//        setEditView()
//    }
    /**
     * 정보 수정 요청
     */
//    private fun postEditUserInfo(map: HashMap<String, Any>) {
//        retrofitService.postEditUserInfo(map).enqueue(object : Callback<BaseResult>{
//            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
//                val responseResult: BaseResult = response.body()!!
//                when(responseResult.baseModel.result){
//                    "Y" -> {
//                        toast(getString(R.string.edit_complete))
//                        setResult(Activity.RESULT_OK, Intent().apply{ putExtra(USER_MODEL_KEY, UserInfoResult.UserModel(
//                            userId, userName, userEmail, userCpNum, getJobKinds()
//                        )) })
//                        finish()
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<BaseResult>, t: Throwable) {
//                error("실패")
//            }
//        })
//    }

    /**
     * 회원 가입 요청
     */
//    private fun postSignUp(map: HashMap<String, Any>) {
//        map[FIELD_USER_DIV] = userType
//        retrofitService.postSignUp(map).enqueue(object : Callback<BaseResult>{
//            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
//                val responseResult: BaseResult = response.body()!!
//                when(responseResult.baseModel.result){
//                    "Y" -> {
//                        isSignedUp = true
//                        congTextView.visibility = View.VISIBLE
//                        findButton.visibility = View.VISIBLE
//                        scrollView.visibility = View.INVISIBLE
//                        backButton.setOnClickListener { startLoginActivity() } }
//                }
//            }
//
//            override fun onFailure(call: Call<BaseResult>, t: Throwable) {
//                error("실패")
//            }
//        })
//    }

    /**
     * 아이디 중복 체크 요철
     */
//    private fun checkId() {
//        info("checkId called")
//        val idBody = getRequestBody(idEditText.text.toString())
//        if(idEditText.text.isNotEmpty()) {
//            retrofitService.postCheckIdDup(idBody).enqueue(object : Callback<BaseResult> {
//                override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
//                    val responseResult: BaseResult = response.body()!!
//                    val result = responseResult.baseModel.result
//                    info(result)
//                    when(result){
//                        "N" -> CustomDialog(this@SignUpActivity, getString(R.string.id_is_exist)).showIdCheck()
//                        else -> {
//                            CustomDialog(this@SignUpActivity, idEditText.text.toString()).showIdCheck()
//                            isIdChecked = true
//                        }
//                    }
//                }
//
//                override fun onFailure(call: Call<BaseResult>, t: Throwable) {
//                    error("실패")
//                }
//            })
//        }else toast(getString(R.string.toast_type_id))
//    }
//    private val idWatcher = object : TextWatcher{
//        override fun afterTextChanged(s: Editable?) { isIdChecked = false }
//        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//    }
//    private val cpWatcher = object : TextWatcher{
//        override fun afterTextChanged(s: Editable?) {
//            if(s.toString().length > 11) {
//                cpEditText.setText(s.toString().dropLast(1))
//                cpEditText.setSelection(11) // todo : 수정?
//            }
//        }
//        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//    }

//    private fun processIntent() {
//        // 일반 / 업주 구분
//        userType = intent.getIntExtra(TYPE_KEY, 0)
//        when(userType){
//            CLIENT_TYPE -> topTextView.text = getString(R.string.client_sign_up)
//            OWNER_TYPE -> setOwnerView()
//        }
//
//        setEditView()
//    }
}
