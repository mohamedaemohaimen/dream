package com.tt.dramatime.ui.dialog.player

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.blankj.utilcode.util.NotificationUtils
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.hjq.language.MultiLanguages
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R
import com.tt.dramatime.action.ToastAction
import com.tt.dramatime.app.AppActivity
import com.tt.dramatime.databinding.FirstDramaDialogBinding
import com.tt.dramatime.http.api.MovieDetailApi
import com.tt.dramatime.http.api.MovieRemindApi
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.LanguageManager
import com.tt.dramatime.util.GlideUtils
import com.tt.dramatime.util.eventbus.MovieRemindNotify
import org.greenrobot.eventbus.EventBus

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc :首发剧弹窗
 * </pre>
 */
class FirstDramaDialog {

    class Builder(val activity: AppActivity, private val mMovieDetailBean: MovieDetailApi.Bean) :
        BaseDialog.Builder<Builder>(activity), BaseDialog.OnDismissListener, ToastAction {

        val binding = FirstDramaDialogBinding.inflate(LayoutInflater.from(activity))
        private var countDownTimer: CountDownTimer? = null

        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_IOS)
            setGravity(Gravity.CENTER)
            setWidth(getResources().getDimension(R.dimen.dp_287).toInt())
            addOnDismissListener(this)

            setOnClickListener(binding.closeBtn, binding.reserveLl)

            GlideUtils.loadImage(activity, mMovieDetailBean.poster, binding.posterIv)

            if (LanguageManager.isArabicLocale(getContext())) {
                binding.countDownLl.shapeDrawableBuilder.setRadius(
                    dp2px(16f).toFloat(), 0f, 0f, dp2px(16f).toFloat()
                ).intoBackground()
            } else {
                binding.countDownLl.shapeDrawableBuilder.setRadius(
                    0f, dp2px(16f).toFloat(), dp2px(16f).toFloat(), 0f
                ).intoBackground()
            }

            mMovieDetailBean.activity?.apply {
                // 计算剩余时间 为了防止还没满1分钟，倒计时会显示为0，这里加1分钟
                val millisInFuture =
                    (timestamp ?: System.currentTimeMillis()) - System.currentTimeMillis()
                        .plus(1000 * 60)
                countDownTimer = object : CountDownTimer(millisInFuture, 1000 * 60) {
                    override fun onTick(millisUntilFinished: Long) {
                        // 计算剩余的小时、分钟和秒数
                        val days = millisUntilFinished / (1000 * 60 * 60 * 24)
                        val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                        val minutes = (millisUntilFinished / (1000 * 60)) % 60
                        // 更新 UI
                        val local = MultiLanguages.getAppLanguage(getContext())
                        binding.dayTv.text = String.format(local, "%d", days)
                        binding.hourTv.text = String.format(local, "%d", hours)
                        binding.minutesTv.text = String.format(local, "%d", minutes)
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onFinish() {
                        // 倒计时完成时执行的操作
                        binding.dayTv.text = "0"
                        binding.hourTv.text = "0"
                        binding.minutesTv.text = "0"
                        dismiss()
                    }
                }
                countDownTimer?.start()
            }
        }

        @SingleClick(1000)
        override fun onClick(view: View) {
            when (view) {
                binding.closeBtn -> dismiss()
                binding.reserveLl -> remind()

            }
        }

        override fun onDismiss(dialog: BaseDialog?) {
            countDownTimer?.cancel()
        }

        private fun remind() {
            mMovieDetailBean.id?.let { movieId ->
                if (NotificationUtils.areNotificationsEnabled().not()) {
                    EnableNotificationsDialog.Builder(getContext(), activity)
                        .addOnDismissListener(object : BaseDialog.OnDismissListener {
                            override fun onDismiss(dialog: BaseDialog?) {
                                if (NotificationUtils.areNotificationsEnabled()) {
                                    remind()
                                }
                            }
                        }).show()
                    return
                }

                EasyHttp.post(getDialog()).api(MovieRemindApi(movieId, 1))
                    .request(object : OnHttpListener<HttpData<Void>> {
                        override fun onHttpSuccess(result: HttpData<Void>?) {
                            toast(R.string.reservation_successful)
                            binding.buttonTv.text = getString(R.string.reserved)
                            binding.buttonIv.setBackgroundResource(R.drawable.first_drama_reserved_ic)
                            EventBus.getDefault().post(MovieRemindNotify(movieId))
                            postDelayed({ dismiss() }, 500)
                        }

                        override fun onHttpFail(throwable: Throwable?) {
                            toast(throwable?.message)
                        }
                    })
            }

        }

    }
}