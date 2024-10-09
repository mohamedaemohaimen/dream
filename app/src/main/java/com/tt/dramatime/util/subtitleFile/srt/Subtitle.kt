package com.tt.dramatime.util.subtitleFile.srt

class Subtitle {
    var id: String? = "0"
    var startTime: String? = null
    var endTime: String? = null
    var text: String? = null
    var timeIn: Long = 0
    var timeOut: Long = 0
    var nextSubtitle: Subtitle? = null

    override fun toString(): String {
        return ("Subtitle [id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", text=" + text
                + ", timeIn=" + timeIn + ", timeOut=" + timeOut + "]")
    }
}