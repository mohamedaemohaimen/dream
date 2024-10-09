package com.tt.dramatime.ui.adapter.wallet

import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.UnlockHistoryItemBinding
import com.tt.dramatime.http.api.UnlockHistoryApi.Bean.ListBean
import com.tt.dramatime.util.GlideUtils

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 解锁历史适配器
 * </pre>
 */
class UnlockHistoryAdapter(dataList: MutableList<ListBean>) :
    BaseVBQuickAdapter<UnlockHistoryItemBinding, ListBean>(dataList) {

    override fun onBindViewHolder(
        holder: BaseVBHolder<UnlockHistoryItemBinding>, position: Int, item: ListBean?
    ) {

        holder.binding.apply {
            item?.apply {
                GlideUtils.loadImage(context, poster, episodesCoverIv)
                episodesTitleTv.text = title
                playNumTv.text = context.getString(R.string.ep_current, currentEpisode)
                timeTv.text = context.getString(R.string.unlocked_on, createTime)
            }
        }

    }

}