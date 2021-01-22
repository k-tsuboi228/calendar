package jp.co.abs.calender;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mTitleText;
    private CalendarAdapter mCalendarAdapter;
    private DateManager mDateManager;
    private Schedule mRemoveSchedule;

    private static final String INTENT_NAME_REQUEST = "RequestCode";
    private static final String INTENT_NAME_SCHEDULE_TEXT = "ScheduleText";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                InputFilter inputHourFilter = new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        String string = dest.toString() + source.toString();
                        if (string.matches("^([2][0-3]|[0-1][0-9]|[0-9])$")) {
                            return source;
                        } else {
                            return "";
                        }
                    }
                };
                scheduleHourEditText.setFilters(new InputFilter[]{inputHourFilter});

                InputFilter inputMinuteFilter = new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        String string = dest.toString() + source.toString();
                        if (string.matches("^([0-5][0-9]|[0-9])$")) {
                            return source;
                        } else {
                            return "";
                        }
                    }
                };
                scheduleMinuteEditText.setFilters(new InputFilter[]{inputMinuteFilter});

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

                                        //スケジュールを削除する場合、アラームを解除
                                        deleteAlarm(removeSchedule);
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
                                int requestCode = PrefUtils.nextRequestCode(MainActivity.this);

                                Schedule schedule = new Schedule(scheduleMinute, scheduleHour, scheduleText, requestCode);

                                List<Schedule> scheduleList = PrefUtils.read(MainActivity.this, selectedDate);

                                scheduleList.remove(mRemoveSchedule);
                                scheduleList.add(schedule);

                                // メモを保存する処理を入れる
                                PrefUtils.write(MainActivity.this, selectedDate, scheduleList);

                                //　スケジュール編集前のアラームを解除
                                deleteAlarm(mRemoveSchedule);

                                setAlarm(schedule, selectedDate);
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    // アラームの削除
    private void deleteAlarm(Schedule schedule) {
        if (schedule == null) return;
        Intent intent = new Intent(getApplicationContext(), AlarmNotification.class);
        PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), schedule.getRequestCode(), intent, 0);
        // アラームを解除する
        AlarmManager am = (AlarmManager) MainActivity.this.getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.cancel(pending);
        }
    }

    // アラームの登録
    private void setAlarm(Schedule schedule, Date selectedDate) {
        if (schedule == null || selectedDate == null) return;
        Intent intent = new Intent(getApplicationContext(), AlarmNotification.class);
        intent.putExtra(INTENT_NAME_REQUEST, schedule.getRequestCode());
        intent.putExtra(INTENT_NAME_SCHEDULE_TEXT, schedule.getScheduleText());
        PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), schedule.getRequestCode(), intent, 0);

        // アラームをセットする
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTime(selectedDate);
        alarmTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(schedule.getHourText()));
        alarmTime.set(Calendar.MINUTE, Integer.parseInt(schedule.getMinuteText()));

        if (am != null) {
            am.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pending);
            Log.i(TAG, "alarmTime: " + alarmTime);
        }
    }
}