package com.tt.dramatime.ui.activity.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.angcyo.tablayout.delegate2.ViewPager2Delegate
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.VideoCatalogActivityBinding
import com.tt.dramatime.http.api.HomeListApi
import com.tt.dramatime.ui.fragment.VideoCatalogFragment

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 更多视频页面
 * </pre>
 */
class VideoCatalogActivity :
    BaseViewBindingActivity<VideoCatalogActivityBinding>({ VideoCatalogActivityBinding.inflate(it) }) {

    companion object {

        private const val KEY_CODE = "key.code"
        private const val KEY_DATA = "key.data"

        fun start(context: Context, data: MutableList<HomeListApi.Bean>, code: String) {
            val intent = Intent(context, VideoCatalogActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelableArrayList(KEY_DATA, ArrayList(data))
            intent.putExtra(KEY_CODE, code)
            intent.putExtras(bundle)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun initView() {

        val dataList: ArrayList<HomeListApi.Bean>? = getBundle()?.getParcelableArrayList(KEY_DATA)
        val moreList = dataList?.filter { it.blockStyle == "1" || it.code == "全部剧集" }
            ?.let { moveLastToFirstImmutable(it) }

        val code = getString(KEY_CODE)!!

        val index = moreList?.indexOfFirst { it.code?.startsWith(code) == true }

        val fragments: ArrayList<Fragment> = arrayListOf()

        binding.slidingTabLayout.tabIndicator.indicatorWidth = -2

        binding.slidingTabLayout.configTabLayoutConfig {
            //选中view的回调
            onSelectViewChange =
                { fromView: View?, selectViewList: List<View>, reselect: Boolean, fromUser: Boolean ->
                    if (reselect.not()) {
                        val toView = selectViewList.first()
                    }
                }
            //选中index的回调
            onSelectIndexChange =
                { fromIndex: Int, selectIndexList: List<Int>, reselect: Boolean, fromUser: Boolean ->
                    val toIndex = selectIndexList.first()
                    binding.viewPager.currentItem = toIndex
                }
        }

        moreList?.forEachIndexed { position, bean ->

            val textView: TextView = LayoutInflater.from(getContext())
                .inflate(R.layout.tab_video_catalog_item, null) as TextView

            textView.text = if (bean.code == "全部剧集") getString(R.string.all_series)
            else bean.title?.let { removeEmoji(it) }

            binding.slidingTabLayout.addView(textView)

            textView.setPadding(
                dp2px(if (position == 0) 16f else 0f),
                0,
                dp2px(if (position == moreList.size - 1) 16f else 0f),
                0
            )

            bean.code?.let { code ->
                bean.movieList?.let { movieList ->
                    fragments.add(VideoCatalogFragment.newInstance(code, movieList, bean.pageNum))
                }
            }
        }

        binding.viewPager.adapter = MyPagerAdapter(this, fragments)

        binding.viewPager.offscreenPageLimit = fragments.size

        ViewPager2Delegate.install(binding.viewPager, binding.slidingTabLayout)

        binding.viewPager.currentItem = index ?: 0

    }

    override fun initData() {}

    private fun removeEmoji(text: String): String {
        val emojiPattern = "[\\p{So}\\p{Cn}]".toRegex()
        return emojiPattern.replace(text, "")
    }

    private fun moveLastToFirstImmutable(list: List<HomeListApi.Bean>): List<HomeListApi.Bean> {
        if (list.isEmpty()) return list
        val lastElement = list.last()
        return listOf(lastElement) + list.dropLast(1)
    }

    class MyPagerAdapter(activity: FragmentActivity, private val fragments: List<Fragment>) :
        FragmentStateAdapter(activity) {

        // 返回总的页面数
        override fun getItemCount(): Int {
            return fragments.size
        }

        // 根据位置返回相应的 Fragment
        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }

}