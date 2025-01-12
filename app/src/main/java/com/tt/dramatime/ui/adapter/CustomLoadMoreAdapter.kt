package com.tt.dramatime.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.tt.dramatime.databinding.ViewLoadMoreBinding

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 自定义的"加载更多"。（包括加载中、加载失败）这里可以做很多事情，这里仅展示了更改自定义布局的使用。
 * </pre>
 */
class CustomLoadMoreAdapter : TrailingLoadStateAdapter<CustomLoadMoreAdapter.CustomVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): CustomVH {
        val viewBinding = ViewLoadMoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomVH(viewBinding).apply {
            viewBinding.loadMoreLoadFailView.setOnClickListener {
                // 失败重试点击事件
                invokeFailRetry()
            }
            viewBinding.loadMoreLoadCompleteView.setOnClickListener {
                // 加载更多，手动点击事件
                invokeLoadMore()
            }
        }
    }

    override fun onBindViewHolder(holder: CustomVH, loadState: LoadState) {
        when (loadState) {
            is LoadState.NotLoading -> {
                if (loadState.endOfPaginationReached) {
                    holder.viewBinding.loadMoreLoadCompleteView.visibility = View.GONE
                    holder.viewBinding.loadMoreLoadingView.visibility = View.GONE
                    holder.viewBinding.loadMoreLoadFailView.visibility = View.GONE
                    holder.viewBinding.loadMoreLoadEndView.visibility = View.VISIBLE
                } else {
                    holder.viewBinding.loadMoreLoadCompleteView.visibility = View.VISIBLE
                    holder.viewBinding.loadMoreLoadingView.visibility = View.GONE
                    holder.viewBinding.loadMoreLoadFailView.visibility = View.GONE
                    holder.viewBinding.loadMoreLoadEndView.visibility = View.GONE
                }
            }
            is LoadState.Loading -> {
                holder.viewBinding.loadMoreLoadCompleteView.visibility = View.GONE
                holder.viewBinding.loadMoreLoadingView.visibility = View.VISIBLE
                holder.viewBinding.loadMoreLoadFailView.visibility = View.GONE
                holder.viewBinding.loadMoreLoadEndView.visibility = View.GONE
            }
            is LoadState.Error -> {
                holder.viewBinding.loadMoreLoadCompleteView.visibility = View.GONE
                holder.viewBinding.loadMoreLoadingView.visibility = View.GONE
                holder.viewBinding.loadMoreLoadFailView.visibility = View.VISIBLE
                holder.viewBinding.loadMoreLoadEndView.visibility = View.GONE
            }
            is LoadState.None -> {
                holder.viewBinding.loadMoreLoadCompleteView.visibility = View.GONE
                holder.viewBinding.loadMoreLoadingView.visibility = View.GONE
                holder.viewBinding.loadMoreLoadFailView.visibility = View.GONE
                holder.viewBinding.loadMoreLoadEndView.visibility = View.GONE
            }
        }
    }


    class CustomVH(val viewBinding: ViewLoadMoreBinding) : RecyclerView.ViewHolder(viewBinding.root)
}