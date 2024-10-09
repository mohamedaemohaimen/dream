package com.tt.dramatime.ui.adapter.bonus

import androidx.core.content.ContextCompat
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.BonusCheckInItemBinding
import com.tt.dramatime.http.api.SignTaskApi.Bean.ListBean
import com.tt.dramatime.util.GlideUtils

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 签到
 * </pre>
 */
class BonusCheckInAdapter(dataList: MutableList<ListBean>) :
    BaseVBQuickAdapter<BonusCheckInItemBinding, ListBean>(dataList) {


    override fun onBindViewHolder(
        holder: BaseVBHolder<BonusCheckInItemBinding>, position: Int, item: ListBean?
    ) {
        item?.apply {
            holder.binding.apply {
                addCoinTv.setTextColor(ContextCompat.getColor(context, R.color.color_EFA943))
                addCoinTv.text = context.getString(R.string.add_coins, integral)
                dayTv.text = name
                coinLl.setBackgroundResource(
                    when (status) {
                        1 -> {
                            holder.binding.addCoinTv.setTextColor(
                                ContextCompat.getColor(context, R.color.color_9732F5)
                            )
                            holder.binding.coinIv.setImageResource(R.drawable.bonus_check_in_hook_ic)
                            R.drawable.bonus_check_in_enable_bg
                        }

                        2 -> {
                            GlideUtils.loadImageTransparent(context, icon, coinIv)
                            R.drawable.bonus_check_in_selected_bg
                        }

                        else -> {
                            GlideUtils.loadImageTransparent(context, icon, coinIv)
                            R.drawable.bonus_check_in_normal_bg
                        }
                    }
                )
            }
        }

    }

}