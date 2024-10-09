package com.tt.dramatime.ui.dialog.player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType.SUBS
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ThreadUtils
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.orhanobut.logger.Logger
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R
import com.tt.dramatime.action.ToastAction
import com.tt.dramatime.app.AppActivity
import com.tt.dramatime.databinding.VipExpiredDialogBinding
import com.tt.dramatime.http.api.CancelOrderApi
import com.tt.dramatime.http.api.CheckOrderApi
import com.tt.dramatime.http.api.CreateOrderApi
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
import com.tt.dramatime.ui.dialog.MessageDialog
import com.tt.dramatime.util.eventbus.PaySuccessNotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc :商城弹窗
 * </pre>
 */
class VipExpiredDialog {
    @SuppressLint("SetTextI18n")
    class Builder(context: Context, val movieCode: String, val currentEpisode: Int) :
        BaseDialog.Builder<Builder>(context), BaseDialog.OnShowListener,
        BaseDialog.OnDismissListener, ToastAction {

        private val binding = VipExpiredDialogBinding.inflate(LayoutInflater.from(context))

        private val billProxy by lazy(LazyThreadSafetyMode.NONE) { GoogleBillHelper() }

        private var isCreateOrder = false
        private var mOrderSn = ""

        /**1单集解锁 2全集解锁 3直接播放*/
        private var unlockType = 0

        private var mProductId: String = ""
        private var mPrice: String = "0.00"
        private var logger: AppEventsLogger
        private val mProductListBean = ProductListBean()

        var isPay = false

        private fun String.Companion.formatPrice(price: Long): String {
            return String.format(Locale.US, "%.2f", price.toDouble() / (1000 * 1000))
        }

        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_BOTTOM)
            setGravity(Gravity.CENTER)
            setCancelable(false)
            addOnShowListener(this)
            setWidth(ConvertUtils.dp2px(303f))
            setHeight(ConvertUtils.dp2px(276f))

            logger = AppEventsLogger.newLogger(getContext())

            setOnClickListener(binding.closeIv, binding.renewTv)

            UserProfileHelper.apply {
                val code = getSubscribeType()
                if (code != null) {
                    binding.titleTv.text = getString(
                        when {
                            code.contains("week") -> R.string.weekly_vip
                            code.contains("month") -> R.string.monthly_vip
                            code.contains("year") -> R.string.annual_vip
                            else -> R.string.weekly_vip
                        }
                    ) + " " + getString(R.string.expired)
                }
            }
        }


        override fun onClick(view: View) {
            when (view) {
                binding.closeIv -> {
                    dismiss()
                    getActivity()?.finish()
                }

                binding.renewTv -> onCreateOrder(mProductListBean, SUBS)
            }
        }

        override fun onShow(dialog: BaseDialog?) {
            initData()
            queryHistory()
        }

        override fun onDismiss(dialog: BaseDialog?) {
            GoogleBillingManager.setBillingListener(null)
        }

        fun initData() {
            UserProfileHelper.apply {
                val list = mutableListOf<ProductListBean>()
                mProductListBean.code = getSubscribeType().toString()
                list.add(mProductListBean)
                onQuerySkuDetails(list, SUBS)
            }
        }

        private fun onQuerySkuDetails(
            productList: MutableList<ProductListBean>, productType: String, type: Int = 0
        ) {
            billProxy.setBillingListener(billingListener)
                .onQuerySkuDetailsAsync(productList, productType, type)
        }

        private fun queryHistory() {
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
        private fun onCreateOrder(productListBean: ProductListBean, productType: String) {
            if (getCheckOrderFailState()) return
            showPurchasingDialog()
            unlockType = 3

            productListBean.apply {
                if (productDetails == null) {
                    onQuerySkuDetails(mutableListOf(this), productType, 1)
                    return
                }
                isCreateOrder = true
                val productId = code
                mProductId = productId

                EasyHttp.post(getDialog()).api(
                    CreateOrderApi(movieCode, currentEpisode, 4, productId, amount, currency)
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

        private fun queryCreateOrder(bean: ProductListBean, productDetails: ProductDetails) {
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

            onCreateOrder(bean, productDetails.productType)
        }

        private val billingListener = object : GoogleBillingListener {

            /**
             * 产品查询成功
             */
            override fun onProductDetailsResult(
                productList: List<ProductDetails>?, bean: ProductListBean?
            ) {
                ThreadUtils.runOnUiThread {
                    if (productList != null && bean != null) {
                        queryCreateOrder(bean, productList[0])
                    } else if (productList.isNullOrEmpty() && bean != null) {
                        toast(R.string.purchase_failed)
                    } else if (productList?.isNotEmpty() == true) {
                        productList.forEach {
                            if (it.productType == SUBS) {
                                setProductAmount(it)
                            }
                        }
                    }
                    hideDialog()
                }
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

        private fun setProductAmount(it: ProductDetails?) {
            mProductListBean.apply {
                it?.apply {
                    productDetails = it
                    subscriptionOfferDetails?.let { subscriptionOfferDetails ->
                        subscriptionOfferDetails.forEach {
                            it.pricingPhases.pricingPhaseList.forEach { pricingPhase ->
                                pricingPhase.apply {
                                    if (priceAmountMicros > 0.toLong() && priceCurrencyCode.isNotEmpty()) {
                                        amount = String.formatPrice(priceAmountMicros)
                                        currency = priceCurrencyCode
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun checkOrder(mCheckOrderApi: CheckOrderApi, restore: Boolean = false) {
            EasyHttp.post(getDialog()).api(mCheckOrderApi)
                .request(object : OnHttpListener<HttpData<Void>> {
                    override fun onHttpSuccess(data: HttpData<Void>?) {
                        CheckOrderFailHelper.clearCheckOrderFailBean()
                        getUserInfo()
                    }

                    override fun onHttpFail(throwable: Throwable?) {
                        hideDialog()
                        if (restore) {
                            toastError(getString(R.string.restore_failed_hint))
                        } else {
                            CheckOrderFailHelper.addCheckOrderFailBean(
                                CheckOrderFailBean(UserProfileHelper.getUserId(), mCheckOrderApi)
                            )
                            getCheckOrderFailState()
                        }
                    }
                })
        }

        private fun getUserInfo() {
            EasyHttp.get(getDialog()).api(UserInfoApi())
                .request(object : OnHttpListener<HttpData<UserInfoApi.Bean?>> {
                    override fun onHttpSuccess(data: HttpData<UserInfoApi.Bean?>?) {
                        hideDialog()
                        data?.getData()?.apply {
                            UserProfileHelper.setProfile(this)
                            EventBus.getDefault().post(PaySuccessNotify(currentEpisode, unlockType))
                            isPay = true
                            //0为补单情况 不关闭弹窗
                            if (unlockType != 0) {
                                dismiss()
                            }
                        }
                    }

                    override fun onHttpFail(throwable: Throwable?) {
                        hideDialog()
                        toastError(throwable?.message)
                    }
                })
        }

        private fun showPurchasingDialog() {
            (getActivity() as AppActivity).showDialog(getString(R.string.purchasing), 0)
        }

        private fun showDialog() {
            (getActivity() as AppActivity).showDialog()
        }

        private fun hideDialog() {
            (getActivity() as AppActivity).hideDialog()
        }
    }
}