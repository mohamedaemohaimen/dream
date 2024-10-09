package com.tt.dramatime.manager.google.pay

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.orhanobut.logger.Logger
import com.tt.base.action.HandlerAction
import com.tt.dramatime.app.AppApplication


/**
 * @author wiggins
 * Date: 2023/12/28
 * Desc:Google 支付管理
 */
object GoogleBillingManager : HandlerAction {

    val TAG: String = GoogleBillingManager::class.java.simpleName

    var billingClient: BillingClient? = null
        private set
    private var billingListener: GoogleBillingListener? = null

    /**
     * 是否准备好了
     *
     * @return
     */
    val isReady: Boolean
        get() = !(null == billingClient || !billingClient!!.isReady)


    /**
     * 创建支付客户端
     */
    fun createClient() {
        billingClient = BillingClient.newBuilder(AppApplication.appContext)
            .setListener { billingResult, purchases ->
                billingListener?.onPurchasesUpdated(billingResult, purchases)
            }.enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            ).build()
        //启动支付连接
        startConn()
    }

    var reconnectMilliseconds: Long = 1000
    const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS: Long = 20 * 1000

    /**
     * 启动连接
     */
    private fun startConn() {
        if (isReady) {
            return
        }
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Logger.e(TAG + "连接成功，可以开始操作了~~~")
                }
            }

            override fun onBillingServiceDisconnected() {
                //连接失败。 可以尝试调用 startConnection 重新建立连接
                postDelayed({ startConn() }, reconnectMilliseconds)
                reconnectMilliseconds =
                    (reconnectMilliseconds * 2).coerceAtMost(RECONNECT_TIMER_MAX_TIME_MILLISECONDS)
                Logger.e(TAG + "连接失败")
            }
        })
    }

    /**
     * 结束连接
     */
    fun endConn() {
        billingClient?.endConnection()
        billingListener = null
    }

    /**
     * 添加监听事件
     */
    fun setBillingListener(billingListener: GoogleBillingListener?) {
        this.billingListener = billingListener
    }

}
