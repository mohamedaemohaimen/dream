package com.tt.dramatime.ui.dialog.player

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType.SUBS
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.blankj.utilcode.util.NotificationUtils.areNotificationsEnabled
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
import com.tt.dramatime.databinding.StoreDialogBinding
import com.tt.dramatime.http.api.CancelOrderApi
import com.tt.dramatime.http.api.CheckOrderApi
import com.tt.dramatime.http.api.CreateOrderApi
import com.tt.dramatime.http.api.RechargeApi
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
import com.tt.dramatime.ui.activity.wallet.StoreActivity
import com.tt.dramatime.ui.adapter.wallet.RechargeAdapter
import com.tt.dramatime.ui.adapter.wallet.SubscriptionsAdapter
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
class StoreDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder(
        context: Context,
        private val mMovieCode: String,
        private val mCurrentEpisode: Int,
        private val allEpisode: Int,
        type: Int
    ) : BaseDialog.Builder<Builder>(context), BaseDialog.OnShowListener,
        BaseDialog.OnDismissListener, ToastAction {

        private val binding = StoreDialogBinding.inflate(LayoutInflater.from(context))

        private val mSubscriptionsAdapter by lazy(LazyThreadSafetyMode.NONE) {
            SubscriptionsAdapter(mutableListOf())
        }
        private val mRechargeAdapter by lazy(LazyThreadSafetyMode.NONE) {
            RechargeAdapter(mutableListOf())
        }

        private val billProxy by lazy(LazyThreadSafetyMode.NONE) { GoogleBillHelper() }

        private var isCreateOrder = false
        private var mOrderSn = ""

        private var epRelationId: String? = null

        /**1单集解锁 2全集解锁*/
        private var unlockType = 0

        private var mSubscribeList: MutableList<ProductListBean>? = null
        private var mRechargeList: MutableList<ProductListBean>? = null
        private var mUnlockBean: ProductListBean? = null
        private var mFirstPurchaseStatus: Boolean? = null

        private var mProductId: String = ""
        private var mPrice: String = "0.00"
        private var logger: AppEventsLogger

        var isPay = false

        private fun String.Companion.formatPrice(price: Long): String {
            return String.format(Locale.US, "%.2f", price.toDouble() / (1000 * 1000))
        }

        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_BOTTOM)
            setGravity(Gravity.BOTTOM)
            addOnShowListener(this)
            setWidth(WindowManager.LayoutParams.MATCH_PARENT)
            setHeight(dp2px(if (type == 0) 622f else 375f))

            logger = AppEventsLogger.newLogger(getContext())

            setOnClickListener(
                binding.arrowBottomIv, binding.storeInclude.epUnlockCl, binding.viewMoreTv
            )

            UserProfileHelper.apply {
                binding.storeInclude.balanceTv.text = getString(
                    R.string.coins_bonus, getCoins().toString(), getBonus().toString()
                )
            }

            binding.storeInclude.fixedGp.visibility = View.GONE

            binding.storeInclude.subscriptionsRv.adapter = mSubscriptionsAdapter
            binding.storeInclude.rechargeRv.adapter = mRechargeAdapter

            mSubscriptionsAdapter.setOnItemClickListener { _, _, position ->
                if (isCreateOrder) return@setOnItemClickListener
                notifyDataSetChanged(position, true)
                mSubscriptionsAdapter.items[position].apply {
                    mPrice = if (discountType != 0) discountPrice ?: "0.00" else price ?: "0.00"
                    this@Builder.unlockType = 2
                    onCreateOrder(this, SUBS)
                }
            }

            mRechargeAdapter.setOnItemClickListener { _, _, position ->
                if (isCreateOrder) return@setOnItemClickListener
                notifyDataSetChanged(position, false)
                mRechargeAdapter.items[position].apply {
                    mPrice = price ?: "0.00"
                    this@Builder.unlockType = 1
                    onCreateOrder(this, BillingClient.ProductType.INAPP)
                }
            }
        }

        private fun notifyDataSetChanged(position: Int, isSubscriptions: Boolean) {
            mSubscriptionsAdapter.items.forEachIndexed { index, subscriptionsListBean ->
                subscriptionsListBean.checkStatus =
                    if (isSubscriptions) position == index else false
            }
            mRechargeAdapter.items.forEachIndexed { index, rechargeListBean ->
                rechargeListBean.checkStatus = if (isSubscriptions) false else position == index
            }
            mRechargeAdapter.notifyDataSetChanged()
            mSubscriptionsAdapter.notifyDataSetChanged()
            binding.storeInclude.selectIv.visibility =
                if (position == -1) View.VISIBLE else View.GONE
        }

        override fun onClick(view: View) {
            when (view) {
                binding.arrowBottomIv -> dismiss()
                binding.viewMoreTv -> StoreActivity.start(getContext(), mMovieCode, mCurrentEpisode)
                binding.storeInclude.epUnlockCl -> {
                    mUnlockBean?.apply {
                        mPrice = price ?: "0.00"
                        this@Builder.unlockType = 2
                        notifyDataSetChanged(-1, false)
                        onCreateOrder(this, BillingClient.ProductType.INAPP)
                    }
                }
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
            EasyHttp.get(getDialog()).api(RechargeApi(true, mMovieCode, mCurrentEpisode))
                .request(object : OnHttpListener<HttpData<RechargeApi.Bean>> {
                    override fun onHttpSuccess(result: HttpData<RechargeApi.Bean>?) {
                        showDialog()
                        result?.getData()?.apply {
                            subscribeList?.apply {
                                mSubscribeList = this
                                onQuerySkuDetails(this, SUBS)
                            }

                            unlock?.apply {
                                mUnlockBean = this
                                binding.storeInclude.selectIv.visibility =
                                    if (checkStatus) View.VISIBLE else View.GONE
                            }

                            rechargeList?.apply {
                                mRechargeList = this
                                val list = mutableListOf<ProductListBean>()
                                mUnlockBean?.let {
                                    list.add(it)
                                    it.originalCode?.let { originalCode ->
                                        val mProductListBean = ProductListBean()
                                        mProductListBean.goodsCode = originalCode
                                        list.add(mProductListBean)
                                    }
                                }
                                list.addAll(this)
                                onQuerySkuDetails(list, BillingClient.ProductType.INAPP)
                            }

                            //动态调整商品类型顺序
                            val constraintSet = ConstraintSet()
                            constraintSet.clone(binding.storeInclude.storeCl)

                            when (topType) {
                                1 -> {
                                    constraintSet.connect(
                                        R.id.recharge_rv,
                                        ConstraintSet.TOP,
                                        ConstraintSet.PARENT_ID,
                                        ConstraintSet.TOP,
                                        0
                                    )

                                    constraintSet.connect(
                                        R.id.subscriptions_rv,
                                        ConstraintSet.TOP,
                                        R.id.recharge_rv,
                                        ConstraintSet.BOTTOM,
                                        0
                                    )
                                }

                                2 -> {
                                    constraintSet.connect(
                                        R.id.ep_unlock_cl,
                                        ConstraintSet.TOP,
                                        ConstraintSet.PARENT_ID,
                                        ConstraintSet.TOP,
                                        0
                                    )

                                    constraintSet.connect(
                                        R.id.subscriptions_rv,
                                        ConstraintSet.TOP,
                                        R.id.ep_unlock_cl,
                                        ConstraintSet.BOTTOM,
                                        dp2px(15f)
                                    )

                                    constraintSet.connect(
                                        R.id.recharge_rv,
                                        ConstraintSet.TOP,
                                        R.id.subscriptions_rv,
                                        ConstraintSet.BOTTOM,
                                        0
                                    )
                                }
                            }

                            constraintSet.applyTo(binding.storeInclude.storeCl)

                            mFirstPurchaseStatus = firstPurchaseStatus
                        }
                    }

                    override fun onHttpFail(throwable: Throwable?) {
                        toast(throwable?.message)
                    }
                })
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

            productListBean.apply {
                if (productDetails == null) {
                    onQuerySkuDetails(mutableListOf(this), productType, 1)
                    return
                }
                isCreateOrder = true
                val productId = if (productType == SUBS) code else goodsCode
                mProductId = productId

                EasyHttp.post(getDialog()).api(
                    CreateOrderApi(mMovieCode, mCurrentEpisode, 0, productId, amount, currency)
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
                    } else if (productList.isNullOrEmpty()) {
                        mSubscriptionsAdapter.submitList(mSubscribeList)
                        mRechargeAdapter.submitList(mRechargeList)
                    } else {
                        productList.forEach {
                            if (it.productType == SUBS) {
                                mSubscribeList?.forEach { bean ->
                                    if (it.productId == bean.code) bean.productDetails = it
                                }
                            } else {
                                mUnlockBean?.apply {
                                    if (it.productId == goodsCode) setUnlockEpUi(it)
                                    if (it.productId == originalCode) {
                                        it.oneTimePurchaseOfferDetails?.apply {
                                            binding.storeInclude.apply {
                                                originalPriceTv.paintFlags =
                                                    originalPriceTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                                originalPriceTv.text = formattedPrice
                                            }
                                        }
                                    }
                                }

                                mRechargeList?.forEach { bean ->
                                    if (it.productId == bean.goodsCode) bean.productDetails = it
                                }
                            }
                        }
                        mSubscriptionsAdapter.submitList(mSubscribeList)
                        mRechargeAdapter.submitList(mRechargeList)
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

        private fun setUnlockEpUi(it: ProductDetails?) {
            mUnlockBean?.apply {
                var mFormattedPrice = ""
                it?.apply {
                    productDetails = it
                    oneTimePurchaseOfferDetails?.apply {
                        amount = String.formatPrice(priceAmountMicros)
                        currency = priceCurrencyCode
                        mFormattedPrice = formattedPrice
                    }
                }

                binding.storeInclude.apply {
                    epUnlockCl.visibility = View.VISIBLE
                    epRelationId = id
                    cornerTv.visibility = if (corner.isNullOrEmpty()) View.GONE else View.VISIBLE
                    cornerTv.text = corner
                    allEpTv.text = getString(
                        R.string.unlock_all_episodes, allEpisode
                    )
                    epPriceTv.text = mFormattedPrice.ifEmpty {
                        getContext().getString(R.string.us_money, price)
                    }
                }
            }
        }

        private fun checkOrder(mCheckOrderApi: CheckOrderApi, restore: Boolean = false) {
            EasyHttp.post(getDialog()).api(mCheckOrderApi)
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
            EasyHttp.get(getDialog()).api(UserInfoApi())
                .request(object : OnHttpListener<HttpData<UserInfoApi.Bean?>> {
                    override fun onHttpSuccess(data: HttpData<UserInfoApi.Bean?>?) {
                        hideDialog()
                        data?.getData()?.apply {
                            wallet?.apply {
                                binding.storeInclude.balanceTv.text = getString(
                                    R.string.coins_bonus, balance.toString(), integral.toString()
                                )
                            }
                            UserProfileHelper.setProfile(this)
                            val showNoticeDialog =
                                mFirstPurchaseStatus == true && areNotificationsEnabled().not()
                            EventBus.getDefault().post(
                                PaySuccessNotify(mCurrentEpisode, unlockType, showNoticeDialog)
                            )
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