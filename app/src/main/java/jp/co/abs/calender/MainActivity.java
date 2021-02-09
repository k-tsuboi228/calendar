package jp.co.abs.calender;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SCHEDULE_DIALOG_TAG = "ScheduleDialog";

    private TextView mTitleText;
    private CalendarAdapter mCalendarAdapter;
    private DateManager mDateManager;

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

        final ScheduleDialog.OnUpdateScheduleListener onUpdateScheduleListener = new ScheduleDialog.OnUpdateScheduleListener() {
            @Override
            public void onUpdateSchedule() {
                mCalendarAdapter.notifyDataSetChanged();
            }
        };

        calendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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