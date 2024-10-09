package com.tt.dramatime.ui.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import com.tt.base.BaseDialog
import com.tt.base.action.AnimAction
import com.tt.dramatime.databinding.CopyDialogBinding

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc :可进行拷贝的副本
 * </pre>
 */
class CopyDialog {

    class Builder(context: Context) : BaseDialog.Builder<Builder>(context) {

        val binding = CopyDialogBinding.inflate(LayoutInflater.from(context))
        init {
            setContentView(binding.root)
            setAnimStyle(AnimAction.ANIM_IOS)
            setGravity(Gravity.BOTTOM)
            setWidth(WindowManager.LayoutParams.MATCH_PARENT)
        }
    }
}