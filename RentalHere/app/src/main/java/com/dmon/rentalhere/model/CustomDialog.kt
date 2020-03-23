package com.dmon.rentalhere.model

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.dmon.rentalhere.R
import kotlinx.android.synthetic.main.id_check_dialog.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.runOnUiThread

class CustomDialog(context: Context, private val messageOrId: String) {
    private val dialog: Dialog = Dialog(context)

    init{
        dialog.run{

        }
    }

    // 아이디 중복 체크 Dialog
    fun showIdCheck(){
        dialog.run {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.id_check_dialog)

            // layout_width 적용안돼서 직접 제어
            val params = dialog.window!!.attributes
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT

            when(messageOrId){
                // 아이디 사용 불가할 떄
                context.getString(R.string.id_is_exist) -> {
                    idIsTextView.text = messageOrId
                    bottomButton.text = context.getString(R.string.confirm)
                }
                // 아이디 사용 가능할 때
                else -> {
                    val message = context.getString(R.string.u_typed) + " ${messageOrId}는"
                    GlobalScope.launch{
                        val ssb = SpannableStringBuilder(message).apply{
                            setSpan(ForegroundColorSpan(Color.parseColor("#0C70FC")), 5, message.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        context.runOnUiThread {
                            idIsTextView.text = ssb
                            bottomTextView.visibility = View.VISIBLE
                        }
                    }
                }
            }
            bottomButton.setOnClickListener{ dismiss() }
            closeButton.setOnClickListener{ dismiss() }
            show()
        }
    }

}