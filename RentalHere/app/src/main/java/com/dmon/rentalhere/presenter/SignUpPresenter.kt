package com.dmon.rentalhere.presenter

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.dmon.rentalhere.R
import com.dmon.rentalhere.constants.SignUpConstants
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.SignUpModel
import com.dmon.rentalhere.retrofit.*
import com.dmon.rentalhere.view.LoginActivity
import com.dmon.rentalhere.view.SIGNUP_TAG
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpPresenter(private val signUpView: SignUpConstants.View, private val context: Context, private val retrofitService: RetrofitService): SignUpConstants.Presenter, View.OnClickListener{
    private lateinit var signUpModel: SignUpModel
    private val idWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { signUpModel.isIdChecked = false }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
    private var cpWatcher: TextWatcher = object : TextWatcher{
        override fun afterTextChanged(s: Editable?) {
            signUpView.limitCpTextView(s)
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
    val getRequestBody: (String) -> RequestBody = { RequestBody.create(MediaType.parse("text/plain"), it) }

    fun processIntent(intent: Intent) {
//        GlobalScope.launch {
            // 일반 / 업주 구분
            signUpModel = SignUpModel(
                intent.getIntExtra(TYPE_KEY, 0),
                idWatcher, cpWatcher, isIdChecked = false, isSignedUp = false, isEditPage = false
            )
            when (signUpModel.userType) {
                CLIENT_TYPE -> signUpView.setTopTextView(context.getString(R.string.client_sign_up))
                OWNER_TYPE -> signUpView.setOwnerView()
            }
            signUpView.run {
                setEditView()
                setViewListener()
            }
//        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.dupCheckButton -> checkId()
            R.id.completeButton -> if(!signUpView.isThereAnyBlanks()) divideFun()
            R.id.findButton -> startLoginActivity()
            R.id.backButton -> signUpView.finishActivity()
        }
    }

    private fun divideFun() {
        GlobalScope.launch {
            val map = HashMap<String, Any>().apply {
                this[FIELD_USER_ID] = signUpView.getEditable(R.id.idEditText)
                this[FIELD_USER_PW] = signUpView.getEditable(R.id.pwEditText)
                this[FIELD_USER_NAME] = signUpView.getEditable(R.id.nameEditText)
                this[FIELD_USER_EMAIL] = signUpView.getEditable(R.id.emailEditText)
                this[FIELD_USER_CP_NUM] = signUpView.getEditable(R.id.cpEditText)
                this[FIELD_USER_JOB_KINDS] = signUpView.getJobKinds()
            }
            if (signUpModel.isEditPage) postEditUserInfo(map) else postSignUp(map)
        }
    }

    private fun postSignUp(map: HashMap<String, Any>){
        map[FIELD_USER_DIV] = signUpModel.userType
        val signUpCallback = object : Callback<BaseResult>{
            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                val responseResult: BaseResult = response.body()!!
                when(responseResult.baseModel.result){
                    "Y" -> {
                        signUpModel.isSignedUp = true
                        signUpView.setViewWhenComplete()
                    }
                }
            }

            override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                error("실패")
            }
        }
        retrofitService.postSignUp(map).enqueue(signUpCallback)
    }

    private fun postEditUserInfo(map: HashMap<String, Any>){
        val editUserCallback = object : Callback<BaseResult>{
            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                val responseResult: BaseResult = response.body()!!
                when(responseResult.baseModel.result){
                    "Y" -> signUpView.run{
                        showToast(context.getString(R.string.edit_complete))
                        setResultWithValue()
                    }
                }
            }

            override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                Log.e(SIGNUP_TAG,"실패")
            }
        }
        retrofitService.postEditUserInfo(map).enqueue(editUserCallback)
    }

    fun startLoginActivity() =
        context.startActivity(Intent(context, LoginActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })

    /**
     * 아이디 중복 체크 요철
     */
    private fun checkId() {
        GlobalScope.launch {
            val editable = signUpView.getEditable(R.id.idEditText)
            val idBody = getRequestBody(editable.toString())
            val checkIdCallback = object : Callback<BaseResult> {
                override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                    val responseResult: BaseResult = response.body()!!
                    val result = responseResult.baseModel.result
//                    info(result)
                    when (result) {
                        "N" -> context.getString(R.string.id_is_exist)
                        else -> {
                            signUpView.showDialog(editable.toString())
                            signUpModel.isIdChecked = true
                        }
                    }
                }

                override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                    Log.e(SIGNUP_TAG, "실패")
                }
            }
            if (editable.isNotEmpty()) retrofitService.postCheckIdDup(idBody).enqueue(
                checkIdCallback
            )
            else signUpView.showToast(context.getString(R.string.toast_type_id))
        }
    }

    override val setIsEditPage: (Boolean) -> Unit = { signUpModel.isEditPage = it }
    override val setIsIdChecked: (Boolean) -> Unit = { signUpModel.isIdChecked = it }
    override val setIsSignedUp: (Boolean) -> Unit = { signUpModel.isSignedUp = it }
    override val isIdChecked: () -> Boolean = { signUpModel.isIdChecked }
    override val getCpWatcher: () -> TextWatcher = { signUpModel.cpWatcher }
    override val getIdWatcher: () -> TextWatcher = { signUpModel.idWatcher }
    override val getUserType: () -> Int = { signUpModel.userType }

    fun onBackPressed(){
        if(signUpModel.isSignedUp) startLoginActivity()
        else signUpView.finishActivity()
    }
}