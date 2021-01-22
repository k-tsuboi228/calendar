package jp.co.abs.calender;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrefUtils {
    private static final String PREF_NAME = "memo";
    private static final String PREF_KEY_REQUEST_CODE = "RequestCode";

    private static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static String convertKey(Date date) {
        return new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(date);
    }

//    public static void write(Context context, Date date, String memo) {
//        getPreference(context).edit().putString(convertKey(date), memo).apply();
//    }

    public static void write(Context context, Date date, List<Schedule> list) {
        Gson gson = new Gson();
        String schedule = gson.toJson(list);
        getPreference(context).edit().putString(convertKey(date), schedule).apply();
    }

    public static List<Schedule> read(Context context, Date date) {
        String schedule = getPreference(context).getString(convertKey(date), "");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Schedule>>() {
        }.getType();

        List<Schedule> scheduleList = gson.fromJson(schedule, listType);
        if (scheduleList == null) {
            return new ArrayList<Schedule>();
        }
        Collections.sort(scheduleList, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule o1, Schedule o2) {
                return o1.getTimeText().compareTo(o2.getTimeText());
            }
        });
        return scheduleList;
    }

    /**
     * 最新のリクエストコードを返す
     *
     * @param context 　Context
     * @return 最新のリクエストコード
     */
    public static int nextRequestCode(Context context) {
        int requestCode = getPreference(context).getInt(PREF_KEY_REQUEST_CODE, 0);
        requestCode++;
        getPreference(context).edit().putInt(PREF_KEY_REQUEST_CODE, requestCode).apply();

        return requestCode;
    }

}
