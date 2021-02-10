package jp.co.abs.calender;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

public class ScheduleDialog extends DialogFragment {
    private static final String TAG = ScheduleDialog.class.getSimpleName();

    private static final String ARGS_KEY_DATE = "Date";

    private static final String HOUR_REGEX = "^([2][0-3]|[0-1][0-9]|[0-9])$";
    private static final String MINUTE_REGEX = "^([0-5][0-9]|[0-9])$";

    private static final String INTENT_NAME_REQUEST = "RequestCode";
    private static final String INTENT_NAME_SCHEDULE_TEXT = "ScheduleText";

    private Context mContext;
    private Schedule mRemoveSchedule;
    private Date mDate;
    private OnUpdateScheduleListener mOnUpdateScheduleListener;

    private AlertDialog mAlertDialog;

    private EditText mScheduleHourEditText;
    private EditText mScheduleMinuteEditText;
    private EditText mScheduleEditText;

    /**
     * Instanceを生成する
     *
     * @param date 　スケジュールの編集・保存をしたい日付
     * @return Instance
     */
    public static ScheduleDialog getInstance(Date date) {
        ScheduleDialog scheduleDialog = new ScheduleDialog();

        Bundle args = new Bundle();
        args.putSerializable(ARGS_KEY_DATE, date);
        scheduleDialog.setArguments(args);

        return scheduleDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mRemoveSchedule = null;
        mContext = requireContext();
        if (getArguments() != null) {
            mDate = (Date) getArguments().getSerializable(ARGS_KEY_DATE);
        }

        View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.custom_dialog, null);

        Log.i(TAG, "date: " + mDate);

        TextView titleView = (TextView) dialogView.findViewById(R.id.dialog_title);
        String titleText = (String) DateFormat.format(mContext.getString(R.string.dialog_title), mDate);
        titleView.setText(titleText);

        final Spinner scheduleGenreSpinner = (Spinner) dialogView.findViewById(R.id.schedule_genre);
        List<String> scheduleGenreList = new ArrayList<>();
        for (ScheduleGenre scheduleGenre : ScheduleGenre.values()) {
            scheduleGenreList.add(mContext.getString(scheduleGenre.getGenreNameResId()));
        }
        scheduleGenreSpinner.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, scheduleGenreList));

        final ListView scheduleListView = dialogView.findViewById(R.id.schedule_list);
        final List<Schedule> selectedDateScheduleList = PrefUtils.read(mContext, mDate);
        scheduleListView.setAdapter(new ScheduleListAdapter(mContext, selectedDateScheduleList));

        mScheduleHourEditText = (EditText) dialogView.findViewById(R.id.schedule_hour);
        mScheduleMinuteEditText = (EditText) dialogView.findViewById(R.id.schedule_minutes);
        mScheduleEditText = (EditText) dialogView.findViewById(R.id.dialog_edit_schedule);

        setEditTextFilters(mScheduleHourEditText, HOUR_REGEX);
        setEditTextFilters(mScheduleMinuteEditText, MINUTE_REGEX);

        final ViewGroup cautionTextLayout = dialogView.findViewById(R.id.caution_layout);
        final TextView cautionHourTextView = dialogView.findViewById(R.id.caution_hour_text);
        final TextView cautionMinuteTextView = dialogView.findViewById(R.id.caution_minute_text);
        final TextView cautionScheduleTextView = dialogView.findViewById(R.id.caution_schedule_text);

        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setVisibility(cautionTextLayout, !isValidInputValue());
                }
            }
        };
        mScheduleHourEditText.setOnFocusChangeListener(onFocusChangeListener);
        mScheduleMinuteEditText.setOnFocusChangeListener(onFocusChangeListener);
        mScheduleEditText.setOnFocusChangeListener(onFocusChangeListener);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setVisibility(cautionHourTextView, TextUtils.isEmpty(mScheduleHourEditText.getText()));
                setVisibility(cautionMinuteTextView, TextUtils.isEmpty(mScheduleMinuteEditText.getText()));
                setVisibility(cautionScheduleTextView, TextUtils.isEmpty(mScheduleEditText.getText()));
                setVisibility(cautionTextLayout, !isValidInputValue());

                mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isValidInputValue());
            }
        };
        mScheduleHourEditText.addTextChangedListener(textWatcher);
        mScheduleMinuteEditText.addTextChangedListener(textWatcher);
        mScheduleEditText.addTextChangedListener(textWatcher);

        final TextView modifyScheduleTextView = dialogView.findViewById(R.id.modify_schedule_text);

        //　ListView内スケジュールの編集
        scheduleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mRemoveSchedule = PrefUtils.read(mContext, mDate).get(position);

                mScheduleHourEditText.setText(mRemoveSchedule.getHourText());
                mScheduleMinuteEditText.setText(mRemoveSchedule.getMinuteText());
                mScheduleEditText.setText(mRemoveSchedule.getScheduleText());
                scheduleGenreSpinner.setSelection(mRemoveSchedule.getScheduleGenre().ordinal());
                modifyScheduleTextView.setText(R.string.modify_schedule_title);
            }
        });

        // スケジュール削除処理
        scheduleListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                showDeleteConfirmDialog(((ScheduleListAdapter) scheduleListView.getAdapter()), position);

                return true;
            }
        });

        // スケジュールダイアログの作成処理
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext)
                .setView(dialogView)
                .setNegativeButton(mContext.getString(R.string.dialog_negative_button_text), null)
                .setPositiveButton(mContext.getString(R.string.schedule_dialog_positive_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String scheduleHour = mScheduleHourEditText.getText().toString();
                        String scheduleMinute = mScheduleMinuteEditText.getText().toString();
                        String scheduleText = mScheduleEditText.getText().toString();
                        int requestCode = PrefUtils.nextRequestCode(mContext);
                        String selectedScheduleGenre = scheduleGenreSpinner.getSelectedItem().toString();
                        ScheduleGenre scheduleGenre = ScheduleGenre.findGenre(selectedScheduleGenre, mContext);

                        Schedule schedule = new Schedule(scheduleMinute, scheduleHour, scheduleText, requestCode, scheduleGenre);

                        List<Schedule> scheduleList = PrefUtils.read(mContext, mDate);
                        scheduleList.remove(mRemoveSchedule);
                        scheduleList.add(schedule);
                        PrefUtils.write(mContext, mDate, scheduleList);

                        if (mOnUpdateScheduleListener != null) {
                            mOnUpdateScheduleListener.onUpdateSchedule();
                        }

                        deleteAlarm(mRemoveSchedule);

                        setAlarm(schedule, mDate);
                    }
                });

        mAlertDialog = alertDialogBuilder.create();

        return mAlertDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isValidInputValue());
    }

    /**
     * EditTextに入力制限を設定する
     *
     * @param editText 入力制限を設定するEditText
     * @param regex    入力制限の正規表現
     */
    private void setEditTextFilters(EditText editText, final String regex) {
        InputFilter inputHourFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
                String string = dest.toString() + source.toString();
                if (string.matches(regex)) {
                    return source;
                } else {
                    return "";
                }
            }
        };
        editText.setFilters(new InputFilter[]{inputHourFilter});
    }

    /**
     * Viewの表示/非表示を設定する
     *
     * @param view       表示/非表示を設定するView
     * @param visibility true: 表示 false: 非表示
     */
    private void setVisibility(View view, boolean visibility) {
        view.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    /**
     * 有効な入力値かどうか確認する
     *
     * @return true: 有効な入力値 false: 無効な入力値
     */
    private boolean isValidInputValue() {
        return !TextUtils.isEmpty(mScheduleHourEditText.getText().toString())
                && !TextUtils.isEmpty(mScheduleMinuteEditText.getText().toString())
                && !TextUtils.isEmpty(mScheduleEditText.getText().toString());
    }

    /**
     * スケジュール削除の確認ダイアログを表示する
     *
     * @param scheduleListAdapter 　スケジュールリストのアダプター
     * @param position            　削除するスケジュールのポジション
     */
    private void showDeleteConfirmDialog(final ScheduleListAdapter scheduleListAdapter, final int position) {
        new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.delete_confirm_dialog_title))
                .setNegativeButton(mContext.getString(R.string.dialog_negative_button_text), null)
                .setPositiveButton(mContext.getString(R.string.delete_confirm_dialog_positive_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<Schedule> scheduleList = PrefUtils.read(mContext, mDate);
                        Schedule removeSchedule = scheduleList.remove(position);
                        PrefUtils.write(mContext, mDate, scheduleList);

                        scheduleListAdapter.remove(removeSchedule);
                        scheduleListAdapter.notifyDataSetChanged();

                        if (mOnUpdateScheduleListener != null) {
                            mOnUpdateScheduleListener.onUpdateSchedule();
                        }

                        //スケジュールを削除する場合、アラームを解除
                        deleteAlarm(removeSchedule);
                    }
                })
                .create()
                .show();
    }

    /**
     * アラームを登録する
     * 　指定された日付に設定したスケジュールを通知する
     *
     * @param schedule     アラームに設定するスケジュール
     * @param selectedDate アラームを登録する日付
     */
    private void setAlarm(Schedule schedule, Date selectedDate) {
        if (schedule == null || selectedDate == null) return;
        Intent intent = new Intent(mContext, AlarmNotification.class);
        intent.putExtra(INTENT_NAME_REQUEST, schedule.getRequestCode());
        intent.putExtra(INTENT_NAME_SCHEDULE_TEXT, schedule.getScheduleText());
        PendingIntent pending = PendingIntent.getBroadcast(mContext, schedule.getRequestCode(), intent, 0);

        AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTime(selectedDate);
        alarmTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(schedule.getHourText()));
        alarmTime.set(Calendar.MINUTE, Integer.parseInt(schedule.getMinuteText()));

        if (am != null) {
            am.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pending);
            Log.i(TAG, "alarmTime: " + alarmTime);
        }
    }

    /**
     * アラームを解除する
     *
     * @param schedule アラームを解除するスケジュール
     */
    private void deleteAlarm(Schedule schedule) {
        if (schedule == null) return;
        Intent intent = new Intent(mContext, AlarmNotification.class);
        PendingIntent pending = PendingIntent.getBroadcast(mContext, schedule.getRequestCode(), intent, 0);
        // アラームを解除する
        AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.cancel(pending);
        }
    }

    /**
     * スケジュールが更新された際に呼び出されるコールバックを定義しているインターフェイス
     */
    public interface OnUpdateScheduleListener {

        /**
         * スケジュールが更新された際に呼び出されるコールバックメソッド
         */
        void onUpdateSchedule();
    }

    /**
     * スケジュールが更新された際に呼び出されるコールバックを設定する
     *
     * @param onUpdateScheduleListener 　呼び出されるコールバック
     */
    public void setOnUpdateScheduleListener(OnUpdateScheduleListener onUpdateScheduleListener) {
        mOnUpdateScheduleListener = onUpdateScheduleListener;
    }

}
