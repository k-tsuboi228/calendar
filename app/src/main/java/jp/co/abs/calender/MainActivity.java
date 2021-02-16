package jp.co.abs.calender;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

import jp.co.abs.calender.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SCHEDULE_DIALOG_TAG = "ScheduleDialog";

    private ActivityMainBinding mBinding;

    private CalendarAdapter mCalendarAdapter;
    private DateManager mDateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mDateManager = new DateManager();

        mBinding.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDateManager.prevMonth();
                mCalendarAdapter.notifyDataSetChanged();
                mBinding.titleText.setText(mCalendarAdapter.getTitle());
            }
        });

        mBinding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDateManager.nextMonth();
                mCalendarAdapter.notifyDataSetChanged();
                mBinding.titleText.setText(mCalendarAdapter.getTitle());
            }
        });

        mCalendarAdapter = new CalendarAdapter(this, mDateManager);
        mBinding.calendarGridView.setAdapter(mCalendarAdapter);
        mBinding.titleText.setText(mCalendarAdapter.getTitle());

        final ScheduleDialog.OnUpdateScheduleListener onUpdateScheduleListener = new ScheduleDialog.OnUpdateScheduleListener() {
            @Override
            public void onUpdateSchedule() {
                mCalendarAdapter.notifyDataSetChanged();
            }
        };

        mBinding.calendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Date selectedDate = (Date) mCalendarAdapter.getItem(position);
                ScheduleDialog scheduleDialog = ScheduleDialog.getInstance(selectedDate);
                scheduleDialog.setOnUpdateScheduleListener(onUpdateScheduleListener);
                scheduleDialog.show(getSupportFragmentManager(), SCHEDULE_DIALOG_TAG);
            }
        });

        ScheduleDialog scheduleDialog = (ScheduleDialog) getSupportFragmentManager().findFragmentByTag(SCHEDULE_DIALOG_TAG);
        if (scheduleDialog != null) {
            scheduleDialog.setOnUpdateScheduleListener(onUpdateScheduleListener);
        }
    }
}