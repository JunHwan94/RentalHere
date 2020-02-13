package com.dmon.rentalhere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_login.*  /** Kotlin Extensions : 바인딩이 자동으로 되어 뭔가 할 필요가 없음 */
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

fun LoginActivity.a(){
    topTextView.text = "TopTextView"
    topTextView.visibility = View.INVISIBLE
}
class LoginActivity : AppCompatActivity(), View.OnClickListener, AnkoLogger {
    override val loggerTag: String
        get() = "LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        startLoginDivideFadeInAnim()
        setButtons()
        GlobalScope.launch{
            var i = 0
            while(true){
                delay(1000L)
                debug("$i 초")
                i++
            }
        }
    }

    private fun setButtons() {
        clientLoginButton.apply {
            text = "일반 로그인"
            setOnClickListener{ v -> onClick(v) }
        }
        ownerLoginButton.apply{
            text = "업주 로그인"
            setOnClickListener{ v -> onClick(v) }
        }
    }

    private val View.fadeIn: () -> Unit get() = { startAnimation(AnimationUtils.loadAnimation(baseContext, R.anim.fade_in)) }

    private val View.fadeOut: (Animation.AnimationListener?) -> Unit get() = {
        val anim = AnimationUtils.loadAnimation(baseContext, R.anim.fade_out)
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

    private fun showLoginDivide() {
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
    private fun startLoginDivideFadeInAnim() {
        topTextView.fadeIn()
        clientLoginButton.fadeIn()
        ownerLoginButton.fadeIn()
    }

    /**
     * 일반, 업주 로그인 뷰 fade out 애니메이션
     **/
    private fun startLoginDivideFadeOutAnim() {
        loginDivideLayout.fadeOut(showLoginLayoutListener)
    }

    /**
     * 로그인 뷰 (입력 등) fade out 애니메이션
     **/
    private fun startLoginLayoutFadeOutAnim() {
        topTextView.fadeOut(null)
        loginLayout.fadeOut(showLoginDivideListener)
    }

    /**
     * 일반, 업주 로그인 뷰 없애고
     * 로그인 뷰 (입력 등) 보이게
     **/
    private fun showLoginLayout(){
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

    override fun onClick(v: View) {
        when(v.id){
            R.id.clientLoginButton, R.id.ownerLoginButton -> startLoginDivideFadeOutAnim()
        }
    }

    override fun onBackPressed() {
        if(loginLayout.visibility == View.INVISIBLE)
            super.onBackPressed()
        else {
            startLoginLayoutFadeOutAnim()
        }
    }
}
