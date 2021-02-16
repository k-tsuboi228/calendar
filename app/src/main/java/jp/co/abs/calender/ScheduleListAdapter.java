package jp.co.abs.calender;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import jp.co.abs.calender.databinding.SchedulelistItemBinding;

public class ScheduleListAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private List<Schedule> mScheduleList;

    public ScheduleListAdapter(Context context, List<Schedule> scheduleList) {
        mLayoutInflater = LayoutInflater.from(context);
        mScheduleList = scheduleList;
    }

    @Override
    public int getCount() {
        return mScheduleList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SchedulelistItemBinding binding;
        if (convertView == null) {
            binding = SchedulelistItemBinding.inflate(mLayoutInflater);
            binding.getRoot().setTag(binding);
        } else {
            binding = (SchedulelistItemBinding) convertView.getTag();
        }
        Schedule schedule = mScheduleList.get(position);

        binding.genreImage.setImageResource(schedule.getScheduleGenre().getGenreImageResId());
        binding.timeText.setText(schedule.getTimeText());
        binding.scheduleText.setText(schedule.getScheduleText());

        return binding.getRoot();
    }

    @Override
    public Object getItem(int position) {
        return mScheduleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * スケジュールリストから指定したスケジュールを削除する
     *
     * @param schedule 削除したいスケジュール
     */
    public void remove(Schedule schedule) {
        if (mScheduleList != null) {
            mScheduleList.remove(schedule);
        }
    }
}
