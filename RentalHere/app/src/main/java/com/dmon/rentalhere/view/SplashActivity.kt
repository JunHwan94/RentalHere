package com.dmon.rentalhere.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dmon.rentalhere.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        textView.apply{ text = "여기렌탈 스플래시 액티비티!"}
        GlobalScope.launch{
            delay(2500)
            startActivity(Intent(applicationContext, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NO_ANIMATION })
            finish()
        }
    }
}
