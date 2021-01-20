package jp.co.abs.calender;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView mTitleText;
    private CalendarAdapter mCalendarAdapter;
    private DateManager mDateManager;
    private Schedule mRemoveSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Schedule test = new Schedule("minute", "hour", "schedule");
//        Schedule test2 = new Schedule("minute", "hour", "schedule");
//        Log.i("test", "1 " + test.equals(test));
//        Log.i("test", "2 " + test2.equals(test2));
//        Log.i("test", "3 " + test.equals(test2));

        mDateManager = new DateManager();

        mTitleText = findViewById(R.id.titleText);

        Button prevButton = findViewById(R.id.prevButton);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDateManager.prevMonth();
                mCalendarAdapter.notifyDataSetChanged();
                mTitleText.setText(mCalendarAdapter.getTitle());
            }
        });

        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDateManager.nextMonth();
                mCalendarAdapter.notifyDataSetChanged();
                mTitleText.setText(mCalendarAdapter.getTitle());
            }
        });

        GridView calendarGridView = findViewById(R.id.calenderGridView);
        mCalendarAdapter = new CalendarAdapter(this, mDateManager);
        calendarGridView.setAdapter(mCalendarAdapter);
        mTitleText.setText(mCalendarAdapter.getTitle());

        calendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.i("clicktest", String.valueOf(position));

                mRemoveSchedule = null;

                // メモを追加する機能
                final Date selectedDate = (Date) mCalendarAdapter.getItem(position);

                Log.i("date", "" + mCalendarAdapter.getItem(position));
                View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_dialog, null);

                final TextView modifyScheduleTextView = dialogView.findViewById(R.id.modify_schedule_text);

                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd(E)のメモ", Locale.JAPAN);

                TextView titleView = (TextView) dialogView.findViewById(R.id.dialog_title);
                titleView.setText(format.format(selectedDate));

                final ListView scheduleListView = dialogView.findViewById(R.id.schedule_list);
                List<Schedule> list = PrefUtils.read(MainActivity.this, selectedDate);

                final List<String> scheduleItemList = new ArrayList<>();

                for (Schedule schedule : list) { // listの中身をscheduleに入れている
                    String scheduleText = schedule.getTimeText() + "\t" + schedule.getScheduleText();
                    scheduleItemList.add(scheduleText);
                }

                scheduleListView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, scheduleItemList));

                final EditText scheduleHourEditText = (EditText) dialogView.findViewById(R.id.schedule_hour);
                final EditText scheduleMinuteEditText = (EditText) dialogView.findViewById(R.id.schedule_minutes);
                final EditText scheduleEditText = (EditText) dialogView.findViewById(R.id.dialog_edit_schedule);

                //ListView内スケジュールの編集
                scheduleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mRemoveSchedule = PrefUtils.read(MainActivity.this, selectedDate).get(position);

                        scheduleHourEditText.setText(mRemoveSchedule.getHourText());
                        scheduleMinuteEditText.setText(mRemoveSchedule.getMinuteText());
                        scheduleEditText.setText(mRemoveSchedule.getScheduleText());

                        modifyScheduleTextView.setText(R.string.modify_schedule_title);
                    }
                });

                scheduleListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                        final Schedule removeSchedule = PrefUtils.read(MainActivity.this, selectedDate).get(position);

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("このスケジュールを削除しますか")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        // 削除する処理
                                        List<Schedule> scheduleList = PrefUtils.read(MainActivity.this, selectedDate);
                                        scheduleList.remove(removeSchedule);

                                        PrefUtils.write(MainActivity.this, selectedDate, scheduleList);

                                        scheduleItemList.remove(position);
                                        ((BaseAdapter) scheduleListView.getAdapter()).notifyDataSetChanged();

                                        mCalendarAdapter.notifyDataSetChanged();
                                    }
                                })
                                .create()
                                .show();

                        return true;
                    }
                });

                //保存ボタン
                new AlertDialog.Builder(MainActivity.this)
                        .setView(dialogView)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String scheduleHour = scheduleHourEditText.getText().toString();
                                String scheduleMinute = scheduleMinuteEditText.getText().toString();
                                String scheduleText = scheduleEditText.getText().toString();

                                Schedule schedule = new Schedule(scheduleMinute, scheduleHour, scheduleText);

                                List<Schedule> scheduleList = PrefUtils.read(MainActivity.this, selectedDate);

                                scheduleList.remove(mRemoveSchedule);
                                scheduleList.add(schedule);

                                // メモを保存する処理を入れる
                                PrefUtils.write(MainActivity.this, selectedDate, scheduleList);
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

}