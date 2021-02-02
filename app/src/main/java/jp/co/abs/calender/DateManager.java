package jp.co.abs.calender;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateManager {
    private Calendar mCalendar;

    public DateManager() {
        mCalendar = Calendar.getInstance();
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    //当月の要素を取得
    public List<Date> getDays() {
        //現在の状態を保持
        Date startDate = mCalendar.getTime();

        //GridViewに表示するマスの合計を計算
        int dateCount = getWeeks() * 7;

        //当月のカレンダーに表示される前月分の日数を計算
        mCalendar.set(Calendar.DATE, 1);
        int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        mCalendar.add(Calendar.DATE, -dayOfWeek);

        List<Date> days = new ArrayList<>();

        for (int i = 0; i < dateCount; i++) {
            days.add(mCalendar.getTime());
            mCalendar.add(Calendar.DATE, 1);
        }

        //状態を復元
        mCalendar.setTime(startDate);

        return days;
    }

    /**
     * 表示中の月かどうか確認
     *
     * @param date 確認したい日付
     * @return true:表示中の月 false:表示中の月以外
     */
    public boolean isDisplayingMonth(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM", Locale.US);
        String currentMonth = format.format(mCalendar.getTime());
        return currentMonth.equals(format.format(date));
    }

    /**
     * 当日の日付かどうか確認
     *
     * @param date 　確認したい日付
     * @return true:当日の日付 false:当日の日付以外
     */
    public boolean isCurrentDay(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd", Locale.US);
        String currentDay = format.format(new Date());
        return currentDay.equals(format.format(date));
    }

    //週数を取得
    public int getWeeks() {
        return mCalendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
    }

    //曜日を取得
    public int getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    //翌月へ
    public void nextMonth() {
        mCalendar.add(Calendar.MONTH, 1);
    }

    //前月へ
    public void prevMonth() {
        mCalendar.add(Calendar.MONTH, -1);
    }
}