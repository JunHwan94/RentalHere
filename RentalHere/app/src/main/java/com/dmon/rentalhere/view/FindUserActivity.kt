package com.dmon.rentalhere.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dmon.rentalhere.R
import com.dmon.rentalhere.presenter.PW_TYPE
import com.dmon.rentalhere.presenter.TYPE_KEY
import kotlinx.android.synthetic.main.activity_find_user.*

class FindUserActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_user)
        setViewListener()
        processIntent()
    }

    private fun processIntent() {
        when(intent.getIntExtra(TYPE_KEY, 0)){
            PW_TYPE -> {
                topTextView.text = getString(R.string.find_pw)
                findButton.text = getString(R.string.find_pw)
            }
        }
    }

    private fun setViewListener() {
        backButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            backButton -> finish()
        }
    }
}
