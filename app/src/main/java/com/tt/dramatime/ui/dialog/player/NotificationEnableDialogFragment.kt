package com.tt.dramatime.ui.dialog.player

import android.view.Gravity
import android.view.View
import com.blankj.utilcode.util.NotificationUtils
import com.gyf.immersionbar.ImmersionBar
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseViewBindDialogFragment
import com.tt.dramatime.databinding.NotificationEnableDialogBinding
import com.tt.dramatime.util.StartNotificationUtils.getNotificationIntent

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc :推送通知弹窗
 * </pre>
 */
class NotificationEnableDialogFragment :
    BaseViewBindDialogFragment<NotificationEnableDialogBinding>({
        NotificationEnableDialogBinding.inflate(it)
    }) {

    companion object {
        const val TAG = "NotificationEnableDialogFragment"
    }

    override fun getGravity(): Int {
        return Gravity.BOTTOM
    }

    override fun cancelable(): Boolean {
        return false
    }

    override fun initView() {
        ImmersionBar.with(this).navigationBarColor(R.color.white).init()
        setOnClickListener(binding.closeIv, binding.enableTv)
    }

    override fun initData() {

    }

    override fun onResume() {
        super.onResume()
        if (NotificationUtils.areNotificationsEnabled()) {
            toastBonus("+100")
            dismiss()
        }
    }


    override fun onClick(view: View) {
        when (view) {
            binding.closeIv -> dismiss()
            binding.enableTv -> {
                if (NotificationUtils.areNotificationsEnabled().not()) {
                    startActivity(getNotificationIntent(requireActivity().packageName))
                }
            }
        }
    }
}