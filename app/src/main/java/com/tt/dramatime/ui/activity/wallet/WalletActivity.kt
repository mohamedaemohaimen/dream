package com.tt.dramatime.ui.activity.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import com.gyf.immersionbar.ImmersionBar
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.WalletActivityBinding
import com.tt.dramatime.http.db.UserProfileHelper

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 钱包
 * </pre>
 */
class WalletActivity :
    BaseViewBindingActivity<WalletActivityBinding>({ WalletActivityBinding.inflate(it) }) {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, WalletActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig().navigationBarColor(R.color.color_F6F7F9)
    }

    override fun initView() {
        setOnClickListener(binding.rechargeTv, binding.unlockHistorySb, binding.transactionSb)
    }

    override fun onResume() {
        super.onResume()
        UserProfileHelper.apply {
            binding.totalCoinsTv.text = getCoins().plus(getBonus()).toString()
            binding.coinsBonusTv.text =
                getString(R.string.coins_bonus, getCoins().toString(), getBonus().toString())
        }
    }

    override fun initData() {}

    override fun onClick(view: View) {
        when (view) {
            binding.rechargeTv -> StoreActivity.start(getContext())
            binding.unlockHistorySb -> UnlockHistoryActivity.start(getContext())
            binding.transactionSb -> TransactionRecordsActivity.start(getContext())
        }
    }
}