package com.tt.dramatime.app

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter4.BaseSingleItemAdapter
import java.lang.reflect.ParameterizedType

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/11/29
 *   desc :使用 ViewBinding 的适配器
 * </pre>
 */
abstract class BaseVBSingleItemAdapter<VB : ViewBinding, T : Any>:
    BaseSingleItemAdapter<T, BaseVBHolder<VB>>() {

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(
        context: Context, parent: ViewGroup, viewType: Int
    ): BaseVBHolder<VB> {

        val vbClass: Class<VB> =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VB>

        val inflate = vbClass.getDeclaredMethod(
            "inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
        )
        val viewBinding =
            inflate.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB

        return BaseVBHolder(viewBinding, viewBinding.root)
    }
}