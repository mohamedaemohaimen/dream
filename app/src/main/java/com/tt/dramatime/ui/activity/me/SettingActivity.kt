package com.tt.dramatime.ui.activity.me

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.NotificationUtils
import com.bumptech.glide.Glide
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.gyf.immersionbar.ImmersionBar
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.tencent.mmkv.MMKV
import com.tt.base.BaseDialog
import com.tt.dramatime.R
import com.tt.dramatime.app.AppConstant
import com.tt.dramatime.app.AppConstant.FACEBOOK_LOGIN
import com.tt.dramatime.app.AppConstant.GOOGLE_LOGIN
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.SettingActivityBinding
import com.tt.dramatime.http.api.DeleteUserApi
import com.tt.dramatime.http.api.HttpUrls
import com.tt.dramatime.http.api.LogoutApi
import com.tt.dramatime.http.api.UserInfoApi
import com.tt.dramatime.http.api.UuidLoginApi
import com.tt.dramatime.http.db.MMKVDurableConstant
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.MMKVUserConstant
import com.tt.dramatime.http.db.MMKVUserConstant.Companion.KEY_LOGIN_SOURCE
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.CacheDataManager
import com.tt.dramatime.ui.activity.BrowserActivity
import com.tt.dramatime.ui.dialog.MessageDialog
import com.tt.dramatime.util.StartNotificationUtils
import com.tt.dramatime.util.eventbus.LoginAgainNotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/12/9
 *   desc : 设置界面
 * </pre>
 */
class SettingActivity :
    BaseViewBindingActivity<SettingActivityBinding>({ SettingActivityBinding.inflate(it) }) {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SettingActivity::class.java)
            if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig().navigationBarColor(R.color.color_F6F7F9)
    }

    override fun initView() {
        // 获取应用缓存大小
        binding.settingCacheSb.setRightText(CacheDataManager.getTotalCacheSize(getContext()))

        val source = MMKVExt.getUserMmkv()?.decodeString(KEY_LOGIN_SOURCE)

        if (source == FACEBOOK_LOGIN || source == GOOGLE_LOGIN) {
            binding.signOutLl.visibility = View.VISIBLE
            binding.deleteAccountSb.visibility = View.VISIBLE
        }

        setOnClickListener(
            binding.notificationSb,
            binding.settingCacheSb,
            binding.termsServiceSb,
            binding.privacyPolicySb,
            binding.deleteAccountSb,
            binding.signOutLl,
        )
    }

    override fun onResume() {
        super.onResume()
        binding.notificationSb.setRightText(
            getString(
                if (NotificationUtils.areNotificationsEnabled()) R.string.enable else R.string.disable
            )
        )
    }

    override fun initData() {

    }

    @SingleClick
    override fun onClick(view: View) {
        when (view) {
            binding.notificationSb -> {
                if (NotificationUtils.areNotificationsEnabled().not()) {
                    startActivity(StartNotificationUtils.getNotificationIntent(packageName))
                }
            }

            binding.settingCacheSb -> {
                MessageDialog.Builder(this).setMessage(getString(R.string.clear_all_cache))
                    .setConfirm(R.string.dialog_clear)
                    .setListener(object : MessageDialog.OnListener {
                        override fun onConfirm(dialog: BaseDialog?) {
                            // 清除内存缓存（必须在主线程）
                            Glide.get(getContext()).clearMemory()
                            lifecycleScope.launch(Dispatchers.IO) {
                                CacheDataManager.clearAllCache(getContext())
                                // 清除本地缓存（必须在子线程）
                                Glide.get(getContext()).clearDiskCache()
                                withContext(Dispatchers.Main) {
                                    // 重新获取应用缓存大小
                                    binding.settingCacheSb.setRightText(
                                        CacheDataManager.getTotalCacheSize(getContext())
                                    )
                                }
                            }
                        }
                    }).show()
            }

            binding.termsServiceSb -> BrowserActivity.start(getContext(), HttpUrls.SERVICE_URL)
            binding.privacyPolicySb -> BrowserActivity.start(getContext(), HttpUrls.PRIVACY_URL)

            binding.deleteAccountSb -> {
                MessageDialog.Builder(this).setTitleImage(R.drawable.dialog_hint_ic)
                    .setMessage(R.string.delete_account_hint).setCancelColour()
                    .setListener(object : MessageDialog.OnListener {
                        override fun onConfirm(dialog: BaseDialog?) {
                            deleteUser()
                        }
                    }).show()
            }

            binding.signOutLl -> {
                MessageDialog.Builder(this).setMessage(R.string.sign_out_hint).setCancelColour()
                    .setListener(object : MessageDialog.OnListener {
                        override fun onConfirm(dialog: BaseDialog?) {
                            logout()
                        }
                    }).show()
            }
        }
    }

    private fun logout() {
        showDialog()
        EasyHttp.post(this@SettingActivity).api(LogoutApi())
            .request(object : OnHttpListener<HttpData<UuidLoginApi.Bean?>> {
                override fun onHttpSuccess(result: HttpData<UuidLoginApi.Bean?>) {
                    logout(result)
                }

                override fun onHttpFail(throwable: Throwable?) {
                    toast(throwable?.message)
                    hideDialog()
                }
            })
    }

    private fun logout(result: HttpData<UuidLoginApi.Bean?>) {
        //清除所有 MMKV.defaultMMKV() 和 绑定用户的数据
        MMKV.defaultMMKV().clearAll()
        MMKVExt.getUserMmkv()?.clearAll()
        val authorization = "Bearer ${result.getData()?.accessToken}"
        // 更新 Authorization
        MMKVExt.getUserMmkv()?.putString(MMKVUserConstant.KEY_AUTHORIZATION, authorization)
        MMKVExt.getUserMmkv()?.encode(KEY_LOGIN_SOURCE, AppConstant.UUID_LOGIN)
        result.getData()?.contentLanguage?.let { contentLanguage ->
            MMKVExt.getDurableMMKV()
                ?.encode(MMKVDurableConstant.KEY_CONTENT_LANGUAGE, contentLanguage)
        }
        getUserInfo()
    }

    private fun deleteUser() {
        showDialog()
        EasyHttp.get(this@SettingActivity).api(DeleteUserApi())
            .request(object : OnHttpListener<HttpData<UuidLoginApi.Bean?>> {
                override fun onHttpSuccess(result: HttpData<UuidLoginApi.Bean?>) {
                    logout(result)
                }

                override fun onHttpFail(throwable: Throwable?) {
                    toast(throwable?.message)
                    hideDialog()
                }
            })
    }

    private fun getUserInfo() {
        EasyHttp.get(this).api(UserInfoApi())
            .request(object : OnHttpListener<HttpData<UserInfoApi.Bean?>> {
                override fun onHttpSuccess(data: HttpData<UserInfoApi.Bean?>) {
                    data.getData()?.let {
                        UserProfileHelper.setProfile(it)
                    }
                    EventBus.getDefault().post(LoginAgainNotify(true))
                    hideDialog()
                    finish()
                }

                override fun onHttpFail(throwable: Throwable?) {
                    EventBus.getDefault().post(LoginAgainNotify(false))
                    hideDialog()
                    finish()
                }
            })
    }
}