package com.tt.dramatime.ui.adapter.wallet

import android.view.View
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.Logger
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.RechargeItemBinding
import com.tt.dramatime.http.api.RechargeApi.Bean.ProductListBean
import java.util.Locale

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 商店列表
 * </pre>
 */
class RechargeAdapter(dataList: MutableList<ProductListBean>) :
    BaseVBQuickAdapter<RechargeItemBinding, ProductListBean>(dataList) {

    private fun String.Companion.formatPrice(price: Long): String {
        return String.format(Locale.US, "%.2f", price.toDouble() / (1000 * 1000))
    }

    override fun onBindViewHolder(
        holder: BaseVBHolder<RechargeItemBinding>, position: Int, item: ProductListBean?
    ) {
        holder.binding.apply {
            item?.apply {
                discountTv.visibility = if (corner.isNullOrEmpty()) View.GONE else View.VISIBLE
                discountTv.text = corner
                coinsTv.text = num.toString()
                bonusTv.visibility = if (giveNum == 0) View.GONE else View.VISIBLE
                bonusTv.text = context.getString(R.string.add_bonus_num, giveNum.toString())
                selectIv.visibility = if (checkStatus) View.VISIBLE else View.GONE


                var mFormattedPrice = ""
                productDetails?.apply {
                    oneTimePurchaseOfferDetails?.apply {
                        amount = String.formatPrice(priceAmountMicros)
                        currency = priceCurrencyCode
                        mFormattedPrice = formattedPrice
                    }
                }

                moneyCycleTv.text = mFormattedPrice.ifEmpty {
                    context.getString(R.string.us_money, price)
                }

                if (amount.contains(",") || amount.isEmpty()) {
                    Firebase.crashlytics.recordException(IllegalArgumentException("amount.数据异常：$amount"))
                }
                Logger.e("priceAmountMicros:$amount priceCurrencyCode:$currency")
            }
        }
    }

}