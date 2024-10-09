package com.tt.dramatime.ui.dialog

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType.SUBS
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.orhanobut.logger.Logger
import com.tt.base.BaseDialog
import com.tt.dramatime.R
import com.tt.dramatime.action.ToastAction
import com.tt.dramatime.app.AppActivity
import com.tt.dramatime.http.api.CancelOrderApi
import com.tt.dramatime.http.api.CheckOrderApi
import com.tt.dramatime.http.api.CreateOrderApi
import com.tt.dramatime.http.api.PopupRecordApi
import com.tt.dramatime.http.api.RechargeApi.Bean.ProductListBean
import com.tt.dramatime.http.api.UserInfoApi
import com.tt.dramatime.http.bean.CheckOrderFailBean
import com.tt.dramatime.http.db.CheckOrderFailHelper
import com.tt.dramatime.http.db.CheckOrderFailHelper.getCheckOrderFailBean
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.google.pay.GoogleBillHelper
import com.tt.dramatime.manager.google.pay.GoogleBillingListener
import com.tt.dramatime.manager.google.pay.GoogleBillingManager
import com.tt.dramatime.ui.activity.me.ContactUsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/21
 *    desc   : 项目通用 Dialog 布局封装
 */
class PaymentDialog {

    open class Builder<B : Builder<B>>(context: Context) : BaseDialog.Builder<B>(context),
        ToastAction {

        fun String.Companion.formatPrice(price: Long): String {
            return String.format(Locale.US, "%.2f", price.toDouble() / (1000 * 1000))
        }

        private val billProxy by lazy(LazyThreadSafetyMode.NONE) { GoogleBillHelper() }

        private var logger: AppEventsLogger = AppEventsLogger.newLogger(context)

        private var isCreateOrder = false
        private var mOrderSn = ""
        private var mProductId: String = ""
        private var mPrice: String = "0.00"

        var mSource: Int = 0
        var mDialogId: String? = null
        var mSourceId: String? = null

        var movieCode: String? = null
        var ep: Int? = null

        var listener: OnListener? = null

        fun onQuerySkuDetails(
            productList: MutableList<ProductListBean>, productType: String, type: Int = 0
        ) {
            billProxy.setBillingListener(billingListener)
                .onQuerySkuDetailsAsync(productList, productType, type)
        }

        fun queryHistory() {
            //google消费失败的补单
            GoogleBillingManager.billingClient?.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases.isNotEmpty()) {
                    showPurchasingDialog()
                    //循环调用消耗
                    for (purchase in purchases) {
                        purchase?.let {
                            Logger.e("onPurchasesUpdated purchases.data: $purchase")
                            billProxy.setBillingListener(billingListener).onConsumeAsync(it)
                        }
                    }
                } else {
                    getCheckOrderFailState()
                }
            }
        }

        @SingleClick(1000)
        fun onCreateOrder(
            movieCode: String? = null,
            ep: Int? = null,
            productListBean: ProductListBean,
            productType: String
        ) {
            if (getCheckOrderFailState() || isCreateOrder) return
            showPurchasingDialog()

            productListBean.apply {
                if (productDetails == null) {
                    onQuerySkuDetails(mutableListOf(this), productType, 1)
                    hideDialog()
                    return
                }
                isCreateOrder = true
                val productId = if (productType == SUBS) code else goodsCode
                mProductId = productId

                popupRecord(1)

                EasyHttp.post(getDialog()).api(
                    CreateOrderApi(movieCode, ep, mSource, mProductId, amount, currency)
                ).request(object : OnHttpListener<HttpData<String>> {
                    override fun onHttpSuccess(result: HttpData<String>?) {
                        val params = Bundle().apply {
                            Logger.e("checkout.mPrice：$mPrice")
                            putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "USD")  // 货币
                            putString(
                                AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product"
                            )  // 内容类型
                            putString(
                                AppEventsConstants.EVENT_PARAM_CONTENT_ID, mProductId
                            )  // 商品 ID
                            putInt(AppEventsConstants.EVENT_PARAM_NUM_ITEMS, 1)  // 商品数量
                            putDouble(
                                AppEventsConstants.EVENT_PARAM_VALUE_TO_SUM, mPrice.toDouble()
                            ) // 总金额
                        }

                        // 上报 Initiate Checkout 事件
                        logger.logEvent(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, params)

                        isCreateOrder = false
                        result?.getData()?.let { orderSn ->
                            this@Builder.mOrderSn = orderSn
                            billProxy.onOpenGooglePlay(productListBean.productDetails, orderSn)
                        }
                    }

                    override fun onHttpFail(throwable: Throwable?) {
                        isCreateOrder = false
                        hideDialog()
                        toast(throwable?.message)
                    }
                })
            }
        }

        private fun cancelOrder() {
            EasyHttp.post(getDialog()).api(CancelOrderApi(mOrderSn))
                .request(object : OnHttpListener<HttpData<Void>> {
                    override fun onHttpSuccess(result: HttpData<Void>?) {}
                    override fun onHttpFail(throwable: Throwable?) {}
                })
        }

        private fun getCheckOrderFailState(): Boolean {
            getCheckOrderFailBean()?.let {
                if (it.userid == UserProfileHelper.getUserId()) {
                    showPayFailedDialog(it)
                }
            }
            return getCheckOrderFailBean() != null && getCheckOrderFailBean()?.userid == UserProfileHelper.getUserId()
        }

        private fun showPayFailedDialog(it: CheckOrderFailBean? = null) {
            MessageDialog.Builder(getContext()).setTitleImage(R.drawable.dialog_hint_ic)
                .setCancelable(false).setMessage(R.string.restore_hint).setConfirm(R.string.retry)
                .setCancel(R.string.contact_us).setListener(object : MessageDialog.OnListener {
                    override fun onConfirm(dialog: BaseDialog?) {
                        it?.checkOrderApi?.apply {
                            val mCheckOrderApi = CheckOrderApi(
                                packageName, orderSn, purchaseToken, goodsCode
                            )
                            showPurchasingDialog()
                            checkOrder(mCheckOrderApi, true)
                        } ?: queryHistory()
                    }

                    override fun onCancel(dialog: BaseDialog?) {
                        ContactUsActivity.start(getContext())
                    }
                }).show()
        }

        fun queryCreateOrder(bean: ProductListBean, productDetails: ProductDetails) {
            if (productDetails.productType == SUBS) {
                productDetails.subscriptionOfferDetails?.let { subscriptionOfferDetails ->
                    subscriptionOfferDetails.forEach {
                        it.pricingPhases.pricingPhaseList.forEach { pricingPhase ->
                            pricingPhase.apply {
                                if (priceAmountMicros > 0.toLong() && priceCurrencyCode.isNotEmpty()) {
                                    bean.amount = String.formatPrice(priceAmountMicros)
                                    bean.currency = priceCurrencyCode
                                }
                            }
                        }
                    }
                }
            } else {
                productDetails.apply {
                    oneTimePurchaseOfferDetails?.apply {
                        bean.amount = String.formatPrice(priceAmountMicros)
                        bean.currency = priceCurrencyCode
                    }
                }
            }

            onCreateOrder(movieCode, ep, bean, productDetails.productType)
        }


        private val billingListener = object : GoogleBillingListener {

            /**
             * 产品查询成功
             */
            override fun onProductDetailsResult(
                productList: List<ProductDetails>?, bean: ProductListBean?
            ) {
                listener?.onProductDetailsResult(productList, bean)
            }

            /**
             * 购买监听
             */
            override fun onPurchasesUpdated(result: BillingResult?, purchases: List<Purchase?>?) {
                //https://developer.android.com/reference/com/android/billingclient/api/BillingClient.BillingResponseCode#FEATURE_NOT_SUPPORTED
                //错误码 OK = 0 USER_CANCELED = 1 SERVICE_UNAVAILABLE = 2 SERVICE_TIMEOUT = -3 SERVICE_DISCONNECTED = -1 ...
                Logger.e("onPurchasesUpdated Code: " + result?.responseCode)
                Logger.e("onPurchasesUpdated MESSAGE: " + result?.debugMessage)
                Logger.e("onPurchasesUpdated purchases: " + purchases?.size)
                if (result?.responseCode == BillingClient.BillingResponseCode.OK && purchases?.isNotEmpty() == true) {
                    // 记录购买事件
                    Logger.e("logPurchase.mPrice：$mPrice")
                    logger.logPurchase(BigDecimal(mPrice.toDouble()), // 购买金额
                        Currency.getInstance("USD"), // 货币类型
                        Bundle().apply {
                            putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
                            putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, mProductId)
                            putInt(AppEventsConstants.EVENT_PARAM_NUM_ITEMS, 1) // 购买数量
                        })

                    //循环调用消耗
                    for (purchase in purchases) {
                        purchase?.let {
                            Logger.e("onPurchasesUpdated purchases.data: $purchase")
                            billProxy.onConsumeAsync(it)
                        }
                    }
                } else {
                    if (result?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                        isCreateOrder = false
                        toastError(getString(R.string.purchase_canceled))
                        cancelOrder()
                    }
                    hideDialog()
                }
            }

            /**
             * 消费监听
             */
            override fun onConsumeStatus(
                purchaseToken: String, purchase: Purchase, status: Boolean
            ) {
                Logger.e("onConsumeStatus.purchaseToken:$purchaseToken productId:${purchase.products[0]} status:$status")
                Logger.e("onConsumeStatus.orderSn:$mOrderSn ,obfuscatedProfileId:${purchase.accountIdentifiers?.obfuscatedProfileId}")
                if (status) {
                    //去与后台验证。处理APP的页面逻辑， 比如充值后发放金币啥的~~~
                    purchase.apply {
                        val orderSn = accountIdentifiers?.obfuscatedProfileId.toString()
                        val mCheckOrderApi = CheckOrderApi(
                            packageName, orderSn, purchaseToken, products[0]
                        )
                        popupRecord(2)
                        checkOrder(mCheckOrderApi)
                    }
                } else {
                    hideDialog()
                    getDialog()?.lifecycleScope?.launch(Dispatchers.Main) {
                        showPayFailedDialog()
                    }
                }
            }
        }

        private fun checkOrder(mCheckOrderApi: CheckOrderApi, restore: Boolean = false) {
            EasyHttp.post(getDialog()).api(mCheckOrderApi)
                .request(object : OnHttpListener<HttpData<Void>> {
                    override fun onHttpSuccess(data: HttpData<Void>?) {
                        CheckOrderFailHelper.clearCheckOrderFailBean()
                        getUserInfo(restore)
                    }

                    override fun onHttpFail(throwable: Throwable?) {
                        hideDialog()
                        if (restore) {
                            toastError(getString(R.string.restore_failed_hint))
                        } else {
                            CheckOrderFailHelper.addCheckOrderFailBean(
                                CheckOrderFailBean(
                                    UserProfileHelper.getUserId(), mCheckOrderApi
                                )
                            )
                            getCheckOrderFailState()
                        }
                    }
                })
        }

        private fun getUserInfo(restore: Boolean) {
            EasyHttp.get(getDialog()).api(UserInfoApi())
                .request(object : OnHttpListener<HttpData<UserInfoApi.Bean?>> {
                    override fun onHttpSuccess(data: HttpData<UserInfoApi.Bean?>?) {
                        hideDialog()
                        data?.getData()?.apply {
                            UserProfileHelper.setProfile(this)
                        }
                        if (restore.not()) listener?.onPaymentSuccessful()
                    }

                    override fun onHttpFail(throwable: Throwable?) {
                        hideDialog()
                        toastError(throwable?.message)
                        if (restore.not()) listener?.onPaymentSuccessful()
                    }
                })
        }

        private fun popupRecord(type: Int) {
            if (mDialogId != null && mSourceId != null) {
                EasyHttp.post(getDialog()).api(PopupRecordApi(mDialogId!!, type, mSourceId!!))
                    .request(object : OnHttpListener<HttpData<PopupRecordApi.Bean?>> {
                        override fun onHttpSuccess(data: HttpData<PopupRecordApi.Bean?>?) {}
                        override fun onHttpFail(throwable: Throwable?) {}
                    })
            }
        }

        fun setPayButtonAnim(v: View) {
            // 创建放大动画
            val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.2f)
            val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.2f)
            scaleX.duration = 500
            scaleY.duration = 500

            val enlargeSet = AnimatorSet()
            enlargeSet.playTogether(scaleX, scaleY)

            // 创建缩小动画
            val scaleXBack = ObjectAnimator.ofFloat(v, "scaleX", 1.2f, 1f)
            val scaleYBack = ObjectAnimator.ofFloat(v, "scaleY", 1.2f, 1f)
            scaleXBack.duration = 500
            scaleYBack.duration = 500

            val shrinkSet = AnimatorSet()
            shrinkSet.playTogether(scaleXBack, scaleYBack)

            // 合并动画，创建循环效果
            val finalAnimatorSet = AnimatorSet()
            finalAnimatorSet.playSequentially(enlargeSet, shrinkSet)

            finalAnimatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    animator.start() // 动画结束时重新开始
                }
            })

            // 开始动画
            finalAnimatorSet.start()
        }

        fun setSelectedPrice(
            currencyTypeTv: TextView, moneyCycleTv: TextView, productListBean: ProductListBean?
        ) {
            var mFormattedPrice = ""
            productListBean?.productDetails?.apply {
                oneTimePurchaseOfferDetails?.apply {
                    mFormattedPrice = formattedPrice
                }
            }

            if (mFormattedPrice.isNotEmpty()) {
                Logger.e("mFormattedPrice: $mFormattedPrice")
                // 定义正则表达式模式，使用括号 () 捕获两部分
                val pattern = Regex("""(.+\$)([\d.]+)""")

                // 使用 find() 函数查找匹配的部分
                val matchResult = pattern.find(mFormattedPrice)

                Logger.e("matchResult: $matchResult")

                if (matchResult != null) {
                    val currency = matchResult.groups[1]?.value // 第一组匹配的货币符号部分
                    val amount = matchResult.groups[2]?.value   // 第二组匹配的金额部分
                    currencyTypeTv.text = currency
                    moneyCycleTv.text = amount
                } else {
                    currencyTypeTv.text = getString(R.string.us_money_text)
                    moneyCycleTv.text = productListBean?.price
                }
            } else {
                currencyTypeTv.text = getString(R.string.us_money_text)
                moneyCycleTv.text = productListBean?.price
            }
        }

        fun showPurchasingDialog() {
            (getActivity() as AppActivity).showDialog(getString(R.string.purchasing), 0)
        }

        fun showDialog() {
            (getActivity() as AppActivity).showDialog()
        }

        fun hideDialog() {
            (getActivity() as AppActivity).hideDialog()
        }

        interface OnListener {
            /**
             * 点击确定时回调
             */
            fun onProductDetailsResult(
                productList: List<ProductDetails>?, bean: ProductListBean?
            )

            fun onPaymentSuccessful()
        }
    }

}