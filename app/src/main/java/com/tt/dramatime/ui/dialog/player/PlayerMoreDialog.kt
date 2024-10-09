package com.tt.dramatime.ui.dialog.player

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.tt.base.BaseDialog
import com.tt.base.BottomSheetDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.databinding.PlayerMoreDialogBinding
import com.tt.dramatime.ui.activity.me.ContactUsActivity
import com.tt.dramatime.ui.activity.player.ReportActivity


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc :播放器更多弹窗
 * </pre>
 */
class PlayerMoreDialog {

    class Builder(context: Context, val movieId: String, val url: String) : BaseDialog.Builder<Builder>(context) {

        val binding = PlayerMoreDialogBinding.inflate(LayoutInflater.from(context))

        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_BOTTOM)
            setGravity(Gravity.BOTTOM)
            setWidth(WindowManager.LayoutParams.MATCH_PARENT)
            setBackgroundDimEnabled(false)
            setOnClickListener(binding.shareSb,binding.customerSb,binding.reportSb)
        }

        override fun createDialog(context: Context, themeId: Int): BaseDialog {
            return BottomSheetDialog(context, themeId)
        }

        override fun onClick(view: View) {
            when (view) {
                binding.shareSb -> {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.setType("text/plain")
                    shareIntent.putExtra(Intent.EXTRA_TEXT, url)

                    startActivity(Intent.createChooser(shareIntent, "分享链接到："))
                }
                binding.customerSb ->  ContactUsActivity.start(getContext())
                binding.reportSb -> ReportActivity.start(getContext(), movieId)
            }
        }

    }
}