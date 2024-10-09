package com.tt.dramatime.ui.dialog.player

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.R
import com.tt.dramatime.action.ToastAction
import com.tt.dramatime.databinding.EpisodesPlayDialogBinding
import com.tt.dramatime.http.bean.EpisodesPlayBean
import com.tt.dramatime.http.bean.NumberEpisodesBean
import com.tt.dramatime.ui.adapter.player.EpisodesPlayAdapter
import com.tt.dramatime.ui.adapter.player.NumberEpisodesAdapter
import com.tt.dramatime.util.eventbus.SelectEpisodesNotify
import org.greenrobot.eventbus.EventBus

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc :剧集弹窗
 * </pre>
 */
class EpisodesPlayDialog {

    @SuppressLint("NotifyDataSetChanged", "DefaultLocale")
    class Builder(
        context: Context,
        numEpList: List<NumberEpisodesBean>,
        epPlayList: List<EpisodesPlayBean>,
        total: Int?,
        title: String?,
        viewNum: Int,
        currentEp: Int,
    ) : BaseDialog.Builder<Builder>(context), ToastAction {

        private val binding = EpisodesPlayDialogBinding.inflate(LayoutInflater.from(context))
        private val mNumberEpisodesAdapter = NumberEpisodesAdapter(mutableListOf())
        private val mEpisodesPlayAdapter = EpisodesPlayAdapter(mutableListOf())

        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_BOTTOM)
            setGravity(Gravity.BOTTOM)
            setBackgroundDimEnabled(false)
            setWidth(WindowManager.LayoutParams.MATCH_PARENT)
            setHeight(dp2px(527f))
            binding.episodesTitleTv.text = title
            binding.epTv.text = getString(R.string.ep_in_total, total)

            val viewNumStr = if (viewNum > 1000 * 1000) {
                String.format("%.1fM", viewNum / 1000000.0)
            } else if (viewNum > 1000) {
                String.format("%.1fK", viewNum / 1000.0)
            } else {
                viewNum.toString()
            }

            binding.playbackQuantityTv.text = viewNumStr

            binding.numberEpisodesRv.adapter = mNumberEpisodesAdapter
            binding.episodesRv.adapter = mEpisodesPlayAdapter

            mNumberEpisodesAdapter.submitList(numEpList)
            mEpisodesPlayAdapter.submitList(epPlayList)

            setOnClickListener(binding.arrowBottomIv)

            mNumberEpisodesAdapter.setOnItemClickListener { adapter, view, position ->
                notifyNumberEpisodesData(position)
                scrollToPosition(position)
            }

            binding.episodesRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    var firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    //往上滑动的时候多显示一行切换分类
                    if (dy < 0) {
                        firstVisibleItem += 5
                    }

                    var position = firstVisibleItem / 30

                    if (lastVisibleItem + 1 == epPlayList.size) {
                        position = mNumberEpisodesAdapter.items.size - 1
                    }

                    notifyNumberEpisodesData(position)
                }
            })

            mEpisodesPlayAdapter.setOnItemClickListener { adapter, view, position ->
                val previousEpisode = position - 1
                //当前是第一集或者上一集已解锁才可以跳集
                if (position == 0 || mEpisodesPlayAdapter.items[previousEpisode].isUnlock) {
                    EventBus.getDefault().post(SelectEpisodesNotify(position))
                    dismiss()
                } else {
                    toast(getString(R.string.more_excitement_hint))
                }
            }

            scrollToPosition(0, currentEp)
        }

        private fun notifyNumberEpisodesData(position: Int) {
            mNumberEpisodesAdapter.items.forEachIndexed { index, numberEpisodesBean ->
                mNumberEpisodesAdapter.items[index].isSelect = index == position
            }
            mNumberEpisodesAdapter.notifyDataSetChanged()
        }

        private fun scrollToPosition(position: Int, currentEp: Int = 0) {
            val num: Int = if (currentEp > 0) {
                currentEp - 1
            } else {
                position * 30 + 1
            }
            // 获取指定位置的视图
            val layoutManager = binding.episodesRv.layoutManager as GridLayoutManager
            layoutManager.scrollToPositionWithOffset(num, 0)
        }

        override fun onClick(view: View) {
            dismiss()
        }

    }
}