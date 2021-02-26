package jp.co.abs.calender

import android.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapterData(date: Date, dateManager: DateManager, scheduleList: List<Schedule>) {
    // 日付によるcellの背景色ID
    val backgroundColor: Int = when {
        dateManager.isCurrentDay(date) -> Color.YELLOW // 当日の日付を黄色にする
        dateManager.isDisplayingMonth(date) -> Color.WHITE // 表示中の月の日付を白色にする
        else -> Color.LTGRAY // 表示中の月以外の日付を灰色にする
    }

    // 曜日によるテキストの色ID
    val textColor: Int = when {
        dateManager.getDayOfWeek(date) == Calendar.SUNDAY -> Color.RED
        dateManager.getDayOfWeek(date) == Calendar.SATURDAY -> Color.BLUE
        else -> Color.BLACK // Defaultの値
    }

    // 日付のテキスト
    private val dateFormat = SimpleDateFormat("d", Locale.US)
    val dateText: String = dateFormat.format(date)

    // ImageDrawableResourceID
    val scheduleImageResourceId: Int? = if (scheduleList.isEmpty()) null else R.drawable.ic_baseline_schedule_24
}