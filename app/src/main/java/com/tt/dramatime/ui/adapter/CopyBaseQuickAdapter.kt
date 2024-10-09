package com.tt.dramatime.ui.adapter

import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.CopyItemBinding

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 测试BaseVBQuickAdapter使用
 * </pre>
 */
class CopyBaseQuickAdapter(dataList: MutableList<String>) :
    BaseVBQuickAdapter<CopyItemBinding, String>(dataList) {

    override fun onBindViewHolder(
        holder: BaseVBHolder<CopyItemBinding>, position: Int, item: String?
    ) {

    }

}