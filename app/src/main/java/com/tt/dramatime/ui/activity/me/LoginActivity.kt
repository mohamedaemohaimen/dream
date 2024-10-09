package com.tt.dramatime.ui.activity.me

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.tt.dramatime.aop.Log
import com.tt.dramatime.app.AppConstant
import com.tt.dramatime.app.AppConstant.thirdLoginIntegral
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.LoginActivityBinding
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
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 登录界面
 * </pre>
 */
class LoginActivity :
    BaseViewBindingActivity<LoginActivityBinding>({ LoginActivityBinding.inflate(it) }),
    OnClickableSpanListener {

    companion object {
        @Log
        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig().navigationBarColor(R.color.white)
    }

    private var mGoogleSignInClient: GoogleSignInClient? = null

    private fun signInClient() {
        if (mGoogleSignInClient == null) {
            val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                    .requestIdToken(getString(R.string.web_client_id)).build()
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        }
    }

    private fun getGoogleIntent(): Intent {
        if (mGoogleSignInClient == null) {
            signInClient()
        }
        return mGoogleSignInClient!!.signInIntent
    }

    override fun initView() {
        val simplifySpanBuild = SimplifySpanBuild()
        simplifySpanBuild.append(getString(R.string.agree_privacy_policy)).append(
            SpecialTextUnit(getString(R.string.terms_service)).setClickableUnit(
                SpecialClickableUnit(binding.privacyPolicyTv, this).setTag(R.string.terms_service)
                    .showUnderline()
            ).setTextColor(ContextCompat.getColor(this, R.color.color_333333))
        ).append(" ").append(getString(R.string.and)).append(" ").append(
            SpecialTextUnit(getString(R.string.privacy_policy)).setClickableUnit(
                SpecialClickableUnit(binding.privacyPolicyTv, this).setTag(R.string.privacy_policy)
                    .showUnderline()
            ).setTextColor(ContextCompat.getColor(this, R.color.color_333333))
        )
        binding.privacyPolicyTv.text = simplifySpanBuild.build()

        binding.firstLoginFl.visibility = if (thirdLoginIntegral == null) View.GONE else {
            binding.firstLoginTv.text = getString(R.string.first_login, thirdLoginIntegral)
            View.VISIBLE
        }

        /*val source = MMKVExt.getDurableMMKV()?.decodeString(KEY_LAST_THIRD_LOGIN)
        if (source == AppConstant.GOOGLE_LOGIN) {
            binding.googleLastTv.visibility = View.VISIBLE
        } else if (source == AppConstant.FACEBOOK_LOGIN) {
            binding.facebookLastTv.visibility = View.VISIBLE
        }*/

        setOnClickListener(binding.googleCl, binding.faceBookCl)
    }

    override fun onClick(tv: TextView?, clickableSpan: CustomClickableSpan?) {
        when (clickableSpan?.tag) {
            R.string.terms_service -> BrowserActivity.start(getContext(), HttpUrls.SERVICE_URL)
            R.string.privacy_policy -> BrowserActivity.start(getContext(), HttpUrls.PRIVACY_URL)
        }
    }

    @SingleClick
    override fun onClick(view: View) {
        when (view) {
            binding.googleCl -> {
                showDialog()
                signInClient()
                googleLauncher.launch(getGoogleIntent())
            }

            binding.faceBookCl -> {
                facebookSignIn()
            }
        }
    }


    override fun initData() {
        checkKeyStoreHash()
    }

    /**获取apk签名散列信息 用于facebook登录*/
    private fun checkKeyStoreHash() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_SIGNING_CERTIFICATES
                )

                val signingInfo = info.signingInfo

                val apkContentsSigners = signingInfo.apkContentsSigners

                for (signature in apkContentsSigners) {
                    val md: MessageDigest = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val keyStoreHash = encodeToString(md.digest(), DEFAULT)
                    Logger.d("keyStoreHash:$keyStoreHash")
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    private var googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account?.idToken?.let { idToken ->
                        authBindApi(idToken, AppConstant.GOOGLE_LOGIN)
                    }
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
        val permissions: List<String> = mutableListOf("public_profile", "email")
        LoginManager.getInstance().logInWithReadPermissions(this, callbackManager, permissions)

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    showDialog()
                    authBindApi(result.accessToken.token, AppConstant.FACEBOOK_LOGIN)
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