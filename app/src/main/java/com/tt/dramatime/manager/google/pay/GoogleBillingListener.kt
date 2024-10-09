package com.tt.dramatime.manager.google.pay

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.tt.dramatime.http.api.RechargeApi.Bean.ProductListBean


/**
 * @author wiggins
 * Date: 2023/12/28
 * Desc:监听
 */
interface GoogleBillingListener {

    /**
     * 查询商品详情结果
     *
     * @param productList
     */
    fun onProductDetailsResult(productList: List<ProductDetails>?, bean: ProductListBean?)

    /**
     * 购买监听
     *
     * @param result
     * @param purchases
     */
    fun onPurchasesUpdated(result: BillingResult?, purchases: List<Purchase?>?)

    /**
     * 商品消费成功
     *
     * @param purchaseToken 相当于transactionReceipt
     * @param purchase 包含 productId transactionId
     * @param status 是否消费成功
     */
    fun onConsumeStatus(purchaseToken: String, purchase: Purchase, status: Boolean)
}
