package com.tt.dramatime.ui.adapter.player

import android.view.View
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.EpisodesDialogItemBinding
import com.tt.dramatime.http.bean.EpisodesPlayBean

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 剧集播放列表适配器
 * </pre>
 */
class EpisodesPlayAdapter(dataList: MutableList<EpisodesPlayBean>) :
    BaseVBQuickAdapter<EpisodesDialogItemBinding, EpisodesPlayBean>(dataList) {

    override fun onBindViewHolder(
        holder: BaseVBHolder<EpisodesDialogItemBinding>, position: Int, item: EpisodesPlayBean?
    ) {

        item?.apply {
            holder.binding.apply {
                freeTv.visibility = if (isUnlock && isVipFree) View.VISIBLE else View.GONE
                lockIv.visibility = if (isUnlock) View.GONE else View.VISIBLE
                episodesNumTv.text = number
                if (isPlay) {
                    playerLav.playAnimation()
                    playerFl.visibility = View.VISIBLE
                } else {
                    if (playerLav.isAnimating) {
                        playerLav.cancelAnimation()
                    }
                    playerFl.visibility = View.GONE
                }
            }
        }
    }

}