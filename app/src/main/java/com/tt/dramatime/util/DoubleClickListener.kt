package com.tt.dramatime.util

import android.os.Handler
import android.os.Looper
import android.view.View

class DoubleClickListener : View.OnClickListener{

    private var isClicking = false
    private var singleClickListener : View.OnClickListener? = null
    private var doubleClickListener : View.OnClickListener? = null
    private var delayTime = 300L
    private var clickCallBack : Runnable? = null
    private var handler : Handler = Handler(Looper.getMainLooper())

    override fun onClick(v: View?) {
        // 创建一个单击延迟任务，延迟时间到了之后触发单击事件
        clickCallBack = clickCallBack?: Runnable {
            singleClickListener?.onClick(v)
            isClicking = false
        }
        // 如果已经点击过一次，在延迟时间内再次接受到点击
        // 意味着这是个双击事件
        if (isClicking){
            // 移除延迟任务，回调双击监听器
            handler.removeCallbacks(clickCallBack!!)
            doubleClickListener?.onClick(v)
            isClicking = false
        }else{
            // 第一次点击，发送延迟任务
            isClicking = true
            handler.postDelayed(clickCallBack!!,delayTime)
        }
    }

    fun setSingleClickListener(singleClickListener : View.OnClickListener){
        this.singleClickListener=singleClickListener
    }

    fun setDoubleClickListener(doubleClickListener : View.OnClickListener){
        this.doubleClickListener=doubleClickListener
    }

}
