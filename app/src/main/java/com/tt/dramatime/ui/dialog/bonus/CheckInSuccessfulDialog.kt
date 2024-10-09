package com.tt.dramatime.ui.dialog.bonus

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.blankj.utilcode.util.NotificationUtils
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R
import com.tt.dramatime.databinding.CheckInSuccessfulDialogBinding
import com.tt.dramatime.http.api.TaskListApi
import com.tt.dramatime.ui.dialog.MessageDialog.Builder
import com.tt.dramatime.ui.dialog.MessageDialog.OnListener

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc :签到成功弹窗
 * </pre>
 */
class CheckInSuccessfulDialog {

    class Builder(context: Context, bonus: Int, mMovieAdBean: TaskListApi.Bean?) :
        BaseDialog.Builder<Builder>(context) {

        private val binding = CheckInSuccessfulDialogBinding.inflate(LayoutInflater.from(context))
        private var listener: OnListener? = null

        fun setListener(listener: OnListener?): Builder = apply {
            this.listener = listener
        }

        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_IOS)
            setGravity(Gravity.CENTER)
            setWidth(getResources().getDimension(R.dimen.dp_283).toInt())


            var mAdMovieCompletionStatus = true
            var mMovieAdTaskBonus=0

            mMovieAdBean?.list?.forEachIndexed { index, listBean ->
                if (listBean.status == 0 && mAdMovieCompletionStatus) {
                    mMovieAdTaskBonus = listBean.integral
                    mAdMovieCompletionStatus = false
                    return@forEachIndexed
                }
            }

            if (mAdMovieCompletionStatus.not()){
                binding.checkInTv.text = getString(R.string.extra_bonus, mMovieAdTaskBonus)
                binding.reminderIv.setBackgroundResource(R.drawable.bonus_ad_ic)
                binding.reminderIv.visibility = View.VISIBLE
            }else if (NotificationUtils.areNotificationsEnabled().not()) {
                binding.checkInTv.text = getString(R.string.remind_me_next_time)
                binding.reminderIv.setBackgroundResource(R.drawable.bonus_reminder_ic)
                binding.reminderIv.visibility = View.VISIBLE
            }

            binding.addBonus.text = getString(R.string.add_coins, bonus)

            setOnClickListener(binding.checkInBtn, binding.closeIv)
        }

        @SingleClick(1000)
        override fun onClick(view: View) {
            when (view) {
                binding.closeIv -> dismiss()
                binding.checkInBtn -> {
                    listener?.onConfirm(getDialog())
                    dismiss()
                }
            }
        }

    }

    interface OnListener {
        /**
         * 点击确定时回调
         */
        fun onConfirm(dialog: BaseDialog?)
    }
}