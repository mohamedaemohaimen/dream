package com.tt.dramatime.ui.dialog.limit

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.android.billingclient.api.BillingClient.ProductType.INAPP
import com.android.billingclient.api.ProductDetails
import com.blankj.utilcode.util.ThreadUtils
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R
import com.tt.dramatime.databinding.LimitOneDiscountsDialogBinding
import com.tt.dramatime.http.api.DiscountPopupApi
import com.tt.dramatime.http.api.RechargeApi.Bean.ProductListBean
import com.tt.dramatime.ui.dialog.PaymentDialog
import com.tt.dramatime.util.GlideUtils
import com.tt.dramatime.util.eventbus.PaySuccessNotify
import org.greenrobot.eventbus.EventBus

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc :限时折扣弹窗
 * </pre>
 */
class LimitOneDiscountsDialog {

    class Builder(
        context: Context,
        bean: DiscountPopupApi.Bean,
        val recharge: MutableList<ProductListBean>,
        source: Int,
        mCurrentEpisode: Int? = null
    ) : PaymentDialog.Builder<Builder>(context), BaseDialog.OnShowListener,
        BaseDialog.OnDismissListener {

        private val binding = LimitOneDiscountsDialogBinding.inflate(LayoutInflater.from(context))
        private var countDownTimer: CountDownTimer? = null

        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_IOS)
            setGravity(Gravity.CENTER)
            setYOffset(-getResources().getDimension(R.dimen.dp_35).toInt())
            setWidth(getResources().getDimension(R.dimen.dp_283).toInt())
            setCancelable(false)
            addOnShowListener(this)
            addOnDismissListener(this)

            setOnClickListener(binding.closeBtn, binding.payTv)

            setPayButtonAnim(binding.payTv)

            mDialogId = bean.id
            mSource = source

            if (recharge.size > 0) {
                recharge[0].apply {
                    binding.apply {
                        titleTv.text = bean.name
                        coinsTv.text = num.toString()
                        mSourceId = id
                        if (giveNum > 0) boundsTv.text = getString(R.string.add_bonus_num, giveNum)

                        promotionIcon?.let {
                            GlideUtils.loadImageTransparent(context, it, binding.labelIv)
                            binding.labelUpIv.visibility = View.VISIBLE
                        }

                        if (bean.countdown > 0) {
                            binding.countDownLl.visibility = View.VISIBLE
                            countDownTimer =
                                object : CountDownTimer(bean.countdown * 1000.toLong(), 100) {
                                    @SuppressLint("DefaultLocale")
                                    override fun onTick(millisUntilFinished: Long) {
                                        // 计算剩余的小时、分钟和秒数
                                        val minutes = (millisUntilFinished / 1000) / 60
                                        val seconds = (millisUntilFinished / 1000) % 60
                                        val milliseconds = (millisUntilFinished % 1000) / 100
                                        // 更新 UI
                                        minutesTv.text = String.format("%02d", minutes)
                                        secondsTv.text = String.format("%02d", seconds)
                                        millisecondsTv.text = String.format("%d", milliseconds)
                                    }

                                    @SuppressLint("SetTextI18n")
                                    override fun onFinish() {
                                        // 倒计时完成时执行的操作
                                        minutesTv.text = "00"
                                        secondsTv.text = "00"
                                        millisecondsTv.text = "0"
                                        dismiss()
                                    }
                                }
                            countDownTimer?.start()
                        }
                    }
                }
                listener = object : OnListener {
                    override fun onProductDetailsResult(
                        productList: List<ProductDetails>?, bean: ProductListBean?
                    ) {
                        ThreadUtils.runOnUiThread {
                            if (productList != null && bean != null) {
                                queryCreateOrder(bean, productList[0])
                            } else if (productList.isNullOrEmpty() && bean != null) {
                                toast(R.string.purchase_failed)
                            } else if (productList.isNullOrEmpty()) {
                                binding.currencyTypeTv.text = getString(R.string.us_money_text)
                                binding.moneyCycleTv.text = recharge[0].price
                            } else {
                                productList.forEach {
                                    it.apply {
                                        recharge.forEach { recharge ->
                                            recharge.productDetails = this
                                            setSelectedPrice(
                                                binding.currencyTypeTv,
                                                binding.moneyCycleTv,
                                                recharge
                                            )
                                        }
                                    }
                                }
                            }
                            hideDialog()
                        }
                    }

                    override fun onPaymentSuccessful() {
                        mCurrentEpisode?.let {
                            EventBus.getDefault().post(
                                PaySuccessNotify(mCurrentEpisode, 1, false)
                            )
                        }
                        dismiss()
                    }
                }
            }
        }

        override fun onClick(view: View) {
            when (view) {
                binding.closeBtn -> dismiss()
                binding.payTv -> onCreateOrder(productListBean = recharge[0], productType = INAPP)
            }

        }

        override fun onDismiss(dialog: BaseDialog?) {
            countDownTimer?.cancel()
        }

        override fun onShow(dialog: BaseDialog?) {
            onQuerySkuDetails(recharge, INAPP)
            queryHistory()
        }
    }
}