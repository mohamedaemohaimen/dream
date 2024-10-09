package com.tt.dramatime.ui.adapter.wallet

import android.graphics.Paint
import android.view.View
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.Logger
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.SubscriptionsItemBinding
import com.tt.dramatime.http.api.RechargeApi.Bean.ProductListBean
import java.util.Locale


/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 订阅列表
 * </pre>
 */
class SubscriptionsAdapter(dataList: MutableList<ProductListBean>) :
    BaseVBQuickAdapter<SubscriptionsItemBinding, ProductListBean>(dataList) {

    private fun String.Companion.formatPrice(price: Long): String {
        return String.format(Locale.US, "%.2f", price.toDouble() / (1000 * 1000))
    }

    override fun onBindViewHolder(
        holder: BaseVBHolder<SubscriptionsItemBinding>, position: Int, item: ProductListBean?
    ) {
        holder.binding.apply {
            item?.apply {
                val type: String
                vipRoot.setBackgroundResource(
                    when {
                        code.contains("week") -> {
                            type = "week"
                            vipFrameIv.setBackgroundResource(R.drawable.vip_weekly_frame_ic)
                            R.drawable.vip_weekly_bg
                        }

                        code.contains("month") -> {
                            type = "month"
                            vipFrameIv.setBackgroundResource(R.drawable.vip_monthly_frame_ic)
                            R.drawable.vip_monthly_bg
                        }

                        code.contains("year") -> {
                            type = "year"
                            vipFrameIv.setBackgroundResource(R.drawable.vip_annual_frame_ic)
                            R.drawable.vip_annual_bg
                        }

                        else -> {
                            type = "week"
                            vipFrameIv.setBackgroundResource(R.drawable.vip_weekly_frame_ic)
                            R.drawable.vip_weekly_bg
                        }
                    }
                )

                firstTrialTv.paintFlags = firstTrialTv.paintFlags
                firstTrialTv.text = when (discountType) {
                    1 -> context.getString(R.string.first_trial, price, type)

                    2 -> {
                        firstTrialTv.paintFlags =
                            firstTrialTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        context.getString(R.string.us_money, price)
                    }

                    else -> ""
                }

                vipTv.text = name
                vipDescTv.text = desc
                cornerTv.visibility = if (corner.isNullOrEmpty()) View.GONE else View.VISIBLE
                cornerTv.text = corner
                selectIv.visibility = if (checkStatus) View.VISIBLE else View.GONE

                //通过google查询到商品详情 获取价格
                var mFormattedPrice = ""
                productDetails?.apply {
                    subscriptionOfferDetails?.let { subscriptionOfferDetails ->
                        subscriptionOfferDetails.forEach {
                            it.pricingPhases.pricingPhaseList.forEach { pricingPhase ->
                                pricingPhase.apply {
                                    if (priceAmountMicros > 0.toLong() && priceCurrencyCode.isNotEmpty()) {
                                        amount = String.formatPrice(priceAmountMicros)
                                        currency = priceCurrencyCode
                                        mFormattedPrice = formattedPrice
                                    }
                                }
                            }
                        }
                    }
                    if (amount.contains(",") || amount.isEmpty()) {
                        Firebase.crashlytics.recordException(IllegalArgumentException("amount.数据异常：$amount"))
                    }
                    Logger.e("priceAmountMicros:$amount priceCurrencyCode:$currency")
                }

                vipPriceTv.text = mFormattedPrice.ifEmpty {
                    context.getString(
                        R.string.us_money,
                        if (discountType != 0) discountPrice else price
                    )
                }
            }
        }
    }

}