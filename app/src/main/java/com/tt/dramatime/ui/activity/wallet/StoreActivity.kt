package com.tt.dramatime.ui.activity.wallet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import cn.iwgang.simplifyspan.SimplifySpanBuild
import cn.iwgang.simplifyspan.customspan.CustomClickableSpan
import cn.iwgang.simplifyspan.other.OnClickableSpanListener
import cn.iwgang.simplifyspan.unit.SpecialClickableUnit
import cn.iwgang.simplifyspan.unit.SpecialTextUnit
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.blankj.utilcode.util.ThreadUtils
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.gyf.immersionbar.ImmersionBar
import com.hjq.bar.TitleBar
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
import com.hjq.http.listener.OnHttpListener
import com.orhanobut.logger.Logger
import com.tt.base.BaseDialog
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.StoreActivityBinding
import com.tt.dramatime.http.api.CancelOrderApi
import com.tt.dramatime.http.api.CheckOrderApi
import com.tt.dramatime.http.api.CreateOrderApi
import com.tt.dramatime.http.api.HttpUrls
import com.tt.dramatime.http.api.RechargeApi
import com.tt.dramatime.http.api.RechargeApi.Bean.ProductListBean
import com.tt.dramatime.http.api.UserInfoApi
import com.tt.dramatime.http.bean.CheckOrderFailBean
import com.tt.dramatime.http.db.CheckOrderFailHelper
import com.tt.dramatime.http.db.CheckOrderFailHelper.getCheckOrderFailBean
import com.tt.dramatime.http.db.CommonConfigHelper
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.google.pay.GoogleBillHelper
import com.tt.dramatime.manager.google.pay.GoogleBillingListener
import com.tt.dramatime.manager.google.pay.GoogleBillingManager
import com.tt.dramatime.ui.activity.BrowserActivity
import com.tt.dramatime.ui.activity.me.ContactUsActivity
import com.tt.dramatime.ui.adapter.wallet.RechargeAdapter
import com.tt.dramatime.ui.adapter.wallet.SubscriptionsAdapter
import com.tt.dramatime.ui.dialog.MessageDialog
import com.tt.dramatime.util.eventbus.PaySuccessNotify
import com.tt.dramatime.util.eventbus.ShowLimitDialogNotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 商店
 * </pre>
 */
@SuppressLint("NotifyDataSetChanged")
class StoreActivity :
    BaseViewBindingActivity<StoreActivityBinding>({ StoreActivityBinding.inflate(it) }),
    OnClickableSpanListener {

    companion object {
        private const val KEY_MOVIE_CODE = "key.movieCode"
        private const val KEY_EP = "key.ep"
        fun start(context: Context, movieCode: String? = null, ep: Int? = null) {
            val intent = Intent(context, StoreActivity::class.java)
            intent.putExtra(KEY_MOVIE_CODE, movieCode)
            intent.putExtra(KEY_EP, ep)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig().navigationBarColor(R.color.color_232323)
            .statusBarDarkFont(false)
    }

    private val mSubscriptionsAdapter by lazy(LazyThreadSafetyMode.NONE) {
        SubscriptionsAdapter(mutableListOf())
    }

    private val mRechargeAdapter by lazy(LazyThreadSafetyMode.NONE) {
        RechargeAdapter(mutableListOf())
    }

    private val billProxy by lazy(LazyThreadSafetyMode.NONE) { GoogleBillHelper() }

    private var isCreateOrder = false
    private var mOrderSn = ""

    private var mSubscribeList: MutableList<ProductListBean>? = null
    private var mRechargeList: MutableList<ProductListBean>? = null

    private var mProductId: String = ""
    private var mPrice: String = "0.00"
    private lateinit var logger: AppEventsLogger

    private var isPay = false

    private var mMovieCode: String? = null
    private var mEp: Int? = null

    /**1单集解锁 2全集解锁*/
    private var unlockType = 0

    private fun String.Companion.formatPrice(price: Long): String {
        return String.format(Locale.US, "%.2f", price.toDouble() / (1000 * 1000))
    }

    override fun initView() {
        logger = AppEventsLogger.newLogger(this)

        mMovieCode = getString(KEY_MOVIE_CODE)
        mEp = getInt(KEY_EP)

        UserProfileHelper.apply {
            binding.storeInclude.balanceTv.text = getString(
                R.string.coins_bonus, getCoins().toString(), getBonus().toString()
            )
        }
        CommonConfigHelper.getRechargeTip()?.let {
            binding.storeInclude.storeHint.text = Html.fromHtml(it, FROM_HTML_MODE_LEGACY)
        }

        binding.storeInclude.subscriptionsRv.adapter = mSubscriptionsAdapter
        binding.storeInclude.rechargeRv.adapter = mRechargeAdapter

        mSubscriptionsAdapter.setOnItemClickListener { _, _, position ->
            if (isCreateOrder) return@setOnItemClickListener
            notifyDataSetChanged(position, true)
            mSubscriptionsAdapter.items[position].apply {
                mPrice = if (discountType != 0) discountPrice ?: "0.00" else price ?: "0.00"
                unlockType = 2
                onCreateOrder(this, BillingClient.ProductType.SUBS)
            }
        }

        mRechargeAdapter.setOnItemClickListener { _, _, position ->
            if (isCreateOrder) return@setOnItemClickListener
            notifyDataSetChanged(position, false)
            mRechargeAdapter.items[position].apply {
                mPrice = price ?: "0.00"
                unlockType = 1
                onCreateOrder(this, BillingClient.ProductType.INAPP)
            }
        }

        binding.storeInclude.feedbackTv.let { feedbackTv ->
            feedbackTv.text =
                SimplifySpanBuild().append(getString(R.string.store_need_help)).append(" ").append(
                    SpecialTextUnit(getString(R.string.store_feedback)).setClickableUnit(
                        SpecialClickableUnit(feedbackTv, this).setTag(R.string.store_feedback)
                            .showUnderline()
                    ).setTextColor(ContextCompat.getColor(this, R.color.white))
                ).build()
        }

        binding.storeInclude.protocolTv.let { protocolTv ->
            protocolTv.text = SimplifySpanBuild().append(
                SpecialTextUnit(getString(R.string.terms_service)).setClickableUnit(
                    SpecialClickableUnit(protocolTv, this).setTag(R.string.terms_service)
                        .showUnderline()
                ).setTextColor(ContextCompat.getColor(this, R.color.color_999999))
            ).append(" | ").append(
                SpecialTextUnit(getString(R.string.privacy_policy)).setClickableUnit(
                    SpecialClickableUnit(protocolTv, this).setTag(R.string.privacy_policy)
                        .showUnderline()
                ).setTextColor(ContextCompat.getColor(this, R.color.color_999999))
            ).append(" | ").append(
                SpecialTextUnit(getString(R.string.renewal_agreement)).setClickableUnit(
                    SpecialClickableUnit(protocolTv, this).setTag(R.string.renewal_agreement)
                        .showUnderline()
                ).setTextColor(ContextCompat.getColor(this, R.color.color_999999))
            ).build()
        }

        queryHistory()
    }

    private fun notifyDataSetChanged(position: Int, isSubscriptions: Boolean) {
        mSubscriptionsAdapter.items.forEachIndexed { index, subscriptionsListBean ->
            subscriptionsListBean.checkStatus = if (isSubscriptions) position == index else false
        }
        mRechargeAdapter.items.forEachIndexed { index, rechargeListBean ->
            rechargeListBean.checkStatus = if (isSubscriptions) false else position == index
        }
        mRechargeAdapter.notifyDataSetChanged()
        mSubscriptionsAdapter.notifyDataSetChanged()
    }

    override fun initData() {
        EasyHttp.get(this).api(RechargeApi())
            .request(object : HttpCallbackProxy<HttpData<RechargeApi.Bean>>(this) {
                override fun onHttpSuccess(result: HttpData<RechargeApi.Bean>?) {
                    result?.getData()?.apply {
                        showDialog()
                        subscribeList?.let { subscribeList ->
                            mSubscribeList = subscribeList
                            onQuerySkuDetails(subscribeList, BillingClient.ProductType.SUBS)
                        }

                        rechargeList?.let { rechargeList ->
                            mRechargeList = rechargeList
                            onQuerySkuDetails(rechargeList, BillingClient.ProductType.INAPP)
                        }
                    }
                }
            })
    }

    @SingleClick(1000)
    override fun onClick(tv: TextView?, clickableSpan: CustomClickableSpan) {
        when (clickableSpan.tag) {
            R.string.store_feedback -> ContactUsActivity.start(getContext())
            R.string.terms_service -> BrowserActivity.start(getContext(), HttpUrls.SERVICE_URL)
            R.string.privacy_policy -> BrowserActivity.start(getContext(), HttpUrls.PRIVACY_URL)
            R.string.renewal_agreement -> BrowserActivity.start(
                getContext(), HttpUrls.RENEWAL_AGREEMENT_URL
            )
        }
    }

    @SingleClick(1000)
    override fun onRightClick(titleBar: TitleBar?) {
        queryHistory()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPay.not() && mMovieCode == null) {
            Logger.e("商城关闭")
            EventBus.getDefault().post(ShowLimitDialogNotify(1, source = 6))
        }
        GoogleBillingManager.setBillingListener(null)
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
                showDialog(getString(R.string.purchasing), 0)
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

    /**
     * 创建订单
     */
    @SingleClick(1000)
    private fun onCreateOrder(productListBean: ProductListBean, productType: String) {
        if (getCheckOrderFailState()) return
        showDialog(getString(R.string.purchasing), 0)

        productListBean.apply {
            if (productDetails == null) {
                onQuerySkuDetails(mutableListOf(this), productType, 1)
                return
            }
            isCreateOrder = true
            val productId = if (productType == BillingClient.ProductType.SUBS) code else goodsCode
            mProductId = productId
            EasyHttp.post(this@StoreActivity).api(
                CreateOrderApi(mMovieCode, mEp, 1, productId, amount, currency)
            ).request(object : OnHttpListener<HttpData<String>> {
                override fun onHttpSuccess(result: HttpData<String>?) {
                    val params = Bundle().apply {
                        Logger.e("checkout.mPrice：$mPrice")
                        putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "USD")  // 货币
                        putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")  // 内容类型
                        putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, mProductId)  // 商品 ID
                        putInt(AppEventsConstants.EVENT_PARAM_NUM_ITEMS, 1)  // 商品数量
                        putDouble(
                            AppEventsConstants.EVENT_PARAM_VALUE_TO_SUM, mPrice.toDouble()
                        ) // 总金额
                    }

                    // 上报 Initiate Checkout 事件
                    logger.logEvent(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, params)

                    isCreateOrder = false
                    result?.getData()?.let { orderSn ->
                        this@StoreActivity.mOrderSn = orderSn
                        billProxy.onOpenGooglePlay(productDetails, orderSn)
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    isCreateOrder = false
                    hideDialog()
                    toastError(throwable?.message)
                }
            })
        }

    }

    private fun cancelOrder() {
        EasyHttp.post(this).api(CancelOrderApi(mOrderSn))
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
        MessageDialog.Builder(this).setTitleImage(R.drawable.dialog_hint_ic).setCancelable(false)
            .setMessage(R.string.restore_hint).setConfirm(R.string.retry)
            .setCancel(R.string.contact_us).setListener(object : MessageDialog.OnListener {
                override fun onConfirm(dialog: BaseDialog?) {
                    it?.checkOrderApi?.apply {
                        val mCheckOrderApi = CheckOrderApi(
                            packageName, orderSn, purchaseToken, goodsCode
                        )
                        showDialog()
                        checkOrder(mCheckOrderApi, true)
                    } ?: queryHistory()
                }

                override fun onCancel(dialog: BaseDialog?) {
                    ContactUsActivity.start(getContext())
                }
            }).show()
    }

    private fun queryCreateOrder(bean: ProductListBean, productDetails: ProductDetails) {
        if (productDetails.productType == BillingClient.ProductType.SUBS) {
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
         * 产品查询结果
         */
        override fun onProductDetailsResult(
            productList: List<ProductDetails>?, bean: ProductListBean?
        ) {
            ThreadUtils.runOnUiThread {
                if (productList != null && bean != null) {
                    queryCreateOrder(bean, productList[0])
                } else if (productList.isNullOrEmpty() && bean != null) {
                    toast(R.string.purchase_failed)
                } else if (productList.isNullOrEmpty()) {
                    mSubscriptionsAdapter.submitList(mSubscribeList)
                    mRechargeAdapter.submitList(mRechargeList)
                    binding.storeInclude.fixedGp.visibility = View.VISIBLE
                } else {
                    productList.forEach {
                        if (it.productType == BillingClient.ProductType.SUBS) {
                            mSubscribeList?.forEach { bean ->
                                if (it.productId == bean.code) bean.productDetails = it
                            }
                        } else {
                            mRechargeList?.forEach { bean ->
                                if (it.productId == bean.goodsCode) bean.productDetails = it
                            }
                        }
                    }

                    mSubscriptionsAdapter.submitList(mSubscribeList)
                    mRechargeAdapter.submitList(mRechargeList)
                    binding.storeInclude.fixedGp.visibility = View.VISIBLE
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
        override fun onConsumeStatus(purchaseToken: String, purchase: Purchase, status: Boolean) {
            Logger.e("onConsumeStatus.purchaseToken:$purchaseToken productId:${purchase.products[0]} status:$status")
            Logger.e("onConsumeStatus.orderSn:$mOrderSn ,obfuscatedProfileId:${purchase.accountIdentifiers?.obfuscatedProfileId}")
            Logger.e("onConsumeStatus.originalJson:${purchase.originalJson}")

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
                lifecycleScope.launch(Dispatchers.Main) {
                    showPayFailedDialog()
                }
            }
        }
    }

    private fun checkOrder(mCheckOrderApi: CheckOrderApi, restore: Boolean = false) {
        EasyHttp.post(this@StoreActivity).api(mCheckOrderApi)
            .request(object : OnHttpListener<HttpData<Void>> {
                override fun onHttpSuccess(data: HttpData<Void>?) {
                    isPay = true
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
        EasyHttp.get(this).api(UserInfoApi())
            .request(object : OnHttpListener<HttpData<UserInfoApi.Bean?>> {
                override fun onHttpSuccess(data: HttpData<UserInfoApi.Bean?>) {
                    data.getData()?.apply {
                        wallet?.apply {
                            binding.storeInclude.balanceTv.text = getString(
                                R.string.coins_bonus, balance.toString(), integral.toString()
                            )
                        }
                        UserProfileHelper.setProfile(this)
                    }
                    hideDialog()
                    toastSuccess(getString(R.string.purchase_successful))
                    if (mMovieCode != null && mEp != null) {
                        EventBus.getDefault().post(
                            PaySuccessNotify(mEp!!, unlockType, false)
                        )
                        finish()
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    hideDialog()
                    toastError(throwable?.message)
                }
            })
    }

}