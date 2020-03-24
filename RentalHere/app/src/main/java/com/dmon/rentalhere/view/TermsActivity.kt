package com.dmon.rentalhere.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.dmon.rentalhere.R
import com.dmon.rentalhere.presenter.TYPE_KEY
import kotlinx.android.synthetic.main.activity_terms.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.toast

class TermsActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener, View.OnClickListener{
    private var userType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)
        processIntent()
        setViewListener()
    }

    private fun setViewListener() {
        agreeAllCheckBox.setOnCheckedChangeListener(this)
        agreePolicyCheckBox.setOnCheckedChangeListener(this)
        agreeTermsCheckBox.setOnCheckedChangeListener(this)
        signUpButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
    }

    private fun processIntent() {
        userType = intent.getIntExtra(TYPE_KEY, 0)
    }

    override fun onCheckedChanged(v: CompoundButton, isChecked: Boolean) {
        when(v){
            agreeAllCheckBox -> {
                agreePolicyCheckBox.isChecked = isChecked || agreePolicyCheckBox.isChecked
                agreeTermsCheckBox.isChecked = isChecked || agreeTermsCheckBox.isChecked
            }
            else -> {
                agreeAllCheckBox.isChecked = agreeTermsCheckBox.isChecked && agreePolicyCheckBox.isChecked
            }
        }
    }

    override fun onClick(v: View?) {
        when(v){
            signUpButton -> when {
                agreePolicyCheckBox.isChecked && agreeTermsCheckBox.isChecked ->
                    startActivity(Intent(this, SignUpActivity::class.java).apply {
                        putExtra(TYPE_KEY, userType)
                    })
                else -> toast(getString(R.string.toast_agree))
            }
            backButton -> finish()
        }
    }
}
