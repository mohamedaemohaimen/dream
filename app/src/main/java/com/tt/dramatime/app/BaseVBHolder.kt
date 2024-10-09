package com.tt.dramatime.app

import android.view.View
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter4.viewholder.QuickViewHolder

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/11/29
 *   desc :使用 ViewBinding 的适配器
 * </pre>
 */
class BaseVBHolder<VB : ViewBinding>(val binding: VB, view: View) : QuickViewHolder(view)