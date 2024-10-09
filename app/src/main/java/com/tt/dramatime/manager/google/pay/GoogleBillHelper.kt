package com.tt.dramatime.manager.google.pay

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.google.common.collect.ImmutableList
import com.orhanobut.logger.Logger
import com.tt.dramatime.http.api.RechargeApi.Bean.ProductListBean
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.manager.ActivityManager
import com.tt.dramatime.ui.activity.HomeActivity


/**
 * @author wiggins
 * Date: 2023/12/28
 * Desc:支付的具体操作
 * 1.查询
 * 2.购买
 * 3.消费
 */
class GoogleBillHelper {

    companion object {
        val TAG: String = GoogleBillHelper::class.java.simpleName
    }

    private var billingListener: GoogleBillingListener? = null

    /***
     * 设置接口监听
     *
     * @param billingListener : 接口监听
     */
    fun setBillingListener(billingListener: GoogleBillingListener): GoogleBillHelper {
        if (this.billingListener == null) {
            this.billingListener = billingListener
        }
        return this
    }

    /**
     * 查询商品详情
     *
     * @param productList 商品列表 。里面的商品对应Google 后台的
     * @param type 0 代表正常查询 1 查询购买
     * BillingClient.ProductType.INAPP（一次性商品）
     * BillingClient.ProductType.SUBS（订阅）
     */
    fun onQuerySkuDetailsAsync(productList: List<ProductListBean>, productType: String, type: Int) {
        val mProductListBean: ProductListBean? = if (type == 1) productList[0] else null

        if (productList.isEmpty() || !GoogleBillingManager.isReady) {
            billingListener?.onProductDetailsResult(null, mProductListBean)
            Logger.e("$TAG onQuerySkuDetailsAsync Google 支付服务还未准备")
            return
        }

        val builder = ImmutableList.builder<Product>()
        productList.forEach {
            builder.add(
                Product.newBuilder()
                    .setProductId(if (productType == BillingClient.ProductType.SUBS) it.code else it.goodsCode)
                    .setProductType(productType).build()
            )
        }

        val mProductList = builder.build()

        /*val productList = ImmutableList.of(
            Product.newBuilder().setProductId(productIds).setProductType(productType).build(),
        )*/

        val params = QueryProductDetailsParams.newBuilder().setProductList(mProductList).build()

        GoogleBillingManager.billingClient?.queryProductDetailsAsync(params) { billingResult, list ->
            if (BillingClient.BillingResponseCode.OK == billingResult.responseCode) {
                mProductListBean?.productDetails = list[0]
                billingListener?.onProductDetailsResult(list, mProductListBean)
            } else {
                billingListener?.onProductDetailsResult(null, mProductListBean)
                Logger.e("$TAG onQuerySkuDetailsAsync code : " + billingResult.responseCode + " message : " + billingResult.debugMessage)
                when (billingResult.responseCode) {
                    //不支持功能
                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {}
                    //服务未连接
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {}
                    //取消
                    BillingClient.BillingResponseCode.USER_CANCELED -> {}
                    //服务不可用
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {}
                    //购买不可用
                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {}
                    //商品不存在
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {}
                    //提供给 API 的无效参数
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {}
                    //错误
                    BillingClient.BillingResponseCode.ERROR -> {}
                    //未消耗掉
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {}
                    //不可购买
                    BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {}
                }
            }
        }
    }

    /**
     * 打开支付面板
     *
     * @param details
     */
    fun onOpenGooglePlay(details: ProductDetails?, orderSn: String) {
        Logger.e("$TAG onOpenGooglePlay: ${details.toString()}")
        if (null == details) {
            return
        }

        //根据自己的判断是否是连续包月还是一次性消费连续包月需要添加setOfferToken不加不能调谷歌支付面板
        //就是一次性商品是一个商品连续包月的话里面有优惠卷
        val params: ImmutableList<ProductDetailsParams>? =
            if (details.productType == BillingClient.ProductType.SUBS) {
                //添加购买数据
                val productDetailsParams =
                    ProductDetailsParams.newBuilder().setProductDetails(details)
                        .setOfferToken(details.subscriptionOfferDetails!![0].offerToken).build()
                ImmutableList.of(productDetailsParams)
            } else {
                val productDetailsParams =
                    ProductDetailsParams.newBuilder().setProductDetails(details).build()
                ImmutableList.of(productDetailsParams)
            }

        val billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(params!!)
            //订单号
            .setObfuscatedProfileId(orderSn)
            //用户id，用于关联到对应的用户，发放道具时使用
            .setObfuscatedAccountId(UserProfileHelper.profile.userId.toString()).build()

        //唤起google支付页面 并获得响应code 码
        val responseCode: Int? =
            ActivityManager.getInstance().getActivity(HomeActivity::class.java)?.let {
                GoogleBillingManager.billingClient?.launchBillingFlow(
                    it, billingFlowParams
                )?.responseCode
            }
        Logger.e("$TAG onOpenGooglePlay: $responseCode")

        //成功换起
        if (BillingClient.BillingResponseCode.OK == responseCode) {
            //添加购买监听
            GoogleBillingManager.setBillingListener(billingListener)
        }
    }

    /**
     * 消费商品
     * 对于购买类型的商品需要手动调用一次消费方法 （目的：用户可以再次购买此商品）
     *
     * @param purchase
     */
    fun onConsumeAsync(purchase: Purchase) {
        if (!GoogleBillingManager.isReady) {
            return
        }

        val consumeParams =
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()

        GoogleBillingManager.billingClient?.consumeAsync(consumeParams) { billingResult: BillingResult, purchaseToken: String ->
            val responseCode = billingResult.responseCode
            //BillingResponseCode.OK 0为一次性消费 5为连续订阅
            billingListener?.onConsumeStatus(
                purchaseToken, purchase, responseCode == 0 || responseCode == 5
            )
            Logger.e("$TAG onConsumeAsync 消费结果 code : " + billingResult.responseCode + " message : " + billingResult.debugMessage)
        }
    }

}
