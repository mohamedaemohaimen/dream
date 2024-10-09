package com.tt.dramatime.ui.dialog.login

import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cn.iwgang.simplifyspan.SimplifySpanBuild
import cn.iwgang.simplifyspan.customspan.CustomClickableSpan
import cn.iwgang.simplifyspan.other.OnClickableSpanListener
import cn.iwgang.simplifyspan.unit.SpecialClickableUnit
import cn.iwgang.simplifyspan.unit.SpecialTextUnit
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.gyf.immersionbar.ImmersionBar
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.orhanobut.logger.Logger
import com.tencent.mmkv.MMKV
import com.tt.dramatime.R
import com.tt.dramatime.app.AppConstant.FACEBOOK_LOGIN
import com.tt.dramatime.app.AppConstant.GOOGLE_LOGIN
import com.tt.dramatime.app.AppConstant.thirdLoginIntegral
import com.tt.dramatime.app.BaseViewBindDialogFragment
import com.tt.dramatime.databinding.LoginDialogBinding
import com.tt.dramatime.http.api.AuthBindApi
import com.tt.dramatime.http.api.HttpUrls
import com.tt.dramatime.http.api.UserInfoApi
import com.tt.dramatime.http.api.UuidLoginApi
import com.tt.dramatime.http.db.MMKVDurableConstant
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_LAST_THIRD_LOGIN
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.MMKVUserConstant
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.ui.activity.BrowserActivity
import com.tt.dramatime.util.eventbus.LoginAgainNotify
import org.greenrobot.eventbus.EventBus

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/4/9
 *   desc :登录弹窗
 * </pre>
 */
class LoginDialogFragment :
    BaseViewBindDialogFragment<LoginDialogBinding>({ LoginDialogBinding.inflate(it) }),
    OnClickableSpanListener {

    companion object {
        const val TAG = "LoginDialogFragment"
    }

    override fun getGravity(): Int {
        return Gravity.BOTTOM
    }

    override fun getBackgroundDimEnabled(): Boolean {
        return false
    }

    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun initView() {
        ImmersionBar.with(this).navigationBarColor(R.color.white).init()
        context?.let { context ->
            val simplifySpanBuild = SimplifySpanBuild()
            simplifySpanBuild.append(
                SpecialTextUnit(getString(R.string.terms_service)).setClickableUnit(
                    SpecialClickableUnit(
                        binding.privacyPolicyTv, this
                    ).setTag(R.string.terms_service).showUnderline()
                ).setTextColor(ContextCompat.getColor(context, R.color.color_999999))
            ).append(" ").append(getString(R.string.and)).append(" ").append(
                SpecialTextUnit(getString(R.string.privacy_policy)).setClickableUnit(
                    SpecialClickableUnit(
                        binding.privacyPolicyTv, this
                    ).setTag(R.string.privacy_policy).showUnderline()
                ).setTextColor(ContextCompat.getColor(context, R.color.color_999999))
            )
            binding.privacyPolicyTv.text = simplifySpanBuild.build()
        }

        binding.firstLoginFl.visibility = if (thirdLoginIntegral == null) View.GONE else {
            binding.firstLoginTv.text = getString(R.string.first_login,thirdLoginIntegral)
            View.VISIBLE
        }

        setOnClickListener(binding.googleTv, binding.facebookTv, binding.closeIv)
    }

    override fun initData() {}

    override fun onClick(tv: TextView?, clickableSpan: CustomClickableSpan?) {
        context?.let { context ->
            when (clickableSpan?.tag) {
                R.string.terms_service -> BrowserActivity.start(context, HttpUrls.SERVICE_URL)
                R.string.privacy_policy -> BrowserActivity.start(context, HttpUrls.PRIVACY_URL)
            }
        }
    }

    @SingleClick
    override fun onClick(view: View) {
        when (view) {
            binding.googleTv -> {
                showDialog()
                signInClient()
                googleLauncher.launch(getGoogleIntent())
            }

            binding.facebookTv -> facebookSignIn()

            binding.closeIv -> dismiss()
        }
    }

    private fun signInClient() {
        if (mGoogleSignInClient == null) {
            val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                    .requestIdToken(getString(R.string.web_client_id)).build()
            mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        }
    }

    private fun getGoogleIntent(): Intent {
        if (mGoogleSignInClient == null) {
            signInClient()
        }
        return mGoogleSignInClient!!.signInIntent
    }

    private var googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account?.idToken?.let {idToken->
                        authBindApi(idToken, GOOGLE_LOGIN)
                    }
                    Logger.d("token:" + account.idToken)
                } catch (e: ApiException) {
                    hideDialog()
                    Logger.d("task：" + e.localizedMessage)
                }
            } else {
                hideDialog()
            }
        }

    private fun facebookSignIn() {
        val callbackManager = CallbackManager.Factory.create()
        // 调用 Facebook SDK 提供的登录方法
        // 定义需要的权限列表
        val permissions: List<String> =
            mutableListOf("public_profile", "email", "user_gender", "user_birthday")
        LoginManager.getInstance()
            .logInWithReadPermissions(requireActivity(), callbackManager, permissions)

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Logger.d("token:" + result.accessToken.token)
                    showDialog()
                    authBindApi(result.accessToken.token, FACEBOOK_LOGIN)
                }

                override fun onCancel() {
                    toast(R.string.cancel)
                }

                override fun onError(error: FacebookException) {
                    toast(error.message)
                }
            })
    }

    private fun authBindApi(token: String, source: String) {
        EasyHttp.post(this).api(AuthBindApi(token, source))
            .request(object : OnHttpListener<HttpData<UuidLoginApi.Bean?>> {
                override fun onHttpSuccess(data: HttpData<UuidLoginApi.Bean?>) {
                    //清除所有 MMKV.defaultMMKV() 和 绑定用户的数据
                    MMKV.defaultMMKV().clearAll()
                    MMKVExt.getUserMmkv()?.clearAll()
                    val authorization = "Bearer ${data.getData()?.accessToken}"
                    // 更新 Authorization
                    MMKVExt.getUserMmkv()
                        ?.putString(MMKVUserConstant.KEY_AUTHORIZATION, authorization)
                    data.getData()?.contentLanguage?.let { contentLanguage ->
                        MMKVExt.getDurableMMKV()
                            ?.encode(MMKVDurableConstant.KEY_CONTENT_LANGUAGE, contentLanguage)
                    }
                    MMKVExt.getUserMmkv()?.encode(MMKVUserConstant.KEY_LOGIN_SOURCE, source)
                    MMKVExt.getDurableMMKV()?.encode(KEY_LAST_THIRD_LOGIN, source)
                    getUserInfo()
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
                    dismiss()
                }

                override fun onHttpFail(throwable: Throwable?) {
                    EventBus.getDefault().post(LoginAgainNotify(false))
                    hideDialog()
                    dismiss()
                }
            })
    }
}