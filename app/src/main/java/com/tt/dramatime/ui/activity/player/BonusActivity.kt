package com.tt.dramatime.ui.activity.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.commit
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.BonusActivityBinding
import com.tt.dramatime.ui.fragment.BonusFragment

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 任务页面
 * </pre>
 */
class BonusActivity : BaseViewBindingActivity<BonusActivityBinding>({ BonusActivityBinding.inflate(it)}){

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, BonusActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }


    override fun initView() {
        val fragment = BonusFragment.newInstance()
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.my_fragment, fragment)
        }

        binding.backBtn.setOnClickListener { finish() }
    }

    override fun initData() {}
}