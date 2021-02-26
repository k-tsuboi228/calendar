package jp.co.abs.calender;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.Date;
import java.util.List;

import jp.co.abs.calender.databinding.CalendarCellBinding;

public class CalendarAdapter extends BaseAdapter {
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final CalendarCellBinding binding;
        if (convertView == null) {
            binding = CalendarCellBinding.inflate(mLayoutInflater);
            binding.getRoot().setTag(binding);
        } else {
            binding = (CalendarCellBinding) convertView.getTag();
        }

        //セルのサイズを指定
        float dp = mContext.getResources().getDisplayMetrics().density;
        int cellWidth = parent.getWidth() / 7 - (int) dp;
        int cellHeight = (parent.getHeight() - (int) dp * mDateManager.getWeeks()) / mDateManager.getWeeks();
        ViewGroup.LayoutParams params = binding.getRoot().getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(cellWidth, cellHeight);
        } else {
            params.width = cellWidth;
            params.height = cellHeight;
        }
        binding.getRoot().setLayoutParams(params);

        List<Schedule> scheduleList = PrefUtils.read(mContext, mDateList.get(position));

        // CalendarAdapterDataの引数: ポジションに対する日付,DateManager,ポジションに対する日付に登録されているスケジュールのリスト
        binding.setCalendarAdapterData(new CalendarAdapterData(mDateList.get(position), mDateManager, scheduleList));

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

    @Override
    public void notifyDataSetChanged() {
        mDateList = mDateManager.getDays();

        super.notifyDataSetChanged();
    }

}