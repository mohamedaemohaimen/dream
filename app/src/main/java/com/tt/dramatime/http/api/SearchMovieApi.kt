package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 搜索剧集
 * </pre>
 */
class SearchMovieApi(val keyWords: String, val pageNum: Int, val pageSize: Int = 10) : IRequestApi {

    override fun getApi(): String {
        return "movie/getAppPage"
    }

    class Bean {
        /**
         * total : 10
         * list : [{"id":"1778669813597405185","code":"mmflLI","title":"My Heart Beats For You","summary":"","tags":["test9"],"cover":"https://img.miiowtv.com/2024/04/12/a81a05c9f5764e988490291646513bf6.jpg","totalEpisode":100},{"id":"1779778712069144577","code":"Z4ZZ8e","title":"His Alluring Ex-wife","summary":"Follows the story of a strong and daring woman who returns to society after a long absence, facing personal and professional challenges. The new queen finds herself in a complex dynamic with her former husband, who now regrets and longs for her. The series explores intricate conflicts and interactions among characters, with compelling plot developments revealing intersecting emotions and new challenges in a world full of secrets and surprises.","tags":["test2"],"cover":"https://img.miiowtv.com/2024/04/30/b9c322e6ea334369bc51274360e6ebee.jpg","totalEpisode":87},{"id":"1775450383388180481","code":"0ynnuQ","title":"My Darling","summary":"The story of a CEO and his long-lost son.","tags":["test9"],"cover":"https://img.miiowtv.com/2024/04/03/f995f2d43e874a9d991a2b50260f1203.jpg","totalEpisode":60},{"id":"1774707416876646401","code":"iyvK4Q","title":"After Taking Revenge, I Married His Uncle","summary":"The series \"Revenge of the Cheating Man: I Married the Little Uncle\" revolves around the story of the main woman, Li Xiaolin, who decides to take revenge after being betrayed by her lover, by cooperating with her little uncle, Li Xiaolen.","tags":["test8"],"cover":"https://img.miiowtv.com/2024/05/25/e900e1d4506d418ca1565d3e48d77851.jpg","totalEpisode":97},{"id":"1774677918227562498","code":"w8SaIv","title":"My Distant Beloved","summary":"A romantic drama series that tells an exciting love story between a young man and a girl from different social backgrounds, who find themselves facing many exciting challenges.","tags":["test8"],"cover":"https://img.miiowtv.com/2024/04/30/51a4fcc411d94b16a45a7c3c7dd7b910.jpg","totalEpisode":87},{"id":"1772977074144780290","code":"5j3lKk","title":"Love Without Return","summary":"Love You Without Asking When You'll Return\" is a poignant tale that explores the depths of human emotion, reminding us that love is not bound by time or circumstance, but by the unwavering commitment to cherish and support each other, even in the face of adversity.","tags":["test7"],"cover":"https://img.miiowtv.com/2024/05/25/26e57c6134314d0797369bc1078ae98b.jpg","totalEpisode":88},{"id":"1770379530797555714","code":"4OJ9WS","title":"A Journey of Mutual Souls","summary":"The events of the series revolve around a unique experience that brings together the souls of two different people, as their bodies are exchanged in an unforgettable adventure. The series explores how this exchange affects their personal and emotional lives, and how each learns the value of each other's life experience.","tags":["test5"],"cover":"https://img.miiowtv.com/2024/05/25/c726b52bc5a24344acb13cc15dfe12cd.jpg","totalEpisode":87},{"id":"1770367208213422081","code":"Wpji34","title":"Gone with the Wind","summary":"In their previous lives, she endured torture and her beloved young leader died. Fate gives them another chance. How will they face it?","tags":["test3"],"cover":"https://img.miiowtv.com/2024/05/25/8e84f4ae1c45499ab232e6f9ae7f71a0.jpg","totalEpisode":89},{"id":"1770374306406047746","code":"gp3m1s","title":"Behind The Mask","summary":"A poignant unrequited love","tags":["test4"],"cover":"https://img.miiowtv.com/2024/05/25/422653993b2c4c07b2a674c58939938e.jpg","totalEpisode":100},{"id":"1759057491822354434","code":"qsydZC","title":"Queen's Counterattack","summary":"The younger sister teamed up with the stepmother to persecute the female lead in order to compete for her sister's husband. She decides to break her silence and rise up in rebellion.","tags":["tes1"],"cover":"https://img.miiowtv.com/2024/04/02/dabacadca02c4c679e9b4bbaed999b67.jpg","totalEpisode":94}]
         */
        @SerializedName("total")
        var total: Int = 0

        @SerializedName("list")
        var list: List<ListBean>? = null

        class ListBean {
            /**
             * id : 1778669813597405185
             * code : mmflLI
             * title : My Heart Beats For You
             * summary :
             * tags : ["test9"]
             * cover : https://img.miiowtv.com/2024/04/12/a81a05c9f5764e988490291646513bf6.jpg
             * totalEpisode : 100
             */
            @SerializedName("id")
            var id: String? = null

            @SerializedName("code")
            var code: String? = null

            @SerializedName("title")
            var title: String? = null

            @SerializedName("summary")
            var summary: String? = null

            @SerializedName("cover")
            var cover: String? = null

            @SerializedName("url")
            var url: String? = null

            @SerializedName("icon")
            var icon: String? = null

            @SerializedName("totalEpisode")
            var totalEpisode: Int = 0

            @SerializedName("tags")
            var tags: List<String>? = null
        }
    }
}