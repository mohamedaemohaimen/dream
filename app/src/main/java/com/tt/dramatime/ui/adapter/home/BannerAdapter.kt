package com.tt.dramatime.ui.adapter.home

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.hjq.language.MultiLanguages
import com.hjq.shape.layout.ShapeLinearLayout
import com.tt.dramatime.R
import com.tt.dramatime.http.api.BannerApi
import com.tt.dramatime.manager.LanguageManager
import com.tt.dramatime.ui.adapter.home.BannerAdapter.ImageTitleHolder
import com.tt.dramatime.util.GlideUtils.Companion.loadImage
import com.tt.dramatime.widget.fonttext.FontTextView
import com.tt.dramatime.widget.fonttext.MyShapeTextView
import com.youth.banner.adapter.BannerAdapter

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 首页banner适配器
 * </pre>
 */
class BannerAdapter(mData: List<BannerApi.Bean?>?) :
    BannerAdapter<BannerApi.Bean?, ImageTitleHolder>(mData) {

    private val timerMap = mutableMapOf<Int, CountDownTimer>()

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): ImageTitleHolder {
        return ImageTitleHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.banner_image_title, parent, false)
        )
    }

    override fun onBindView(
        holder: ImageTitleHolder, data: BannerApi.Bean?, position: Int, size: Int
    ) {

        holder.apply {
            loadImage(imageView.context, data?.poster, imageView)

            playFl.visibility = if (data?.urlType == "1") View.VISIBLE else View.GONE

            countDownLl.visibility = View.GONE

            playTv.text = playTv.context.getString(R.string.play)

            /*val timestamp = if (position == 0) 1725616920000 else System.currentTimeMillis()
            timestamp.apply {*/

            data?.activity?.timestamp?.apply {
                // 计算剩余时间
                val totalTime = this.minus(System.currentTimeMillis())

                if (totalTime <= 0) return

                //为了防止还没满1分钟，倒计时会显示为0，这里加1分钟
                totalTime.plus(1000 * 60)

                playTv.text = playTv.context.getString(R.string.trailer)

                setCountDown(totalTime, holder)

                countDownLl.visibility = View.VISIBLE

                if (LanguageManager.isArabicLocale(countDownLl.context)) {
                    countDownLl.shapeDrawableBuilder.setRadius(
                        dp2px(16f).toFloat(), 0f, 0f, dp2px(16f).toFloat()
                    ).intoBackground()
                } else {
                    countDownLl.shapeDrawableBuilder.setRadius(
                        0f, dp2px(16f).toFloat(), dp2px(16f).toFloat(), 0f
                    ).intoBackground()
                }

                if (timerMap.size > position) timerMap[position]?.cancel()

                val countDownTimer = object : CountDownTimer(totalTime, 1000 * 60) {
                    override fun onTick(millisUntilFinished: Long) {
                        setCountDown(millisUntilFinished, holder)
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onFinish() {
                        // 倒计时完成时执行的操作
                        playTv.text = playTv.context.getString(R.string.play)
                        countDownLl.visibility = View.GONE
                    }
                }

                timerMap[position] = countDownTimer
                countDownTimer.start()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setCountDown(
        millisUntilFinished: Long,
        holder: ImageTitleHolder
    ) {
        // 计算剩余的小时、分钟和秒数
        val days = millisUntilFinished / (1000 * 60 * 60 * 24)
        val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
        val minutes = (millisUntilFinished / (1000 * 60)) % 60
        // 更新 UI
        holder.apply {
            val local = MultiLanguages.getAppLanguage(dayTv.context)
            val daysString = String.format(local, "%d", days)
            val hoursString = String.format(local, "%d", hours)
            val minutesString = String.format(local, "%d", minutes)

            if (daysString == "0" && hoursString == "0" && minutesString == "0") {
                playTv.text = playTv.context.getString(R.string.play)
                countDownLl.visibility = View.GONE
            }else{
                dayTv.text = daysString
                hourTv.text = hoursString
                minutesTv.text = minutesString
            }
        }
    }

    class ImageTitleHolder(view: View) : RecyclerView.ViewHolder(view) {
        var countDownLl: ShapeLinearLayout = view.findViewById(R.id.count_down_ll)
        var dayTv: MyShapeTextView = view.findViewById(R.id.day_tv)
        var hourTv: MyShapeTextView = view.findViewById(R.id.hour_tv)
        var minutesTv: MyShapeTextView = view.findViewById(R.id.minutes_tv)
        var imageView: ImageView = view.findViewById(R.id.image)
        var playFl: FrameLayout = view.findViewById(R.id.play_fl)
        var playTv: FontTextView = view.findViewById(R.id.play_tv)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        cancelTimer()
    }

    private fun cancelTimer() {
        timerMap.values.forEach { it.cancel() }
        timerMap.clear()
    }
}
