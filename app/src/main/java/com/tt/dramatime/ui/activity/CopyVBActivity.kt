package com.tt.dramatime.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.CopyActivityBinding

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 可进行拷贝的副本
 * </pre>
 */
class CopyVBActivity : BaseViewBindingActivity<CopyActivityBinding>({ CopyActivityBinding.inflate(it)}){

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CopyVBActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }


    override fun initView() {}

    override fun initData() {}
}