package com.tt.dramatime.ui.adapter.bonus

import android.view.View
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.BonusTaskItemBinding
import com.tt.dramatime.http.api.TaskListApi
import com.tt.dramatime.util.GlideUtils

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 任务
 * </pre>
 */
class BonusTaskAdapter(dataList: MutableList<TaskListApi.Bean>) :
    BaseVBQuickAdapter<BonusTaskItemBinding, TaskListApi.Bean>(dataList) {

    override fun onBindViewHolder(
        holder: BaseVBHolder<BonusTaskItemBinding>, position: Int, item: TaskListApi.Bean?
    ) {
        item?.apply {
            holder.binding.apply {
                GlideUtils.loadImageTransparent(context, icon, coinsIv)
                taskTitleTv.text = taskName
                when (businessType) {
                    2, 9 -> {
                        list?.let { list ->
                            var hasUndone = true
                            var hasCompletion = false
                            var integral = 0
                            list.forEach {
                                hasUndone = it.status == 0
                                integral += it.integral
                            }

                            list.forEach {
                                if (hasCompletion.not()) {
                                    hasCompletion = it.status == 1
                                }
                            }

                            if (businessType == 2) {
                                claimLl.setBackgroundResource(R.drawable.button_minimal_30_bg)
                                claimTv.setTextColor(getColor(R.color.white))
                                claimTv.text = context.getString(R.string.claim)
                                claimLl.visibility = if (hasUndone) {
                                    watchAdsIv.visibility = View.VISIBLE
                                    View.VISIBLE
                                } else View.GONE
                            } else {
                                claimLl.setBackgroundResource(
                                    if (hasCompletion) R.drawable.button_minimal_30_bg else R.drawable.button_stroke_minimal_30_bg
                                )

                                claimTv.setTextColor(getColor(if (hasCompletion) R.color.white else R.color.color_9732F5))

                                claimTv.text =
                                    context.getString(if (hasCompletion) R.string.claim else R.string.go)

                                claimLl.visibility =
                                    if (hasCompletion || hasUndone) View.VISIBLE else View.GONE

                                watchAdsIv.visibility = View.GONE
                            }

                            addCoinsTv.text = context.getString(
                                R.string.add_bonus_frequency,
                                integral,
                                finishNum,
                                list[list.size - 1].taskNum
                            )
                            adsRv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                            adsRv.adapter = BonusAdsAdapter(list)
                            var adWatchNum = 0
                            list.forEachIndexed { index, listBean ->
                                if (listBean.status == 2) {
                                    adWatchNum = index
                                }
                            }
                            if (list.size > 5 && adWatchNum > 1) {
                                adsRv.scrollToPosition(adWatchNum)
                            }
                        }
                    }

                    else -> {
                        watchAdsIv.visibility = View.GONE
                        adsRv.visibility = View.GONE
                        addCoinsTv.text = context.getString(R.string.add_coins, task?.integral)

                        claimLl.setBackgroundResource(
                            if (task?.status == 0) R.drawable.button_stroke_minimal_30_bg else R.drawable.button_minimal_30_bg
                        )

                        claimTv.setTextColor(getColor(if (task?.status == 0) R.color.color_9732F5 else R.color.white))

                        claimTv.text =
                            context.getString(if (task?.status == 0) R.string.go else R.string.claim)

                        claimLl.visibility = if (task?.status == 2) View.GONE else View.VISIBLE
                    }
                }
            }
        }
    }

}