package com.tt.dramatime.ui.adapter.home

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.blankj.utilcode.util.ConvertUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.HomeListItemBinding
import com.tt.dramatime.http.api.HomeListApi
import com.tt.dramatime.manager.LanguageManager
import com.tt.dramatime.ui.activity.home.VideoCatalogActivity
import com.tt.dramatime.util.HomeGridSpacingItemDecoration
import com.tt.dramatime.widget.SlideBottomMonitorRecycleView

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 首页列表适配器
 * </pre>
 */
class HomeListAdapter(val dataList: MutableList<HomeListApi.Bean>) :
    BaseVBQuickAdapter<HomeListItemBinding, HomeListApi.Bean>(dataList) {

    var list: MutableList<BaseQuickAdapter<*, *>> = mutableListOf()

    fun setAdapterList(list: MutableList<BaseQuickAdapter<*, *>>) {
        this.list = list
    }

    override fun onBindViewHolder(
        holder: BaseVBHolder<HomeListItemBinding>, position: Int, item: HomeListApi.Bean?
    ) {
        item?.apply {
            holder.binding.apply {
                titleTv.text = title
                moreTv.visibility =
                    if (pageStatus == true && (blockStyle == "1" || blockStyle == "5")) View.VISIBLE else View.GONE

                space.visibility = if (blockStyle == "5") View.GONE else View.VISIBLE

                val linearItemDecoration = object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
                    ) {
                        val childPosition = parent.getChildAdapterPosition(view)
                        val itemSize = movieList?.size?.minus(1) ?: 0
                        val blockStyle = blockStyle

                        if (blockStyle == "2" || blockStyle == "7") {
                            outRect.left = 0
                            outRect.right = 0
                            if (childPosition > 0) {
                                outRect.top = ConvertUtils.dp2px(16f)
                            }
                        } else {
                            if (LanguageManager.isArabicLocale(context)) {
                                outRect.right =
                                    ConvertUtils.dp2px(if (childPosition == 0) 16f else 0f)
                                outRect.left =
                                    ConvertUtils.dp2px(if (childPosition == itemSize) 16f else 13f)
                            } else {
                                outRect.left =
                                    ConvertUtils.dp2px(if (childPosition == 0) 16f else 0f)
                                outRect.right =
                                    ConvertUtils.dp2px(if (childPosition == itemSize) 16f else 13f)
                            }
                        }
                    }
                }

                if (episodesRv.itemDecorationCount > 0) {
                    episodesRv.removeItemDecorationAt(0)
                }

                movieList?.let { _ ->
                    episodesRv.setOnBottomCallback(object :
                        SlideBottomMonitorRecycleView.OnBottomCallback {
                        override fun onBottom() {
                            if (blockStyle == "1" && pageStatus == true) {
                                code?.let { code ->
                                    VideoCatalogActivity.start(context, items.toMutableList(), code)
                                }
                            }
                        }
                    })

                    when (blockStyle) {
                        "2", "7" -> {
                            episodesRv.layoutManager =
                                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                            episodesRv.addItemDecoration(linearItemDecoration)
                        }

                        "3" -> {
                            val layoutManager = GridLayoutManager(context, 2)
                            episodesRv.layoutManager = layoutManager
                            episodesRv.addItemDecoration(
                                HomeGridSpacingItemDecoration(
                                    2, ConvertUtils.dp2px(16f), ConvertUtils.dp2px(13f)
                                )
                            )
                        }

                        "5" -> {
                            episodesRv.layoutManager =
                                GridLayoutManager(context, 3, GridLayoutManager.HORIZONTAL, false)
                        }

                        "6" -> {
                            val layoutManager = GridLayoutManager(context, 3)
                            episodesRv.layoutManager = layoutManager
                            episodesRv.addItemDecoration(
                                HomeGridSpacingItemDecoration(
                                    3, ConvertUtils.dp2px(16f), ConvertUtils.dp2px(11f)
                                )
                            )
                        }

                        else -> {
                            episodesRv.layoutManager =
                                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                            episodesRv.addItemDecoration(linearItemDecoration)
                        }
                    }
                    episodesRv.itemAnimator?.apply {
                        // 禁用更改动画
                        (this as SimpleItemAnimator).supportsChangeAnimations = false
                    }
                    episodesRv.adapter = list[position]
                }
            }
        }
    }

}