package com.tt.dramatime.ui.adapter.limit

import android.view.View
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.LimitDiscountsItemBinding
import com.tt.dramatime.http.api.RechargeApi.Bean.ProductListBean
import com.tt.dramatime.util.GlideUtils
import java.util.Locale


/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 限时折扣弹窗
 * </pre>
 */
class LimitDiscountsAdapter(dataList: MutableList<ProductListBean>) :
    BaseVBQuickAdapter<LimitDiscountsItemBinding, ProductListBean>(dataList) {

    private fun String.Companion.formatPrice(price: Long): String {
        return String.format(Locale.US, "%.2f", price.toDouble() / (1000 * 1000))
    }

    private var listener: OnLoaderListener? = null

    fun setOnLoadListener(listener: OnLoaderListener?) {
        this.listener = listener
    }

    override fun onBindViewHolder(
        holder: BaseVBHolder<LimitDiscountsItemBinding>, position: Int, item: ProductListBean?
    ) {
        item?.apply {
            holder.binding.apply {

                GlideUtils.loadImageTransparent(context, promotionIcon, labelIv)

                labelUpIv.visibility =
                    if (promotionIcon.isNullOrEmpty()) View.GONE else View.VISIBLE

                limitFl.setBackgroundResource(if (checkStatus) R.drawable.limit_item_select_bg else 0)

                labelTv.visibility = if (corner.isNullOrEmpty()) View.GONE else {
                    labelTv.text = corner
                    View.VISIBLE
                }

                coinsTv.text = num.toString()
                boundsTv.text = giveNum.toString()

                var mFormattedPrice = ""
                productDetails?.apply {
                    oneTimePurchaseOfferDetails?.apply {
                        amount = String.formatPrice(priceAmountMicros)
                        currency = priceCurrencyCode
                        mFormattedPrice = formattedPrice
                    }
                }
            }

            if (position == itemCount - 1) listener?.onLoadEnd()
        }
    }

    interface OnLoaderListener {
        fun onLoadEnd()
    }

}