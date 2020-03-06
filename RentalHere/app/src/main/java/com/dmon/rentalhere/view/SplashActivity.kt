package com.dmon.rentalhere.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dmon.rentalhere.R
import com.dmon.rentalhere.presenter.CLIENT_TYPE
import com.dmon.rentalhere.presenter.ID_KEY
import com.dmon.rentalhere.presenter.PREF_KEY
import com.dmon.rentalhere.presenter.TYPE_KEY
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val editor = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        val id = editor.getString(ID_KEY, "")!!
        val userType = editor.getInt(TYPE_KEY, 0)

        GlobalScope.launch{
            delay(2500)
            // 자동로그인 체크
            when {
                id == "" -> startActivity(Intent(applicationContext, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                userType == CLIENT_TYPE -> startActivity(Intent(applicationContext, ClientMainActivity::class.java).apply {
                    putExtra(ID_KEY, id)
                    flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                else -> startActivity(Intent(applicationContext, OwnerMainActivity::class.java).apply {
                    putExtra(ID_KEY, id)
                    flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
            }
            finish()
        }
    }
}
