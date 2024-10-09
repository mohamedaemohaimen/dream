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
import com.tt.dramatime.databinding.EpDiscountsDialogBinding
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
 *   desc :剧集折扣弹窗
 * </pre>
 */
class EpDiscountsDialog {

    class Builder(
        context: Context,
        bean: DiscountPopupApi.Bean,
        val unlock: ProductListBean,
        source: Int,
        mCurrentEpisode: Int? = null
    ) : PaymentDialog.Builder<Builder>(context), BaseDialog.OnShowListener,
        BaseDialog.OnDismissListener {

        private val binding = EpDiscountsDialogBinding.inflate(LayoutInflater.from(context))
        private var countDownTimer: CountDownTimer? = null

        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_IOS)
            setCancelable(false)
            setGravity(Gravity.CENTER)
            addOnShowListener(this)
            setWidth(getResources().getDimension(R.dimen.dp_319).toInt())
            setOnClickListener(binding.closeBtn, binding.payTv)

            mSource = source
            mDialogId = bean.id

            unlock.apply {
                binding.apply {
                    titleTv.text = bean.name
                    goodsNameTv.text = goodsName
                    GlideUtils.loadImage(context, cover, coverIv)
                    mSourceId = id

                    if (bean.countdown > 0) {
                        countDownLl.visibility = View.VISIBLE
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
                            binding.payTv.text = getString(R.string.us_money, unlock.price)
                        } else {
                            productList.forEach {
                                it.apply {
                                    unlock.productDetails = this
                                    oneTimePurchaseOfferDetails?.apply {
                                        unlock.amount = String.formatPrice(priceAmountMicros)
                                        unlock.currency = priceCurrencyCode
                                        binding.payTv.text = formattedPrice
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
                            PaySuccessNotify(mCurrentEpisode, 2, false)
                        )
                    }
                    dismiss()
                }
            }
        }

        override fun onClick(view: View) {
            when (view) {
                binding.closeBtn -> dismiss()
                binding.payTv -> unlock.apply {
                    onCreateOrder(movieCode, ep, this, INAPP)
                }
            }
        }

        override fun onShow(dialog: BaseDialog?) {
            onQuerySkuDetails(mutableListOf(unlock), INAPP)
            queryHistory()
        }

        override fun onDismiss(dialog: BaseDialog?) {
            countDownTimer?.cancel()
        }

    }
}