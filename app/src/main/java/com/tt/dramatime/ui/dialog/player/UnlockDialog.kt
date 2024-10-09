package com.tt.dramatime.ui.dialog.player

import android.content.Context
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.orhanobut.logger.Logger
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R
import com.tt.dramatime.databinding.UnlockDialogBinding

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc : 观看视频解锁弹窗
 * </pre>
 */
class UnlockDialog {

    class Builder(context: Context, private var watchAdNum: Int, singleUnlock: Int?) :
        BaseDialog.Builder<Builder>(context), BaseDialog.OnShowListener,
        BaseDialog.OnDismissListener {

        val binding = UnlockDialogBinding.inflate(LayoutInflater.from(context))

        private var mWatchAdCallback: OnWatchAdCallback? = null

        private var countDownTimer: CountDownTimer? = null

        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_IOS)
            setGravity(Gravity.CENTER)
            addOnShowListener(this)
            addOnDismissListener(this)
            setWidth(getResources().getDimension(R.dimen.dp_319).toInt())


            binding.watchUnlockTv.text = if (singleUnlock != null && singleUnlock > 1) getString(
                R.string.watch_ads_to_unlock_ep, singleUnlock
            ) else getString(R.string.watch_ads_to_unlock)

            binding.prepareTv.text = getString(R.string.preparing_ads, 1, watchAdNum)

            setOnClickListener(binding.watchUnlockTv, binding.adFreeTv, binding.closeBtn)
        }

        override fun onClick(view: View) {
            when (view) {
                binding.watchUnlockTv -> {
                    countDownTimer?.cancel()
                    mWatchAdCallback?.watchAd()
                }

                binding.adFreeTv -> {
                    mWatchAdCallback?.adFree()
                    dismiss()
                }

                binding.closeBtn -> dismiss()
            }
        }

        private fun starTimer() {
            // 如果计时器已经存在且正在运行，先停止它
            if (countDownTimer != null) {
                countDownTimer?.cancel()
            }
            countDownTimer = object : CountDownTimer(5 * 1000.toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // 计算剩余的小时、分钟和秒数
                    val seconds = millisUntilFinished / 1000
                    binding.timeTv.text = getString(R.string.seconds, seconds)
                }

                override fun onFinish() {
                    mWatchAdCallback?.watchAd()
                }
            }
            countDownTimer?.start()
        }

        fun setOnWatchAdCallback(watchAdCallback: OnWatchAdCallback?): Builder {
            mWatchAdCallback = watchAdCallback
            return this
        }

        fun setWatchNum(watchNum: Int) {
            Logger.e("watchNum: $watchNum watchAdNum:$watchAdNum")
            binding.prepareTv.text = getString(
                R.string.preparing_ads, watchNum + 1, watchAdNum
            )
            starTimer()
        }

        override fun onDismiss(dialog: BaseDialog?) {
            countDownTimer?.cancel()
        }

        override fun onShow(dialog: BaseDialog?) {
            starTimer()
        }
    }


    interface OnWatchAdCallback {
        fun watchAd()
        fun adFree()
    }

}