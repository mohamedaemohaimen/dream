package com.tt.dramatime.ui.dialog.limit

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.android.billingclient.api.BillingClient.ProductType.INAPP
import com.android.billingclient.api.ProductDetails
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.blankj.utilcode.util.ThreadUtils
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R
import com.tt.dramatime.databinding.LimitDiscountsDialogBinding
import com.tt.dramatime.http.api.DiscountPopupApi
import com.tt.dramatime.http.api.RechargeApi.Bean.ProductListBean
import com.tt.dramatime.ui.adapter.limit.LimitDiscountsAdapter
import com.tt.dramatime.ui.dialog.PaymentDialog
import com.tt.dramatime.util.eventbus.PaySuccessNotify
import org.greenrobot.eventbus.EventBus

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc :限时折扣弹窗
 * </pre>
 */
class LimitDiscountsDialog {

    @SuppressLint("NotifyDataSetChanged")
    class Builder(
        context: Context,
        bean: DiscountPopupApi.Bean,
        val recharge: MutableList<ProductListBean>,
        source: Int,
        mCurrentEpisode: Int? = null
    ) : PaymentDialog.Builder<Builder>(context), BaseDialog.OnShowListener,
        BaseDialog.OnDismissListener {

        val binding = LimitDiscountsDialogBinding.inflate(LayoutInflater.from(context))

        val mLimitDiscountsAdapter = LimitDiscountsAdapter(mutableListOf())

        private var mProductListBean: ProductListBean? = null

        private var countDownTimer: CountDownTimer? = null

        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_IOS)
            setGravity(Gravity.CENTER)
            setWidth(getResources().getDimension(R.dimen.dp_283).toInt())
            setYOffset(-dp2px(if (recharge.size > 2) 15f else 35f))//3个50 2个35
            setCancelable(false)
            addOnShowListener(this)
            addOnDismissListener(this)
            val twoHeight = getResources().getDimension(R.dimen.dp_346).toInt()
            val oneHeight = getResources().getDimension(R.dimen.dp_63).toInt()
            binding.contentLl.layoutParams.height =
                if (recharge.size > 2) {
                    binding.maskIv.visibility = View.VISIBLE
                    twoHeight + oneHeight
                } else twoHeight
            recharge[0].checkStatus = true
            binding.limitRv.adapter = mLimitDiscountsAdapter

            mLimitDiscountsAdapter.setOnItemClickListener { adapter, _, position ->
                adapter.items.forEachIndexed { index, productListBean ->
                    productListBean.checkStatus = index == position
                }
                setSelectData()
                onCreateOrder(productListBean = adapter.items[position], productType = INAPP)
                adapter.notifyDataSetChanged()
            }

            mLimitDiscountsAdapter.setOnLoadListener(object :
                LimitDiscountsAdapter.OnLoaderListener {
                override fun onLoadEnd() {
                    setSelectData()
                    mLimitDiscountsAdapter.setOnLoadListener(null)
                }
            })

            binding.payTv.setOnClickListener {
                mProductListBean?.let {
                    onCreateOrder(productListBean = it, productType = INAPP)
                }
            }

            binding.apply {
                titleTv.text = bean.name

                setPayButtonAnim(payTv)

                if (bean.countdown > 0) {
                    countDownLl.visibility = View.VISIBLE
                    countDownTimer = object : CountDownTimer(bean.countdown * 1000.toLong(), 100) {
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

            setOnClickListener(binding.closeBtn)

            mSource = source
            mDialogId = bean.id

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
                            mLimitDiscountsAdapter.submitList(recharge)
                        } else {
                            productList.forEach {
                                if (it.productType == INAPP) {
                                    recharge.forEach { bean ->
                                        if (it.productId == bean.goodsCode) bean.productDetails = it
                                    }
                                }
                            }
                            mLimitDiscountsAdapter.submitList(recharge)
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

        private fun setSelectData() {
            mLimitDiscountsAdapter.items.forEachIndexed { index, productListBean ->
                if (productListBean.checkStatus) {
                    setSelectedPrice(
                        binding.currencyTypeTv, binding.moneyCycleTv, productListBean
                    )
                    mSourceId = mLimitDiscountsAdapter.items[index].id
                    mProductListBean = mLimitDiscountsAdapter.items[index]
                }
            }
        }

        override fun onShow(dialog: BaseDialog?) {
            onQuerySkuDetails(recharge, INAPP)
            queryHistory()
        }

        override fun onClick(view: View) {
            dismiss()
        }

        override fun onDismiss(dialog: BaseDialog?) {
            countDownTimer?.cancel()
        }

    }
}