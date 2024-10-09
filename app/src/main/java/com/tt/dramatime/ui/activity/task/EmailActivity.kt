package com.tt.dramatime.ui.activity.task

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ConvertUtils
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.EmailActivityBinding
import com.tt.dramatime.http.api.BindEmailApi
import com.tt.dramatime.http.api.SendCodeApi
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.LanguageManager
import com.tt.dramatime.other.KeyboardWatcher
import com.tt.dramatime.ui.adapter.bonus.EmailAdapter

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 邮箱任务界面
 * </pre>
 */
class EmailActivity :
    BaseViewBindingActivity<EmailActivityBinding>({ EmailActivityBinding.inflate(it) }),
    TextWatcher, KeyboardWatcher.SoftKeyboardStateListener {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, EmailActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private val mEmailAdapter by lazy {
        EmailAdapter(mutableListOf())
    }

    private var isSendEmail = false

    private var email = ""

    private var countDownTimer: CountDownTimer? = null

    override fun initView() {
        binding.emailEt.addTextChangedListener(this)
        binding.emailEt.textDirection =
            if (LanguageManager.isArabicLocale(getContext())) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR
        postDelayed({
            KeyboardWatcher.with(this@EmailActivity).setListener(this@EmailActivity)
        }, 300)

        binding.emailRv.adapter = mEmailAdapter

        mEmailAdapter.addOnItemChildClickListener(R.id.email_tv) { _, _, position ->
            binding.emailEt.setText(mEmailAdapter.getItem(position))
            binding.emailEt.setSelection(binding.emailEt.editableText.length)
        }

        setOnClickListener(binding.inputOperationTv, binding.verifyTv)
    }

    override fun initData() {

    }

    @SingleClick(1000)
    override fun onClick(view: View) {
        when (view) {
            binding.inputOperationTv -> {
                if (isSendEmail) verifyEmail() else binding.emailEt.setText("")
            }

            binding.verifyTv -> {
                if (binding.emailEt.editableText.isEmpty()) {
                    toast(if (isSendEmail) R.string.enter_code_hint else R.string.enter_email_hint)
                } else {
                    if (isSendEmail) confirmCode() else {
                        email = binding.emailEt.text.toString()
                        verifyEmail()
                    }
                }
            }
        }
    }

    private fun verifyEmail() {
        if (Patterns.EMAIL_ADDRESS.matcher(binding.emailEt.editableText).matches()) {
            EasyHttp.get(this).api(SendCodeApi(email))
                .request(object : HttpCallbackProxy<HttpData<SendCodeApi.Bean?>>(this) {
                    override fun onHttpSuccess(data: HttpData<SendCodeApi.Bean?>) {
                        isSendEmail = true
                        binding.emailEt.setText("")
                        binding.emailEt.setHint(getString(R.string.verification_code))
                        binding.emailEt.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                        binding.emailInputHintTv.visibility = View.VISIBLE
                        binding.emailInputHintTv.text =
                            getString(R.string.verification_code_sent, email)

                        binding.inputOperationTv.setTextColor(
                            ContextCompat.getColor(
                                getContext(), R.color.color_707070
                            )
                        )
                        binding.verifyTv.text = getString(R.string.verify)

                        countDown()
                    }
                })
        } else {
            toast(R.string.valid_email_hint)
        }
    }

    private fun confirmCode() {
        EasyHttp.post(this).api(BindEmailApi(email, binding.emailEt.text.toString()))
            .request(object : HttpCallbackProxy<HttpData<BindEmailApi.Bean?>>(this) {
                override fun onHttpSuccess(data: HttpData<BindEmailApi.Bean?>) {
                    finish()
                    toast(R.string.linked_success)
                }
            })
    }

    private fun countDown() {
        binding.inputOperationTv.isEnabled = false
        binding.emailEt.maxEms = 6
        countDownTimer = object : CountDownTimer(120 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 计算剩余的小时、分钟和秒数
                val seconds = ((millisUntilFinished % (1000 * 120)) / 1000).toInt()
                binding.inputOperationTv.text = seconds.toString()
            }

            override fun onFinish() {
                // 倒计时完成时执行的操作
                binding.inputOperationTv.isEnabled = true
                binding.inputOperationTv.setTextColor(
                    ContextCompat.getColor(getContext(), R.color.color_D81E06)
                )
                binding.inputOperationTv.text = getString(R.string.resend)
            }
        }
        countDownTimer?.start()
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        setCleanEmail(if (s.isNotEmpty() && isSendEmail.not()) R.drawable.mail_clean_ic else 0)
        binding.emailRv.visibility =
            if (s.contains("com") || s.isEmpty() || isSendEmail) View.GONE else {
                val emailList = mutableListOf<String>()
                val emailSuffixList =
                    mutableListOf("@gmail.com", "@yahoo.com", "@hotmail.com", "@icloud.com")
                for (i in 0..3) {
                    if (s.contains("@")) {
                        val parts = s.split("@").filter { it.isNotBlank() }
                        val split: String = if (parts.isNotEmpty()) parts[0] else ""
                        emailList.add(split + emailSuffixList[i])
                    } else {
                        emailList.add(s.toString() + emailSuffixList[i])
                    }
                }
                mEmailAdapter.submitList(emailList)
                View.VISIBLE
            }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable?) {}

    private fun setCleanEmail(@DrawableRes drawable: Int) {
        binding.inputOperationTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
    }

    /** 动画时间 */
    private val animTime: Int = 300
    override fun onSoftKeyboardOpened(keyboardHeight: Int) {
        // 执行位移动画
        binding.emailCl.let {
            val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(
                it, "translationY", 0f, -ConvertUtils.dp2px(100f).toFloat()
            )
            objectAnimator.duration = animTime.toLong()
            objectAnimator.interpolator = AccelerateDecelerateInterpolator()
            objectAnimator.start()
        }
    }

    override fun onSoftKeyboardClosed() {
        // 执行位移动画
        binding.emailCl.let {
            val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(
                it, "translationY", it.translationY, 0f
            )
            objectAnimator.duration = animTime.toLong()
            objectAnimator.interpolator = AccelerateDecelerateInterpolator()
            objectAnimator.start()
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}