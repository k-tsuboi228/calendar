package jp.co.abs.calender;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.co.abs.calender.databinding.CalendarCellBinding;

public class CalendarAdapter extends BaseAdapter {
    private static final Map<Integer, Integer> DAYS_COLOR = Collections.unmodifiableMap(new HashMap<Integer, Integer>() {{
        put(Calendar.SUNDAY, Color.RED);
        put(Calendar.SATURDAY, Color.BLUE);
    }});

    private List<Date> mDateList;
    private Context mContext;
    private DateManager mDateManager;
    private LayoutInflater mLayoutInflater;

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
        CalendarCellBinding binding;
        if (convertView == null) {
            binding = CalendarCellBinding.inflate(mLayoutInflater);
            binding.getRoot().setTag(binding);
        } else {
            binding = (CalendarCellBinding) convertView.getTag();
        }

        //セルのサイズを指定
        float dp = mContext.getResources().getDisplayMetrics().density;
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(parent.getWidth() / 7 - (int) dp, (parent.getHeight() - (int) dp * mDateManager.getWeeks()) / mDateManager.getWeeks());
        binding.getRoot().setLayoutParams(params);

        //日付のみ表示させる
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.US);
        binding.dateText.setText(dateFormat.format(mDateList.get(position)));

        List<Schedule> scheduleList = PrefUtils.read(mContext, mDateList.get(position));
        if (scheduleList.isEmpty()) {
            binding.genreImage.setImageDrawable(null);
        } else {
            binding.genreImage.setImageResource(R.drawable.ic_baseline_schedule_24);
        }

        if (mDateManager.isCurrentDay(mDateList.get(position))) {
            // 当日の日付を黄色にする
            binding.getRoot().setBackgroundColor(Color.YELLOW);
        } else if (mDateManager.isDisplayingMonth(mDateList.get(position))) {
            // 表示中の月の日付を白色にする
            binding.getRoot().setBackgroundColor(Color.WHITE);
        } else {
            // 表示中の月以外の日付を灰色にする
            binding.getRoot().setBackgroundColor(Color.LTGRAY);
        }

        //日曜日を赤、土曜日を青に
        Integer colorId = DAYS_COLOR.get(mDateManager.getDayOfWeek(mDateList.get(position)));
        if (colorId != null) {
            binding.dateText.setTextColor(colorId);
        } else {
            binding.dateText.setTextColor(Color.BLACK); // Defaultの値
        }
        return binding.getRoot();
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