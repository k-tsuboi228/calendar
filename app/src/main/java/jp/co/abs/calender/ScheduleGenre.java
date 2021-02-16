package jp.co.abs.calender;

import android.content.Context;

public enum ScheduleGenre {
    WORK(R.string.schedule_genre_work, R.drawable.ic_baseline_work_24),
    PRIVATE(R.string.schedule_genre_private, R.drawable.ic_baseline_private_24),
    SCHOOL(R.string.schedule_genre_school, R.drawable.ic_baseline_school_24),
    ;

    private int mGenreNameResId;
    private int mGenreImageResId;

    ScheduleGenre(int genreNameResId, int genreImageResId) {
        mGenreNameResId = genreNameResId;
        mGenreImageResId = genreImageResId;
    }

    public int getGenreNameResId() {
        return mGenreNameResId;
    }

    public int getGenreImageResId() {
        return mGenreImageResId;
    }

    public static ScheduleGenre findGenre(String genreName, Context context) {
        for (ScheduleGenre scheduleGenre : values()) {
            if (genreName.equals(context.getString(scheduleGenre.getGenreNameResId()))) {
                return scheduleGenre;
            }
        }
        return WORK;
    }
}
