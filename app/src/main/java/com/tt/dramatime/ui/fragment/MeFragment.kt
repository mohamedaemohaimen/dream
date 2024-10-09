package com.tt.dramatime.ui.fragment

import android.view.View
import android.widget.LinearLayout
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.tt.dramatime.BuildConfig
import com.tt.dramatime.R
import com.tt.dramatime.app.AppConstant.FACEBOOK_LOGIN
import com.tt.dramatime.app.AppConstant.GOOGLE_LOGIN
import com.tt.dramatime.app.AppConstant.UUID_LOGIN
import com.tt.dramatime.app.AppConstant.thirdLoginIntegral
import com.tt.dramatime.app.BaseViewBindTitleBarFragment
import com.tt.dramatime.databinding.MeFragmentBinding
import com.tt.dramatime.http.api.UserInfoApi
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.MMKVUserConstant.Companion.KEY_LOGIN_SOURCE
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.DailyShowOnceManager
import com.tt.dramatime.ui.activity.HomeActivity
import com.tt.dramatime.ui.activity.me.ContactUsActivity
import com.tt.dramatime.ui.activity.me.LanguageSelectActivity
import com.tt.dramatime.ui.activity.me.LoginActivity
import com.tt.dramatime.ui.activity.me.SettingActivity
import com.tt.dramatime.ui.activity.wallet.StoreActivity
import com.tt.dramatime.ui.activity.wallet.WalletActivity
import com.tt.dramatime.ui.dialog.login.LoginDialogFragment
import com.tt.dramatime.util.GlideUtils
import com.tt.dramatime.util.TimeUtil

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 我的 Fragment
 * </pre>
 */
class MeFragment :
    BaseViewBindTitleBarFragment<MeFragmentBinding, HomeActivity>({ MeFragmentBinding.inflate(it) }) {

    companion object {
        fun newInstance(): MeFragment {
            return MeFragment()
        }
    }

    override fun isStatusBarEnabled(): Boolean {
        // 使用沉浸式状态栏
        return !super.isStatusBarEnabled()
    }

    private var source = ""

    override fun initView() {
        binding.versionTv.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        setOnClickListener(
            binding.loginTv,
            binding.walletCl,
            binding.topUpTv,
            binding.languageSb,
            binding.contactUsSb,
            binding.settingsSb,
            binding.vipBackgroundIv
        )

        binding.settingRfl.viewTreeObserver.addOnGlobalLayoutListener {
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, binding.settingRfl.height
            )
            binding.vipSpace.layoutParams = layoutParams
        }

        binding.settingSl.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            binding.vipSl.scrollY = scrollY
        }
    }

    override fun initData() {
        source = MMKVExt.getUserMmkv()?.decodeString(KEY_LOGIN_SOURCE) ?: UUID_LOGIN
        if (source == UUID_LOGIN && DailyShowOnceManager.shouldShowToday()) {
            LoginDialogFragment().show(childFragmentManager, LoginDialogFragment.TAG)
            DailyShowOnceManager.markAsShown()
        }
    }

    override fun onClick(view: View) {
        context?.let { context ->
            when (view) {
                binding.loginTv -> LoginActivity.start(context)
                binding.walletCl -> WalletActivity.start(context)
                binding.topUpTv, binding.vipBackgroundIv -> StoreActivity.start(context)
                binding.languageSb -> LanguageSelectActivity.start(context)
                binding.contactUsSb -> ContactUsActivity.start(context)
                binding.settingsSb -> SettingActivity.start(context)
                else -> {}
            }
        }
    }

    override fun onResume() {
        super.onResume()

        source = MMKVExt.getUserMmkv()?.decodeString(KEY_LOGIN_SOURCE) ?: UUID_LOGIN

        binding.loginTv.visibility =
            if (source == FACEBOOK_LOGIN || source == GOOGLE_LOGIN) View.GONE else View.VISIBLE

        setUserInfo()

        if (thirdLoginIntegral != null) {
            setFirstLoginTv()
        }
    }

    private fun setFirstLoginTv() {
        binding.firstLoginFl.visibility = if (binding.loginTv.visibility == View.VISIBLE) {
            binding.firstLoginTv.text = getString(R.string.first_login, thirdLoginIntegral)
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun setUserInfo() {
        if (isAdded.not()) return
        UserProfileHelper.apply {
            if (getUserId() == 0) {
                getUserInfo()
            } else {
                binding.apply {
                    profile.nickname?.let {
                        nickNameTv.text = it
                    }

                    if (source == UUID_LOGIN) {
                        avatarIv.setImageResource(R.drawable.avatar_ic)
                    } else {
                        context?.let { context ->
                            GlideUtils.loadImage(context, profile.avatar, binding.avatarIv)
                        }
                    }
                    userIdTv.text = getString(R.string.user_id, getUserId().toString())
                    coinsTv.text = getCoins().plus(getBonus()).toString()
                    val code = getSubscribeType()
                    vipAvatarBgIv.visibility = if (getVipState() && code != null) {
                        vipAvatarBgIv.setBackgroundResource(
                            when {
                                code.contains("week") -> R.drawable.vip_weekly_frame_ic
                                code.contains("month") -> R.drawable.vip_monthly_frame_ic
                                code.contains("year") -> R.drawable.vip_annual_frame_ic
                                else -> R.drawable.vip_weekly_frame_ic
                            }
                        )
                        View.VISIBLE
                    } else View.GONE

                    if (profile.subscribeStatus != 0 && code != null) {
                        settingRfl.setDragRate(0.3f)
                        vipGp.visibility = View.VISIBLE
                        vipSl.visibility = View.VISIBLE
                        vipIv.setBackgroundResource(
                            when {
                                code.contains("week") -> R.drawable.vip_weekly_ic
                                code.contains("month") -> R.drawable.vip_monthly_ic
                                code.contains("year") -> R.drawable.vip_annual_ic
                                else -> R.drawable.vip_weekly_frame_ic
                            }
                        )

                        vipTypeTv.text = getString(
                            when {
                                code.contains("week") -> R.string.weekly_vip
                                code.contains("month") -> R.string.monthly_vip
                                code.contains("year") -> R.string.annual_vip
                                else -> R.string.weekly_vip
                            }
                        )

                        vipExpirationTimeTv.text = if (getVipState()) getString(
                            R.string.expire_on,
                            TimeUtil.convertTimestampToDateUsingUtil(profile.expireTimestamp)
                        ) else getString(R.string.expired)
                        vipMoreIv.visibility = if (getVipState()) View.GONE else View.VISIBLE

                        vipExpirationTimeTv.isEnabled = getVipState().not()

                        vipTypeTv.isEnabled = getVipState().not()
                        vipBackgroundIv.isEnabled = getVipState().not()
                        vipBottomCl.isEnabled = getVipState().not()
                        unlockAllTv.isEnabled = getVipState().not()
                        noAdTv.isEnabled = getVipState().not()
                        frameTv.isEnabled = getVipState().not()
                    } else {
                        settingRfl.setDragRate(0f)
                        vipGp.visibility = View.GONE
                        vipSl.visibility = View.GONE
                    }
                }
            }
        }
    }

    fun setDefaultUserInfo() {
        if (isAdded) {
            binding.avatarIv.setImageResource(R.drawable.avatar_ic)
            binding.nickNameTv.text = getString(R.string.visitor)
            binding.userIdTv.text = getString(R.string.user_id, "000000")
            binding.coinsTv.text = "0"
        }
    }

    private fun getUserInfo() {
        EasyHttp.get(this).api(UserInfoApi())
            .request(object : OnHttpListener<HttpData<UserInfoApi.Bean?>> {
                override fun onHttpSuccess(data: HttpData<UserInfoApi.Bean?>) {
                    data.getData()?.let {
                        UserProfileHelper.setProfile(it)
                        setUserInfo()
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    setDefaultUserInfo()
                }
            })
    }

}