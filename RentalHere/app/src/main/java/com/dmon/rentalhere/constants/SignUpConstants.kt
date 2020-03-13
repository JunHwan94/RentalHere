package com.dmon.rentalhere.constants

import android.text.Editable
import android.text.TextWatcher

interface SignUpConstants {
    interface View{
        fun setTopTextView(s: String)
        fun limitCpTextView(e: Editable?)
        fun setOwnerView()
        fun setEditView()
        fun getEditable(id: Int): Editable
        val showDialog: (String) -> Unit
        val showToast: (String) -> Unit
        fun isThereAnyBlanks(): Boolean
        fun getJobKinds(): String
        fun setViewWhenComplete()
        fun setResultWithValue()
        fun finishActivity()
        fun setViewListener()
    }
    interface Presenter{
        val setIsEditPage: (Boolean) -> Unit
        val setIsIdChecked: (Boolean) -> Unit
        val setIsSignedUp: (Boolean) -> Unit
        val isIdChecked: () -> Boolean
        val getIdWatcher: () -> TextWatcher
        val getCpWatcher: () -> TextWatcher
        val getUserType: () -> Int
    }
}