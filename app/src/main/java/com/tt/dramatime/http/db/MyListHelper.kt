package com.tt.dramatime.http.db

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import com.hjq.gson.factory.GsonFactory
import com.tt.dramatime.http.api.MovieListApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/12/7
 *   desc : 观看记录管理
 * </pre>
 */
object MyListHelper {

    private var _myList: List<MovieListApi.Bean.ListBean>? = null
    val myList:List<MovieListApi.Bean.ListBean> get() = _myList!!

    init {
        if (_myList == null) {
            try {
                val jsonStr = MMKVExt.getUserMmkv()?.decodeString(MMKVUserConstant.KEY_MY_LIST)
                if (!TextUtils.isEmpty(jsonStr)) {
                    _myList = GsonFactory.getSingletonGson()
                        .fromJson(jsonStr, object : TypeToken<ArrayList<MovieListApi.Bean.ListBean>?>() {}.type)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (_myList == null) {
            /* 真就为空了、 构建一个 */
            _myList = mutableListOf()
        }
    }

    fun setMyList(myList:List<MovieListApi.Bean.ListBean>) {
        _myList = myList
        saveMyList()
    }

    private fun saveMyList() {
        if (_myList == null) {
            return
        }
        MMKVExt.getUserMmkv()?.encode(MMKVUserConstant.KEY_MY_LIST, GsonFactory.getSingletonGson().toJson(myList))
    }

}