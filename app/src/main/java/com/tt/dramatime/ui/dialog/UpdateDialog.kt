package com.tt.dramatime.ui.dialog

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R


/**
 *  author : Android 轮子哥
 *  github : https://github.com/getActivity/AndroidProject-Kotlin
 *  time   : 2019/03/20
 *  desc   : 升级对话框
 */
class UpdateDialog {

    class Builder(context: Context) : BaseDialog.Builder<Builder>(context) {

        private val detailsView: TextView? by lazy { findViewById(R.id.tv_update_details) }
        private val updateView: TextView? by lazy { findViewById(R.id.tv_update_update) }
        private val laterView: TextView? by lazy { findViewById(R.id.tv_update_close) }
        private val closeView: ImageButton? by lazy { findViewById(R.id.btn_update_close) }

        /** 是否强制更新 */
        private var forceUpdate = false

        /** Google Play 商店的链接 */
        private var playStoreUrl: String? = ""

        init {
            setContentView(R.layout.update_dialog)
            setAnimStyle(AnimAction.ANIM_BOTTOM)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setOnClickListener(updateView, closeView, laterView)

            // 让 TextView 支持滚动
            detailsView?.movementMethod = ScrollingMovementMethod()
            laterView?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        }

        /**
         * 设置更新日志
         */
        fun setUpdateLog(text: CharSequence?): Builder = apply {
            detailsView?.text = text
            detailsView?.visibility = if (text == null) View.GONE else View.VISIBLE
        }

        /**
         * 设置Google Play 商店的链接
         */
        fun setPlayStoreUrl(url: String?): Builder = apply {
            playStoreUrl = url
        }

        /**
         * 设置强制更新
         */
        fun setForceUpdate(force: Boolean): Builder = apply {
            forceUpdate = force
            closeView?.visibility = if (force) View.GONE else View.VISIBLE
            laterView?.visibility = if (force) View.GONE else View.VISIBLE
            setCancelable(!force)
        }

        @SingleClick
        override fun onClick(view: View) {
            if (view === closeView || view === laterView) {
                dismiss()
            } else if (view === updateView) {
                // 创建 Intent 打开 Google Play 商店链接
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }


}