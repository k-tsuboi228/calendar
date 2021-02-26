package jp.co.abs.calender;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Date;

import jp.co.abs.calender.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SCHEDULE_DIALOG_TAG = "ScheduleDialog";

    private ActivityMainBinding mBinding;
    private CalendarAdapter mCalendarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.setLifecycleOwner(this);
        mBinding.setViewModel(new ViewModelProvider(this).get(MainViewModel.class));
        mBinding.getViewModel().getTitleText().observe(this, s -> mCalendarAdapter.notifyDataSetChanged());

        mCalendarAdapter = new CalendarAdapter(this, mBinding.getViewModel().getDateManager());
        mBinding.calendarGridView.setAdapter(mCalendarAdapter);

        final ScheduleDialog.OnUpdateScheduleListener onUpdateScheduleListener = () -> mCalendarAdapter.notifyDataSetChanged();

        mBinding.calendarGridView.setOnItemClickListener((parent, view, position, id) -> {
            Date selectedDate = (Date) mCalendarAdapter.getItem(position);
            ScheduleDialog scheduleDialog = ScheduleDialog.getInstance(selectedDate);
            scheduleDialog.setOnUpdateScheduleListener(onUpdateScheduleListener);
            scheduleDialog.show(getSupportFragmentManager(), SCHEDULE_DIALOG_TAG);
        });

        ScheduleDialog scheduleDialog = (ScheduleDialog) getSupportFragmentManager().findFragmentByTag(SCHEDULE_DIALOG_TAG);
        if (scheduleDialog != null) {
            scheduleDialog.setOnUpdateScheduleListener(onUpdateScheduleListener);
        }
    }
}