package com.tt.dramatime.ui.adapter.player

import android.view.View
import androidx.core.content.ContextCompat
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.NumberEpisodesItemBinding
import com.tt.dramatime.http.bean.NumberEpisodesBean

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 剧集集数列表适配器
 * </pre>
 */
class NumberEpisodesAdapter(dataList: MutableList<NumberEpisodesBean>) :
    BaseVBQuickAdapter<NumberEpisodesItemBinding, NumberEpisodesBean>(dataList) {


    override fun onBindViewHolder(
        holder: BaseVBHolder<NumberEpisodesItemBinding>, position: Int, item: NumberEpisodesBean?
    ) {
        holder.binding.numberEpisodesTv.text = item?.number
        holder.binding.numberEpisodesTv.setTextColor(
            ContextCompat.getColor(
                context, if (item?.isSelect == true) R.color.white else R.color.color_999999
            )
        )
        holder.binding.indicator.visibility =
            if (item?.isSelect == true) View.VISIBLE else View.INVISIBLE

        holder.binding.lineView.visibility =
            if (position == itemCount - 1) View.GONE else View.VISIBLE
    }

}