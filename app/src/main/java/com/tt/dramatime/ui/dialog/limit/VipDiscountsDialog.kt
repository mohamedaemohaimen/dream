package com.tt.dramatime.ui.dialog.limit

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.android.billingclient.api.BillingClient.ProductType.SUBS
import com.android.billingclient.api.ProductDetails
import com.blankj.utilcode.util.ThreadUtils
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R
import com.tt.dramatime.action.ToastAction
import com.tt.dramatime.databinding.VipDiscountsDialogBinding
import com.tt.dramatime.http.api.RechargeApi.Bean.ProductListBean
import com.tt.dramatime.manager.google.pay.GoogleBillingManager
import com.tt.dramatime.ui.dialog.PaymentDialog
import com.tt.dramatime.util.eventbus.PaySuccessNotify
import org.greenrobot.eventbus.EventBus

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc :Vip折扣弹窗
 * </pre>
 */
class VipDiscountsDialog {
    @SuppressLint("SetTextI18n")
    class Builder(
        context: Context,
        id: String?,
        private val sub: ProductListBean,
        source: Int,
        mCurrentEpisode: Int? = null
    ) : PaymentDialog.Builder<Builder>(context), BaseDialog.OnShowListener,
        BaseDialog.OnDismissListener, ToastAction {

        private val binding = VipDiscountsDialogBinding.inflate(LayoutInflater.from(context))


        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_IOS)
            setGravity(Gravity.CENTER)
            setCancelable(false)
            addOnShowListener(this)
            setWidth(getResources().getDimension(R.dimen.dp_303).toInt())

            mSource = source
            mDialogId = id
            mSourceId= sub.id

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
                            binding.renewTv.text = getString(R.string.us_money, sub.price)
                        } else {
                            productList.forEach {
                                if (it.productType == SUBS) {
                                    setProductAmount(it)
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

            setOnClickListener(binding.closeIv, binding.renewTv)

            binding.apply {
                sub.apply {
                    titleTv.text = goodsName
                    vipFrameIv.setBackgroundResource(when {
                        code.contains("week") -> {
                            R.drawable.vip_weekly_frame_ic
                        }

                        code.contains("month") -> {
                            R.drawable.vip_monthly_frame_ic
                        }

                        code.contains("year") -> {
                            R.drawable.vip_annual_frame_ic
                        }

                        else -> {
                            R.drawable.vip_weekly_frame_ic
                        }
                    })
                    introduceTv.text = desc
                }
            }
        }

        private fun setProductAmount(mProductDetails: ProductDetails?) {
            sub.apply {
                mProductDetails?.apply {
                    productDetails = mProductDetails
                    subscriptionOfferDetails?.let { subscriptionOfferDetails ->
                        subscriptionOfferDetails.forEach {
                            it.pricingPhases.pricingPhaseList.forEach { pricingPhase ->
                                pricingPhase.apply {
                                    if (priceAmountMicros > 0.toLong() && priceCurrencyCode.isNotEmpty()) {
                                        amount = String.formatPrice(priceAmountMicros)
                                        currency = priceCurrencyCode
                                        binding.renewTv.text = formattedPrice
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        override fun onClick(view: View) {
            when (view) {
                binding.closeIv -> {
                    dismiss()
                }

                binding.renewTv -> onCreateOrder(productListBean = sub, productType = SUBS)
            }
        }

        override fun onShow(dialog: BaseDialog?) {
            onQuerySkuDetails(mutableListOf(sub), SUBS)
            queryHistory()
        }

        override fun onDismiss(dialog: BaseDialog?) {
            GoogleBillingManager.setBillingListener(null)
        }
    }
}