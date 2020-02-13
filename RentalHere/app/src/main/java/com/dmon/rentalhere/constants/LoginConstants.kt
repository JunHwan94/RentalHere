package com.dmon.rentalhere.constants

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.dmon.rentalhere.R

/**

 * Created by jjun on 2020-02-13.

 */
interface LoginConstants {
    interface View{
        fun setButtons()
        fun startLoginDivideFadeInAnim()
        fun startLoginDivideFadeOutAnim()
        fun startLoginLayoutFadeOutAnim()
        fun showLoginLayout()
        fun showLoginDivide()
        val android.view.View.fadeIn: () -> Unit
        val android.view.View.fadeOut: (Animation.AnimationListener) -> Unit
    }

    interface Presenter{
        fun onClick(v: android.view.View)
        fun onBackPressed()
    }
}