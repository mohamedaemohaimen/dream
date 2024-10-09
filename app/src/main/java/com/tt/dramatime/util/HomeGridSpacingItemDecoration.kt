package com.tt.dramatime.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tt.dramatime.manager.LanguageManager

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/5/13 15:13
 *   Desc : 仅适用于首页2-3spanCount样式
 * </pre>
 */
class HomeGridSpacingItemDecoration(
    private val spanCount: Int, private val edgeSpacing: Int, private val internalSpacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column
        val shareSpacing =
            if (spanCount == 2) 0 else (edgeSpacing - internalSpacing) * 2 / spanCount

        // 设置左右间隙
        when (column) {
            0 -> {//左
                if (LanguageManager.isArabicLocale(view.context))
                    rightSpacing(outRect, shareSpacing) else leftSpacing(outRect, shareSpacing)
            }

            spanCount - 1 -> {//右
                if (LanguageManager.isArabicLocale(view.context))
                    leftSpacing(outRect, shareSpacing) else rightSpacing(outRect, shareSpacing)
            }

            else -> {//中间
                outRect.left = internalSpacing / 2 + shareSpacing
                outRect.right = internalSpacing / 2 + shareSpacing
            }
        }

        // 设置上下间隙
        if (position >= spanCount) {
            outRect.top = edgeSpacing
        }
    }

    private fun leftSpacing(outRect: Rect, shareSpacing: Int) {
        outRect.left = edgeSpacing
        outRect.right = internalSpacing / 2 - shareSpacing
    }

    private fun rightSpacing(outRect: Rect, shareSpacing: Int) {
        outRect.left = internalSpacing / 2 - shareSpacing
        outRect.right = edgeSpacing
    }
}
