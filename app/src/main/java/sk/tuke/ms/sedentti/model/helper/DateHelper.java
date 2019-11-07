package sk.tuke.ms.sedentti.model.helper;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

public class DateHelper {
    private final static long daySeconds = 86400;
    private final static long weekSeconds = daySeconds * 7;
    private final static long monthSeconds = daySeconds * 30;

    @NotNull
    public static Date getLastDay() {
        long todayTimestamp = new Date().getTime();
        return DateHelper.getNormalizedDate(
                new Date(todayTimestamp - daySeconds)
        );
    }

    @NotNull
    public static Date getLastDay(@NotNull Date from) {
        long fromTimestamp = from.getTime();
        return DateHelper.getNormalizedDate(
                new Date(fromTimestamp - daySeconds)
        );
    }

    @NotNull
    public static Date getLastWeek() {
        long todayTimestamp = new Date().getTime();
        return DateHelper.getNormalizedDate(
                new Date(todayTimestamp - weekSeconds)
        );
    }

    @NotNull
    public static Date getLastWeek(@NotNull Date from) {
        long fromTimestamp = from.getTime();
        return DateHelper.getNormalizedDate(
                new Date(fromTimestamp - weekSeconds)
        );
    }

    @NotNull
    public static Date getLastMonth() {
        long todayTimestamp = new Date().getTime();
        return DateHelper.getNormalizedDate(
                new Date(todayTimestamp - monthSeconds)
        );
    }

    @NotNull
    public static Date getLastMonth(@NotNull Date from) {
        long fromTimestamp = from.getTime();
        return DateHelper.getNormalizedDate(
                new Date(fromTimestamp - monthSeconds)
        );
    }

    public static Date getNormalizedDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        return calendar.getTime();
    }
}
