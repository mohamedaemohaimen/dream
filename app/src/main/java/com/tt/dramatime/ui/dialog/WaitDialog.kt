package com.tt.dramatime.ui.dialog

import android.content.Context
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/12/2
 *    desc   : 等待加载对话框
 */
class WaitDialog {

    class Builder(context: Context, private val automaticShutdown: Boolean = false) :
        BaseDialog.Builder<Builder>(context), BaseDialog.OnShowListener,
        BaseDialog.OnDismissListener {

        private val messageView: TextView? by lazy { findViewById(R.id.tv_wait_message) }
        private var countDownTimer: CountDownTimer? = null

        init {
            setContentView(R.layout.wait_dialog)
            setAnimStyle(AnimAction.ANIM_TOAST)
            setBackgroundDimEnabled(false)
            setCancelable(false)
            addOnShowListener(this)
            addOnDismissListener(this)
        }

        override fun onShow(dialog: BaseDialog?) {
            if (automaticShutdown) {
                // 创建一个倒计时任务，总时长为 20 秒，每隔 1 秒回调一次 onTick() 方法
                countDownTimer = object : CountDownTimer(20 * 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        if (isShowing()) dismiss()
                    }
                }

                // 启动倒计时任务
                countDownTimer?.start()
            }
        }

        override fun onDismiss(dialog: BaseDialog?) {
            countDownTimer?.cancel()
        }

        fun setMessage(@StringRes id: Int): Builder = apply {
            setMessage(getString(id))
        }

        fun setMessage(text: CharSequence?): Builder = apply {
            messageView?.text = text
            messageView?.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

    }
}