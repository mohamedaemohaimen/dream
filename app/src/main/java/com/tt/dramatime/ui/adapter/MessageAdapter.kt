package com.tt.dramatime.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.tt.dramatime.databinding.MessageTextItemBinding
import com.tt.dramatime.http.api.MsgListApi.Bean.MessageBean

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 客服
 * </pre>
 */
class MessageAdapter(data: List<MessageBean>) : BaseMultiItemAdapter<MessageBean>(data) {

    class TextMessageVH(val viewBinding: MessageTextItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    init {
        addItemType(TEXT_TYPE, object : OnMultiItemAdapterListener<MessageBean, TextMessageVH> {
            override fun onCreate(
                context: Context, parent: ViewGroup, viewType: Int
            ): TextMessageVH {
                val viewBinding =
                    MessageTextItemBinding.inflate(LayoutInflater.from(context), parent, false)
                return TextMessageVH(viewBinding)
            }

            override fun onBind(holder: TextMessageVH, position: Int, item: MessageBean?) {
                holder.viewBinding.apply {
                    item?.apply {
                        if (isMe) {
                            startSp.visibility = View.GONE
                            endSp.visibility = View.VISIBLE
                        } else {
                            startSp.visibility = View.VISIBLE
                            endSp.visibility = View.GONE
                        }

                        textMsgTv.text = content
                        timeTv.text = createTime
                    }
                }
            }
        }).onItemViewType { position, list ->
            list[position].msgType
        }
    }

    companion object {
        //消息类型 0文字 1表情 2图片 3视频
        private const val TEXT_TYPE = 0
    }
}
