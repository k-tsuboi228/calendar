package jp.co.abs.calender;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarAdapter extends BaseAdapter {
    private static final Map<Integer, Integer> DAYS_COLOR = Collections.unmodifiableMap(new HashMap<Integer, Integer>() {{
        put(Calendar.SUNDAY, Color.RED);
        put(Calendar.SATURDAY, Color.BLUE);
    }});

    private List<Date> mDateList;
    private Context mContext;
    private DateManager mDateManager;
    private LayoutInflater mLayoutInflater;

    //カスタムセルを拡張したらここでWidgetを定義
    private static class ViewHolder {
        public TextView dateText;
        public List<ImageView> genreImageViewList;
    }

    public CalendarAdapter(Context context, DateManager dateManager) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mDateManager = dateManager;
        mDateList = mDateManager.getDays();
    }

    @Override
    public int getCount() {
        return mDateList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.calender_cell, parent, false);
            holder = new ViewHolder();
            holder.dateText = convertView.findViewById(R.id.date_text);

            holder.genreImageViewList = new ArrayList<>();
            for (ScheduleGenre scheduleGenre : ScheduleGenre.values()) {
                ImageView genreImage = new ImageView(mContext);
                holder.genreImageViewList.add(genreImage);
                LinearLayout linearLayout = (LinearLayout) convertView;
                linearLayout.addView(genreImage);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //セルのサイズを指定
        float dp = mContext.getResources().getDisplayMetrics().density;
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(parent.getWidth() / 7 - (int) dp, (parent.getHeight() - (int) dp * mDateManager.getWeeks()) / mDateManager.getWeeks());
        convertView.setLayoutParams(params);

        //日付のみ表示させる
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.US);
        holder.dateText.setText(dateFormat.format(mDateList.get(position)));

        List<Schedule> scheduleList = PrefUtils.read(mContext, mDateList.get(position));
        for (ScheduleGenre scheduleGenre : ScheduleGenre.values()) {
            holder.genreImageViewList.get(scheduleGenre.ordinal()).setImageDrawable(null);
            for (Schedule schedule : scheduleList) {
                if (scheduleGenre == schedule.getScheduleGenre()) {
                    holder.genreImageViewList.get(scheduleGenre.ordinal()).setImageResource(scheduleGenre.getGenreImageResId());
                    break;
                }
            }
        }

        if (mDateManager.isCurrentDay(mDateList.get(position))) {
            // 当日の日付を黄色にする
            convertView.setBackgroundColor(Color.YELLOW);
        } else if (mDateManager.isDisplayingMonth(mDateList.get(position))) {
            // 表示中の月の日付を白色にする
            convertView.setBackgroundColor(Color.WHITE);
        } else {
            // 表示中の月以外の日付を灰色にする
            convertView.setBackgroundColor(Color.LTGRAY);
        }

        //日曜日を赤、土曜日を青に
        Integer colorId = DAYS_COLOR.get(mDateManager.getDayOfWeek(mDateList.get(position)));
        if (colorId != null) {
            holder.dateText.setTextColor(colorId);
        } else {
            holder.dateText.setTextColor(Color.BLACK); // Defaultの値
        }
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mDateList.get(position);
    }

    //表示月を取得
    public String getTitle() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM", Locale.US);
        return format.format(mDateManager.getCalendar().getTime());
    }

    @Override
    public void notifyDataSetChanged() {
        mDateList = mDateManager.getDays();

        super.notifyDataSetChanged();
    }

}