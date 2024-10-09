package com.tt.dramatime.ui.adapter.wallet

import androidx.core.content.ContextCompat
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.TransactionRecordsItemBinding
import com.tt.dramatime.http.api.TransactionHistoryApi.Bean.ListBean

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 交易记录适配器
 * </pre>
 */
class TransactionRecordsAdapter(dataList: MutableList<ListBean>) :
    BaseVBQuickAdapter<TransactionRecordsItemBinding, ListBean>(dataList) {

    override fun onBindViewHolder(
        holder: BaseVBHolder<TransactionRecordsItemBinding>, position: Int, item: ListBean?
    ) {
        holder.binding.apply {
            item?.apply {
                recordsTitle.text = action
                recordsTime.text = createTime
                variableQuantityTv.text = context.getString(
                    if (type == 0) R.string.add_coins else R.string.remove_coins, tokenNums
                )
                variableQuantityTv.setTextColor(
                    ContextCompat.getColor(
                        context, if (type == 0) R.color.color_C640FF else R.color.color_23E1FF
                    )
                )
            }
        }

    }

}