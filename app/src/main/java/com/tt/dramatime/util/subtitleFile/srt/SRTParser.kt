package com.tt.dramatime.util.subtitleFile.srt

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.Logger
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_CONTENT_LANGUAGE
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.util.subtitleFile.SRTUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object SRTParser {

    private val PATTERN_TIME: Pattern =
        Pattern.compile("([\\d]{2}:[\\d]{2}:[\\d]{2},[\\d]{3}).*([\\d]{2}:[\\d]{2}:[\\d]{2},[\\d]{3})")
    private val PATTERN_NUMBERS: Pattern = Pattern.compile("(\\d+)")
    private val DEFAULT_CHARSET: Charset = StandardCharsets.UTF_8

    private const val REGEX_REMOVE_TAGS = "<[^>]*>"

    private const val PATTERN_TIME_REGEX_GROUP_START_TIME = 1
    private const val PATTERN_TIME_REGEX_GROUP_END_TIME = 2

    /**
     * This method is responsible for parsing a STR file.
     *
     *
     * This method will not have any new line and also will not make the use of nodes see: Node [SRTParser.getSubtitlesFromFile]}
     *
     *
     * Metodo responsavel por fazer parse de um arquivos de legenda. <br></br>
     * Obs. O texto nao vai conter quebra de linhas e nao é utilizado Node [SRTParser.getSubtitlesFromFile]}
     *
     * @param path
     * @return
     */
    fun getSubtitlesFromFile(path: String): ArrayList<Subtitle>? {
        return getSubtitlesFromFile(path, false, false)
    }

    /**
     * This method is responsible for parsing a STR file.
     *
     *
     * This method will not have any new line and also will not make the use of nodes see: Node [SRTParser.getSubtitlesFromFile]}
     *
     *
     * Metodo responsavel por fazer parse de um arquivos de legenda. <br></br>
     * Obs. O texto nao vai conter quebra de linhas e nao é utilizado Node [SRTParser.getSubtitlesFromFile]}
     *
     * @param path 文件地址
     * @return ArrayList<Subtitle>
    </Subtitle> */
    fun getSubtitlesFromFile(path: String, keepNewlinesEscape: Boolean): ArrayList<Subtitle>? {
        return getSubtitlesFromFile(path, keepNewlinesEscape, false)
    }

    /**
     * This method is responsible for parsing a STR file.
     *
     *
     * This method will not have any new line and also will not make the use of nodes see: Node [SRTParser.getSubtitlesFromFile]}
     * Note that you can configure if you want to make the use of Nodes: by setting the parameter usingNodes to true
     *
     *
     * Metodo responsavel por fazer parse de um arquivos de legenda. <br></br>
     *
     * @param path
     * @param keepNewlinesEscape
     * @param usingNodes
     * @return
     */
    @JvmStatic
    fun getSubtitlesFromFile(
        path: String, keepNewlinesEscape: Boolean, usingNodes: Boolean
    ): ArrayList<Subtitle>? {
        var subtitles: ArrayList<Subtitle>? = null
        var subtitle: Subtitle
        var srt: StringBuilder

        try {
            BufferedReader(
                InputStreamReader(FileInputStream(path), DEFAULT_CHARSET)
            ).use { bufferedReader ->
                subtitles = ArrayList()
                subtitle = Subtitle()
                srt = StringBuilder()
                while (bufferedReader.ready()) {
                    var line = bufferedReader.readLine()

                    var matcher = PATTERN_NUMBERS.matcher(line)

                    if (matcher.find()) {
                        subtitle.id = matcher.group(1) ?: "0" // index
                        line = bufferedReader.readLine()
                    }

                    matcher = PATTERN_TIME.matcher(line)

                    if (matcher.find()) {
                        subtitle.startTime =
                            matcher.group(PATTERN_TIME_REGEX_GROUP_START_TIME) // start time
                        subtitle.timeIn = SRTUtils.textTimeToMillis(subtitle.startTime)
                        subtitle.endTime =
                            matcher.group(PATTERN_TIME_REGEX_GROUP_END_TIME) // end time
                        subtitle.timeOut = SRTUtils.textTimeToMillis(subtitle.endTime)
                    }

                    var aux: String
                    while ((bufferedReader.readLine()
                            .also { aux = it }) != null && aux.isNotEmpty()
                    ) {
                        srt.append(aux)
                        if (keepNewlinesEscape) srt.append("\n")
                        else {
                            if (!line!!.endsWith(" ")) // for any new lines '\n' removed from BufferedReader
                                srt.append(" ")
                        }
                    }

                    srt.delete(srt.length - 1, srt.length) // remove '\n' or space from end string

                    line = srt.toString()
                    srt.setLength(0) // Clear buffer

                    if (line.isNotEmpty()) line =
                        line.replace(REGEX_REMOVE_TAGS.toRegex(), "") // clear all tags

                    subtitle.text = line
                    subtitles!!.add(subtitle)

                    if (usingNodes) {
                        subtitle.nextSubtitle = Subtitle()
                        subtitle = subtitle.nextSubtitle!!
                    } else {
                        subtitle = Subtitle()
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("error parsing srt file:" + e.message)
            Firebase.crashlytics.recordException(
                Exception(
                    "getSubtitlesFromFileException:${e.message}," + "用户ID：${UserProfileHelper.getUserId()},path:${path},lang:${
                        MMKVExt.getDurableMMKV()?.getString(KEY_CONTENT_LANGUAGE, "")
                    }"
                )
            )
        }
        return subtitles
    }

    /**
     * 解析SRT文件
     *
     * @param file 字幕srt文件
     * @return 字幕数组
     */
    fun parseSRT(file: File): ArrayList<Subtitle> {
        val subtitles = arrayListOf<Subtitle>()
        val bufferedReader = BufferedReader(file.inputStream().bufferedReader())

        var index: String? = null
        var startTime: String? = null
        var endTime: String? = null
        var text: StringBuilder? = null
        try {
            bufferedReader.use { reader ->
                reader.lineSequence().forEach { line ->
                    when {
                        line.trim().isEmpty() -> {
                            // 空行，表示一个字幕块结束
                            if (index != null && startTime != null && endTime != null && text != null) {
                                val subtitle = Subtitle()
                                subtitle.id = index
                                subtitle.startTime = startTime
                                subtitle.timeIn = SRTUtils.textTimeToMillis(subtitle.startTime)
                                subtitle.endTime = endTime
                                subtitle.timeOut = SRTUtils.textTimeToMillis(subtitle.endTime)
                                subtitle.text = text.toString().trim()
                                subtitles.add(subtitle)
                            }
                            // 重置
                            index = null
                            startTime = null
                            endTime = null
                            text = StringBuilder()
                        }

                        index == null -> {
                            // 读取序号
                            index = line.trim()
                        }

                        startTime == null -> {
                            // 读取时间戳
                            val times = line.trim().split(" --> ")
                            startTime = times[0]
                            endTime = times[1]
                        }

                        else -> {
                            // 读取字幕文本
                            if (text == null) {
                                text = StringBuilder()
                            }
                            text?.appendLine(line)
                        }
                    }
                }
            }

            // 处理最后一个字幕块（如果没有空行结束）
            if (index != null && startTime != null && endTime != null && text != null) {
                val subtitle = Subtitle()
                subtitle.id = index
                subtitle.startTime = startTime
                subtitle.timeIn = SRTUtils.textTimeToMillis(subtitle.startTime)
                subtitle.endTime = endTime
                subtitle.timeOut = SRTUtils.textTimeToMillis(subtitle.endTime)
                subtitle.text = text.toString().trim()
                subtitles.add(subtitle)
            }
        } catch (e: Exception) {
            Logger.e("error parsing srt file:" + e.message)

        }
        return subtitles
    }
}
