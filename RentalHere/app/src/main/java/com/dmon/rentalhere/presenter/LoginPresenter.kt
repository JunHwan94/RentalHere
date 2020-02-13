package com.dmon.rentalhere.presenter

import android.view.View
import com.dmon.rentalhere.R
import com.dmon.rentalhere.constants.LoginConstants
import com.dmon.rentalhere.model.LoginModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

/**

 * Created by jjun on 2020-02-13.

 */
class LoginPresenter(private val loginView: LoginConstants.View): LoginConstants.Presenter, AnkoLogger {
    override val loggerTag: String get() = "LoginPresenter"
    private val loginModel: LoginModel = LoginModel()

    init {
        loginView.setButtons()
        loginView.startLoginDivideFadeInAnim()
        GlobalScope.launch{
            var i = 0
            while(true){
                delay(1000L)
                debug("$i 초")
                i++
            }
        }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.clientLoginButton, R.id.ownerLoginButton -> loginView.startLoginDivideFadeOutAnim()
        }
    }

    override fun onBackPressed() {
        loginView.startLoginLayoutFadeOutAnim()
    }
}