package com.tt.dramatime.ui.activity.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.chad.library.adapter4.BaseQuickAdapter
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.tt.dramatime.R
import com.tt.dramatime.app.CommonListActivity
import com.tt.dramatime.http.api.TransactionHistoryApi
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.ui.adapter.wallet.TransactionRecordsAdapter

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 交易记录
 * </pre>
 */
class TransactionRecordsActivity : CommonListActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, TransactionRecordsActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private val mTransactionRecordsAdapter by lazy {
        TransactionRecordsAdapter(mutableListOf())
    }

    override fun getAdapter(): BaseQuickAdapter<*, *> {
        return mTransactionRecordsAdapter
    }

    override fun initView() {
        super.initView()
        setTitle(R.string.transaction_history)
    }

    override fun requestData(page: Int) {
        EasyHttp.get(this).api(TransactionHistoryApi(page))
            .request(object : OnHttpListener<HttpData<TransactionHistoryApi.Bean?>> {
                override fun onHttpSuccess(data: HttpData<TransactionHistoryApi.Bean?>?) {
                    data?.getData()?.list?.let {
                        successData(it)
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    throwable?.let { failure(throwable) }
                }
            })
    }

    override fun getEmptyDataTip(): String {
        return getString(R.string.records_hint)
    }

}