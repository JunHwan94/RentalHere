package com.dmon.rentalhere.model

import android.text.TextWatcher

data class SignUpModel(val userType: Int,
                       val idWatcher: TextWatcher,
                       val cpWatcher: TextWatcher,
                       var isIdChecked: Boolean,
                       var isSignedUp: Boolean,
                       var isEditPage: Boolean)