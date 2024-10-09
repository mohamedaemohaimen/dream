package com.tt.dramatime.ui.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils
import com.tt.base.BaseAdapter
import com.tt.base.action.HandlerAction
import com.tt.dramatime.R
import com.tt.dramatime.app.AppAdapter
import com.tt.dramatime.http.db.UserProfileHelper


/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2021/02/28
 *    desc   : 导航栏适配器
 */
class NavigationAdapter constructor(context: Context) :
    AppAdapter<NavigationAdapter.MenuItem>(context), BaseAdapter.OnItemClickListener,
    HandlerAction {

    /** 当前选中条目位置 */
    private var selectedPosition: Int = 0

    /** 导航栏点击监听 */
    private var listener: OnNavigationListener? = null

    init {
        setOnItemClickListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder()
    }

    override fun generateDefaultLayoutManager(context: Context): RecyclerView.LayoutManager {
        return GridLayoutManager(context, getCount(), RecyclerView.VERTICAL, false)
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    /**
     * 设置导航栏监听
     */
    fun setOnNavigationListener(listener: OnNavigationListener?) {
        this.listener = listener
    }

    /**
     * [BaseAdapter.OnItemClickListener]
     */
    override fun onItemClick(recyclerView: RecyclerView?, itemView: View?, position: Int) {
        if (selectedPosition == position) {
            listener?.onIdenticalSelected(position)
            return
        }
        if (listener == null) {
            selectedPosition = position
            notifyDataSetChanged()
            return
        }
        if (listener!!.onNavigationItemSelected(position)) {
            selectedPosition = position
            //初始化home标签滚动状态 防止点击其他tab触发动画
            getItem(0).isScrollingUp = null
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder : AppViewHolder(R.layout.home_navigation_item) {

        private val iconView: ImageView? by lazy { findViewById(R.id.iv_home_navigation_icon) }
        private val titleView: TextView? by lazy { findViewById(R.id.tv_home_navigation_title) }
        private val checkInTv: TextView? by lazy { findViewById(R.id.check_in_tv) }

        override fun onBindView(position: Int) {
            getItem(position).apply {
                //设置home Tab位移动画
                isScrollingUp?.let {
                    val translationY = ConvertUtils.dp2px(22f)
                    val startTranslationY = (if (it) -translationY else translationY).toFloat()

                    val translationYIn = ObjectAnimator.ofFloat(
                        iconView, "translationY", startTranslationY, 0f
                    )
                    translationYIn.setDuration(150)
                    translationYIn.start()
                }

                iconView?.setImageDrawable(getDrawable())
                titleView?.text =
                    if (position == 0 && selectedPosition != 0) getString(R.string.home_nav_index) else getText()
                iconView?.isSelected = (selectedPosition == position)
                titleView?.isSelected = (selectedPosition == position)

                checkInTv?.visibility =
                    if (getText() == getBonusString() && UserProfileHelper.getSignStatus() == 0) View.VISIBLE else View.GONE
            }
        }

        private fun getBonusString(): String {
            return getContext().getString(R.string.home_nav_bonus)
        }
    }

    class MenuItem constructor(
        private val text: String?,
        private var drawable: Drawable?,
        var isScrollingUp: Boolean? = null
    ) {

        fun getText(): String? {
            return text
        }

        fun getDrawable(): Drawable? {
            return drawable
        }

        fun setDrawable(drawable: Drawable?) {
            this.drawable = drawable
        }
    }

    interface OnNavigationListener {
        fun onNavigationItemSelected(position: Int): Boolean

        fun onIdenticalSelected(position: Int)
    }
}