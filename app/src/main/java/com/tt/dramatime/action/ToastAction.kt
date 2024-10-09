package com.tt.dramatime.action

import androidx.annotation.StringRes
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.hjq.toast.style.CustomToastStyle
import com.tt.dramatime.R

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject-Kotlin
 * time   : 2019/12/08
 * desc   : 吐司意图
 */
interface ToastAction {

    fun toast(text: CharSequence?) {
        Toaster.show(text)
    }

    fun toast(@StringRes id: Int) {
        Toaster.show(id)
    }

    fun toast(`object`: Any?) {
        Toaster.show(`object`)
    }

    fun toastError(text: CharSequence?) {
        val params = ToastParams()
        params.text = text
        params.style = CustomToastStyle(R.layout.toast_error)
        Toaster.show(params)
    }

    fun toastSuccess(text: CharSequence?) {
        val params = ToastParams()
        params.text = text
        params.style = CustomToastStyle(R.layout.toast_success)
        Toaster.show(params)
    }

    fun toastHint(text: CharSequence?) {
        val params = ToastParams()
        params.text = text
        params.style = CustomToastStyle(R.layout.toast_hint)
        Toaster.show(params)
    }

    fun toastBonus(text: CharSequence?) {
        val params = ToastParams()
        params.text = text
        params.style = CustomToastStyle(R.layout.toast_bonus)
        Toaster.show(params)
    }
}