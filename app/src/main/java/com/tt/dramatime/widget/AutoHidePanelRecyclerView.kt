package com.tt.dramatime.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.effective.android.panel.PanelSwitchHelper

class AutoHidePanelRecyclerView @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0
) : RecyclerView(context!!, attrs, defStyle) {

    private var panelSwitchHelper: PanelSwitchHelper? = null

    fun setPanelSwitchHelper(panelSwitchHelper: PanelSwitchHelper?) {
        this.panelSwitchHelper = panelSwitchHelper
    }

    init {
        layoutManager = LinearLayoutManager(context)
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        if (e != null && e.action != MotionEvent.ACTION_CANCEL) {
            if (panelSwitchHelper != null) {
                panelSwitchHelper!!.hookSystemBackByPanelSwitcher()
            }
        }
        return super.onTouchEvent(e)
    }
}
