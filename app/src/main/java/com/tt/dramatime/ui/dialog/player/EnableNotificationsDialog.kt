package com.tt.dramatime.ui.dialog.player

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.NotificationUtils
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R
import com.tt.dramatime.databinding.EnableNotificationsDialogBinding
import com.tt.dramatime.util.StartNotificationUtils


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc :打开通知弹窗
 * </pre>
 */
class EnableNotificationsDialog {

    class Builder(context: Context, private val lifecycleOwner: LifecycleOwner) :
        BaseDialog.Builder<Builder>(context), DefaultLifecycleObserver,
        BaseDialog.OnDismissListener {

        val binding = EnableNotificationsDialogBinding.inflate(LayoutInflater.from(context))

        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_IOS)
            setGravity(Gravity.CENTER)
            setYOffset(-getResources().getDimension(R.dimen.dp_77).toInt())
            setWidth(getResources().getDimension(R.dimen.dp_283).toInt())
            addOnDismissListener(this)
            lifecycleOwner.lifecycle.addObserver(this)

            val spannableString = SpannableString(getString(R.string.receive_timely_all))

            val receiveTimelyLength = getString(R.string.receive_timely)?.length ?: 0
            val checkInBonusLength = getString(R.string.check_in_bonus)?.length?.plus(receiveTimelyLength)?.plus(1) ?: 1

            // 获取图片资源
            val coins = ContextCompat.getDrawable(context, R.drawable.coins_54_ic)
            coins?.setBounds(0, 0, coins.intrinsicWidth, coins.intrinsicHeight)
            val movie = ContextCompat.getDrawable(context, R.drawable.enable_notification_movie_ic)
            movie?.setBounds(0, 0, movie.intrinsicWidth, movie.intrinsicHeight)

            // 创建 ImageSpan 并设置图片
            val coinsImageSpan = coins?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
            val movieImageSpan = movie?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
            // 将 ImageSpan 应用到指定位置
            spannableString.setSpan(
                coinsImageSpan,
                receiveTimelyLength,
                receiveTimelyLength + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                movieImageSpan,
                checkInBonusLength,
                checkInBonusLength + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.enableNotificationsTv.text = spannableString

            setOnClickListener(binding.allowTv, binding.notNowTv)
        }

        override fun onClick(view: View) {
            when (view) {
                binding.allowTv -> getActivity()?.packageName?.let {
                    startActivity(StartNotificationUtils.getNotificationIntent(it))
                }

                binding.notNowTv -> dismiss()
            }
        }

        // DefaultLifecycleObserver 回调方法
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            // 当宿主的 onResume 被调用时触发
            if (NotificationUtils.areNotificationsEnabled()) {
                dismiss()
            }
        }

        override fun onDismiss(dialog: BaseDialog?) {
            // 对话框关闭时从宿主生命周期中移除观察者
            lifecycleOwner.lifecycle.removeObserver(this)
        }

    }
}