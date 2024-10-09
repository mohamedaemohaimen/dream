package com.tt.dramatime.http.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LimitDialogNoticeBean(
    val idList: List<String>
) : Parcelable