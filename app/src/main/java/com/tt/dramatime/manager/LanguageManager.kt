package com.tt.dramatime.manager

import android.content.Context
import com.hjq.language.LocaleContract
import com.hjq.language.MultiLanguages

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/3/18 13:59
 *   Desc : 语言管理类
 * </pre>
 */
object LanguageManager {

    fun isArabicLocale(context: Context): Boolean {
        return MultiLanguages.getAppLanguage(context).language == LocaleContract.getArabicLocale().language
    }

    fun isJapaneseLocale(context: Context): Boolean {
        return MultiLanguages.getAppLanguage(context).language == LocaleContract.getJapaneseLocale().language
    }

    fun isSpainLocale(context: Context): Boolean {
        return MultiLanguages.getAppLanguage(context).language == LocaleContract.getSpainLocale().language
    }


}