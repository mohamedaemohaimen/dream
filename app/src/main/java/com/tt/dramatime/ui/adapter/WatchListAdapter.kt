package com.tt.dramatime.ui.adapter

import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.WatchListItemBinding
import com.tt.dramatime.http.api.MovieListApi.Bean.ListBean
import com.tt.dramatime.util.GlideUtils

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 观看列表适配器
 * </pre>
 */
class WatchListAdapter(dataList: MutableList<ListBean>) :
    BaseVBQuickAdapter<WatchListItemBinding, ListBean>(dataList) {

    override fun onBindViewHolder(
        holder: BaseVBHolder<WatchListItemBinding>, position: Int, item: ListBean?
    ) {
        GlideUtils.loadImage(context, item?.poster, holder.binding.episodesCoverIv)
        holder.binding.episodesTitleTv.text = item?.title
        holder.binding.playNumTv.text =context.getString(R.string.ep_current,item?.watchEpisode)
        holder.binding.allEpisodesNumTv.text = context.getString(R.string.ep_total,item?.totalEpisode)

    }

}