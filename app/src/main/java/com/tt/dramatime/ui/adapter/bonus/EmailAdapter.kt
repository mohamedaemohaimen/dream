package com.tt.dramatime.ui.adapter.bonus

import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.EmailItemBinding

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 邮箱适配器
 * </pre>
 */
class EmailAdapter(dataList: MutableList<String>) :
    BaseVBQuickAdapter<EmailItemBinding, String>(dataList) {

    override fun onBindViewHolder(
        holder: BaseVBHolder<EmailItemBinding>, position: Int, item: String?
    ) {
        item?.let { email ->
            holder.binding.emailTv.text = email
        }
    }

}