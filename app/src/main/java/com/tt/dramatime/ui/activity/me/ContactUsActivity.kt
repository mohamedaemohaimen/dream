package com.tt.dramatime.ui.activity.me

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.effective.android.panel.PanelSwitchHelper
import com.effective.android.panel.interfaces.ContentScrollMeasurer
import com.effective.android.panel.interfaces.listener.OnEditFocusChangeListener
import com.effective.android.panel.interfaces.listener.OnKeyboardStateListener
import com.effective.android.panel.interfaces.listener.OnPanelChangeListener
import com.effective.android.panel.interfaces.listener.OnViewClickListener
import com.effective.android.panel.view.panel.IPanelView
import com.effective.android.panel.view.panel.PanelView
import com.gyf.immersionbar.ImmersionBar
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
import com.hjq.http.listener.OnHttpListener
import com.orhanobut.logger.Logger
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.ContactUsActivityBinding
import com.tt.dramatime.http.api.MsgListApi
import com.tt.dramatime.http.api.MsgListApi.Bean.MessageBean
import com.tt.dramatime.http.api.SendMsgApi
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.ui.adapter.MessageAdapter
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 客服
 * </pre>
 */
class ContactUsActivity :
    BaseViewBindingActivity<ContactUsActivityBinding>({ ContactUsActivityBinding.inflate(it) }) {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ContactUsActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig().navigationBarColor(R.color.color_F6F6F6)
    }

    override fun keyboardConflictEnable(): Boolean {
        return true
    }

    private val mMessageAdapter by lazy(LazyThreadSafetyMode.NONE) {
        MessageAdapter(mutableListOf())
    }

    private var mHelper: PanelSwitchHelper? = null
    private var unfilledHeight = 0

    override fun onStart() {
        super.onStart()
        if (mHelper == null) {
            mHelper = PanelSwitchHelper.Builder(this) //可选
                .addKeyboardStateListener(object : OnKeyboardStateListener {
                    override fun onKeyboardChange(visible: Boolean, height: Int) {
                        Logger.d("系统键盘是否可见 : $visible 高度为：$height")
                    }
                }).addEditTextFocusChangeListener(object : OnEditFocusChangeListener {
                    override fun onFocusChange(view: View?, hasFocus: Boolean) {
                        Logger.d("输入框是否获得焦点 : $hasFocus")
                        if (hasFocus) {
                            scrollToBottom()
                        }
                    }
                }).addViewClickListener(object : OnViewClickListener {
                    override fun onClickBefore(view: View?) {
                        when (view?.id) {
                            R.id.chat_message_input -> {
                                scrollToBottom()
                            }
                        }
                        Logger.d("点击了View : $view")
                    }
                }) //可选
                .addPanelChangeListener(object : OnPanelChangeListener {
                    override fun onKeyboard() {
                        Logger.d("唤起系统输入法")
                        scrollToBottom()
                    }

                    override fun onNone() {}

                    override fun onPanel(panel: IPanelView?) {
                        Logger.d("唤起面板 : $panel")
                        if (panel is PanelView) {
                            scrollToBottom()
                        }
                    }

                    override fun onPanelSizeChange(
                        panel: IPanelView?,
                        portrait: Boolean,
                        oldWidth: Int,
                        oldHeight: Int,
                        width: Int,
                        height: Int
                    ) {
                    }
                }).addContentScrollMeasurer(object : ContentScrollMeasurer {
                    override fun getScrollDistance(defaultDistance: Int): Int {
                        return defaultDistance - unfilledHeight
                    }

                    override fun getScrollViewId(): Int {
                        return R.id.message_rv
                    }
                }).logTrack(true) //output log
                .build()

            binding.messageRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager
                    if (layoutManager is LinearLayoutManager) {
                        val position = layoutManager.findLastVisibleItemPosition()
                        val lastChildView = layoutManager.findViewByPosition(position)
                        if (lastChildView != null) {
                            val bottom = lastChildView.bottom
                            val listHeight: Int =
                                binding.messageRv.height - binding.messageRv.paddingBottom
                            unfilledHeight = listHeight - bottom
                        }
                    }
                }
            })
        }
        binding.messageRv.setPanelSwitchHelper(mHelper)
    }

    override fun initView() {
        binding.messageRv.adapter = mMessageAdapter
        setOnClickListener(binding.sendBtn)
    }

    override fun initData() {
        startPolling()
    }

    override fun onClick(view: View) {
        when (view) {
            binding.sendBtn -> sendMsg()
        }
    }

    private fun getMsgList() {
        EasyHttp.get(this).api(MsgListApi())
            .request(object : OnHttpListener<HttpData<MsgListApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<MsgListApi.Bean>?) {
                    result?.getData()?.list?.let {
                        //必须toMutableList 不然add数据会抛异常
                        mMessageAdapter.submitList(it.reversed().toMutableList())
                    }
                    scrollToBottom()
                }

                override fun onHttpFail(throwable: Throwable?) {
                    toast(throwable?.message)
                }
            })
    }

    private fun sendMsg() {
        if (binding.chatMessageInput.text.isNullOrEmpty()) {
            toast(getString(R.string.content_hint))
            return
        }
        EasyHttp.post(this).api(SendMsgApi(0, binding.chatMessageInput.text.toString()))
            .request(object : HttpCallbackProxy<HttpData<SendMsgApi.Bean>>(this) {
                override fun onHttpSuccess(result: HttpData<SendMsgApi.Bean>?) {
                    val bean = MessageBean()
                    bean.msgType = 0
                    bean.content = binding.chatMessageInput.text.toString()
                    bean.isMe = true
                    mMessageAdapter.add(bean)
                    scrollToBottom()
                    binding.chatMessageInput.setText("")
                }
            })
    }

    private fun startPolling() {
        lifecycleScope.launch {
            while (true) {
                getMsgList()
                delay(5000) // 间隔5秒
            }
        }
    }

    private fun scrollToBottom() {
        binding.messageRv.scrollToPosition(mMessageAdapter.items.size - 1)
    }

    override fun onBackPressed() {
        if (mHelper != null && mHelper!!.hookSystemBackByPanelSwitcher()) {
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.cancel()
    }

}