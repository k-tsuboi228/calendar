package jp.co.abs.calender;

import android.content.Context;

public enum ScheduleGenre {
    WORK(R.string.schedule_genre_work, R.string.calendar_cell_text_work),
    PRIVATE(R.string.schedule_genre_private, R.string.calendar_cell_text_private),
    ;

    private int mGenreNameResId;
    private int mCalendarCellTextResId;

    ScheduleGenre(int genreNameResId, int calendarCellTextResId) {
        mGenreNameResId = genreNameResId;
        mCalendarCellTextResId = calendarCellTextResId;
    }

    public int getGenreNameResId() {
        return mGenreNameResId;
    }

    public int getCalendarCellTextResId() {
        return mCalendarCellTextResId;
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
