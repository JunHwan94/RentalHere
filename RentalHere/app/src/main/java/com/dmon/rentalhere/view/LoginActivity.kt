package com.dmon.rentalhere.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import com.dmon.rentalhere.constants.LoginConstants
import com.dmon.rentalhere.presenter.LoginPresenter
import com.dmon.rentalhere.R
import kotlinx.android.synthetic.main.activity_login.*
/** Kotlin Extensions : 바인딩이 자동으로 되어 뭔가 할 필요가 없음 */
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity(), View.OnClickListener, AnkoLogger, LoginConstants.View {
    override val loggerTag: String get() = "LoginActivity"
    private lateinit var loginPresenter: LoginPresenter
    override val userId: () -> String = { idEditText.text.toString() }
    override val userPw: () -> String = { pwEditText.text.toString() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        debug("onCreate called")
        loginPresenter = LoginPresenter(this, this)
    }

    override fun setButtons() {
        clientLoginButton.setOnClickListener(this)
        ownerLoginButton.setOnClickListener(this)
        signUpButton.setOnClickListener(this)
        loginButton.setOnClickListener(this)
    }

    override val View.fadeIn: () -> Unit get() = { startAnimation(AnimationUtils.loadAnimation(baseContext, R.anim.fade_in)) }

    override val View.fadeOut: (Animation.AnimationListener?) -> Unit get() = {
        val anim = AnimationUtils.loadAnimation(baseContext,
            R.anim.fade_out
        )
        if(it != null) anim.setAnimationListener(it)
        startAnimation(anim)
    }

    private val showLoginLayoutListener = object: Animation.AnimationListener{
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            showLoginLayout()
        }

        override fun onAnimationStart(animation: Animation?) {
        }
    }

    private val showLoginDivideListener = object: Animation.AnimationListener{
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            showLoginDivide()
        }

        override fun onAnimationStart(animation: Animation?) {
        }
    }

    /**
     * 로그인 뷰 (입력 등) 없애고
     * 일반, 업주 로그인 뷰 보이게
     **/
    override fun showLoginDivide() {
        loginLayout.visibility = View.INVISIBLE
        loginDivideLayout.apply{
            background = getDrawable(R.drawable.splash)
            fadeIn()
        }
        topTextView.apply{
            fadeIn()
            setTextColor(getColor(R.color.white))
            val params = (layoutParams as ConstraintLayout.LayoutParams)
            params.bottomMargin = 0
            layoutParams = params
        }
        startLoginDivideFadeInAnim()
        clientLoginButton.visibility = View.VISIBLE
        ownerLoginButton.visibility = View.VISIBLE
    }

    /**
     * 일반, 업주 로그인 뷰 fade in 애니메이션
     **/
    override fun startLoginDivideFadeInAnim() {
        topTextView.fadeIn()
        clientLoginButton.fadeIn()
        ownerLoginButton.fadeIn()
    }

    /**
     * 일반, 업주 로그인 뷰 fade out 애니메이션
     **/
    override fun startLoginDivideFadeOutAnim() {
        loginDivideLayout.fadeOut(showLoginLayoutListener)
    }

    /**
     * 로그인 뷰 (입력 등) fade out 애니메이션
     **/
    override fun startLoginLayoutFadeOutAnim() {
        topTextView.fadeOut(null)
        loginLayout.fadeOut(showLoginDivideListener)
    }

    /**
     * 일반, 업주 로그인 뷰 없애고
     * 로그인 뷰 (입력 등) 보이게
     **/
    override fun showLoginLayout(){
        loginDivideLayout.background = null
        clientLoginButton.visibility = View.INVISIBLE
        ownerLoginButton.visibility = View.INVISIBLE
        topTextView.apply{
            fadeIn()
            setTextColor(getColor(R.color.colorPrimary))
            val params = (layoutParams as ConstraintLayout.LayoutParams)
            params.bottomMargin = 700
            layoutParams = params
        }
        loginLayout.apply { fadeIn(); visibility = View.VISIBLE }
    }

    override fun checkBlank(): Boolean{
        when{
            idEditText.text.isEmpty() -> toast(getString(R.string.toast_type_id))
            pwEditText.text.isEmpty() -> toast(getString(R.string.toast_type_pw))
            else -> return true
        }
        return false
    }

    override fun onClick(v: View?) {
        loginPresenter.onClick(v!!)
    }

    override fun onBackPressed() {
        if(loginLayout.visibility == View.INVISIBLE)
            super.onBackPressed()
        else {
            loginPresenter.onBackPressed()
        }
    }
}