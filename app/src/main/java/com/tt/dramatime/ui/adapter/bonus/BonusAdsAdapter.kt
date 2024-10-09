package com.tt.dramatime.ui.adapter.bonus

import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.smart.adapter.util.ScreenUtils
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.AdAddCoinItemBinding
import com.tt.dramatime.http.api.TaskListApi
import com.tt.dramatime.manager.LanguageManager

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 任务-广告
 * </pre>
 */
class BonusAdsAdapter(dataList: MutableList<TaskListApi.Bean.ListBean>) :
    BaseVBQuickAdapter<AdAddCoinItemBinding, TaskListApi.Bean.ListBean>(dataList) {

    private val oneUnit = dp2px(1f).toFloat()

    override fun onBindViewHolder(
        holder: BaseVBHolder<AdAddCoinItemBinding>, position: Int, item: TaskListApi.Bean.ListBean?
    ) {
        val width = (ScreenUtils.getScreenWidth(context) - dp2px(48f)) / 5

        val layoutParams: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            width, ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        item?.apply {
            holder.binding.apply {
                addCoinRoot.layoutParams = layoutParams

                var leftRadius = 0f
                var rightRadius = 0f

                addCoinLl.setBackgroundResource(
                    //状态 0 未完成 1 已完成 2已领取
                    if (status == 2) {
                        when (position) {
                            0 -> {
                                leftRadius = oneUnit
                                rightRadius =
                                    if (items.size > 1 && items[1].status == 2) 0f else oneUnit
                            }

                            items.size - 1 -> rightRadius = oneUnit

                            else -> rightRadius =
                                if (items[position + 1].status == 2) 0f else oneUnit
                        }

                        addCoinIv.setBackgroundResource(R.drawable.coins_grey_ic)
                        addBonusTv.setTextColor(getColor(R.color.color_B7B7B7))
                        taskNameTv.setTextColor(getColor(R.color.color_B7B7B7))
                        R.drawable.ad_add_coin_normal_bg
                    } else {
                        if (position == 0) {
                            leftRadius = dp2px(1f).toFloat()
                        } else if (position == items.size - 1) {
                            rightRadius = dp2px(1f).toFloat()
                        }
                        addCoinIv.setBackgroundResource(R.drawable.coins_54_ic)
                        addBonusTv.setTextColor(getColor(R.color.white))
                        taskNameTv.setTextColor(getColor(R.color.black))
                        R.drawable.ad_add_coin_bg
                    }
                )

                roundView.shapeDrawableBuilder.setSolidColor(getColor(if (status > 0) R.color.color_9732F5 else R.color.color_E8E8EC))
                    .intoBackground()
                lineView.shapeDrawableBuilder.setSolidColor(getColor(if (status > 0) R.color.color_9732F5 else R.color.color_E8E8EC))
                    .intoBackground()

                if (LanguageManager.isArabicLocale(context)) {
                    lineView.shapeDrawableBuilder.setBottomLeftRadius(rightRadius)
                        .setTopLeftRadius(rightRadius).setTopRightRadius(leftRadius)
                        .setBottomRightRadius(leftRadius).intoBackground()
                } else {
                    lineView.shapeDrawableBuilder.setBottomLeftRadius(leftRadius)
                        .setTopLeftRadius(leftRadius).setTopRightRadius(rightRadius)
                        .setBottomRightRadius(rightRadius).intoBackground()
                }

                addBonusTv.text = context.getString(R.string.add_coins, integral)
                taskNameTv.text = name
            }
        }
    }

}