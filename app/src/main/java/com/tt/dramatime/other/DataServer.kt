package com.tt.dramatime.other


/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
object DataServer {
    const val HTTPS_AVATARS1_GITHUBUSERCONTENT_COM_LINK =
        "https://avatars1.githubusercontent.com/u/7698209?v=3&s=460"
    const val CYM_CHAD = "CymChad"
    const val CHAY_CHAN = "ChayChan"

    fun getSampleData(lenth: Int): MutableList<String> {
        val list: MutableList<String> = ArrayList()
        for (i in 0 until lenth) {
            val status: String = "Chad$i"
            list.add(status)
        }
        return list
    }

    val strData: List<String>
        get() {
            val list: MutableList<String> = ArrayList()
            for (i in 0..19) {
                var str = HTTPS_AVATARS1_GITHUBUSERCONTENT_COM_LINK
                if (i % 2 == 0) {
                    str = CYM_CHAD
                }
                list.add(str)
            }
            return list
        }
}