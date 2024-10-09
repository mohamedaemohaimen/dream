package com.tt.dramatime.ui.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/21
 *    desc   : 项目通用 Dialog 布局封装
 */
class CommonDialog {

    @Suppress("UNCHECKED_CAST", "LeakingThis")
    open class Builder<B : Builder<B>>(context: Context) : BaseDialog.Builder<B>(context) {

        private var autoDismiss = true

        private val containerLayout: ViewGroup? by lazy { findViewById(R.id.ll_ui_container) }
        private val titleView: TextView? by lazy { findViewById(R.id.tv_ui_title) }
        private val cancelView: TextView? by lazy { findViewById(R.id.tv_ui_cancel) }
        private val lineView: View? by lazy { findViewById(R.id.v_ui_line) }
        private val confirmView: TextView? by lazy { findViewById(R.id.tv_ui_confirm) }

        init {
            setContentView(R.layout.ui_dialog)
            setAnimStyle(AnimAction.ANIM_BOTTOM)
            setGravity(Gravity.CENTER)
            setOnClickListener(cancelView, confirmView)
        }

        fun setCustomView(@LayoutRes id: Int): B {
            return setCustomView(LayoutInflater.from(getContext()).inflate(id, containerLayout, false))
        }

        fun setCustomView(view: View?): B {
            containerLayout?.addView(view, 1)
            return this as B
        }

        fun setTitle(@StringRes id: Int): B {
            return setTitle(getString(id))
        }

        fun setTitleImage(@DrawableRes id: Int): B {
            titleView?.visibility = View.VISIBLE
            titleView?.setBackgroundResource(id)
            return this as B
        }

        fun setTitle(text: CharSequence?): B {
            titleView?.visibility = View.VISIBLE
            titleView?.text = text
            return this as B
        }

        fun setCancel(@StringRes id: Int): B {
            return setCancel(getString(id))
        }

        fun setCancel(text: CharSequence?): B {
            cancelView?.text = text
            cancelView?.visibility = if (text == null || "" == text.toString()) View.GONE else View.VISIBLE
            lineView?.visibility = if (text == null || "" == text.toString()) View.GONE else View.VISIBLE
            return this as B
        }

        fun setConfirm(@StringRes id: Int): B {
            return setConfirm(getString(id))
        }

        fun setConfirm(text: CharSequence?): B {
            confirmView?.text = text
            return this as B
        }

        fun setCancelColour(): B {
            cancelView?.setBackgroundResource(R.drawable.button_middle_38_bg)
            cancelView?.setTextColor(ContextCompat.getColor(getContext(),R.color.white))
            confirmView?.setBackgroundResource(R.drawable.dialog_normal_btn_bg)
            confirmView?.setTextColor(ContextCompat.getColor(getContext(),R.color.color_C640FF))
            return this as B
        }

        fun setAutoDismiss(dismiss: Boolean): B {
            autoDismiss = dismiss
            return this as B
        }

        fun autoDismiss() {
            if (autoDismiss) {
                dismiss()
            }
        }
    }
}