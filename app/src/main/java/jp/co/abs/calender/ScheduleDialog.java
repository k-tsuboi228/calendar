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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.co.abs.calender.databinding.CustomDialogBinding;

import static android.content.Context.ALARM_SERVICE;

public class ScheduleDialog extends DialogFragment {
    private static final String TAG = ScheduleDialog.class.getSimpleName();

    private static final String ARGS_KEY_DATE = "Date";

    private static final String HOUR_REGEX = "^([2][0-3]|[0-1][0-9]|[0-9])$";
    private static final String MINUTE_REGEX = "^([0-5][0-9]|[0-9])$";

    private static final String INTENT_NAME_REQUEST = "RequestCode";
    private static final String INTENT_NAME_SCHEDULE_TEXT = "ScheduleText";

    private CustomDialogBinding mBinding;
    private Context mContext;
    private Schedule mRemoveSchedule;
    private Date mDate;
    private OnUpdateScheduleListener mOnUpdateScheduleListener;

    private AlertDialog mAlertDialog;

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

        mBinding = CustomDialogBinding.inflate(requireActivity().getLayoutInflater());

        Log.i(TAG, "date: " + mDate);

        String titleText = (String) DateFormat.format(mContext.getString(R.string.dialog_title), mDate);
        mBinding.dialogTitle.setText(titleText);

        List<String> scheduleGenreList = new ArrayList<>();
        for (ScheduleGenre scheduleGenre : ScheduleGenre.values()) {
            scheduleGenreList.add(mContext.getString(scheduleGenre.getGenreNameResId()));
        }
        mBinding.scheduleGenre.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, scheduleGenreList));

        final List<Schedule> selectedDateScheduleList = PrefUtils.read(mContext, mDate);
        mBinding.scheduleList.setAdapter(new ScheduleListAdapter(mContext, selectedDateScheduleList));

        setEditTextFilters(mBinding.scheduleHour, HOUR_REGEX);
        setEditTextFilters(mBinding.scheduleMinutes, MINUTE_REGEX);

        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setVisibility(mBinding.cautionLayout, !isValidInputValue());
                }
            }
        };
        mBinding.scheduleHour.setOnFocusChangeListener(onFocusChangeListener);
        mBinding.scheduleMinutes.setOnFocusChangeListener(onFocusChangeListener);
        mBinding.dialogEditSchedule.setOnFocusChangeListener(onFocusChangeListener);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setVisibility(mBinding.cautionHourText, TextUtils.isEmpty(mBinding.scheduleHour.getText()));
                setVisibility(mBinding.cautionMinuteText, TextUtils.isEmpty(mBinding.scheduleMinutes.getText()));
                setVisibility(mBinding.cautionScheduleText, TextUtils.isEmpty(mBinding.dialogEditSchedule.getText()));
                setVisibility(mBinding.cautionLayout, !isValidInputValue());

                mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isValidInputValue());
            }
        };
        mBinding.scheduleHour.addTextChangedListener(textWatcher);
        mBinding.scheduleMinutes.addTextChangedListener(textWatcher);
        mBinding.dialogEditSchedule.addTextChangedListener(textWatcher);

        //　ListView内スケジュールの編集
        mBinding.scheduleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mRemoveSchedule = PrefUtils.read(mContext, mDate).get(position);

                mBinding.scheduleHour.setText(mRemoveSchedule.getHourText());
                mBinding.scheduleMinutes.setText(mRemoveSchedule.getMinuteText());
                mBinding.dialogEditSchedule.setText(mRemoveSchedule.getScheduleText());
                mBinding.scheduleGenre.setSelection(mRemoveSchedule.getScheduleGenre().ordinal());
                mBinding.modifyScheduleText.setText(R.string.modify_schedule_title);
            }
        });

        // スケジュール削除処理
        mBinding.scheduleList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                showDeleteConfirmDialog(((ScheduleListAdapter) mBinding.scheduleList.getAdapter()), position);

                return true;
            }
        });

        // スケジュールダイアログの作成処理
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext)
                .setView(mBinding.getRoot())
                .setNegativeButton(mContext.getString(R.string.dialog_negative_button_text), null)
                .setPositiveButton(mContext.getString(R.string.schedule_dialog_positive_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String scheduleHour = mBinding.scheduleHour.getText().toString();
                        String scheduleMinute = mBinding.scheduleMinutes.getText().toString();
                        String scheduleText = mBinding.dialogEditSchedule.getText().toString();
                        int requestCode = PrefUtils.nextRequestCode(mContext);
                        String selectedScheduleGenre = mBinding.scheduleGenre.getSelectedItem().toString();
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
        return !TextUtils.isEmpty(mBinding.scheduleHour.getText().toString())
                && !TextUtils.isEmpty(mBinding.scheduleMinutes.getText().toString())
                && !TextUtils.isEmpty(mBinding.dialogEditSchedule.getText().toString());
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

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTime(selectedDate);
        alarmTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(schedule.getHourText()));
        alarmTime.set(Calendar.MINUTE, Integer.parseInt(schedule.getMinuteText()));
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

        // 過去の時間にスケジュールを登録する場合、アラームを登録しない
        if (alarmTime.getTimeInMillis() < System.currentTimeMillis()) return;

        Intent intent = new Intent(mContext, AlarmNotification.class);
        intent.putExtra(INTENT_NAME_REQUEST, schedule.getRequestCode());
        intent.putExtra(INTENT_NAME_SCHEDULE_TEXT, schedule.getScheduleText());
        PendingIntent pending = PendingIntent.getBroadcast(mContext, schedule.getRequestCode(), intent, 0);

        AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
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
