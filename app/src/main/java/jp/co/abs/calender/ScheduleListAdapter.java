package jp.co.abs.calender;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ScheduleListAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private List<Schedule> mScheduleList;

    private static class ViewHolder {
        public ImageView genreImageView;
        public TextView timeTextView;
        public TextView scheduleTextView;
    }

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
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.schedulelist_item, parent, false);
            holder = new ViewHolder();
            holder.genreImageView = convertView.findViewById(R.id.genre_image);
            holder.timeTextView = convertView.findViewById(R.id.time_text);
            holder.scheduleTextView = convertView.findViewById(R.id.schedule_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Schedule schedule = mScheduleList.get(position);
        holder.genreImageView.setImageResource(schedule.getScheduleGenre().getGenreImageResId());
        holder.timeTextView.setText(schedule.getTimeText());
        holder.scheduleTextView.setText(schedule.getScheduleText());

        return convertView;
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
