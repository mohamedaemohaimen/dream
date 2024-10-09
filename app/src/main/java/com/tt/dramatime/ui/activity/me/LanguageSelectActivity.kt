package com.tt.dramatime.ui.activity.me

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
import com.hjq.language.LocaleContract
import com.hjq.language.MultiLanguages
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.LanguageSelectActivityBinding
import com.tt.dramatime.http.api.UpdateLanguageApi
import com.tt.dramatime.http.db.MMKVDurableConstant
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.MyListHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.util.CustomLinearLayoutManager
import java.util.Locale


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 语言选择
 * </pre>
 */
@SuppressLint("NotifyDataSetChanged")
class LanguageSelectActivity : BaseViewBindingActivity<LanguageSelectActivityBinding>({
    LanguageSelectActivityBinding.inflate(it)
}) {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LanguageSelectActivity::class.java)
            if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig().navigationBarColor(R.color.color_F6F7F9)
    }

    private var onItemClickListener: OnItemClickListener? = null
    private val languageMap: MutableMap<String, String> = HashMap()
    private val languageList: MutableList<Pair<String, String>> = ArrayList()
    private val adapter by lazy {
        SelectAdapter()
    }
    private var currentLanguage: String? = null
    private var currentLanguageName: String? = null


    override fun initView() {
        currentLanguage = MultiLanguages.getAppLanguage(getContext()).language
        if (TextUtils.isEmpty(currentLanguage)) {
            val locale: Locale = resources.configuration.locales[0]
            currentLanguage = locale.language
        }

        binding.themeRecyclerView.adapter = adapter
        binding.themeRecyclerView.layoutManager = CustomLinearLayoutManager(this)
        onItemClickListener = object : OnItemClickListener {
            override fun onClick(language: String?) {
                currentLanguage =
                    if (TextUtils.equals(currentLanguage, language)) return else language
                selectCurrentLanguage()
                adapter.notifyDataSetChanged()
            }
        }
        setOnClickListener(binding.confirmTv)
    }

    override fun initData() {
        languageList.clear()
        languageMap.clear()
        currentLanguageName = getString(R.string.current_language)
        val english = Pair.create(
            getString(R.string.language_english_key), getString(R.string.language_english)
        )
        val arabic = Pair.create(
            getString(R.string.language_arabic_key), getString(R.string.language_arabic)
        )
        val japanese = Pair.create(
            getString(R.string.language_japanese_key), getString(R.string.language_japanese)
        )
        val spain = Pair.create(
            getString(R.string.language_spain_key), getString(R.string.language_spain)
        )
        languageList.add(english)
        languageMap[english.first] = LocaleContract.getEnglishLocale().language
        languageList.add(arabic)
        languageMap[arabic.first] = LocaleContract.getArabicLocale().language
        languageList.add(japanese)
        languageMap[japanese.first] = LocaleContract.getJapaneseLocale().language
        languageList.add(spain)
        languageMap[spain.first] = LocaleContract.getSpainLocale().language
        selectCurrentLanguage()
        adapter.notifyDataSetChanged()
    }

    override fun onClick(view: View) {
        updateLanguage()
    }

    private fun updateLanguage() {
        EasyHttp.post(this).api(currentLanguage?.let { UpdateLanguageApi(it) })
            .request(object : HttpCallbackProxy<HttpData<UpdateLanguageApi.Bean?>>(this) {
                override fun onHttpSuccess(data: HttpData<UpdateLanguageApi.Bean?>) {
                    data.getData()?.contentLanguage?.let { contentLanguage ->
                        MMKVExt.getDurableMMKV()
                            ?.encode(MMKVDurableConstant.KEY_CONTENT_LANGUAGE, contentLanguage)
                    }
                    when (currentLanguage) {
                        LocaleContract.getArabicLocale().language -> MultiLanguages.setAppLanguage(
                            getContext(), LocaleContract.getArabicLocale()
                        )

                        LocaleContract.getJapaneseLocale().language -> MultiLanguages.setAppLanguage(
                            getContext(), LocaleContract.getJapaneseLocale()
                        )

                        LocaleContract.getSpainLocale().language -> MultiLanguages.setAppLanguage(
                            getContext(), LocaleContract.getSpainLocale()
                        )

                        else -> MultiLanguages.setAppLanguage(
                            getContext(), LocaleContract.getEnglishLocale()
                        )
                    }

                    //切换语言清除观看历史
                    MyListHelper.setMyList(mutableListOf())

                    finish()
                }

                override fun onHttpFail(throwable: Throwable?) {
                    toast(throwable?.message)
                }
            })
    }

    private fun selectCurrentLanguage() {
        if (TextUtils.equals(currentLanguage, LocaleContract.getEnglishLocale().language)) {
            adapter.setSelectedItem(0)
        } else if (TextUtils.equals(currentLanguage, LocaleContract.getArabicLocale().language)) {
            adapter.setSelectedItem(1)
        } else if (TextUtils.equals(currentLanguage, LocaleContract.getJapaneseLocale().language)) {
            adapter.setSelectedItem(2)
        }else if (TextUtils.equals(currentLanguage, LocaleContract.getSpainLocale().language)) {
            adapter.setSelectedItem(3)
        } else {
            adapter.setSelectedItem(0)
        }
    }

    internal inner class SelectAdapter : RecyclerView.Adapter<SelectAdapter.SelectViewHolder>() {
        private var selectedItem = -1
        fun setSelectedItem(selectedItem: Int) {
            this.selectedItem = selectedItem
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectViewHolder {
            val view = LayoutInflater.from(this@LanguageSelectActivity).inflate(
                R.layout.language_select_item_layout, parent, false
            )
            return SelectViewHolder(view)
        }

        override fun onBindViewHolder(holder: SelectViewHolder, position: Int) {
            val languagePair = languageList[position]
            holder.name.text = languagePair.first
            holder.selectedIcon.setImageResource(
                if (selectedItem == position) R.drawable.language_sel_ic else R.drawable.language_un_ic
            )
            holder.name.setTextColor(
                ContextCompat.getColor(
                    getContext(),
                    if (selectedItem == position) R.color.color_C640FF else R.color.black
                )
            )
            holder.itemView.setOnClickListener { onItemClickListener!!.onClick(languageMap[languagePair.first]) }
        }

        override fun getItemCount(): Int {
            return languageMap.size
        }

        internal inner class SelectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var name: TextView = itemView.findViewById(R.id.name)
            var selectedIcon: ImageView = itemView.findViewById(R.id.selected_icon)
        }
    }

    interface OnItemClickListener {
        fun onClick(language: String?)
    }

}